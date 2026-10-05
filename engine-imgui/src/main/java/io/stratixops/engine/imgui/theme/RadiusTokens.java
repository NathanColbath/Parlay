// GENERATED from docs/design/tokens.json by the STR-22 theme generator. Do not edit by hand.
package io.stratixops.engine.imgui.theme;

/** Corner radius tokens (`radius.*`). */
public final class RadiusTokens {

    public final float RADIUS_XS;
    public final float RADIUS_SM;
    public final float RADIUS_MD;
    public final float RADIUS_LG;
    public final float RADIUS_PILL;

    RadiusTokens(TokenDocument doc) {
        RADIUS_XS = doc.number("radius/radius.xs");
        RADIUS_SM = doc.number("radius/radius.sm");
        RADIUS_MD = doc.number("radius/radius.md");
        RADIUS_LG = doc.number("radius/radius.lg");
        RADIUS_PILL = doc.number("radius/radius.pill");
    }

    /** @return every token path covered by this token group. */
    public static String[] paths() {
        return new String[] { "radius.xs", "radius.sm", "radius.md", "radius.lg", "radius.pill" };
    }
}
