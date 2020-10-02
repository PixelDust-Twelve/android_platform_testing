/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.server.wm.flicker.common.traces.windowmanager.windows

/**
 * Represents an activity in the window manager hierarchy
 *
 * This is a generic object that is reused by both Flicker and Winscope and cannot
 * access internal Java/Android functionality
 *
 **/
open class Activity(
    name: String,
    val state: String,
    visible: Boolean,
    val frontOfTask: Boolean,
    val procId: Int,
    val isTranslucent: Boolean,
    private val parent: WindowContainer,
    windowContainer: WindowContainer
) : WindowContainer(windowContainer, name, visible) {
    init {
        require(parent is ActivityTask) { "Activity parent must be a task" }
    }

    override val kind: String = "Activity"
    val task: ActivityTask get() = parent as ActivityTask

    override fun toString(): String {
        return "$kind {$token $title} state=$state visible=$isVisible"
    }
}