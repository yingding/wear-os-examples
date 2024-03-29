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
package com.example.wear.tiles.messaging.tile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.LayoutElementBuilders
import androidx.wear.tiles.ModifiersBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.material.Button
import androidx.wear.tiles.material.ButtonColors
import androidx.wear.tiles.material.ChipColors
import androidx.wear.tiles.material.CompactChip
import androidx.wear.tiles.material.layouts.MultiButtonLayout
import androidx.wear.tiles.material.layouts.PrimaryLayout
import com.example.wear.tiles.R
import com.example.wear.tiles.messaging.Contact
import com.example.wear.tiles.messaging.MessagingRepo
import com.example.wear.tiles.tools.IconSizePreview
import com.example.wear.tiles.tools.WearDevicePreview
import com.example.wear.tiles.tools.emptyClickable
import com.google.android.horologist.compose.tools.LayoutElementPreview
import com.google.android.horologist.compose.tools.TileLayoutPreview
import com.google.android.horologist.tiles.images.drawableResToImageResource
import com.google.android.horologist.tiles.images.toImageResource
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer

class MessagingTileRenderer(context: Context) :
    SingleTileLayoutRenderer<MessagingTileState, Map<Contact, Bitmap>>(context) {

    // provide the layout by the Renderer to the TileService
    override fun renderTile(
        state: MessagingTileState,
        deviceParameters: DeviceParametersBuilders.DeviceParameters
    ): LayoutElementBuilders.LayoutElement {
        return messagingTileLayout(
            context = context,
            deviceParameters = deviceParameters,
            state = state,
            searchButtonClickable = launchActivityClickable("search_button", openSearch()),
            contactClickableFactory = { contact ->
                launchActivityClickable(
                    clickableId = contact.id.toString(),
                    androidActivity = openConversation(contact)
                )
            },
            newButtonClickable = launchActivityClickable("new_button", openNewConversation())

        )
    }

    // provide the image resources by the Renderer to the TileService
    override fun ResourceBuilders.Resources.Builder.produceRequestedResources(
        resourceState: Map<Contact, Bitmap>,
        deviceParameters: DeviceParametersBuilders.DeviceParameters,
        resourceIds: MutableList<String>
    ) {
        addIdToImageMapping(ID_IC_SEARCH, drawableResToImageResource(R.drawable.ic_search_24))

        resourceState.forEach { (contact, bitmap) ->
            addIdToImageMapping(
                /* id = */ contact.imageResourceId(),
                /* image = */ bitmap.toImageResource()
            )
        }
    }

    companion object {
        internal const val ID_IC_SEARCH = "ic_search"
    }
}




/**
 * Layout definition for the Messaging Tile.
 */
private fun messagingTileLayout(
    context: Context,
    deviceParameters: DeviceParametersBuilders.DeviceParameters,
    state: MessagingTileState,
    searchButtonClickable: ModifiersBuilders.Clickable,
    contactClickableFactory: (Contact) -> ModifiersBuilders.Clickable,
    newButtonClickable: ModifiersBuilders.Clickable
) = PrimaryLayout.Builder(deviceParameters)
    .setPrimaryChipContent(
        CompactChip.Builder(
        /* context = */ context,
        /* text = */ context.getString(R.string.tile_messaging_create_new),
        /* clickable = */ newButtonClickable, // emptyClickable,
        /* deviceParameters = */ deviceParameters
        )
            .setChipColors(ChipColors.primaryChipColors(MessagingTileTheme.colors))
            .build()
    )
    .setContent(
        MultiButtonLayout.Builder()
            .apply {
                // In a PrimaryLayout with a compact chip at the bottom, we can fit 5 buttons.
                // We're only taking the first 4 contacts so that we can fit a Search button too.
                state.contacts.take(4).forEach { contact ->
                    addButtonContent(
                        contactLayout(
                            context = context,
                            contact = contact,
                            clickable = contactClickableFactory(contact) //emptyClickable
                        )
                    )
                }
            }
            .addButtonContent(searchLayout(context, searchButtonClickable)) // pass the searchButtonClickable
            .build()
//        Text.Builder(context, context.getString(R.string.hello_tile_body))
//            .setTypography(Typography.TYPOGRAPHY_BODY1)
//            .setColor(ColorBuilders.argb(Color.White.toArgb()))
//            .build()
    )
    .build()


private fun searchLayout(
    context: Context,
    clickable: ModifiersBuilders.Clickable,
) = Button.Builder(context, clickable)
    .setContentDescription(context.getString(R.string.tile_messaging_search))
    .setIconContent(MessagingTileRenderer.ID_IC_SEARCH)
    .setButtonColors(ButtonColors.secondaryButtonColors(MessagingTileTheme.colors))
    .build()

private fun contactLayout(
    context: Context,
    contact: Contact,
    clickable: ModifiersBuilders.Clickable,
) = Button.Builder(context, clickable)
    .setContentDescription(contact.name)
    .apply {
        if (contact.avatarUrl != null) {
            // on the Button.Builder
            setImageContent(contact.imageResourceId())
        } else {
            setTextContent(contact.initials)
            setButtonColors(ButtonColors.secondaryButtonColors(MessagingTileTheme.colors))
        }
    }
    .build()


@WearDevicePreview
@Composable
fun MessagingTileRendererPreview() {
    // horologist Preview function
    val state = MessagingTileState(MessagingRepo.knownContacts)
    val context = LocalContext.current
    TileLayoutPreview(
        state = state,
        // resourceState = emptyMap(),
        resourceState = mapOf(
            state.contacts[1] to (context.getDrawable(R.drawable.ali) as BitmapDrawable).bitmap,
            state.contacts[2] to (context.getDrawable(R.drawable.taylor) as BitmapDrawable).bitmap,
        ),
        renderer = MessagingTileRenderer(LocalContext.current)
    )
}

@IconSizePreview
@Composable
private fun SearchButtonPreview() {
    LayoutElementPreview(
        searchLayout(
            context = LocalContext.current,
            clickable = emptyClickable
        )
    ) {
        addIdToImageMapping(
            MessagingTileRenderer.ID_IC_SEARCH,
            drawableResToImageResource(R.drawable.ic_search_24)
        )
    }
}