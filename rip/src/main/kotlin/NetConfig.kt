import kotlinx.serialization.Serializable

@Serializable
data class NetConfig(val router2Connections: Map<IP, List<IP>>)