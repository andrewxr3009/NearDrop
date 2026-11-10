# ADR-002: Capability-based transport abstraction

- **Status:** Accepted
- **Decision:** Discovery and transports are separate traits with explicit capability/provenance reporting. AWDL, Wi-Fi Direct and LAN are distinct adapters; simulation is test-only.
- **Reason:** Prevent a LAN implementation or fabricated peer from being described as AirDrop. Hardware-specific AWDL must not contaminate UI/domain logic.
- **Consequence:** Production AirDrop waits on a verified AWDL provider and protocol implementation. Tests can cover domain flows with simulated byte streams.
