package io.stratixops.engine.imgui.theme;

import java.util.Arrays;

/** Motion duration and easing tokens ({@code motion.*}). */
public final class MotionTokens {

    /** {@code motion.instant} — 0 ms; also used to disable all animation. */
    public final float MOTION_INSTANT;
    public final float MOTION_FAST;
    public final float MOTION_BASE;
    public final float MOTION_SLOW;

    /** Cubic-bezier control points for {@code easeOut} (x1,y1,x2,y2). */
    public final float[] EASING_OUT;
    /** Cubic-bezier control points for {@code easeInOut}. */
    public final float[] EASING_IN_OUT;

    MotionTokens(TokenDocument doc) {
        MOTION_INSTANT = doc.number("motion/motion.instant");
        MOTION_FAST = doc.number("motion/motion.fast");
        MOTION_BASE = doc.number("motion/motion.base");
        MOTION_SLOW = doc.number("motion/motion.slow");
        EASING_OUT = doc.numberArray("motion/easing.out");
        EASING_IN_OUT = doc.numberArray("motion/easing.inOut");
    }

    /**
     * Returns the token duration, collapsed to {@link #MOTION_INSTANT} when
     * reduced motion is requested (design system section 1.5).
     */
    public float duration(float millis, boolean reduceMotion) {
        return reduceMotion ? MOTION_INSTANT : millis;
    }

    /** @return every token path covered by this token group. */
    public static String[] paths() {
        return new String[] {
            "motion.instant", "motion.fast", "motion.base", "motion.slow",
            "easing.out", "easing.inOut"
        };
    }

    @Override
    public String toString() {
        return "MotionTokens[instant=" + MOTION_INSTANT + ", fast=" + MOTION_FAST
                + ", base=" + MOTION_BASE + ", slow=" + MOTION_SLOW
                + ", out=" + Arrays.toString(EASING_OUT) + "]";
    }
}
