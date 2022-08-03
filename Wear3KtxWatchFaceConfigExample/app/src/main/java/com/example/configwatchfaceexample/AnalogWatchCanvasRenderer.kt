package com.example.configwatchfaceexample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.SurfaceHolder
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import com.example.configwatchfaceexample.data.watchface.ColorStyleIdAndResourceIds
import com.example.configwatchfaceexample.data.watchface.WatchFaceColorPalette.Companion.convertToWatchFaceColorPalette
import com.example.configwatchfaceexample.data.watchface.WatchFaceData
import com.example.configwatchfaceexample.utils.COLOR_STYLE_SETTING
import com.example.configwatchfaceexample.utils.DRAW_HOUR_PIPS_STYLE_SETTING
import com.example.configwatchfaceexample.utils.WATCH_HAND_LENGTH_STYLE_SETTING
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sin

// Default for how long each frame is displayed at expected frame rate.
private const val FRAME_PERIOD_MS_DEFAULT: Long = 16L // 60 Hz (1 FPS), 32L = 30 Hz (2 FPS )


/**
 * Renders watch face via data in Room database, Also, updates watch face state based on setting
 * changes by user via [userStyleRepository.addUserStyleListener()].
 */
class AnalogWatchCanvasRenderer (
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    private val complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    canvasType: Int,
    clearWithBackgroundTintBeforeRenderingHighlightLayer: Boolean // HighlightLayer is used by editor to show the parts affected for configuration
) : Renderer.CanvasRenderer2<Renderer.SharedAssets>(
    surfaceHolder,
    currentUserStyleRepository,
    watchState,
    canvasType,
    interactiveDrawModeUpdateDelayMillis = FRAME_PERIOD_MS_DEFAULT,
    clearWithBackgroundTintBeforeRenderingHighlightLayer
) {
    private val scope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /* Represents all data needed to render the watch face. All value defaults are constants. Only
     * three values are changeable by the user (color scheme, ticks being rendered, and length of
     * the minute arm). Those dynamic values are saved in the watch face APIs and we update those
     * here (in the renderer) through a Kotlin Flow.
     */
    private var watchFaceData: WatchFaceData = WatchFaceData()

    // Converts resource ids into Colors and ComplicationDrawable.
    private var watchFaceColors = convertToWatchFaceColorPalette(
        context,
        watchFaceData.activeColorStyle,
        watchFaceData.ambientColorStyle
    )

    // Initializes paint object for painting the clock hands with default values.
    private val clockHandPaint = Paint().apply {
        isAntiAlias = true
        strokeWidth =
            context.resources.getDimensionPixelSize(R.dimen.clock_hand_stroke_width).toFloat()
    }

    private val outerElementPaint = Paint().apply {
        isAntiAlias = true
    }

    // Used to paint the main hour hand text with the hour pips, i.e. 3, 6, 9, and 12 o'clock.
    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = context.resources.getDimensionPixelSize(R.dimen.hour_mark_size).toFloat()
    }

    // private lateinit var hourHandFill: Path
    private lateinit var hourHandBorder: Path
    // private lateinit var minuteHandFill: Path
    private lateinit var minuteHandBorder: Path
    private lateinit var secondHand: Path

    // Changed when setting changes cause a change in the minute hand arm (triggered by user in
    // updateUserStyle() via userStyleRepository.addUserStyleListener()).
    private var armLengthChangedRecalculateClockHands: Boolean = false

    // Default size of watch face drawing area, that is, a no size rectangle. Will be replaced with
    // valid dimensions from the system.
    private var currentWatchFaceSize = Rect(0, 0, 0, 0)

    override suspend fun createSharedAssets(): SharedAssets {
        return object: SharedAssets {
            override fun onDestroy() = Unit
        }
    }

    init {
        scope.launch {
            // userStyle: StateFlow
            currentUserStyleRepository.userStyle.collect { userStyle ->
                updateWatchFaceData(userStyle)
            }
        }
    }

    /**
     * clear the scope in onDestroy()
     */
    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        scope.cancel("AnalogWatchCanvasRenderer scope clear() request")
        super.onDestroy()
    }

    /*
     * Triggered when the user makes changes to the watch face through the settings activity. The
     * function is called by a flow.
     */
    private fun updateWatchFaceData(userStyle: UserStyle) {
        Log.d(TAG, "updateWatchFace(): $userStyle")

        var newWatchFaceData: WatchFaceData = watchFaceData

        // Loops through user style and applies new values to watchFaceData.
        for (options in userStyle) {
            when (options.key.id.toString()) {
                COLOR_STYLE_SETTING -> {
                    // cast the listOption to ListOption
                    val listOption = options.value as
                            UserStyleSetting.ListUserStyleSetting.ListOption

                    newWatchFaceData = newWatchFaceData.copy(
                        activeColorStyle = ColorStyleIdAndResourceIds.getColorStyleConfig(
                            listOption.id.toString()
                        )
                    )
                }
                DRAW_HOUR_PIPS_STYLE_SETTING -> {
                    val booleanValue = options.value as
                            UserStyleSetting.BooleanUserStyleSetting.BooleanOption

                    newWatchFaceData = newWatchFaceData.copy(
                        drawHourPips = booleanValue.value
                    )
                }
                WATCH_HAND_LENGTH_STYLE_SETTING -> {
                    val doubleValue = options.value as
                            UserStyleSetting.DoubleRangeUserStyleSetting.DoubleRangeOption

                    // The arm lengths are usually only calculated the first time the watch face is
                    // loaded to reduce the ops in the onDraw(). Because we updated the minute hand
                    // watch length, we need to trigger a recalculation.
                    armLengthChangedRecalculateClockHands = true

                    // Updates length of minute hand based on edits from user.
                    val newMinuteHandDimensions = newWatchFaceData.minuteHandDimensions.copy(
                        lengthFraction = doubleValue.value.toFloat()
                    )

                    newWatchFaceData = newWatchFaceData.copy(
                        minuteHandDimensions = newMinuteHandDimensions
                    )
                }
            }
        }

        // Only updates if something changed.
        if (watchFaceData != newWatchFaceData) {

            // Recreates the global variable Color and ComplicationDrawable from resource ids.
            watchFaceColors = convertToWatchFaceColorPalette(
                context,
                watchFaceData.activeColorStyle,
                watchFaceData.ambientColorStyle
            )

            // Applies the user chosen complication color scheme changes. ComplicationDrawables for
            // each of the styles are defined in XML so we need to replace the complication's
            // drawables.
            applyAllComplications {
                ComplicationDrawable.getDrawable(
                    context,
                    watchFaceColors.complicationStyleDrawableId
                )?.let {
                    // assign the none null Drawable to the CanvasComplicationDrawable of the ComplicationSlot
                    (this.renderer as CanvasComplicationDrawable).drawable = it
                }
            }
        }
    }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        // draw Background Color
        val backgroundColor = if (renderParameters.drawMode == DrawMode.AMBIENT) {
            watchFaceColors.ambientBackgroundColor
        } else {
            watchFaceColors.activeBackgroundColor
        }
        canvas.drawColor(backgroundColor)

        // CanvasComplicationDrawable already obeys rendererParameters
        // Presumably, it will be draw on the WatchFaceLayer.COMPLICATION be the SlotsManager
        drawComplications(canvas, zonedDateTime)

        // draws hour, minute and second arms, in the WatchFaceLayer.COMPLICATIONS_OVERLAY
        if (renderParameters.watchFaceLayers.contains(WatchFaceLayer.COMPLICATIONS_OVERLAY)) {
            drawClockHands(canvas, bounds, zonedDateTime)
        }

        // Draw the Number pips on WatchFaceLayer.BASE
        if (renderParameters.drawMode == DrawMode.INTERACTIVE &&
            renderParameters.watchFaceLayers.contains(WatchFaceLayer.BASE) &&
            watchFaceData.drawHourPips
        ) {
            drawNumberStyleOuterElement(
                canvas = canvas,
                bounds = bounds,
                numberRadiusFraction = watchFaceData.numberRadiusFraction, // radius fraction from center to the outer ring
                outerCircleStokeWidthFraction = watchFaceData.numberStyleOuterCircleRadiusFraction, // circle element stoke width in the outer ring
                outerElementColor = watchFaceColors.activeOuterElementColor, // the outer ring element colors ( number and circle between the numbers)
                numberStyleOuterCircleRadiusFraction = watchFaceData.numberStyleOuterCircleRadiusFraction, // the radius of the circle element in the outer ring
                gapBetweenOuterCircleAndBorderFraction = watchFaceData.gapBetweenOuterCircleAndBorderFraction
            )
        }
    }

    /**
     * draws hour arms, minutes arms, seconds arms in active and ambient modes
     */
    private fun drawClockHands(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime) {
        // Only recalculate bounds (watch face size/surface) has changed or the arm of one of the
        // clock hands has changed (via user input in the settings).
        // NOTE: Watch face surface usually only updates one time (when the size of the device is
        // initially broadcasted).
        if (currentWatchFaceSize != bounds || armLengthChangedRecalculateClockHands) {
            armLengthChangedRecalculateClockHands = false
            currentWatchFaceSize = bounds
            recalculateClockHands(bounds)
        }

        // Retrieve current time to calculate location/rotation of watch arms.
        val secondOfDay: Int = zonedDateTime.toLocalTime().toSecondOfDay()

        // Determine the rotation of the hour and minute hand

        // Determine how many seconds it takes to make a complete rotation for each hand
        // It takes the hour hand 12 hours to make a complete rotation
        val secondsPerHourHandRotation = Duration.ofHours(12).seconds

        // It takes the minute hand 1 hour to make a complete rotation
        val secondsPerMinuteHandRotation = Duration.ofHours(1).seconds

        // Determine the angle to draw each hand expressed as an angle in degrees from 0 to 360
        // Since each hand does more than one cycle a day, we are only interested in the remainder
        // of the secondOfDay modulo the hand interval
        val hourRotation = secondOfDay.rem(secondsPerHourHandRotation) * 360.0f / secondsPerHourHandRotation
        val minuteRotation = secondOfDay.rem(secondsPerMinuteHandRotation) * 360.0f / secondsPerMinuteHandRotation

        canvas.withScale(
            x = WATCH_HAND_SCALE,
            y = WATCH_HAND_SCALE,
            pivotX = bounds.exactCenterX(),
            pivotY = bounds.exactCenterY(),
        ) {
            val drawAmbient = renderParameters.drawMode == DrawMode.AMBIENT

            clockHandPaint.style = if (drawAmbient) Paint.Style.STROKE else Paint.Style.FILL
            clockHandPaint.color = if (drawAmbient) {
                watchFaceColors.ambientBackgroundColor
            } else {
                watchFaceColors.activePrimaryColor
            }

            // Draw hour hand
            withRotation(hourRotation, bounds.exactCenterX(), bounds.exactCenterY()) {
                drawPath(hourHandBorder, clockHandPaint)
            }

            // Draw minute hand
            withRotation(minuteRotation, bounds.exactCenterX(), bounds.exactCenterY()) {
                drawPath(minuteHandBorder, clockHandPaint)
            }

            // Draw second hand if not in ambient mode
            if (!drawAmbient) {
                clockHandPaint.color = watchFaceColors.activeSecondaryColor

                // Second hand has a different color style (secondary color) and is only drawn in
                // active mode, so we calculate it here (not above with others).
                val secondsPerSecondHandRotation = Duration.ofMinutes(1).seconds
                val secondsRotation = secondOfDay.rem(secondsPerSecondHandRotation) * 360.0f / secondsPerSecondHandRotation

                withRotation(secondsRotation, bounds.exactCenterX(), bounds.exactCenterY()) {
                    drawPath(secondHand, clockHandPaint)
                }
            }
        }
    }

    /**
     * Rarely called (only when watch face surface changes; usually only once) from the
     * drawClockHands() method.
     * This method initialized the hourHandBorder and hourHandFill, minuteHandBorder and Fill,
     * secondHandBorder and Fill
     *
     * @param bounds The watchface size/dimensions
     */
    private fun recalculateClockHands(bounds: Rect) {
        Log.d(TAG, "recalculateClockHands()")
        hourHandBorder = createClockHand(
            bounds,
            watchFaceData.hourHandDimensions.lengthFraction,
            watchFaceData.hourHandDimensions.widthFraction,
            watchFaceData.gapBetweenHandAndCenterFraction,
            watchFaceData.hourHandDimensions.xRadiusRoundedCorners,
            watchFaceData.hourHandDimensions.yRadiusRoundedCorners
        )
        // hourHandFill = hourHandBorder

        minuteHandBorder = createClockHand(
            bounds,
            watchFaceData.minuteHandDimensions.lengthFraction,
            watchFaceData.minuteHandDimensions.widthFraction,
            watchFaceData.gapBetweenHandAndCenterFraction,
            watchFaceData.minuteHandDimensions.xRadiusRoundedCorners,
            watchFaceData.minuteHandDimensions.yRadiusRoundedCorners,
        )
        // minuteHandFill = minuteHandBorder

        secondHand = createClockHand(
            bounds,
            watchFaceData.secondHandDimensions.lengthFraction,
            watchFaceData.secondHandDimensions.widthFraction,
            watchFaceData.gapBetweenHandAndCenterFraction,
            watchFaceData.secondHandDimensions.xRadiusRoundedCorners,
            watchFaceData.secondHandDimensions.yRadiusRoundedCorners
        )
    }

    private fun drawComplications(canvas: Canvas, zonedDateTime: ZonedDateTime) {
        applyAllComplications {
            if (enabled) { render(canvas, zonedDateTime, renderParameters) }
        }
    }

    /**
     * this function allows to apply code block on all ComplicationSlots
     */
    private inline fun applyAllComplications(block: ComplicationSlot.() -> Unit) {
        for ((_, complication) in complicationSlotsManager.complicationSlots) {
            complication.block()
        }
    }

    /**
     * for complication highlight in designer editor
     */
    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        // draw complication background
        canvas.drawColor(renderParameters.highlightLayer!!.backgroundTint)

        // draw a highlight layer for the complication
        applyAllComplications {
            if (enabled) { renderHighlightLayer(canvas, zonedDateTime, renderParameters) }
        }
    }

    /**
     * Returns a round rect clock hand if {@code rx} and {@code ry} equals to 0, otherwise return a
     * rect clock hand. This is a generic method used for hour, minute and second arms
     *
     * @param bounds The bounds use to determine the coordinate of the clock hand.
     * @param length Clock hand's length, in fraction of {@code bounds.width()}.
     * @param thickness Clock hand's thickness, in fraction of {@code bounds.width()}.
     * @param gapBetweenHandAndCenter Gap between inner side of arm and center.
     * @param roundedCornerXRadius The x-radius of the rounded corners on the round-rectangle.
     * @param roundedCornerYRadius The y-radius of the rounded corners on the round-rectangle.
     */
    private fun createClockHand(
        bounds: Rect,
        length: Float,
        thickness: Float,
        gapBetweenHandAndCenter: Float,
        roundedCornerXRadius: Float,
        roundedCornerYRadius: Float
    ): Path {
        val width = bounds.width()
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()
        val left = centerX - thickness / 2 * width
        val top = centerY - (gapBetweenHandAndCenter + length) * width
        val right = centerX + thickness / 2 * width
        val bottom = centerY - gapBetweenHandAndCenter * width
        val path = Path()

        if (roundedCornerXRadius != 0.0f || roundedCornerYRadius != 0.0f) {
            path.addRoundRect(
                left,
                top,
                right,
                bottom,
                roundedCornerXRadius,
                roundedCornerYRadius,
                Path.Direction.CW
            )
        } else {
            path.addRect(
                left,
                top,
                right,
                bottom,
                Path.Direction.CW
            )
        }
        return path
    }

    /**
     * @param canvas current watchface canvas
     * @param bounds the watchface size/dimensions
     * @param numberRadiusFraction Fraction of the bounds.width() from center to the outer ring number
     * @param outerCircleStokeWidthFraction The stokeWidth of the circle in the outer ring between the numbers, fraction of bounds.width()
     * @param outerElementColor The color of the outer ring element, which comprises stocks and numbers
     * @param numberStyleOuterCircleRadiusFraction the radius of the circle in the outer ring between the numbers, fraction of bounds.width()
     * @param gapBetweenOuterCircleAndBorderFraction the gap to the top board of the watch, fraction of bounds.width()
     */
    private fun drawNumberStyleOuterElement(
        canvas: Canvas,
        bounds: Rect,
        numberRadiusFraction: Float,
        outerCircleStokeWidthFraction: Float,
        outerElementColor: Int,
        numberStyleOuterCircleRadiusFraction: Float,
        gapBetweenOuterCircleAndBorderFraction: Float
    ) {

        // Draws text hour indicators (12, 3, 6, and 9).
        val textBounds = Rect()
        textPaint.color = outerElementColor
        for (i in 0 until 4) { // 0, 1, 2, 3
            val rotation = 0.5f * (i + 1).toFloat() * Math.PI  // 2 PI / 4 element, every element 0.5f * Math.PI
            val dx = sin(rotation).toFloat() * numberRadiusFraction * bounds.width().toFloat()
            val dy = -cos(rotation).toFloat() * numberRadiusFraction * bounds.width().toFloat()
            textPaint.getTextBounds(HOUR_MARKS[i], 0, HOUR_MARKS[i].length, textBounds)
            canvas.drawText(
                HOUR_MARKS[i],
                bounds.exactCenterX() + dx - textBounds.width() / 2.0f,
                bounds.exactCenterY() + dy + textBounds.height() / 2.0f,
                textPaint
            )
        }

        // Draws dots for the remain hour indicators between the numbers above.
        outerElementPaint.strokeWidth = outerCircleStokeWidthFraction * bounds.width()
        outerElementPaint.color = outerElementColor
        // save canvas, then rotate and draw
        canvas.save()
        for (i in 0 until 12) { // from 0 to 11
            if (i % 3 != 0) {
                drawTopMiddleCircle(
                    canvas,
                    bounds,
                    numberStyleOuterCircleRadiusFraction,
                    gapBetweenOuterCircleAndBorderFraction
                )
            }
            // rotate canvas around the center
            canvas.rotate(360.0f / 12.0f, bounds.exactCenterX(), bounds.exactCenterY())
        }
        // restore the canvas after the draw and rotate
        canvas.restore()
    }

    /**
     * This method draws the circle between the numbers in the outer ring.
     * @param canvas: watch face canvas
     * @param bounds: the watch face size/dimensions
     * @param radiusFraction: fraction of the bounds.width() to the bounds center
     * @param gapBetweenOuterCircleAndBorderFraction: fraction of the bounds.width() to the Border of the watchface
     */
    private fun drawTopMiddleCircle(
        canvas: Canvas,
        bounds: Rect,
        radiusFraction: Float,
        gapBetweenOuterCircleAndBorderFraction: Float
    ) {
        outerElementPaint.style = Paint.Style.FILL_AND_STROKE

        // X and Y coordinates of the center of the circle.
        val centerX = 0.5f * bounds.width().toFloat() // is the center of the bounds
        val centerY = bounds.width() * (gapBetweenOuterCircleAndBorderFraction + radiusFraction)
        // the centerY of the circle begins from 0 which is the top border of the watchface
        // gapBetweenOuterCircleAndBorderFraction + radiusFraction = space to the top border of the watchface

        canvas.drawCircle(
            centerX,
            centerY,
            radiusFraction * bounds.width(),
            outerElementPaint
        )
    }


    companion object {
        private const val TAG = "AnalogWatchCanvasRenderer"

        // Painted between pips on watch face for hour marks.
        private val HOUR_MARKS = arrayOf("3", "6", "9", "12")

        // Used to canvas.scale() to scale watch hands in proper bounds. This will always be 1.0.
        private const val WATCH_HAND_SCALE = 1.0f
    }

}