// Hello Engine - onboarding template (STR-14).
//
// Two entry points are wired here so the getting-started guide works as written:
//
//   Windows (cmd/PowerShell)   macOS/Linux
//   -------------------------  -------------
//   gradlew.bat run            ./gradlew run         -> the "Hello Engine" window
//   gradlew.bat runSample      ./gradlew runSample   -> the full Gem Dash sample
//
// `run` is provided by the `application` plugin. `runSample` launches the sample
// game that ships with the engine.
//
// JDK: the engine targets Java 17 (see the monorepo `build.gradle.kts` toolchain
// and docs/GETTING_STARTED.md). The toolchain below lets Gradle use a local
// JDK 17 even when `java -version` reports something else.

plugins {
    java
    application
}

group = "io.stratixops"
version = providers.gradleProperty("stratixEngineVersion").get()

repositories {
    // The engine is not on Maven Central yet. If you built it locally with
    // `./gradlew publishToMavenLocal`, mavenLocal() picks it up.
    mavenLocal()
    mavenCentral()
}

// Version of the engine this template builds against. Keep in sync with the
// monorepo `VERSION` file and `gradle.properties`.
val engineVersion = providers.gradleProperty("stratixEngineVersion").get()

// Main class of the Gem Dash sample. Overridable so an API/layout change does
// not require editing this build file:
//   gradlew.bat runSample -Pstratix.sampleMainClass=com.example.MySample
val sampleMainClass = providers.gradleProperty("stratix.sampleMainClass")
    .getOrElse("io.stratixops.gemdash.Main")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

// The sample is a separate engine artifact. It only needs to be on the
// `runSample` classpath, never on the template's own compile classpath.
val sampleRuntime: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    // The gameplay-facing engine surface (Engine, EngineContext, Renderer, ...).
    // `engine-api` is the frozen public contract (STR-13); the runtime and
    // rendering implementations land behind it (STR-5/STR-6).
    implementation("io.stratixops:engine-api:$engineVersion")

    // Gem Dash sample, launched by `runSample`. Ships with the engine.
    sampleRuntime("io.stratixops:gem-dash-sample:$engineVersion")
}

application {
    mainClass.set("io.stratixops.hello.Main")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.named<JavaExec>("run") {
    description = "Runs the Hello Engine starter (this project)."
}

tasks.register<JavaExec>("runSample") {
    group = "application"
    description = "Runs the full Gem Dash sample game shipped with the engine."
    mainClass.set(sampleMainClass)
    classpath = sourceSets.main.get().runtimeClasspath + sampleRuntime
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}
