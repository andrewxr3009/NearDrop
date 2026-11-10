# ADR-003: Narrow JNI boundary

- **Status:** Accepted
- **Decision:** Use direct JNI for the first Kotlin/Rust boundary, isolated in `neardrop-ffi`.
- **Reason:** The public API is intentionally small and not yet stable; JNI avoids generating broad bindings or pulling in UniFFI. Rust internals remain independent of Android.
- **Consequence:** JNI conversion and error mapping need careful tests; never let panics cross the boundary. Revisit UniFFI if the API becomes broad or multi-platform clients emerge.
