import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PacketTest {
    private fun testOne(packets: List<Packet>, p: Double) {
        packets.forEachIndexed { ind, packet ->
            val (newPacket, corrupted) = packet.corrupt(p)
            println("""
                Packet #$ind:
                State before: $packet
                State after:  $newPacket
                Corruption happened: $corrupted, Corruption detected: ${!newPacket.validateCRC()}
            """.trimIndent())
            assertEquals(corrupted, !newPacket.validateCRC())
        }
    }

    private fun splitString(str: String): List<Packet> {
        val result = mutableListOf<Packet>()
        val bytes = str.encodeToByteArray()
        for (i in bytes.indices step Packet.MAX_SIZE) {
            var curData = 0L
            for (j in 0 until minOf(Packet.MAX_SIZE, bytes.size - i)) {
                curData *= 8L
                curData += bytes[i + j]
            }
            result.add(Packet(curData))
        }
        return result
    }

    @Test
    fun testNoCorruption() {
        val data = splitString(stringToTest)
        testOne(data, 0.0)
    }

    @Test
    fun testWithCorruption() {
        val data = splitString(stringToTest)
        testOne(data, 0.02)
    }

    companion object {
        const val stringToTest = "Divide and suffer"
    }
}