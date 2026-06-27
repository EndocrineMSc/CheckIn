package com.endocrine.checkin.domain.model

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import org.junit.jupiter.api.Test

/**
 * Locks the authoritative Gefühlsrad invariants (see `lookup/L1_emotion_wheel.md`).
 * If anyone reorders or mistypes an entry, these break.
 */
class EmotionWheelTest {

    @Test
    fun `has exactly 6 categories`() {
        assertThat(EmotionWheel.categories).hasSize(6)
    }

    @Test
    fun `every category has exactly 6 Ebene-2 emotions`() {
        EmotionWheel.categories.forEach { category ->
            assertThat(category.level2, name = category.name).hasSize(6)
        }
    }

    @Test
    fun `there are 36 Ebene-2 emotions in total`() {
        val total = EmotionWheel.categories.sumOf { it.level2.size }
        assertThat(total).isEqualTo(36)
    }

    @Test
    fun `every Ebene-2 has exactly 2 leaves`() {
        EmotionWheel.categories.forEach { category ->
            category.level2.forEach { l2 ->
                assertThat(l2.leaves, name = "${category.name} > ${l2.name}").hasSize(2)
            }
        }
    }

    @Test
    fun `there are exactly 72 leaves`() {
        assertThat(EmotionWheel.allLeaves).hasSize(72)
        assertThat(EmotionWheel.allLeaves).hasSize(EmotionWheel.EXPECTED_LEAF_COUNT)
    }

    @Test
    fun `every leaf resolves to its correct structural parents`() {
        EmotionWheel.categories.forEach { category ->
            category.level2.forEach { l2 ->
                l2.leaves.forEach { leaf ->
                    val resolved = EmotionWheel.resolve(leaf)
                    assertThat(resolved, name = leaf).isNotNull().isEqualTo(
                        Emotion(category = category.name, l2 = l2.name, l3 = leaf),
                    )
                }
            }
        }
    }

    @Test
    fun `duplicate verletzt resolves to Wut hasserfuellt as a leaf`() {
        val resolved = EmotionWheel.resolve("verletzt")
        assertThat(resolved).isNotNull().all {
            prop(Emotion::category).isEqualTo("Wut")
            prop(Emotion::l2).isEqualTo("hasserfüllt")
            prop(Emotion::l3).isEqualTo("verletzt")
        }
    }

    @Test
    fun `Trauer verletzt is an Ebene-2 name and never a leaf`() {
        val trauer = EmotionWheel.categories.single { it.name == "Trauer" }
        val verletztL2 = trauer.level2.single { it.name == "verletzt" }
        // The L2 "verletzt" exists but its own leaves are different words — it is not selectable.
        assertThat(verletztL2.leaves).containsExactly("beleidigt", "gekränkt")
        // "verletzt" appears exactly once among all leaves (the Wut leaf), not twice.
        assertThat(EmotionWheel.allLeaves.count { it == "verletzt" }).isEqualTo(1)
    }

    @Test
    fun `erregt not erbost is the gereizt leaf`() {
        val gereizt = EmotionWheel.categories
            .single { it.name == "Wut" }
            .level2.single { it.name == "gereizt" }
        assertThat(gereizt.leaves).containsExactly("genervt", "erregt")
    }

    @Test
    fun `no name has leading or trailing whitespace`() {
        EmotionWheel.categories.forEach { category ->
            assertThat(category.name).isEqualTo(category.name.trim())
            category.level2.forEach { l2 ->
                assertThat(l2.name).isEqualTo(l2.name.trim())
                l2.leaves.forEach { leaf ->
                    assertThat(leaf).isEqualTo(leaf.trim())
                }
            }
        }
    }

    @Test
    fun `unknown leaf resolves to null`() {
        assertThat(EmotionWheel.resolve("nichtvorhanden")).isNull()
    }
}
