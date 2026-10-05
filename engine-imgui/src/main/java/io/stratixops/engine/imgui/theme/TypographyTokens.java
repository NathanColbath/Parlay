// GENERATED from docs/design/tokens.json by the STR-22 theme generator. Do not edit by hand.
package io.stratixops.engine.imgui.theme;

/** Typography tokens: `family.*` strings and `type.*` styles. */
public final class TypographyTokens {

    public final String FAMILY_UI;
    public final String FAMILY_MONO;

    public final TypeStyle TYPE_XS;
    public final TypeStyle TYPE_SM;
    public final TypeStyle TYPE_MD;
    public final TypeStyle TYPE_MD_MEDIUM;
    public final TypeStyle TYPE_LG;
    public final TypeStyle TYPE_XL;
    public final TypeStyle TYPE_2XL;
    public final TypeStyle TYPE_MONO;
    public final TypeStyle TYPE_MONO_SM;

    TypographyTokens(TokenDocument doc) {
        FAMILY_UI = doc.string("typography/family.ui");
        FAMILY_MONO = doc.string("typography/family.mono");
        TYPE_XS = new TypeStyle(doc.number("typography/type.xs/size"), doc.number("typography/type.xs/lineHeight"), (int) doc.number("typography/type.xs/weight"), doc.optionalNumber("typography/type.xs/tracking", 0f));
        TYPE_SM = new TypeStyle(doc.number("typography/type.sm/size"), doc.number("typography/type.sm/lineHeight"), (int) doc.number("typography/type.sm/weight"), doc.optionalNumber("typography/type.sm/tracking", 0f));
        TYPE_MD = new TypeStyle(doc.number("typography/type.md/size"), doc.number("typography/type.md/lineHeight"), (int) doc.number("typography/type.md/weight"), doc.optionalNumber("typography/type.md/tracking", 0f));
        TYPE_MD_MEDIUM = new TypeStyle(doc.number("typography/type.mdMedium/size"), doc.number("typography/type.mdMedium/lineHeight"), (int) doc.number("typography/type.mdMedium/weight"), doc.optionalNumber("typography/type.mdMedium/tracking", 0f));
        TYPE_LG = new TypeStyle(doc.number("typography/type.lg/size"), doc.number("typography/type.lg/lineHeight"), (int) doc.number("typography/type.lg/weight"), doc.optionalNumber("typography/type.lg/tracking", 0f));
        TYPE_XL = new TypeStyle(doc.number("typography/type.xl/size"), doc.number("typography/type.xl/lineHeight"), (int) doc.number("typography/type.xl/weight"), doc.optionalNumber("typography/type.xl/tracking", 0f));
        TYPE_2XL = new TypeStyle(doc.number("typography/type.2xl/size"), doc.number("typography/type.2xl/lineHeight"), (int) doc.number("typography/type.2xl/weight"), doc.optionalNumber("typography/type.2xl/tracking", 0f));
        TYPE_MONO = new TypeStyle(doc.number("typography/type.mono/size"), doc.number("typography/type.mono/lineHeight"), (int) doc.number("typography/type.mono/weight"), doc.optionalNumber("typography/type.mono/tracking", 0f));
        TYPE_MONO_SM = new TypeStyle(doc.number("typography/type.monoSm/size"), doc.number("typography/type.monoSm/lineHeight"), (int) doc.number("typography/type.monoSm/weight"), doc.optionalNumber("typography/type.monoSm/tracking", 0f));
    }

    /** @return every token path covered by this token group. */
    public static String[] paths() {
        return new String[] { "family.ui", "family.mono", "type.xs", "type.sm", "type.md", "type.mdMedium", "type.lg", "type.xl", "type.2xl", "type.mono", "type.monoSm" };
    }
}
