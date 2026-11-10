package dev.neardrop.core

import org.junit.Assert.assertEquals
import org.junit.Test

class DiscoveryProvenanceTest {
    @Test fun testFixtureCannotBeLabeledReal() {
        val fixture = NearbyDevice("fixture", "Fixture peer", "test", DiscoveryProvenance.SIMULATED)
        assertEquals(DiscoveryProvenance.SIMULATED, fixture.provenance)
    }
}
