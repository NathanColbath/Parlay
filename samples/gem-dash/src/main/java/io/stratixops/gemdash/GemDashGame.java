package io.stratixops.gemdash;

import io.stratixops.engine.Assets;
import io.stratixops.engine.Camera2D;
import io.stratixops.engine.EngineContext;
import io.stratixops.engine.GameLifecycle;
import io.stratixops.engine.Renderer;
import io.stratixops.engine.TextureAtlas;
import io.stratixops.engine.audio.Audio;
import io.stratixops.engine.input.ActionMap;
import io.stratixops.engine.input.Input;
import io.stratixops.engine.input.Key;
import io.stratixops.engine.math.Vec2;

/**
 * The Gem Dash shape from {@code docs/design/02-api-ergonomics.md} section 3.
 *
 * <p>Compiling excerpt: this is the corrected, buildable version of the sketch.
 * It shows the contract fixes - {@code Audio} stored from {@code ctx},
 * {@code paused} declared, {@code nextLevel()} defined, {@code alpha} forwarded
 * to render, {@code ctx.viewport()} consumed by {@code Camera2D},
 * {@code axis("Move")} for analog-aware movement, and duplicate {@code "Move"}
 * registrations merging.
 */
public final class GemDashGame implements GameLifecycle {

    private final int levelIndex;
    private Audio audio;          // stored from ctx; no global engine handle
    private Camera2D camera;
    private Input input;
    private Level level;
    private Player player;
    private Hud hud;
    private boolean paused;       // declared

    /**
     * Creates a game bound to a level index.
     *
     * @param levelIndex zero-based level index
     */
    public GemDashGame(int levelIndex) {
        this.levelIndex = levelIndex;
    }

    @Override
    public void onCreate(EngineContext ctx) {
        Assets assets = ctx.assets();
        audio = ctx.audio();

        input = ctx.input().bind(ActionMap.def()
            .axis("Move", Key.W, Key.A, Key.S, Key.D)
            .axis("Move", Key.UP, Key.LEFT, Key.DOWN, Key.RIGHT) // merges; 8 keys drive "Move"
            .button("Confirm", Key.SPACE, Key.ENTER)
            .button("Pause", Key.ESCAPE, Key.P)
            .button("Mute", Key.M)
            .build());

        camera = new Camera2D(ctx.viewport()); // logical viewport, e.g. 640x360
        TextureAtlas atlas = assets.atlas("atlas.json");
        level = LevelLoader.load("levels/level" + (levelIndex + 1) + ".lvl", atlas);
        player = new Player(atlas, level.spawn());
        hud = new Hud(atlas, assets.font("font.pixel"));
        audio.music("music/level" + (levelIndex + 1) + ".ogg", 0.8f).loop();
    }

    @Override
    public void onUpdate(float dt) {
        if (input.pressed("Pause")) {
            paused = !paused;
        }
        if (paused) {
            return;
        }

        Vec2 move = input.axis("Move"); // analog-aware; digital diagonals normalized
        player.update(dt, move, level, input);

        level.update(dt, player);       // gems, enemies, portal
        camera.follow(player.position(), dt);
        if (level.portalActive() && level.playerAtPortal(player)) {
            nextLevel();
        }

        if (input.pressed("Mute")) {
            audio.toggleMute();
        }
    }

    @Override
    public void onRender(Renderer r, float alpha) {
        r.clear(Palette.BG);
        r.pushCamera(camera);
        level.render(r, alpha);
        player.render(r, alpha); // sprite + animation inside
        r.popCamera();
        hud.render(r, level.timeLeft(), player.hearts(), level.score());
    }

    /** Sample-owned scene transition. */
    private void nextLevel() {
        // Unload the current level and create the next one.
    }
}
