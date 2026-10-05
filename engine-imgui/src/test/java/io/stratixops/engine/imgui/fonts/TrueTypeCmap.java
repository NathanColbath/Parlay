package io.stratixops.engine.imgui.fonts;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Minimal TrueType {@code cmap} reader used by {@link FontResourcesTest} to
 * prove that every codepoint the UI asks for is present in the shipped fonts.
 *
 * <p>Read-only and dependency-free: parses the table directory, picks the best
 * Unicode subtable (format 12 over format 4) and answers membership queries.
 * This is test-only support code, not engine runtime.
 */
final class TrueTypeCmap {

    private final ByteBuffer buf;
    private final int subtableOffset;
    private final int format;

    private TrueTypeCmap(ByteBuffer buf, int subtableOffset, int format) {
        this.buf = buf;
        this.subtableOffset = subtableOffset;
        this.format = format;
    }

    /** @throws IllegalArgumentException when {@code data} is not a usable TrueType face. */
    static TrueTypeCmap parse(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        if (buf.capacity() < 12) {
            throw new IllegalArgumentException("font too small: " + buf.capacity() + " bytes");
        }
        int numTables = u16(buf, 4);
        int cmapOffset = -1;
        for (int i = 0; i < numTables; i++) {
            int record = 12 + i * 16;
            if (tag(data, record).equals("cmap")) {
                cmapOffset = i32(buf, record + 8);
                break;
            }
        }
        if (cmapOffset < 0) {
            throw new IllegalArgumentException("no cmap table");
        }

        int numSubtables = u16(buf, cmapOffset + 2);
        int format12 = -1;
        int format4 = -1;
        for (int i = 0; i < numSubtables; i++) {
            int record = cmapOffset + 4 + i * 8;
            int platform = u16(buf, record);
            int encoding = u16(buf, record + 2);
            int subtable = cmapOffset + i32(buf, record + 4);
            if (subtable <= 0 || subtable + 2 > buf.capacity()) {
                continue;
            }
            int subFormat = u16(buf, subtable);
            boolean unicode = platform == 0 || (platform == 3 && (encoding == 1 || encoding == 10));
            if (subFormat == 12 && unicode) {
                format12 = subtable;
            } else if (subFormat == 4 && unicode && format4 < 0) {
                format4 = subtable;
            }
        }
        if (format12 >= 0) {
            return new TrueTypeCmap(buf, format12, 12);
        }
        if (format4 >= 0) {
            return new TrueTypeCmap(buf, format4, 4);
        }
        throw new IllegalArgumentException("no Unicode cmap subtable");
    }

    /** @return whether {@code codepoint} maps to a real (non-zero) glyph. */
    boolean contains(int codepoint) {
        return format == 12 ? contains12(codepoint) : contains4(codepoint);
    }

    private boolean contains12(int cp) {
        int groups = i32(buf, subtableOffset + 12);
        for (int g = 0; g < groups; g++) {
            int group = subtableOffset + 16 + g * 12;
            long start = u32(buf, group);
            long end = u32(buf, group + 4);
            if (cp >= start && cp <= end) {
                return true;
            }
            if (cp < start) {
                return false;
            }
        }
        return false;
    }

    private boolean contains4(int cp) {
        if (cp > 0xFFFF) {
            return false;
        }
        int segCount = u16(buf, subtableOffset + 6) / 2;
        int endCodes = subtableOffset + 14;
        int startCodes = endCodes + segCount * 2 + 2;
        int idDeltas = startCodes + segCount * 2;
        int idRangeOffsets = idDeltas + segCount * 2;
        for (int s = 0; s < segCount; s++) {
            int end = u16(buf, endCodes + s * 2);
            int start = u16(buf, startCodes + s * 2);
            if (cp < start || cp > end) {
                continue;
            }
            int delta = (short) u16(buf, idDeltas + s * 2);
            int rangeOffset = u16(buf, idRangeOffsets + s * 2);
            if (rangeOffset == 0) {
                return ((cp + delta) & 0xFFFF) != 0;
            }
            int glyphIndexAddress = idRangeOffsets + s * 2 + rangeOffset + (cp - start) * 2;
            if (glyphIndexAddress + 2 > buf.capacity()) {
                return false;
            }
            int glyph = u16(buf, glyphIndexAddress);
            return glyph != 0 && (((glyph + delta) & 0xFFFF) != 0);
        }
        return false;
    }

    private static String tag(byte[] data, int offset) {
        return new String(data, offset, 4, StandardCharsets.US_ASCII);
    }

    private static int u16(ByteBuffer buf, int offset) {
        return buf.getShort(offset) & 0xFFFF;
    }

    private static int i32(ByteBuffer buf, int offset) {
        return buf.getInt(offset);
    }

    private static long u32(ByteBuffer buf, int offset) {
        return buf.getInt(offset) & 0xFFFFFFFFL;
    }
}
