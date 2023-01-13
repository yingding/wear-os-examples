/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.wear.tiles.hello

import androidx.wear.tiles.DimensionBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.LayoutElementBuilders.LayoutElement
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TimelineBuilders
import com.example.wear.tiles.R
import com.google.android.horologist.tiles.SuspendingTileService

private const val RESOURCES_VERSION = "0"

class HelloWorldTileService : SuspendingTileService() {

    override suspend fun resourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest
    ): ResourceBuilders.Resources {
        return ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .build()
    }

    // create a Tile obj
    override suspend fun tileRequest(
        requestParams: RequestBuilders.TileRequest
    ): TileBuilders.Tile {
        // Define the type of timeline
        val singleTileTimeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                // add timeline entry object
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        // set the layout of the current timeline entry container
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(
                                // add tileLayout to be the root of timeline entry container
                                tileLayout()
                            ).build()
                    )
                    .build()
            )
            .build()

        return TileBuilders.Tile.Builder()
            // set the version
            .setResourcesVersion(RESOURCES_VERSION)
            // set the timeline for the tile
            .setTimeline(singleTileTimeline)
            .build()
    }

    private fun tileLayout(): LayoutElement {
        val text = getString(R.string.hello_tile_body)
        return LayoutElementBuilders.Box.Builder() // Box
            .setVerticalAlignment(LayoutElementBuilders.VERTICAL_ALIGN_CENTER)
            .setWidth(DimensionBuilders.expand()) // fill the parent
            .setHeight(DimensionBuilders.expand()) // fill the parent
            .addContent(
                LayoutElementBuilders.Text.Builder() // text
                    .setText(text)
                    .build()
            )
            .build()
    }
}
