package com.example.configwatchfaceexample.data.watchface

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.wear.watchface.complications.rendering.ComplicationDrawable

/**
 * Color resources and drawable id needed to render the watch face. Translated from
 * [ColorStyleIdAndResourceIds] constant ids to actual resources with context at run time.
 *
 * This is only needed when the watch face is active.
 *
 * Note: We do not use the context to generate a [ComplicationDrawable] from the
 * complicationStyleDrawableId (representing the style), because a new, separate
 * [ComplicationDrawable] is needed for each complication. Because the renderer will loop through
 * all the complications and there can be more than one, this also allows the renderer to create
 * as many [ComplicationDrawable]s as needed.
 */
data class WatchFaceColorPalette (
    val activePrimaryColor: Int,
    val activeSecondaryColor: Int,
    val activeBackgroundColor: Int,
    val activeOuterElementColor: Int,
    @DrawableRes val complicationStyleDrawableId: Int,
    val ambientPrimaryColor: Int,
    val ambientSecondaryColor: Int,
    val ambientBackgroundColor: Int,
    val ambientOuterElementColor: Int
) {
    companion object {
        /**
         * Converts [ColorStyleIdAndResourceIds] to [WatchFaceColorPalette].
         */
        fun convertToWatchFaceColorPalette(
            context: Context,
            activeColorStyle: ColorStyleIdAndResourceIds,
            ambientColorStyle: ColorStyleIdAndResourceIds,
        ): WatchFaceColorPalette {
            return WatchFaceColorPalette(
                // Active colors
                activePrimaryColor = context.getColor(activeColorStyle.primaryColorId),
                activeSecondaryColor = context.getColor(activeColorStyle.secondaryColorId),
                activeBackgroundColor = context.getColor(activeColorStyle.backgroundColorId),
                activeOuterElementColor = context.getColor(activeColorStyle.outerElementColorId),
                // Complication color style
                complicationStyleDrawableId = activeColorStyle.complicationStyleDrawableId,
                // Ambient colors
                ambientPrimaryColor = context.getColor(ambientColorStyle.primaryColorId),
                ambientSecondaryColor = context.getColor(ambientColorStyle.secondaryColorId),
                ambientBackgroundColor = context.getColor(ambientColorStyle.backgroundColorId),
                ambientOuterElementColor = context.getColor(ambientColorStyle.outerElementColorId)
            )
        }
    }
}