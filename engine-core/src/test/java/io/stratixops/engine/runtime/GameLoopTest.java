package io.stratixops.engine.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Fixed-timestep timing contract (ADR-0003, AC2.1/AC2.2). */
class GameLoopTest {

    private static final class RecordingStep implements GameLoop.Step {
        private int updates;
        private int renders;
        private float lastDt = -1f;
        private float lastAlpha = -1f;

        @Override
        public void update(float dt) {
            updates++;
            lastDt = dt;
        }

        @Override
        public void render(float alpha) {
            renders++;
            lastAlpha = alpha;
        }
    }

    @Test
    void oneFrameAtSixtyHertzRunsExactlyOneFixedUpdate() {
        GameLoop loop = new GameLoop();
        RecordingStep step = new RecordingStep();

        loop.frame(1f / 60f, step);

        assertThat(step.updates).isEqualTo(1);
        assertThat(step.renders).isEqualTo(1);
        assertThat(step.lastDt).isEqualTo(GameLoop.FIXED_DT);
        assertThat(step.lastAlpha).isBetween(0f, 1f);
        assertThat(step.lastAlpha).isLessThan(1f);
    }

    @Test
    void sixtyFramesAdvanceSixtyFixedSteps() {
        GameLoop loop = new GameLoop();
        RecordingStep step = new RecordingStep();

        for (int i = 0; i < 60; i++) {
            loop.frame(1f / 60f, step);
        }

        assertThat(step.updates).isEqualTo(60);
        assertThat(step.renders).isEqualTo(60);
    }

    @Test
    void longFrameIsClampedToTheSpiralOfDeathGuard() {
        GameLoop loop = new GameLoop();
        RecordingStep step = new RecordingStep();

        loop.frame(2.0f, step);

        // A 0.25 s clamp at a 1/60 s step is exactly 15 updates.
        assertThat(step.updates).isEqualTo(15);
        assertThat(step.renders).isEqualTo(1);
    }

    @Test
    void partialFrameRendersWithInterpolationAndNoUpdate() {
        GameLoop loop = new GameLoop();
        RecordingStep step = new RecordingStep();

        loop.frame(0.01f, step);

        assertThat(step.updates).isZero();
        assertThat(step.renders).isEqualTo(1);
        assertThat(step.lastAlpha).isBetween(0f, 1f);
        assertThat(loop.alpha()).isEqualTo(step.lastAlpha);
    }

    @Test
    void negativeFrameTimeIsTreatedAsZero() {
        GameLoop loop = new GameLoop();
        RecordingStep step = new RecordingStep();

        loop.frame(-5f, step);

        assertThat(step.updates).isZero();
        assertThat(step.renders).isEqualTo(1);
        assertThat(loop.accumulator()).isZero();
    }

    @Test
    void resetDropsAccumulatedTime() {
        GameLoop loop = new GameLoop();
        RecordingStep step = new RecordingStep();

        loop.frame(0.01f, step);
        assertThat(loop.accumulator()).isGreaterThan(0f);

        loop.reset();

        assertThat(loop.accumulator()).isZero();
        assertThat(loop.alpha()).isZero();
    }

    @Test
    void aStallRecoversOnTheFollowingFrames() {
        GameLoop loop = new GameLoop();
        RecordingStep step = new RecordingStep();

        loop.frame(2.0f, step);
        int afterStall = step.updates;

        loop.frame(1f / 60f, step);

        assertThat(step.updates).isEqualTo(afterStall + 1);
    }
}
