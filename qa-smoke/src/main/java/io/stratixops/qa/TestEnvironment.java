package io.stratixops.qa;

/**
 * QA-side test environment helpers shared by engine test suites.
 *
 * <p>Engine modules can depend on the test classpath of this module (or on a
 * future {@code qa-support} module) to branch on runner capabilities.
 */
public final class TestEnvironment {

    /** System property that gates display/GPU-bound tests. */
    public static final String GPU_TESTS_PROPERTY = "stratix.qa.gpuTests";

    private TestEnvironment() {
    }

    /** @return true when the runner explicitly enabled GPU/display-bound tests. */
    public static boolean gpuTestsEnabled() {
        return Boolean.parseBoolean(System.getProperty(GPU_TESTS_PROPERTY, "false"));
    }

    /** @return true when the JVM was started in headless mode. */
    public static boolean isHeadless() {
        return Boolean.getBoolean("java.awt.headless");
    }
}
