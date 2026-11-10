# Native core

The Rust workspace is split by stable responsibility: `neardrop-core` owns transfer/session state and domain types; `neardrop-protocol` owns bounded wire codecs; `neardrop-discovery` owns discovery-provider traits and expiry; `neardrop-transport` owns byte-stream/link interfaces and an explicitly simulated adapter for tests; `neardrop-security` owns size/path/consent/replay policy; `neardrop-ffi` exposes the small JNI surface.

No AirDrop codec, BLE packet, mDNS advertiser, TLS identity scheme or AWDL adapter is implemented yet. Public protocol work should add independent fixtures and versioned codecs only after documenting evidence and expected Apple OS version.

JNI is chosen over UniFFI at this stage to keep the Kotlin/Rust boundary narrow and avoid generating bindings for unstable domain models. Keep JNI conversion in `neardrop-ffi`; never allow panics across FFI. Native calls should return structured error codes and bounded payloads. Rust's safe core should not depend on Android APIs.
