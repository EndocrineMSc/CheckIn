package com.endocrine.checkin.domain.model

/**
 * The authoritative Gefühlsrad hierarchy (see `lookup/L1_emotion_wheel.md`), encoded
 * verbatim as nested immutable data: 6 categories × 6 Ebene-2 × exactly 2 Ebene-3 leaves.
 *
 * The hierarchy is modelled structurally so a leaf's parents are *known*, never
 * string-searched. This matters because of data quirks (e.g. "verletzt" is both a Wut
 * leaf and a Trauer Ebene-2 name) — see [resolve].
 */
object EmotionWheel {

    data class WheelCategory(
        val name: String,
        val colorHex: String,
        val level2: List<WheelL2>,
    )

    data class WheelL2(
        val name: String,
        val leaves: List<String>,
    )

    val categories: List<WheelCategory> = listOf(
        WheelCategory(
            name = "Freude",
            colorHex = "#F2B33D",
            level2 = listOf(
                WheelL2("optimistisch", listOf("hoffnungsvoll", "eifrig")),
                WheelL2("glücklich", listOf("erfreut", "amüsiert")),
                WheelL2("stolz", listOf("wertvoll", "triumphierend")),
                WheelL2("fröhlich", listOf("ausgelassen", "beschwingt")),
                WheelL2("zufrieden", listOf("ausgeglichen", "froh")),
                WheelL2("euphorisch", listOf("begeistert", "enthusiastisch")),
            ),
        ),
        WheelCategory(
            name = "Überraschung",
            colorHex = "#3BB9C4",
            level2 = listOf(
                WheelL2("bewegt", listOf("angeregt", "berührt")),
                WheelL2("aufgeregt", listOf("energiegeladen", "aufgewühlt")),
                WheelL2("verwirrt", listOf("desillusioniert", "durcheinander")),
                WheelL2("erstaunt", listOf("perplex", "fassungslos")),
                WheelL2("erschrocken", listOf("enttäuscht", "ernüchtert")),
                WheelL2("überwältigt", listOf("verblüfft", "sprachlos")),
            ),
        ),
        WheelCategory(
            name = "Liebe",
            colorHex = "#E86A92",
            level2 = listOf(
                WheelL2("verzaubert", listOf("beeindruckt", "fasziniert")),
                WheelL2("sentimental", listOf("zart", "nostalgisch")),
                WheelL2("dankbar", listOf("gerührt", "anerkennend")),
                WheelL2("romantisch", listOf("verliebt", "leidenschaftlich")),
                WheelL2("liebevoll", listOf("mitfühlend", "warmherzig")),
                WheelL2("friedlich", listOf("vertraut", "ruhig")),
            ),
        ),
        WheelCategory(
            name = "Wut",
            colorHex = "#D9534F",
            level2 = listOf(
                WheelL2("hasserfüllt", listOf("verärgert", "verletzt")),
                // "erregt" is correct here (the design doc's "erbost" was wrong).
                WheelL2("gereizt", listOf("genervt", "erregt")),
                WheelL2("kritisch", listOf("abweisend", "skeptisch")),
                WheelL2("angeekelt", listOf("ablehnend", "verächtlich")),
                WheelL2("aufgebracht", listOf("rasend", "zornig")),
                WheelL2("eifersüchtig", listOf("neidisch", "nachtragend")),
            ),
        ),
        WheelCategory(
            name = "Angst",
            colorHex = "#8E6FC9",
            level2 = listOf(
                WheelL2("verängstigt", listOf("panisch", "verfolgt")),
                WheelL2("abgelehnt", listOf("ausgeschlossen", "unzureichend")),
                WheelL2("entsetzt", listOf("bestürzt", "erschüttert")),
                WheelL2("nervös", listOf("besorgt", "unruhig")),
                WheelL2("unsicher", listOf("unterlegen", "minderwertig")),
                WheelL2("geschockt", listOf("hilflos", "starr")),
            ),
        ),
        WheelCategory(
            name = "Trauer",
            colorHex = "#4A7FB5",
            level2 = listOf(
                WheelL2("verzweifelt", listOf("hoffnungslos", "machtlos")),
                WheelL2("schuldig", listOf("beschämt", "betroffen")),
                WheelL2("gleichgültig", listOf("apathisch", "ausdruckslos")),
                WheelL2("deprimiert", listOf("miserabel", "leer")),
                // "verletzt" (Trauer Ebene-2) — note: this is an L2 name, not a leaf.
                WheelL2("verletzt", listOf("beleidigt", "gekränkt")),
                WheelL2("einsam", listOf("vernachlässigt", "isoliert")),
            ),
        ),
    )

    /** 6 × 6 × 2 = 72 selectable leaves. */
    const val EXPECTED_LEAF_COUNT = 72

    /**
     * Resolve an Ebene-3 [l3Leaf] to its full [Emotion] with structurally-known parents.
     *
     * Only *leaves* are matched, so "verletzt" unambiguously resolves to
     * **Wut → hasserfüllt → verletzt** and never collides with the Trauer Ebene-2
     * named "verletzt" (which is not a leaf). Returns `null` if no leaf matches.
     */
    fun resolve(l3Leaf: String): Emotion? {
        for (category in categories) {
            for (l2 in category.level2) {
                if (l3Leaf in l2.leaves) {
                    return Emotion(category = category.name, l2 = l2.name, l3 = l3Leaf)
                }
            }
        }
        return null
    }

    /** Every selectable leaf, flattened (for validation/iteration). */
    val allLeaves: List<String> =
        categories.flatMap { category -> category.level2.flatMap { it.leaves } }
}
