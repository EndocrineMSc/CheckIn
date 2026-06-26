package com.endocrine.checkin.domain.model

/**
 * One emotion = the chosen Ebene-3 leaf together with its two fixed parents.
 * Parents are stored automatically; the user only ever taps the leaf ([l3]).
 *
 * @param category Ebene 1, e.g. `Wut`
 * @param l2 Ebene 2, e.g. `hasserfüllt`
 * @param l3 Ebene 3 leaf, e.g. `verletzt`
 */
data class Emotion(
    val category: String,
    val l2: String,
    val l3: String,
)
