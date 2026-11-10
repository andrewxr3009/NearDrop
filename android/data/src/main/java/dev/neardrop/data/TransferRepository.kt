package dev.neardrop.data

import dev.neardrop.core.DiscoveryProvenance
import dev.neardrop.core.NearbyDevice

/** Development-only local state. Never returns fake nearby peers. */
class TransferRepository {
    val nearbyDevices: List<NearbyDevice> = emptyList()
    val discoveryStatus: String = "Nenhum transporte de descoberta ativo"
    val provenance: DiscoveryProvenance = DiscoveryProvenance.UNAVAILABLE
}
