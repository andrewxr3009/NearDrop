package dev.neardrop.core

import android.net.Uri
import kotlinx.coroutines.flow.Flow

enum class DiscoveryProvenance { REAL, SIMULATED, UNAVAILABLE }
data class NearbyDevice(val id: String, val displayName: String, val transport: String, val provenance: DiscoveryProvenance)
data class SharedItem(val uri: Uri, val displayName: String, val sizeBytes: Long?)
data class PendingTransfer(val id: String, val sender: String, val names: List<String>, val totalBytes: Long)
data class TransferRequest(val recipientId: String, val items: List<SharedItem>)

sealed interface TransferEvent {
    data object Started : TransferEvent
    data class Progress(val completedBytes: Long, val totalBytes: Long) : TransferEvent
    data object Completed : TransferEvent
    data class Failed(val safeMessage: String) : TransferEvent
}

interface TransferTransport {
    val kind: String
    val provenance: DiscoveryProvenance
    fun send(request: TransferRequest): Flow<TransferEvent>
}
