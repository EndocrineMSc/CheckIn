package com.endocrine.checkin.data.export

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.startsWith
import com.endocrine.checkin.domain.model.BodyState
import com.endocrine.checkin.domain.model.CheckinEntry
import com.endocrine.checkin.domain.model.Emotion
import org.junit.jupiter.api.Test

class CsvSerializerTest {

    private val serializer = CsvSerializer()

    private fun entry(
        id: Long = 1,
        timestamp: Long = 1_000,
        timezone: String = "Europe/Berlin",
        note: String? = null,
    ) = CheckinEntry(
        id = id,
        timestamp = timestamp,
        timezone = timezone,
        body = BodyState(energy = 3, fatigue = 2, hunger = 4, tension = 1),
        emotion = Emotion(category = "Wut", l2 = "hasserfüllt", l3 = "verletzt"),
        note = note,
    )

    @Test
    fun `header order matches the data model`() {
        val csv = serializer.serialize(emptyList())
        assertThat(csv).isEqualTo(CsvSerializer.HEADER + "\r\n")
    }

    @Test
    fun `empty list yields header only`() {
        val csv = serializer.serialize(emptyList())
        assertThat(csv.lines().filter { it.isNotEmpty() }).hasSize(1)
    }

    @Test
    fun `a plain entry produces an unquoted row`() {
        val csv = serializer.serialize(listOf(entry(note = "ruhig")))
        val rows = csv.split("\r\n").filter { it.isNotEmpty() }
        assertThat(rows[1]).isEqualTo("1,1000,Europe/Berlin,3,2,4,1,Wut,hasserfüllt,verletzt,ruhig")
    }

    @Test
    fun `a note with comma quote and newline is RFC-4180 quoted`() {
        val tricky = "hallo, \"welt\"\nzeile2"
        val csv = serializer.serialize(listOf(entry(note = tricky)))
        // Inner quotes doubled, whole field wrapped; the embedded newline stays inside the quotes.
        assertThat(csv).contains("\"hallo, \"\"welt\"\"\nzeile2\"")
    }

    @Test
    fun `a quoted note round-trips back to the original value`() {
        val tricky = "x,y\"z\r\nw"
        val csv = serializer.serialize(listOf(entry(note = tricky)))
        val field = lastFieldOf(csv)
        assertThat(field).isEqualTo(tricky)
    }

    @Test
    fun `null note serializes as an empty field`() {
        val csv = serializer.serialize(listOf(entry(note = null)))
        val rows = csv.split("\r\n").filter { it.isNotEmpty() }
        assertThat(rows[1]).startsWith("1,1000,Europe/Berlin,3,2,4,1,Wut,hasserfüllt,verletzt,")
        assertThat(rows[1].endsWith(",")).isEqualTo(true)
    }

    @Test
    fun `entries are emitted sorted by timestamp ascending`() {
        val csv = serializer.serialize(
            listOf(
                entry(id = 3, timestamp = 300),
                entry(id = 1, timestamp = 100),
                entry(id = 2, timestamp = 200),
            ),
        )
        val ids = csv.split("\r\n")
            .filter { it.isNotEmpty() }
            .drop(1) // header
            .map { it.substringBefore(",") }
        assertThat(ids).isEqualTo(listOf("1", "2", "3"))
    }

    @Test
    fun `full dataset emits one row per entry plus header`() {
        val entries = (1..5L).map { entry(id = it, timestamp = it * 10) }
        val csv = serializer.serialize(entries)
        val lines = csv.split("\r\n").filter { it.isNotEmpty() }
        assertThat(lines).hasSize(6)
    }

    /**
     * Minimal RFC-4180 parser for the *last* field of the single data row, used to prove
     * that what [CsvSerializer] quotes can be read back to the original string.
     */
    private fun lastFieldOf(csv: String): String {
        // Strip header + trailing CRLF; what remains is one record (which may contain CRLFs
        // inside the quoted note).
        val record = csv.removePrefix(CsvSerializer.HEADER + "\r\n").removeSuffix("\r\n")
        var i = 0
        var lastField = ""
        val current = StringBuilder()
        var inQuotes = false
        while (i < record.length) {
            val c = record[i]
            when {
                inQuotes && c == '"' && i + 1 < record.length && record[i + 1] == '"' -> {
                    current.append('"'); i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    lastField = current.toString(); current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        lastField = current.toString()
        return lastField
    }
}
