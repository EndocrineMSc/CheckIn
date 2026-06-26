package com.endocrine.checkin.data.local

import com.endocrine.checkin.domain.model.BodyState
import com.endocrine.checkin.domain.model.CheckinEntry
import com.endocrine.checkin.domain.model.Emotion

/** Entity → domain. */
fun CheckinEntryEntity.toDomain(): CheckinEntry = CheckinEntry(
    id = id,
    timestamp = timestamp,
    timezone = timezone,
    body = BodyState(
        energy = energy,
        fatigue = fatigue,
        hunger = hunger,
        tension = tension,
    ),
    emotion = Emotion(
        category = emotionCategory,
        l2 = emotionL2,
        l3 = emotionL3,
    ),
    note = note,
)

/** Domain → entity. A new entry keeps `id = 0` so Room autogenerates the PK. */
fun CheckinEntry.toEntity(): CheckinEntryEntity = CheckinEntryEntity(
    id = id,
    timestamp = timestamp,
    timezone = timezone,
    energy = body.energy,
    fatigue = body.fatigue,
    hunger = body.hunger,
    tension = body.tension,
    emotionCategory = emotion.category,
    emotionL2 = emotion.l2,
    emotionL3 = emotion.l3,
    note = note,
)
