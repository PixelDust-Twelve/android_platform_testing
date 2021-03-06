/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.server.wm.traces.common.errors

/**
 * Flicker Error identified in a WindowManager or SurfaceFlinger trace
 * @param stacktrace Stacktrace to identify source of errors
 * @param message Message to explain error briefly
 * @param layerId The layer which the error is associated with
 * @param windowToken The window which the error is associated with
 * @param taskId The task which the error is associated with
 * @param assertionName The class name of the assertion that generated the error
 */
data class Error(
    val stacktrace: String,
    val message: String,
    val layerId: Int = 0,
    val windowToken: String = "",
    val taskId: Int = 0,
    val assertionName: String = ""
)