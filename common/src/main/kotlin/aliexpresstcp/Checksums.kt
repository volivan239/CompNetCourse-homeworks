package aliexpresstcp

private const val MOD = 1.shl(16)

private val ByteArray.rawCheckSum: Int
    get() = foldIndexed(0) { ind, acc, byte ->
        val shift = 8 * (ind % 2)
        (acc + byte.toInt().shl(shift)) % MOD
    }

/**
 * @return checksum calculated by the algorithm of TCP checksums
 */
internal val ByteArray.checkSum: Int
    get() = MOD - 1 - rawCheckSum

internal fun ByteArray.validateCheckSum(expected: Int): Boolean {
    return (rawCheckSum + expected) % MOD == MOD - 1
}