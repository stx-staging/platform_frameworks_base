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

package com.android.systemui.qs.tiles.impl.ambientmusicmodes.domain.interactor

import android.app.ActivityManager.RunningAppProcessInfo
import android.app.IActivityManager
import android.content.Context
import android.content.Intent
import android.os.RemoteException
import com.android.systemui.animation.Expandable
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.qs.tiles.base.domain.actions.QSTileIntentUserInputHandler
import com.android.systemui.qs.tiles.base.domain.interactor.QSTileUserActionInteractor
import com.android.systemui.qs.tiles.base.domain.model.QSTileInput
import com.android.systemui.qs.tiles.base.shared.model.QSTileUserAction
import com.android.systemui.qs.tiles.impl.ambientmusicmodes.domain.model.AmbientMusicModesTileModel
import com.android.systemui.statusbar.policy.ui.dialog.AmbientMusicModesDialogDelegate
import javax.inject.Inject

@SysUISingleton
class AmbientMusicModesTileUserActionInteractor
@Inject
constructor(
    val context: Context,
    private val activityManager: IActivityManager,
    private val qsTileIntentUserInputHandler: QSTileIntentUserInputHandler,
    private val dialogDelegate: AmbientMusicModesDialogDelegate,
) : QSTileUserActionInteractor<AmbientMusicModesTileModel> {

    override suspend fun handleInput(input: QSTileInput<AmbientMusicModesTileModel>) {
        with(input) {
            when (action) {
                is QSTileUserAction.Click -> {
                    handleClick(action.expandable)
                }
                is QSTileUserAction.ToggleClick -> {
                    handleToggleClick(input.data, action.expandable)
                }
                is QSTileUserAction.LongClick -> {}
            }
        }
    }

    suspend fun handleClick(expandable: Expandable?) {
        // Show a dialog with the list of modes to configure.
        dialogDelegate.showDialog(expandable)
    }

    suspend fun handleToggleClick(ambientMusicModesTileModel: AmbientMusicModesTileModel, expandable: Expandable?) {
        val isRunning = try {
            val runningProcesses = activityManager.runningAppProcesses
            runningProcesses?.any { processInfo ->
                processInfo.processName == "com.sourajitk.ambient_music" &&
                processInfo.importance <= RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE
            } ?: false
        } catch (e: RemoteException) {
            false
        }
        val intent = Intent(ACTION_TOGGLE_PLAYBACK_QS).apply{
            setPackage("com.sourajitk.ambient_music")
        }
        if (isRunning) {
            context.startForegroundService(intent)
        } else {
            dialogDelegate.showDialog(expandable)
        }
    }

    fun handleLongClick(expandable: Expandable?) {}

    companion object {
        const val ACTION_TOGGLE_PLAYBACK_QS = "com.sourajitk.ambient_music.ACTION_TOGGLE_PLAYBACK_QS"
    }
}
