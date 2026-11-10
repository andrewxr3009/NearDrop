# Data flow

```mermaid
sequenceDiagram
  participant Share as Android Sharesheet / SAF
  participant App as Compose UI
  participant Nearby as Nearby Connections SDK
  participant Chooser as Android Sharesheet
  participant QuickShare as Quick Share system service
  participant Peer as Remote peer
  Share->>App: content URI + read grant
  App->>Nearby: discover peers / request connection
  Nearby->>Peer: authenticated nearby connection
  App-->>App: both users compare and approve authentication code
  App->>Nearby: send file offer
  Peer-->>App: recipient accepts or rejects
  App->>Nearby: send file payloads
  Nearby-->>App: transfer progress / completion
  Note over App,Peer: Recipient approval is required before payload transfer
  App->>Chooser: ACTION_SEND + content URI + read grant
  Chooser->>QuickShare: user selects Quick Share, if offered
  QuickShare->>Peer: system-managed discovery, consent and transfer
```

The Android NearDrop-to-NearDrop path uses `NearbyTransferManager`; it does not yet use the Rust core. The Android Sharesheet path is separate: Android/Quick Share controls discovery, transfer consent, progress and destination handling; NearDrop only grants selected content URIs to the system chooser. For inbound shares from other apps, NearDrop receives the shared URI and previews metadata. Quick Share's own peer receive path remains owned by the system app and normally saves into system storage rather than handing the transfer to NearDrop. Neither flow is AirDrop interoperability.

Incoming Nearby payloads are copied into Downloads/NearDrop after explicit consent and size validation. A user-selected SAF destination, background service, checksum verification, and Rust-core integration remain future work.
