package io.stratixops.engine.imgui.widget;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Design system 3.3: fuzzy filtering for the searchable create menu. */
class FuzzyMatcherTest {

    @Test
    void matchesSubsequenceCaseInsensitively() {
        FuzzyMatcher.Match match = FuzzyMatcher.match("tr", "Transform");

        assertThat(match).isNotNull();
        assertThat(match.positions()).containsExactly(0, 1);
        assertThat(match.at(0)).isTrue();
        assertThat(match.at(1)).isTrue();
        assertThat(match.at(2)).isFalse();
        assertThat(match.score()).isPositive();
    }

    @Test
    void highlightsScatteredCharactersInOrder() {
        FuzzyMatcher.Match match = FuzzyMatcher.match("sr", "Sprite Renderer");

        assertThat(match).isNotNull();
        assertThat(match.positions()).containsExactly(0, 2);
    }

    @Test
    void returnsNullWhenQueryIsNotASubsequence() {
        assertThat(FuzzyMatcher.match("zz", "Transform")).isNull();
        assertThat(FuzzyMatcher.match("a", "")).isNull();
        assertThat(FuzzyMatcher.match("a", null)).isNull();
    }

    @Test
    void emptyQueryMatchesEverythingWithZeroScore() {
        FuzzyMatcher.Match match = FuzzyMatcher.match("", "Anything");

        assertThat(match).isNotNull();
        assertThat(match.score()).isZero();
        assertThat(match.positions()).isEmpty();
    }

    @Test
    void boundaryMatchesScoreHigherThanMidWordMatches() {
        int boundary = FuzzyMatcher.match("s", "Sprite").score();
        int midWord = FuzzyMatcher.match("s", "Aspect").score();

        assertThat(boundary).isGreaterThan(midWord);
    }
}
