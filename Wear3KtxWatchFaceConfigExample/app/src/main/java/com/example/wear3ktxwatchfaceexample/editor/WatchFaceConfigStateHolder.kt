package com.example.wear3ktxwatchfaceexample.editor

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.RenderParameters
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.editor.EditorSession
import androidx.wear.watchface.style.UserStyle
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import com.example.wear3ktxwatchfaceexample.data.watchface.MINUTE_HAND_LENGTH_FRACTION_DEFAULT
import com.example.wear3ktxwatchfaceexample.data.watchface.MINUTE_HAND_LENGTH_FRACTION_MAXIMUM
import com.example.wear3ktxwatchfaceexample.data.watchface.MINUTE_HAND_LENGTH_FRACTION_MINIMUM
import com.example.wear3ktxwatchfaceexample.utils.COLOR_STYLE_SETTING
import com.example.wear3ktxwatchfaceexample.utils.DRAW_HOUR_PIPS_STYLE_SETTING
import com.example.wear3ktxwatchfaceexample.utils.WATCH_HAND_LENGTH_STYLE_SETTING
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlinx.coroutines.yield

/**
 * Maintains the [WatchFaceConfigActivity] state, i.e., handles reads and writes to the
 * [EditorSession] which is basically the watch face data layer. This allows the user to edit their
 * watch face through [WatchFaceConfigActivity].
 *
 * Note: This doesn't use an Android ViewModel because the [EditorSession]'s constructor requires a
 * ComponentActivity and Intent (needed for the library's complication editing UI which is triggered
 * through the [EditorSession]). Generally, Activities and Views shouldn't be passed to Android
 * ViewModels, so this is named StateHolder to avoid confusion.
 *
 * Also, the scope is passed in and we recommend you use the of the lifecycleScope of the Activity.
 *
 * For the [EditorSession] itself, this class uses the keys, [UserStyleSetting], for each of our
 * user styles and sets their values [UserStyleSetting.Option]. After a new value is set, creates a
 * new image preview via screenshot class and triggers a listener (which creates new data for the
 * [StateFlow] that feeds back to the Activity).
 */
class WatchFaceConfigStateHolder (
    private val scope: CoroutineScope,
    private val activity: ComponentActivity
) {
    private lateinit var editorSession: EditorSession

    // Keys from Watch Face Data Structure
    private lateinit var colorStyleKey: UserStyleSetting.ListUserStyleSetting
    private lateinit var drawPipsKey: UserStyleSetting.BooleanUserStyleSetting
    private lateinit var minuteHandLengthKey: UserStyleSetting.DoubleRangeUserStyleSetting

    val uiState: StateFlow<EditWatchFaceUiState> =
        flow<EditWatchFaceUiState> {
            editorSession = EditorSession.createOnWatchEditorSession(
                activity = activity
            )

            extractsUserStyles(editorSession.userStyleSchema)

            // TODO: need to get more info about flow
            emitAll(
                combine(
                    editorSession.userStyle,
                    editorSession.complicationsPreviewData
                ) {
                    userStyle, complicationsPreviewData ->
                    yield()
                    EditWatchFaceUiState.Success(
                        createWatchFacePreview(userStyle, complicationsPreviewData)
                    )
                }
            )
        }.stateIn(
            scope = scope + Dispatchers.Main.immediate,
            started = SharingStarted.Eagerly,
            initialValue = EditWatchFaceUiState.Loading("Initializing")
        )

    /**
     * this method extract the UserStyleSetting to the local class variable from UserStyleSchema
     */
    private fun extractsUserStyles(userStyleSchema: UserStyleSchema) {
        // Loops through user styles and retrieves user editable styles
        for (setting in userStyleSchema.userStyleSettings) {
            when (setting.id.toString()) {
                COLOR_STYLE_SETTING -> {
                    colorStyleKey = setting as UserStyleSetting.ListUserStyleSetting
                }

                DRAW_HOUR_PIPS_STYLE_SETTING -> {
                    drawPipsKey = setting as UserStyleSetting.BooleanUserStyleSetting
                }

                WATCH_HAND_LENGTH_STYLE_SETTING -> {
                    minuteHandLengthKey = setting as UserStyleSetting.DoubleRangeUserStyleSetting
                }
            }
        }
    }

    /* Creates a new bitmap render of the updated watch face and passes it along (with all the other
     * updated values) to the Activity to render.
     */
    private fun createWatchFacePreview(
        userStyle: UserStyle,
        complicationPreviewData: Map<Int, ComplicationData>
    ):  UserStylesAndPreview {
        Log.d(TAG, "updatesWatchFacePreview()")

        val bitmap: Bitmap = editorSession.renderWatchFaceToBitmap(
            renderParameters = RenderParameters(
                DrawMode.INTERACTIVE,
                WatchFaceLayer.ALL_WATCH_FACE_LAYERS,
                RenderParameters.HighlightLayer(
                    highlightedElement = RenderParameters.HighlightedElement.AllComplicationSlots,
                    highlightTint = Color.RED, // Red complication highlight.
                    backgroundTint = Color.argb(128, 0,0, 0) // Darken everything else
                )
            ),
            instant = editorSession.previewReferenceInstant,
            slotIdToComplicationData = complicationPreviewData
        )

        val colorStyle =
            userStyle[colorStyleKey] as UserStyleSetting.ListUserStyleSetting.ListOption
        val ticksEnabledStyle =
            userStyle[drawPipsKey] as UserStyleSetting.BooleanUserStyleSetting.BooleanOption
        val minuteHandStyle =
            userStyle[minuteHandLengthKey] as UserStyleSetting.DoubleRangeUserStyleSetting.DoubleRangeOption

        Log.d(TAG, "/new values: $colorStyle, $ticksEnabledStyle, $minuteHandStyle")

        return UserStylesAndPreview(
            colorStyleId = colorStyle.id.toString(),
            ticksEnabled = ticksEnabledStyle.value,
            minuteHandLength = multiplyByMultipleForSlider(minuteHandStyle.value).toFloat(),
            previewImage = bitmap
        )
    }

    // make sealed class to be the parent class of data class
    sealed class EditWatchFaceUiState {
        data class Success(val userStylesAndPreview: UserStylesAndPreview) : EditWatchFaceUiState()
        data class Loading(val message: String) : EditWatchFaceUiState()
        data class Error(val exception: Throwable) : EditWatchFaceUiState()
    }

    data class UserStylesAndPreview(
        val colorStyleId: String,
        val ticksEnabled: Boolean,
        val minuteHandLength: Float,
        val previewImage: Bitmap
    )

    companion object {
        private const val TAG = "WatchFaceConfigStateHolder"

        // To convert the double representing the arm length to valid float value in the range the
        // slider can support, we need to multiply the original value times 1,000.
        private const val MULTIPLE_FOR_SLIDER: Float = 1000f

        const val MINUTE_HAND_LENGTH_MINIMUM_FOR_SLIDER =
            MINUTE_HAND_LENGTH_FRACTION_MINIMUM * MULTIPLE_FOR_SLIDER

        const val MINUTE_HAND_LENGTH_MAXIMUM_FOR_SLIDER =
            MINUTE_HAND_LENGTH_FRACTION_MAXIMUM * MULTIPLE_FOR_SLIDER

        const val MINUTE_HAND_LENGTH_DEFAULT_FOR_SLIDER =
            MINUTE_HAND_LENGTH_FRACTION_DEFAULT * MULTIPLE_FOR_SLIDER

        private fun multiplyByMultipleForSlider(lengthFraction: Double) =
            lengthFraction * MULTIPLE_FOR_SLIDER
    }

    /* TODO: Useful function */
    fun setComplication(complicationLocation: Int) = Unit

    fun setColorStyle(newColorStyleId: String) = Unit

    fun setDrawPips(enabled: Boolean) = Unit

    fun setMinuteHandArmLength(newLengthRatio: Float) = Unit


    private fun setUserStyleOption(
        userStyleSetting: UserStyleSetting,
        userStyleOption: UserStyleSetting.Option
    ) = Unit

}