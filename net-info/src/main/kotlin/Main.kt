import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket

fun main(args: Array<String>) {
    val localAddress = Inet4Address.getLocalHost()
    val maskPrefix = NetworkInterface.getByInetAddress(localAddress).interfaceAddresses[0].networkPrefixLength
    println("Local address = ${localAddress.hostAddress}")
    println("Subnet = /$maskPrefix")

    val parser = ArgParser("net-info")
    val askedHost by parser.argument(ArgType.String, "address", "address to analyze")
    val firstPort by parser.argument(ArgType.Int, "firstPort", "First port number of the range")
    val lastPort by parser.argument(ArgType.Int, "lastPort", "Last port number of the range")
    parser.parse(args)

    val availablePorts = (firstPort..lastPort).filter { currentPortNum ->
        runCatching {
            ServerSocket().use {
                it.reuseAddress = true
                it.bind(InetSocketAddress(askedHost, currentPortNum))
            }
        }.isSuccess
    }
    println("Ports in range available for $askedHost: $availablePorts")
}