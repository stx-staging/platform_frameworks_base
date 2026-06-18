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

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.systemui.common.shared.model.copy
import com.android.systemui.common.ui.compose.Icon
import com.android.systemui.statusbar.policy.ui.dialog.viewmodel.AmbientMusicModeTileViewModel

enum class AmbientMusicModeTileType {
    // The standard tile type. Will be used when the tile is shown in a dialog.
    DEFAULT,

    // Special types used for the details view.
    START_TILE,
    MIDDLE_TILE,
    END_TILE,
    ONLY_TILE,
}

data class AmbientMusicModeTileDimension(
    val titleFontWeight: FontWeight?,
    val titleStyle: TextStyle?,
    val subtitleFontWeight: FontWeight?,
    val subtitleStyle: TextStyle?,
) {
    companion object {
        val Default =
            AmbientMusicModeTileDimension(
                titleFontWeight = FontWeight.W500,
                titleStyle = null,
                subtitleFontWeight = FontWeight.W400,
                subtitleStyle = null,
            )

        // Applied to the mode tiles under the details view when the desktop sizing feature enabled.
        val DesktopSizingDimens: AmbientMusicModeTileDimension
            @Composable
            @OptIn(ExperimentalMaterial3ExpressiveApi::class)
            get() =
                AmbientMusicModeTileDimension(
                    titleFontWeight = null,
                    titleStyle = MaterialTheme.typography.titleSmallEmphasized,
                    subtitleFontWeight = null,
                    subtitleStyle = MaterialTheme.typography.labelMedium,
                )
    }
}

@Composable
fun AmbientMusicModeTile(
    viewModel: AmbientMusicModeTileViewModel,
    modifier: Modifier = Modifier,
    type: AmbientMusicModeTileType = AmbientMusicModeTileType.DEFAULT,
    dimension: AmbientMusicModeTileDimension = AmbientMusicModeTileDimension.Default,
) {
    val tileColor: Color by
        animateColorAsState(
            when {
                viewModel.enabled -> MaterialTheme.colorScheme.primary
                type == AmbientMusicModeTileType.DEFAULT -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        )
    val contentColor: Color by
        animateColorAsState(
            when {
                viewModel.enabled -> MaterialTheme.colorScheme.onPrimary
                type == AmbientMusicModeTileType.DEFAULT -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Surface(
            color = tileColor,
            shape = AmbientMusicModeTileShape.getTileShape(type),
            modifier = modifier,
        ) {
            Row(
                modifier =
                    Modifier.combinedClickable(
                            onClick = viewModel.onClick,
                            onLongClick = viewModel.onLongClick,
                            onLongClickLabel = viewModel.onLongClickLabel,
                        )
                        .padding(16.dp)
                        .semantics { stateDescription = viewModel.stateDescription },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(space = 12.dp, alignment = Alignment.Start),
            ) {
                // Clear the content description of the icon to prevent the mode name from getting
                // called out twice (once by the icon and once by the text).
                val decorativeIcon = viewModel.icon.copy(contentDescription = null)
                Icon(icon = decorativeIcon, modifier = Modifier.size(24.dp))
                Column {
                    Text(
                        viewModel.text,
                        fontWeight = dimension.titleFontWeight,
                        style = dimension.titleStyle ?: LocalTextStyle.current,
                        modifier = Modifier.tileMarquee().testTag("name"),
                    )
                    Text(
                        viewModel.subtext,
                        fontWeight = dimension.subtitleFontWeight,
                        style = dimension.subtitleStyle ?: LocalTextStyle.current,
                        modifier =
                            Modifier.tileMarquee()
                                .testTag(if (viewModel.enabled) "stateOn" else "stateOff")
                                .clearAndSetSemantics {
                                    contentDescription = viewModel.subtextDescription
                                },
                    )
                }
            }
        }
    }
}

private fun Modifier.tileMarquee(): Modifier {
    return this.basicMarquee(iterations = 1)
}

private object AmbientMusicModeTileShape {
    const val DEFAULT_RADIUS = 16
    const val LARGE_RADIUS = 28
    const val NO_RADIUS = 0

    fun getTileShape(type: AmbientMusicModeTileType): RoundedCornerShape {
        return when (type) {
            AmbientMusicModeTileType.DEFAULT -> RoundedCornerShape(DEFAULT_RADIUS.dp)
            AmbientMusicModeTileType.START_TILE ->
                RoundedCornerShape(topStart = LARGE_RADIUS.dp, topEnd = LARGE_RADIUS.dp)
            AmbientMusicModeTileType.MIDDLE_TILE -> RoundedCornerShape(NO_RADIUS)
            AmbientMusicModeTileType.END_TILE ->
                RoundedCornerShape(bottomStart = LARGE_RADIUS.dp, bottomEnd = LARGE_RADIUS.dp)
            AmbientMusicModeTileType.ONLY_TILE -> RoundedCornerShape(LARGE_RADIUS)
        }
    }
}
