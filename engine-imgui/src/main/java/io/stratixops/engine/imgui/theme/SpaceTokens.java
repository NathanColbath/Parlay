// GENERATED from docs/design/tokens.json by the STR-22 theme generator. Do not edit by hand.
package io.stratixops.engine.imgui.theme;

/** 4 px spacing scale tokens (`space.0`..`space.8`). */
public final class SpaceTokens {

    public final float SPACE_0;
    public final float SPACE_1;
    public final float SPACE_2;
    public final float SPACE_3;
    public final float SPACE_4;
    public final float SPACE_5;
    public final float SPACE_6;
    public final float SPACE_7;
    public final float SPACE_8;

    SpaceTokens(TokenDocument doc) {
        SPACE_0 = doc.number("space/space.0");
        SPACE_1 = doc.number("space/space.1");
        SPACE_2 = doc.number("space/space.2");
        SPACE_3 = doc.number("space/space.3");
        SPACE_4 = doc.number("space/space.4");
        SPACE_5 = doc.number("space/space.5");
        SPACE_6 = doc.number("space/space.6");
        SPACE_7 = doc.number("space/space.7");
        SPACE_8 = doc.number("space/space.8");
    }

    /** @return every token path covered by this token group. */
    public static String[] paths() {
        return new String[] { "space.0", "space.1", "space.2", "space.3", "space.4", "space.5", "space.6", "space.7", "space.8" };
    }
}
