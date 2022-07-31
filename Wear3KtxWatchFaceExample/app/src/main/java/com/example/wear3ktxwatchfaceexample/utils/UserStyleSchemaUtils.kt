package com.example.wear3ktxwatchfaceexample.utils

import android.content.Context
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import com.example.wear3ktxwatchfaceexample.R
import com.example.wear3ktxwatchfaceexample.data.watchface.ColorStyleIdAndResourceIds
import com.example.wear3ktxwatchfaceexample.data.watchface.DRAW_HOUR_PIPS_DEFAULT
import com.example.wear3ktxwatchfaceexample.data.watchface.MINUTE_HAND_LENGTH_FRACTION_DEFAULT
import com.example.wear3ktxwatchfaceexample.data.watchface.MINUTE_HAND_LENGTH_FRACTION_MAXIMUM
import com.example.wear3ktxwatchfaceexample.data.watchface.MINUTE_HAND_LENGTH_FRACTION_MINIMUM

/* Keys to matched content in the  the user style settings. We listen for changes to these
 * values in the renderer and if new, we will update the database and update the watch face
 * being rendered.
 */
const val COLOR_STYLE_SETTING = "color_style_setting"
const val DRAW_HOUR_PIPS_STYLE_SETTING = "draw_hour_pips_style_setting"
const val WATCH_HAND_LENGTH_STYLE_SETTING = "watch_hand_length_style_setting"

/*
 * Creates user styles in the settings activity associated with the watch face, so users can
 * edit different parts of the watch face. In the renderer (after something has changed), the
 * watch face listens for a flow from the watch face API data layer and updates the watch face.
 */
fun createUserStyleSchemaHelper(context: Context): UserStyleSchema {
    // 1. Allows user to change the color styles of the watch face (if any are available).
    val colorStyleSetting = UserStyleSetting.ListUserStyleSetting(
        id = UserStyleSetting.Id(COLOR_STYLE_SETTING),
        resources = context.resources,
        displayNameResourceId = R.string.colors_style_setting,
        descriptionResourceId = R.string.colors_style_setting_description,
        icon = null,
        options = ColorStyleIdAndResourceIds.toOptionList(context),
        affectsWatchFaceLayers = listOf(
            WatchFaceLayer.BASE,
            WatchFaceLayer.COMPLICATIONS,
            WatchFaceLayer.COMPLICATIONS_OVERLAY
        )
    )

    // 2. Allows user to toggle on/off the hour pips (dashes around the outer edge of the watch
    // face).
    val drawHourPipsStyleSetting = UserStyleSetting.BooleanUserStyleSetting(
        id = UserStyleSetting.Id(DRAW_HOUR_PIPS_STYLE_SETTING),
        resources = context.resources,
        displayNameResourceId = R.string.watchface_pips_setting,
        descriptionResourceId = R.string.watchface_pips_setting_description,
        icon = null,
        affectsWatchFaceLayers = listOf(WatchFaceLayer.BASE), // WatchFaceLayer.BASE is the background layer
        defaultValue = DRAW_HOUR_PIPS_DEFAULT
    )

    // 3. Allows user to change the length of the minute hand.
    val watchHandLengthStyleSetting = UserStyleSetting.DoubleRangeUserStyleSetting(
        id = UserStyleSetting.Id(WATCH_HAND_LENGTH_STYLE_SETTING),
        resources = context.resources,
        displayNameResourceId = R.string.watchface_hand_length_setting,
        descriptionResourceId = R.string.watchface_hand_length_setting_description,
        icon = null,
        minimumValue = MINUTE_HAND_LENGTH_FRACTION_MINIMUM.toDouble(),
        maximumValue = MINUTE_HAND_LENGTH_FRACTION_MAXIMUM.toDouble(),
        affectsWatchFaceLayers = listOf(WatchFaceLayer.COMPLICATIONS_OVERLAY), // WatchFaceLayer.COMPLICATIONS_OVERLAY are over the complication
        defaultValue = MINUTE_HAND_LENGTH_FRACTION_DEFAULT.toDouble()
    )

    // 4. Create style settings to hold all options.
    return UserStyleSchema(
        listOf(
            colorStyleSetting,
            drawHourPipsStyleSetting,
            watchHandLengthStyleSetting
        )
    )
}