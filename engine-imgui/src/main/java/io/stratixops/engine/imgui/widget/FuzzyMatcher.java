package io.stratixops.engine.imgui.widget;

import java.util.Arrays;
import java.util.Locale;

/**
 * Case-insensitive fuzzy matcher for menu search (design system section 3.3).
 *
 * <p>A candidate matches when the query characters appear in order anywhere in
 * the candidate (a subsequence). The result carries the candidate indices of the
 * matched characters so the renderer can paint them in {@code accent.text}, plus
 * a score that rewards consecutive and word-boundary matches so the best result
 * floats to the top.
 *
 * <p>Pure logic with no ImGui dependency, so it is exercised directly by the
 * headless gate.
 */
public final class FuzzyMatcher {

    /** A successful match: score plus the candidate indices of matched chars. */
    public record Match(int score, int[] positions) {

        /** @return whether {@code index} in the candidate was matched. */
        public boolean at(int index) {
            for (int position : positions) {
                if (position == index) {
                    return true;
                }
            }
            return false;
        }
    }

    private FuzzyMatcher() {
    }

    /**
     * Scores {@code query} against {@code candidate}.
     *
     * @return the match, or {@code null} when the query is not a subsequence of
     *         the candidate. An empty query matches everything with score 0.
     */
    public static Match match(String query, String candidate) {
        if (query == null || query.isEmpty()) {
            return new Match(0, new int[0]);
        }
        if (candidate == null || candidate.isEmpty()) {
            return null;
        }
        String needle = query.toLowerCase(Locale.ROOT);
        String hay = candidate.toLowerCase(Locale.ROOT);

        int[] positions = new int[needle.length()];
        int found = 0;
        int score = 0;
        int streak = 0;
        int previous = -2;

        for (int i = 0; i < hay.length() && found < needle.length(); i++) {
            if (hay.charAt(i) != needle.charAt(found)) {
                streak = 0;
                continue;
            }
            positions[found] = i;
            score += 1;
            if (found > 0 && i == previous + 1) {
                streak++;
                score += streak;
            } else {
                streak = 0;
            }
            if (i == 0 || isBoundary(hay.charAt(i - 1), candidate.charAt(i))) {
                score += 3;
            }
            previous = i;
            found++;
        }

        if (found < needle.length()) {
            return null;
        }
        if (positions.length != found) {
            positions = Arrays.copyOf(positions, found);
        }
        return new Match(score, positions);
    }

    private static boolean isBoundary(char previous, char current) {
        if (previous == ' ' || previous == '_' || previous == '-' || previous == '/'
                || previous == '.') {
            return true;
        }
        return Character.isLowerCase(previous) && Character.isUpperCase(current);
    }
}
