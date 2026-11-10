# Security model

Treat every nearby packet and file as hostile. Proximity is not identity. Discovery is not consent. The Android Nearby Connections adapter is active and relies on the SDK's authenticated encrypted connection; it displays the authentication token on both devices and requires the user to confirm the matching code on both endpoints. This is not AirDrop TLS or Apple identity verification.

Future implementation requirements:

- TLS 1.2+ with current platform cryptography; verify identity according to a documented protocol profile. Do not accept self-signed identities silently or claim Apple identity validation without implementing it.
- Explicit consent for each incoming transfer; display sender, file count, names, sizes and requested destination. No auto-accept preference in the first release.
- Current Nearby offers are limited to 50 files, 2 GiB per file and 5 GiB total. Names are reduced to a filename leaf and control characters are replaced. The receiver bounds copied bytes and checks the declared length before publishing to Downloads.
- Bound total request bytes, nesting depth, collection count, filename length, archive entries, decompressed bytes and compression ratio. Reject duplicate/conflicting transfer IDs and stale/replayed state transitions.
- Normalize and validate filenames; reject absolute paths, traversal, separators/control characters and collisions. Extract archives into app-private staging using descriptor-safe APIs, then publish only after integrity and user acceptance.
- Stream SHA-256/integrity checks are still required before a production release; current code validates declared versus actual size and relies on Nearby's encrypted transport integrity. Avoid buffering unbounded files in memory.
- Keep network parsing in the unprivileged process. Any future AWDL radio daemon with network capabilities must have a narrow IPC protocol and must not parse attacker-controlled transfer payloads.
- Never use private Apple certificates, Apple ID credentials, or contact databases. Contacts Only support requires valid Apple identity proof; do not spoof it.
- Fuzz parsers and state machines with malformed/truncated/oversized inputs. Security reports about upstream protocols inform defensive tests only; do not ship exploit logic.

The WOOT 2026 study reports unauthenticated complex parser paths and denial-of-service defects in current proximity stacks. Accordingly, no codec should be exposed on a privileged listener until resource limits and malformed-input behavior are tested.
