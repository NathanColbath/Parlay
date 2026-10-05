package io.stratixops.engine.math;

/**
 * Tiny allocation-free scalar helpers shared by engine and game code.
 *
 * <p>The engine deliberately ships only the handful of helpers the runtime and
 * sample need; collision and gameplay math stay sample-side (ADR-0001).
 */
public final class Mathf {

    private Mathf() {
    }

    /**
     * Clamps a value to the inclusive range {@code [min, max]}.
     *
     * @param value value to clamp
     * @param min   lower bound
     * @param max   upper bound
     * @return the clamped value
     * @throws IllegalArgumentException if {@code min > max}
     */
    public static float clamp(float value, float min, float max) {
        if (min > max) {
            throw new IllegalArgumentException("min " + min + " > max " + max);
        }
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    /**
     * Clamps an int to the inclusive range {@code [min, max]}.
     *
     * @param value value to clamp
     * @param min   lower bound
     * @param max   upper bound
     * @return the clamped value
     * @throws IllegalArgumentException if {@code min > max}
     */
    public static int clamp(int value, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min " + min + " > max " + max);
        }
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    /**
     * Linear interpolation from {@code a} to {@code b}. The factor is not
     * clamped, so extrapolation is possible.
     *
     * @param a start value
     * @param b end value
     * @param t interpolation factor (0 gives {@code a}, 1 gives {@code b})
     * @return the interpolated value
     */
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /**
     * Moves {@code current} toward {@code target} by at most {@code maxDelta},
     * never overshooting. Useful for frame-rate-independent easing.
     *
     * @param current  current value
     * @param target   desired value
     * @param maxDelta maximum step (must be >= 0)
     * @return the approached value
     * @throws IllegalArgumentException if {@code maxDelta < 0}
     */
    public static float approach(float current, float target, float maxDelta) {
        if (maxDelta < 0f) {
            throw new IllegalArgumentException("maxDelta must be >= 0 but was " + maxDelta);
        }
        float delta = target - current;
        if (delta > maxDelta) {
            return current + maxDelta;
        }
        if (delta < -maxDelta) {
            return current - maxDelta;
        }
        return target;
    }
}
