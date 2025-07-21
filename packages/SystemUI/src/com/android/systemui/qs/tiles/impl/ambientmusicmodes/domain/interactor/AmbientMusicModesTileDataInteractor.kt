/*
 * Copyright (C) 2024 The Android Open Source Project
 * Copyright (C) 2026 StatiXOS
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

package com.android.systemui.qs.tiles.impl.ambientmusicmodes.domain.interactor

import android.content.Context
import android.os.UserHandle
import com.android.app.tracing.coroutines.flow.flowName
import com.android.systemui.common.shared.model.ContentDescription
import com.android.systemui.common.shared.model.Icon
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.qs.tiles.AmbientMusicModesTile
import com.android.systemui.qs.tiles.base.domain.interactor.QSTileDataInteractor
import com.android.systemui.qs.tiles.base.domain.model.DataUpdateTrigger
import com.android.systemui.qs.tiles.impl.ambientmusicmodes.domain.model.AmbientMusicModesTileModel
import com.android.systemui.res.R
import com.android.systemui.shade.ShadeDisplayAware
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@SysUISingleton
class AmbientMusicModesTileDataInteractor
@Inject
constructor(
    @ShadeDisplayAware val context: Context,
    @Background val bgDispatcher: CoroutineDispatcher,
) : QSTileDataInteractor<AmbientMusicModesTileModel> {

    private val activeModeId = MutableStateFlow<String?>(null)

    override fun tileData(
        user: UserHandle,
        triggers: Flow<DataUpdateTrigger>,
    ): Flow<AmbientMusicModesTileModel> = tileData()

    fun tileData(): Flow<AmbientMusicModesTileModel> =
        activeModeId
            .map { id -> buildTileData(id) }
            .flowName("AmbientMusicTileData")
            .flowOn(bgDispatcher)
            .distinctUntilChanged()

    suspend fun getCurrentTileModel(): AmbientMusicModesTileModel {
        return buildTileData(activeModeId.value)
    }

    fun setActiveMode(id: String?) {
        activeModeId.value = id
    }

    private suspend fun buildTileData(id: String?): AmbientMusicModesTileModel {
        val isActivated = id != null

        val activeModes = if (id != null) {
            listOf(AmbientMusicModesTileModel.ActiveMode(id = id, name = id.replaceFirstChar { it.uppercase() }))
        } else {
            emptyList()
        }

        val icon =
            if (id == "calm"){
                Icon.Loaded(
                    context.getDrawable(R.drawable.ic_calm)!!,
                    contentDescription = ContentDescription.Loaded("Calm"),
                    resId = R.drawable.ic_calm,
                )
            } else if (id == "chill"){
                Icon.Loaded(
                    context.getDrawable(R.drawable.ic_chill)!!,
                    contentDescription = ContentDescription.Loaded("Chill"),
                    resId = R.drawable.ic_chill,
                )
            } else if (id == "sleep"){
                Icon.Loaded(
                    context.getDrawable(R.drawable.ic_sleep)!!,
                    contentDescription = ContentDescription.Loaded("Sleep"),
                    resId = R.drawable.ic_sleep,
                )
            } else if (id == "focus"){
                Icon.Loaded(
                    context.getDrawable(R.drawable.ic_focus)!!,
                    contentDescription = ContentDescription.Loaded("Focus"),
                    resId = R.drawable.ic_focus,
                )
            } else {
                getDefaultTileIcon()
            }

        return AmbientMusicModesTileModel(
            isActivated = isActivated,
            activeModes = activeModes,
            icon = icon,
        )
    }

    private fun getDefaultTileIcon() =
        Icon.Loaded(
            context.getDrawable(AmbientMusicModesTile.ICON_RES_ID)!!,
            contentDescription = null,
            resId = AmbientMusicModesTile.ICON_RES_ID,
        )

    override fun availability(user: UserHandle): Flow<Boolean> = flowOf(true)
}
