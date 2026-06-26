package com.endocrine.checkin.domain.model

/**
 * A fully-completed check-in: both mandatory steps (4 body scales + 1 emotion) done.
 *
 * @param id 0 for a not-yet-saved entry; assigned by Room on insert.
 * @param timestamp capture moment, epoch millis UTC. Preserved across edits.
 * @param timezone IANA zone id at capture, e.g. `Europe/Berlin`.
 * @param note optional free text.
 */
data class CheckinEntry(
    val id: Long = 0,
    val timestamp: Long,
    val timezone: String,
    val body: BodyState,
    val emotion: Emotion,
    val note: String? = null,
)
