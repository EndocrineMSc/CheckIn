package com.endocrine.checkin.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe destinations for the single-activity nav host.
 *
 * Single feature ⇒ all routes live here and navigation is driven via [androidx.navigation.NavController]
 * (no cross-feature callbacks needed; see L4 navigation decisions).
 */

/** Main screen on manual launch. History *is* the home screen — there is no separate home. */
@Serializable
data object History

/**
 * The two-step check-in (body scales → emotion wheel) hosted as one route with internal
 * pager state and a shared ViewModel (Step 6). Simpler back handling for the "Verwerfen?" dialog.
 *
 * @param entryId `null` = a new check-in. (Editing existing entries uses [Detail] instead;
 *   the field is kept for symmetry/future use.)
 * @param fromNotification `true` when launched directly by a notification tap. Drives post-save
 *   behavior: finish the activity afterwards rather than popping back to [History].
 */
@Serializable
data class Checkin(
    val entryId: Long? = null,
    val fromNotification: Boolean = false,
)

/** Combined detail/edit screen for one existing entry (not the two-step flow replayed). */
@Serializable
data class Detail(val entryId: Long)

/** Reminders, permissions and CSV export trigger. */
@Serializable
data object Settings

/** One-time first-launch onboarding (permissions + battery exemption). */
@Serializable
data object Onboarding
