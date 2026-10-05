// Versioned JSON persistence module (STR-21).
//
// Owns all JSON and `.meta` handling: scenes, prefabs, settings, the asset
// database, GUID identity and the importer registry (ADR-0006). This is the
// only module allowed to depend on Gson; the dependency is `implementation`
// because no Gson type appears in the public surface.

plugins {
    `java-library`
}

dependencies {
    api(project(":engine-core"))
    implementation("com.google.code.gson:gson:2.11.0")
}
