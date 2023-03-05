import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class TestNet {
    private val sampleNet // Sample net from lectures
        get() = Net(
            setOf(1, 2, 3),
            mapOf(1 to 2 to 4, 2 to 3 to 1, 1 to 3 to 50)
        )

    private fun Net.checkNet(expected: Map<Int, Map<Int, Int>>) {
        Thread.sleep(100)
        assertEquals(expected, allDistances)
        assertFalse(anyHasUnprocessedUpdates)
        finishAll()
    }

    @Test
    fun `simple test`() {
        val net = sampleNet

        val expected = mapOf(
            1 to mapOf(1 to 0, 2 to 4, 3 to 5),
            2 to mapOf(1 to 4, 2 to 0, 3 to 1),
            3 to mapOf(1 to 5, 2 to 1, 3 to 0)
        )

        net.checkNet(expected)
    }

    @Test
    fun `test update to lower value`() {
        val net = sampleNet
        Thread.sleep(100)

        net.updateEdge(1, 2, 1)

        val expected = mapOf(
            1 to mapOf(1 to 0, 2 to 1, 3 to 2),
            2 to mapOf(1 to 1, 2 to 0, 3 to 1),
            3 to mapOf(1 to 2, 2 to 1, 3 to 0)
        )

        net.checkNet(expected)
    }

    @Test
    fun `test update to higher value`() {
        val net = sampleNet
        Thread.sleep(100)

        net.updateEdge(1, 2, 60)

        val expected = mapOf(
            1 to mapOf(1 to 0, 2 to 51, 3 to 50),
            2 to mapOf(1 to 51, 2 to 0, 3 to 1),
            3 to mapOf(1 to 50, 2 to 1, 3 to 0)
        )

        net.checkNet(expected)
    }
}