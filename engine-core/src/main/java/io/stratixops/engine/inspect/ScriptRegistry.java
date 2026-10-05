package io.stratixops.engine.inspect;

import io.stratixops.engine.scene.Behaviour;
import io.stratixops.engine.scene.Component;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Discovers user {@link Behaviour} subclasses on the project classpath and
 * registers them with a {@link ComponentRegistry} (ADR-0007).
 *
 * <p>Phase 1 discovers scripts once at editor start; there is no hot reload.
 * Scanning is best-effort: classes that fail to load, abstract types, inner
 * classes and non-{@code public} types are skipped. A registered behaviour
 * missing its required no-arg constructor still fails fast, naming the class.
 */
public final class ScriptRegistry {

    private final ComponentRegistry registry;

    /**
     * Creates a scanner over the given registry.
     *
     * @param registry the registry to populate
     */
    public ScriptRegistry(ComponentRegistry registry) {
        this.registry = registry;
    }

    /**
     * Discovers behaviours compiled next to a known anchor class, under a
     * package prefix. Convenient for the editor's own classpath and for tests.
     *
     * @param anchor        a class whose code source is scanned
     * @param packagePrefix package to include, e.g. {@code io.stratixops.gemdash}
     * @return the number of newly registered behaviours
     */
    public int discoverFrom(Class<?> anchor, String packagePrefix) {
        try {
            Path root = Path.of(anchor.getProtectionDomain().getCodeSource().getLocation().toURI());
            return discover(root, anchor.getClassLoader(), packagePrefix);
        } catch (URISyntaxException | RuntimeException e) {
            return 0;
        }
    }

    /**
     * Scans a classpath directory for behaviours.
     *
     * @param root          classpath directory
     * @param loader        class loader used to resolve classes
     * @param packagePrefix package to include
     * @return the number of newly registered behaviours
     */
    public int discover(Path root, ClassLoader loader, String packagePrefix) {
        if (root == null || !Files.isDirectory(root)) {
            return 0;
        }
        List<String> names = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".class"))
                    .forEach(p -> {
                        String name = toClassName(root, p);
                        if (name != null && !name.contains("$") && name.startsWith(packagePrefix)) {
                            names.add(name);
                        }
                    });
        } catch (IOException e) {
            return 0;
        }

        int count = 0;
        for (String name : names) {
            try {
                Class<?> type = Class.forName(name, false, loader);
                if (isBehaviour(type) && registry.register(type.asSubclass(Component.class)) != null) {
                    count++;
                }
            } catch (ClassNotFoundException | LinkageError ignored) {
                // Best-effort discovery: an unloadable class is skipped, not fatal.
            }
        }
        return count;
    }

    private static boolean isBehaviour(Class<?> type) {
        return Behaviour.class.isAssignableFrom(type)
                && !type.isInterface()
                && !Modifier.isAbstract(type.getModifiers())
                && Modifier.isPublic(type.getModifiers());
    }

    private static String toClassName(Path root, Path classFile) {
        try {
            String relative = root.relativize(classFile).toString();
            String withoutExtension = relative.substring(0, relative.length() - ".class".length());
            return withoutExtension.replace('\\', '.').replace('/', '.');
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
