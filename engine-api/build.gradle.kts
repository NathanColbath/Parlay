// Public API contract module (STR-13).
//
// This module freezes the engine's developer-facing surface before the core
// runtime and rendering implementations land. It contains interfaces, tiny
// value types, and the compiling excerpts used by
// `docs/design/02-api-ergonomics.md` (sections 2 and 3).
//
// It is an API/fixture module: it has no runtime behavior worth covering, so
// the MVP line-coverage gate is disabled here. Implementations live in the
// core/render modules and are covered by their own tests.

// The root build applies the `java` and `jacoco` plugins to every subproject.

tasks.named("jacocoTestCoverageVerification") {
    enabled = false
}
