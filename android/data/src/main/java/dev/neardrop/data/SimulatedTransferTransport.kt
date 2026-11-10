package dev.neardrop.data

import dev.neardrop.core.DiscoveryProvenance
import dev.neardrop.core.TransferEvent
import dev.neardrop.core.TransferRequest
import dev.neardrop.core.TransferTransport
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Local UI-flow fixture only. It moves no bytes and cannot discover remote devices. */
class SimulatedTransferTransport : TransferTransport {
    override val kind = "simulation"
    override val provenance = DiscoveryProvenance.SIMULATED

    override fun send(request: TransferRequest): Flow<TransferEvent> = flow {
        emit(TransferEvent.Started)
        val total = request.items.sumOf { it.sizeBytes ?: 0L }
        emit(TransferEvent.Progress(0L, total))
        delay(30)
        emit(TransferEvent.Progress(total, total))
        emit(TransferEvent.Completed)
    }
}
