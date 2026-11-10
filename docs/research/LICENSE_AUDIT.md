# License audit

Audit date: 2026-11-09. NearDrop includes no third-party implementation source at this stage.

## Project license

The authored starter files in this repository are offered under the root MIT license. Verify contributor provenance before distribution and add notices for future dependencies.

## Sources reviewed

| Project/source | Observed license/status | Decision |
|---|---|---|
| OpenDrop | GPL-3.0; latest GitHub release shown as 2021 | No code copied or linked. GPL obligations are incompatible with treating it as an unexamined dependency of this MIT starter. |
| OWL | GPL-3.0; repository archived in October 2026 | Research reference only. No code copied. |
| OWLy | MIT in current workspace Cargo manifest and LICENSE; Rust 1.88+ | No code copied. Any future reuse still requires dependency/provenance and notice review. |
| AirDrop-for-Everything | Repository declares MIT and includes NOTICE/provenance credits | Potential candidate for a separately reviewed dependency; nothing vendored or imported. Audit transitive dependencies and NOTICE before adoption. |
| Tarish | Project site labels the work Apache-2.0; source links for app/daemon/AWDL currently return 404 in this research session, so component-level files could not be audited | No reuse until source access, component licenses, vendor binary terms and dependency notices are verified. |
| Bada | No license declaration visible on the repository page checked | Treat as all-rights-reserved; no code copied. |
| Mosey-Bada | No license declaration visible on repository page checked; repository archived October 2026 | Treat source as all-rights-reserved. Do not copy, modify or distribute. |
| Apple specifications/assets | Security guide is documentation; no Apple assets, marks, certificates or private identity material included | Use only public factual descriptions; NearDrop branding and artwork remain original. |

## Dependency policy

Before adding any implementation dependency, record its exact version/revision, source, SPDX license, transitive licenses, notices, static/dynamic linking model, distribution obligations and whether it contains copied protocol implementation. Do not incorporate GPL code into an MIT-distributed artifact without a deliberate licensing decision. Public protocol research is not a blanket permission to copy source text or code.

Current Android declarations pin Android Gradle Plugin 9.2.1, built-in Kotlin/Compose compiler plugin 2.2.10, Compose BOM 2024.12.01, coroutines 1.9.0 and JUnit 4.13.2. Rust FFI declares the `jni` 0.21.x series. The `kotlinx.coroutines` project is Apache-2.0, JUnit 4 is EPL-1.0, and `jni` is permissively licensed; export resolved dependency metadata and audit all transitive notices after successful dependency resolution. No lockfiles were generated in this environment.
