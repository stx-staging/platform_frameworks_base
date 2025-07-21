/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.policy.ui.dialog.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.systemui.statusbar.policy.ui.dialog.viewmodel.AmbientMusicModesDialogViewModel

@Composable
fun AmbientMusicModeTileGrid(
    viewModel: AmbientMusicModesDialogViewModel,
    modifier: Modifier = Modifier,
    inDetailsView: Boolean = false,
) {
    val tiles by viewModel.tiles.collectAsStateWithLifecycle(initialValue = emptyList())

    val verticalSpacing = if (inDetailsView) 2.dp else 8.dp

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth().heightIn(max = 280.dp),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tiles.size, key = { index -> tiles[index].id }) { index ->
            AmbientMusicModeTile(
                viewModel = tiles[index],
                type = getAmbientMusicModeTileType(inDetailsView, index, tiles.size),
            )
        }
    }
}

fun getAmbientMusicModeTileType(inDetailsView: Boolean, index: Int, tilesSize: Int): AmbientMusicModeTileType {
    return if (inDetailsView) {
        if (tilesSize == 1) return AmbientMusicModeTileType.ONLY_TILE

        when (index) {
            0 -> AmbientMusicModeTileType.START_TILE
            tilesSize - 1 -> AmbientMusicModeTileType.END_TILE
            else -> AmbientMusicModeTileType.MIDDLE_TILE
        }
    } else {
        AmbientMusicModeTileType.DEFAULT
    }
}
