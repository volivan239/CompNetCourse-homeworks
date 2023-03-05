import kotlin.random.Random

class Packet private constructor(val data: Long, val R: Long) {
    constructor(data: Long): this(data, calcRemainder(data, 0))

    fun validateCRC(): Boolean {
        return calcRemainder(data, R) == 0L
    }

    fun corrupt(p: Double): Pair<Packet, Boolean> {
        var newData = data
        var newR = R
        var anythingChanged = false

        for (i in 0 until MAX_SIZE * 8) {
            if (Random.nextDouble() < p) {
                newData = newData.xor(1L.shl(i))
                anythingChanged = true
            }
        }

        for (i in 0 until r) {
            if (Random.nextDouble() < p) {
                newR = newR.xor(1L.shl(i))
                anythingChanged = true
            }
        }

        return Packet(newData, newR) to anythingChanged
    }

    override fun toString(): String {
        return "DATA = ${data.toString(2).padStart(MAX_SIZE * 8, '0')}, R = ${R.toString(2).padStart(r, '0')}"
    }

    companion object {
        const val MAX_SIZE = 5
        private const val r = 10
        private const val G = 0xFEDCBAL.and(1L.shl(r) - 1) + 1L.shl(r)

        // Based on https://en.wikipedia.org/wiki/Cyclic_redundancy_check
        private fun calcRemainder(data: Long, pad: Long): Long {
            var x = data.shl(r) + pad
            while (x > 0) {
                val pos = x.takeHighestOneBit()
                if (pos < 1L.shl(r)) {
                    break
                }
                val shiftedG = G * pos.shr(r)
                x = x.xor(shiftedG)
            }
            return x
        }
    }
}