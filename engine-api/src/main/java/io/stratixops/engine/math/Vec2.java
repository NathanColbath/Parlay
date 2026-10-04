package io.stratixops.engine.math;

/**
 * Mutable 2D vector used across the public API.
 *
 * <p>Ownership: instances returned by the engine (for example
 * {@code Input.axis(String)}) are <em>reused</em> per frame. Do not retain a
 * reference to them; copy with {@link #set(Vec2)} if you need to keep a value.
 *
 * <p>Units: pixels for positions, a plain direction/magnitude for axes. Angles
 * are radians everywhere in the engine.
 */
public final class Vec2 {

    /** X component. */
    public float x;

    /** Y component. */
    public float y;

    /** Creates a zero vector. */
    public Vec2() {
        this(0f, 0f);
    }

    /**
     * Creates a vector.
     *
     * @param x x component
     * @param y y component
     */
    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copies another vector into this one. Allocation-free.
     *
     * @param other source vector
     * @return this vector, for chaining
     */
    public Vec2 set(Vec2 other) {
        return set(other.x, other.y);
    }

    /**
     * Sets both components. Allocation-free.
     *
     * @param x x component
     * @param y y component
     * @return this vector, for chaining
     */
    public Vec2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Euclidean length.
     *
     * @return the length of this vector
     */
    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Squared length, avoiding a square root.
     *
     * @return x*x + y*y
     */
    public float lengthSquared() {
        return x * x + y * y;
    }

    /**
     * Normalizes this vector in place. A zero-length vector is left at zero.
     *
     * @return this vector, for chaining
     */
    public Vec2 normalize() {
        float len = length();
        if (len > 1e-6f) {
            x /= len;
            y /= len;
        }
        return this;
    }
}
