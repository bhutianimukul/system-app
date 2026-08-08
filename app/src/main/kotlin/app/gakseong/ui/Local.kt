package app.gakseong.ui

import androidx.compose.runtime.compositionLocalOf
import app.gakseong.data.SystemSerializer
import app.gakseong.data.SystemState

/**
 * Ambient state, following the convention `LocalPalette`, `LocalMetrics`, `LocalType` and `LocalHunterClass`
 * already set in this codebase. A screen reads `LocalSystem.current` the same way it reads its palette.
 *
 * Defaults to the seed so a `@Preview` renders without a running Application.
 */
val LocalSystem = compositionLocalOf<SystemState> { SystemSerializer.defaultValue }
