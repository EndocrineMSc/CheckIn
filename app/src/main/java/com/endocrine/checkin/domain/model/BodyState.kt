package com.endocrine.checkin.domain.model

/**
 * The four body scales captured in step 1 of a check-in. Each value is 1..5.
 *
 * Scale direction labels (shown in the UI):
 * - [energy] niedrig → hoch
 * - [fatigue] wach → erschöpft (Müdigkeit)
 * - [hunger] hungrig → satt (Hunger/Sättigung)
 * - [tension] entspannt → angespannt (Anspannung)
 */
data class BodyState(
    val energy: Int,
    val fatigue: Int,
    val hunger: Int,
    val tension: Int,
) {
    init {
        require(energy in RANGE) { "energy out of range: $energy" }
        require(fatigue in RANGE) { "fatigue out of range: $fatigue" }
        require(hunger in RANGE) { "hunger out of range: $hunger" }
        require(tension in RANGE) { "tension out of range: $tension" }
    }

    companion object {
        val RANGE = 1..5
    }
}
