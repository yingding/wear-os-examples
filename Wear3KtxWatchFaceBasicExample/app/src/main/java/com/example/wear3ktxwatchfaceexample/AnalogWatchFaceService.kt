package com.example.wear3ktxwatchfaceexample

import android.util.Log
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import com.example.wear3ktxwatchfaceexample.utils.createComplicationSlotsManagerHelper
import com.example.wear3ktxwatchfaceexample.utils.createUserStyleSchemaHelper

/**
 * Handles much of the boilerplate needed to implement a watch face (minus rendering code; see
 * [AnalogWatchCanvasRenderer]) including the complications and settings (styles user can change on
 * the watch face).
 */
class AnalogWatchFaceService : WatchFaceService() {

    // Used by Watch Face APIs to construct user setting options and repository.
    override fun createUserStyleSchema(): UserStyleSchema =
        createUserStyleSchemaHelper(context = applicationContext)

    // Creates all complication user settings and adds them to the existing user settings
    // repository.
    override fun createComplicationSlotsManager(
        currentUserStyleRepository: CurrentUserStyleRepository
    ): ComplicationSlotsManager = createComplicationSlotsManagerHelper(
        context = applicationContext,
        currentUserStyleRepository = currentUserStyleRepository
    )

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        Log.d(TAG, "createWatchFace()")

        val renderer = AnalogWatchCanvasRenderer(
            context = applicationContext,
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            complicationSlotsManager = complicationSlotsManager,
            currentUserStyleRepository = currentUserStyleRepository,
            canvasType = CanvasType.HARDWARE, // support by SDK 26
            clearWithBackgroundTintBeforeRenderingHighlightLayer = true
        )

        return WatchFace(
            watchFaceType = WatchFaceType.ANALOG,
            renderer = renderer
        )
    }

    companion object {
        const val TAG = "AnalogWatchFaceService"
    }
}