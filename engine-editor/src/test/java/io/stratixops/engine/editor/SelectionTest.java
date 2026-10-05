package io.stratixops.engine.editor;

import io.stratixops.engine.scene.GameObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SelectionTest {

    @Test
    void selectReplacesAndSetsPrimary() {
        Selection selection = new Selection();
        GameObject a = new GameObject();
        GameObject b = new GameObject();

        selection.select(a);
        assertThat(selection.primary()).isEqualTo(a);
        selection.select(b);
        assertThat(selection.size()).isEqualTo(1);
        assertThat(selection.primary()).isEqualTo(b);
    }

    @Test
    void addAndSetKeepOrderAndLastPrimary() {
        Selection selection = new Selection();
        GameObject a = new GameObject();
        GameObject b = new GameObject();

        selection.set(List.of(a, b));
        assertThat(selection.objects()).containsExactly(a, b);
        assertThat(selection.primary()).isEqualTo(b);
    }

    @Test
    void toggleAddsThenRemoves() {
        Selection selection = new Selection();
        GameObject a = new GameObject();

        selection.toggle(a);
        assertThat(selection.contains(a)).isTrue();
        selection.toggle(a);
        assertThat(selection.isEmpty()).isTrue();
        assertThat(selection.primary()).isNull();
    }

    @Test
    void removingPrimaryFallsBack() {
        Selection selection = new Selection();
        GameObject a = new GameObject();
        GameObject b = new GameObject();
        selection.set(List.of(a, b));

        selection.remove(b);
        assertThat(selection.primary()).isEqualTo(a);
        assertThat(selection.size()).isEqualTo(1);
    }

    @Test
    void nullEntriesAreIgnored() {
        Selection selection = new Selection();
        selection.set(java.util.Arrays.asList(null, null));
        assertThat(selection.isEmpty()).isTrue();
        selection.add(null);
        assertThat(selection.isEmpty()).isTrue();
    }

    @Test
    void clearDropsEverything() {
        Selection selection = new Selection();
        selection.select(new GameObject());
        selection.clear();
        assertThat(selection.isEmpty()).isTrue();
        assertThat(selection.primary()).isNull();
    }
}
