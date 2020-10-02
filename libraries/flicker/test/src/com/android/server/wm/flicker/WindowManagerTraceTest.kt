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

package com.android.server.wm.flicker

import com.android.server.wm.flicker.assertions.assertFailed
import com.android.server.wm.flicker.assertions.assertPassed
import com.android.server.wm.flicker.common.Region
import com.android.server.wm.flicker.common.traces.windowmanager.WindowManagerTrace
import com.android.server.wm.flicker.common.traces.windowmanager.WindowManagerState
import com.android.server.wm.flicker.common.traces.windowmanager.windows.WindowContainer
import com.android.server.wm.flicker.traces.windowmanager.WindowManagerTraceParser
import com.android.server.wm.flicker.traces.windowmanager.coversAtLeastRegion
import com.android.server.wm.flicker.traces.windowmanager.coversAtMostRegion
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.lang.reflect.Modifier

/**
 * Contains [WindowManagerTrace] tests. To run this test: `atest
 * FlickerLibTest:WindowManagerTraceTest`
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class WindowManagerTraceTest {
    private val trace: WindowManagerTrace by lazy {
        readWindowManagerTraceFromFile("wm_trace_openchrome.pb")
    }

    @Test
    fun canParseAllEntries() {
        val firstEntry = trace.entries[0]
        assertThat(firstEntry.timestamp).isEqualTo(9213763541297L)
        assertThat(firstEntry.windowStates.size).isEqualTo(10)
        assertThat(firstEntry.visibleWindows.size).isEqualTo(6)
        assertThat(trace.entries[trace.entries.size - 1].timestamp)
                .isEqualTo(9216093628925L)
    }

    @Test
    fun canDetectAboveAppWindowVisibility() {
        val entry = trace.getEntry(9213763541297L)
        entry.isAboveAppWindow("NavigationBar").assertPassed()
        entry.isAboveAppWindow("ScreenDecorOverlay").assertPassed()
        entry.isAboveAppWindow("StatusBar").assertPassed()
        entry.isAboveAppWindow("pip-dismiss-overlay").assertFailed("is invisible")
        entry.isAboveAppWindow("NotificationShade").assertFailed("is invisible")
        entry.isAboveAppWindow("InputMethod").assertFailed("is invisible")
        entry.isAboveAppWindow("AssistPreviewPanel").assertFailed("is invisible")
    }

    @Test
    fun canDetectWindowCoversAtLeastRegion() {
        val entry = trace.getEntry(9213763541297L)
        // Exact size
        entry.coversAtLeastRegion("StatusBar", Region(0, 0, 1440, 171)).assertPassed()
        entry.coversAtLeastRegion(
                "com.google.android.apps.nexuslauncher", Region(0, 0, 1440, 2960))
                .assertPassed()
        // Smaller region
        entry.coversAtLeastRegion("StatusBar", Region(0, 0, 100, 100)).assertPassed()
        entry.coversAtLeastRegion(
                "com.google.android.apps.nexuslauncher", Region(0, 0, 100, 100))
                .assertPassed()
        // Larger region
        entry.coversAtLeastRegion("StatusBar", Region(0, 0, 1441, 171))
                .assertFailed("Uncovered region: SkRegion((1440,0,1441,171))")
        entry.coversAtLeastRegion(
                "com.google.android.apps.nexuslauncher", Region(0, 0, 1440, 2961))
                .assertFailed("Uncovered region: SkRegion((0,2960,1440,2961))")
    }

    @Test
    fun canDetectWindowCoversAtMostRegion() {
        val entry = trace.getEntry(9213763541297L)
        // Exact size
        entry.coversAtMostRegion("StatusBar", Region(0, 0, 1440, 171)).assertPassed()
        entry.coversAtMostRegion(
                "com.google.android.apps.nexuslauncher", Region(0, 0, 1440, 2960))
                .assertPassed()
        // Smaller region
        entry.coversAtMostRegion("StatusBar", Region(0, 0, 100, 100))
                .assertFailed("Out-of-bounds region: SkRegion((100,0,1440,100)(0,100,1440,171))")
        entry.coversAtMostRegion(
                "com.google.android.apps.nexuslauncher", Region(0, 0, 100, 100))
                .assertFailed("Out-of-bounds region: SkRegion((100,0,1440,100)(0,100,1440,2960))")
        // Larger region
        entry.coversAtMostRegion("StatusBar", Region(0, 0, 1441, 171)).assertPassed()
        entry.coversAtMostRegion(
                "com.google.android.apps.nexuslauncher", Region(0, 0, 1440, 2961))
                .assertPassed()
    }

    @Test
    fun canDetectBelowAppWindowVisibility() {
        trace.getEntry(9213763541297L).hasNonAppWindow("wallpaper").assertPassed()
    }

    @Test
    fun canDetectAppWindow() {
        val appWindows = trace.getEntry(9213763541297L).appWindows
        assertWithMessage("Unable to detect app windows").that(appWindows.size).isEqualTo(2)
    }

    @Test
    fun canDetectAppWindowVisibility() {
        trace.getEntry(9213763541297L)
                .isAppWindowVisible("com.google.android.apps.nexuslauncher").assertPassed()
        trace.getEntry(9215551505798L).isAppWindowVisible("com.android.chrome").assertPassed()
    }

    @Test
    fun canFailWithReasonForVisibilityChecks_windowNotFound() {
        trace.getEntry(9213763541297L)
                .hasNonAppWindow("ImaginaryWindow")
                .assertFailed("ImaginaryWindow cannot be found")
    }

    @Test
    fun canFailWithReasonForVisibilityChecks_windowNotVisible() {
        trace.getEntry(9213763541297L)
                .hasNonAppWindow("InputMethod")
                .assertFailed("InputMethod is invisible")
    }

    @Test
    fun canDetectAppZOrder() {
        trace.getEntry(9215551505798L)
                .isAppWindowVisible("com.google.android.apps.nexuslauncher")
                .assertPassed()
        trace.getEntry(9215551505798L)
                .isVisibleAppWindowOnTop("com.android.chrome").assertPassed()
    }

    @Test
    fun canFailWithReasonForZOrderChecks_windowNotOnTop() {
        trace.getEntry(9215551505798L)
                .isVisibleAppWindowOnTop("com.google.android.apps.nexuslauncher")
                .assertFailed("wanted=com.google.android.apps.nexuslauncher")
        trace.getEntry(9215551505798L)
                .isVisibleAppWindowOnTop("com.google.android.apps.nexuslauncher")
                .assertFailed("found=Splash Screen com.android.chrome")
    }

    @Test
    fun canParseFromDump() {
        val trace = try {
            WindowManagerTraceParser.parseFromDump(
                readTestFile("wm_trace_dump.pb"))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        assertWithMessage("Unable to parse dump").that(trace.entries).hasSize(1)
    }

    /**
     * Access all public methods and invokes all public getters from the object
     * to check that all lazy properties contain valid values
     */
    private fun <T> Class<T>.accessProperties(obj: Any) {
        val propertyValues = this.declaredFields
            .filter { Modifier.isPublic(it.modifiers) }
            .map { kotlin.runCatching { Pair(it.name, it.get(obj)) } }
            .filter { it.isFailure }

        assertWithMessage("The following properties could not be read: " +
            propertyValues.joinToString("\n"))
            .that(propertyValues)
            .isEmpty()

        val getterValues = this.declaredMethods
            .filter {
                Modifier.isPublic(it.modifiers) &&
                    it.name.startsWith("get") &&
                    it.parameterCount == 0
            }
            .map { kotlin.runCatching { Pair(it.name, it.invoke(obj)) } }
            .filter { it.isFailure }

        assertWithMessage("The following methods could not be invoked: " +
            getterValues.joinToString("\n"))
            .that(getterValues)
            .isEmpty()

        this.superclass?.accessProperties(obj)
        if (obj is WindowContainer) {
            obj.childrenWindows.forEach { it::class.java.accessProperties(it) }
        }
    }

    /**
     * Tests if all properties of the flicker objects are accessible. This is necessary because
     * most values are lazy initialized and only trigger errors when being accessed for the
     * first time.
     */
    @Test
    fun canAccessAllProperties() {
        arrayOf("wm_trace_activity_transition.pb", "wm_trace_openchrome2.pb").forEach { traceName ->
            val trace = readWindowManagerTraceFromFile(traceName)
            assertWithMessage("Unable to parse dump")
                .that(trace.entries.size)
                .isGreaterThan(1)

            trace.entries.forEach { entry: WindowManagerState ->
                entry::class.java.accessProperties(entry)
                entry.displays.forEach { it::class.java.accessProperties(it) }
            }
        }
    }

    companion object {
        private fun readWindowManagerTraceFromFile(relativePath: String): WindowManagerTrace {
            return try {
                WindowManagerTraceParser.parseFromTrace(readTestFile(relativePath))
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}
