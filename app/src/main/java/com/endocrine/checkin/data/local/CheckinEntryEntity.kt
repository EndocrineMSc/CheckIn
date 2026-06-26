package com.endocrine.checkin.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One fully-completed check-in = one row. Written only when both mandatory steps
 * (4 body scales + 1 emotion) are done; aborted check-ins are never persisted.
 *
 * Entity ⇄ domain mapping is added in Step 3.
 */
@Entity(tableName = "checkin_entries")
data class CheckinEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Capture moment, epoch millis UTC. Preserved across edits — never "last edited". */
    val timestamp: Long,

    /** IANA zone id at capture, e.g. `Europe/Berlin`. */
    val timezone: String,

    /** 1–5, niedrig → hoch. */
    val energy: Int,

    /** 1–5, wach → erschöpft (Müdigkeit). */
    val fatigue: Int,

    /** 1–5, hungrig → satt (Hunger/Sättigung). */
    val hunger: Int,

    /** 1–5, entspannt → angespannt (Anspannung). */
    val tension: Int,

    @ColumnInfo(name = "emotion_category")
    val emotionCategory: String,

    @ColumnInfo(name = "emotion_l2")
    val emotionL2: String,

    @ColumnInfo(name = "emotion_l3")
    val emotionL3: String,

    /** Optional free text. */
    val note: String? = null,
)
