package io.stratixops.engine.inspect;

import io.stratixops.engine.testutil.SampleBehaviour;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ScriptRegistryTest {

    @Test
    void discoversBehavioursNextToTheAnchor() {
        ComponentRegistry registry = ComponentRegistry.withBuiltins();
        ScriptRegistry scripts = new ScriptRegistry(registry);
        int count = scripts.discoverFrom(SampleBehaviour.class, "io.stratixops.engine.testutil");
        assertThat(count).isGreaterThanOrEqualTo(1);
        assertThat(registry.contains(ComponentRegistry.defaultTypeId(SampleBehaviour.class))).isTrue();
    }

    @Test
    void missingRootReturnsZero() {
        ScriptRegistry scripts = new ScriptRegistry(ComponentRegistry.withBuiltins());
        assertThat(scripts.discover(Path.of("does", "not", "exist"), getClass().getClassLoader(),
                "io.stratixops")).isZero();
        assertThat(scripts.discover(null, getClass().getClassLoader(), "io.stratixops")).isZero();
    }
}
