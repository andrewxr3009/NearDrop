# Discovery and transports

Discovery says a peer may exist; transport carries bytes. Neither alone authenticates or authorizes a transfer.

| Adapter | Intended use | Current state |
|---|---|---|
| Google Nearby Connections | Real offline-capable NearDrop-to-NearDrop discovery, connection and file payload transfer | Implemented in Android data module; requires Google Play services, runtime radio permissions and user confirmation on both endpoints |
| AWDL | Native AirDrop interoperability through an actual AWDL link | Not implemented; specialized vendor/kernel/privileged capability only |
| Wi-Fi Direct | NearDrop peer transfers without shared AP | Interface reserved; no live adapter |
| LAN | NearDrop peer transfers on a shared network | Interface reserved; explicitly not AirDrop compatibility |
| Simulated | Deterministic UI/domain tests | Implemented in Rust and Kotlin; never presented as a real nearby peer |

The current Android adapter uses the public Google Play services Nearby Connections SDK (`play-services-nearby:19.3.0`, Apache-2.0 SDK) and `Strategy.P2P_CLUSTER`. The SDK selects Bluetooth/Wi-Fi transports. NearDrop's service ID separates its peers from unrelated Nearby apps. It reports only endpoints returned by the SDK; no synthetic peers are created. The app uses a 10-minute user-visible advertising window and starts discovery from the Send screen.

Connection authentication tokens are shown on both endpoints, and both sides must confirm the matching code. The receiver must then accept each offer before file payloads are sent. Android 10+ files are published into `Downloads/NearDrop` after completion and size validation. Current discovery is foreground-app scoped; a foreground service would be needed for a more durable background receiving promise.

This SDK is not the Quick Share protocol, Apple's BLE/AWDL discovery, or an AirDrop-compatible transport. Quick Share is offered separately through Android Sharesheet. mDNS/Bonjour and IPv6 link-local remain future adapters; neither provides AWDL by itself.

Every adapter reports capabilities and provenance (`real`, `simulated`, `unavailable`); UI must avoid labeling a simulation as a real device.
