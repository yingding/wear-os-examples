package com.example.wear3ktxwatchfaceexample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.SurfaceHolder
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.RenderParameters
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
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
    override suspend fun createSharedAssets(): SharedAssets {
        TODO("Not yet implemented")
    }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        TODO("Not yet implemented")
    }

    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        TODO("Not yet implemented")
    }

}