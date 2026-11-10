package dev.neardrop.data

import dev.neardrop.core.DiscoveryProvenance
import dev.neardrop.core.SharedItem
import dev.neardrop.core.TransferEvent
import dev.neardrop.core.TransferRequest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SimulatedTransferTransportTest {
    @Test fun emitsLocalProgressAndMarksItsProvenance() = runBlocking {
        val adapter = SimulatedTransferTransport()
        val request = TransferRequest("test-fixture", emptyList())
        val events = adapter.send(request).toList()
        assertEquals(DiscoveryProvenance.SIMULATED, adapter.provenance)
        assertEquals(TransferEvent.Started, events.first())
        assertEquals(TransferEvent.Progress(0, 0), events[2])
        assertEquals(TransferEvent.Completed, events.last())
    }
}
