package com.example.wear3ktxwatchfaceexample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.SurfaceHolder
import androidx.wear.watchface.ComplicationSlot
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.complications.rendering.CanvasComplicationDrawable
import androidx.wear.watchface.complications.rendering.ComplicationDrawable
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyle
import com.example.wear3ktxwatchfaceexample.data.watchface.WatchFaceColorPalette.Companion.convertToWatchFaceColorPalette
import com.example.wear3ktxwatchfaceexample.data.watchface.WatchFaceData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

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

    private lateinit var hourHandFill: Path
    private lateinit var hourHandBorder: Path
    private lateinit var minuteHandFill: Path
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

        val newWatchFaceData: WatchFaceData = watchFaceData

        // Loops through user style and applies new values to watchFaceData.
        // TODO: do nothing

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
        drawComplications(canvas, zonedDateTime)


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

    companion object {
        private const val TAG = "AnalogWatchCanvasRenderer"

        // Painted between pips on watch face for hour marks.
        private val HOUR_MARKS = arrayOf("3", "6", "9", "12")

        // Used to canvas.scale() to scale watch hands in proper bounds. This will always be 1.0.
        private const val WATCH_HAND_SCALE = 1.0f
    }

}