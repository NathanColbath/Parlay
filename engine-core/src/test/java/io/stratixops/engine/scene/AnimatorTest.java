package io.stratixops.engine.scene;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnimatorTest {

    private Animator animatorWithFrames(int count, float fps) {
        Animator animator = new Animator();
        animator.setFramesPerSecond(fps);
        for (int i = 0; i < count; i++) {
            animator.frames().add(new AssetRef(Guid.random()));
        }
        return animator;
    }

    @Test
    void stepsFramesAndLoops() {
        Animator animator = animatorWithFrames(2, 2f);
        assertThat(animator.currentFrame()).isZero();
        animator.update(0.6f);
        assertThat(animator.currentFrame()).isEqualTo(1);
        animator.update(0.6f);
        assertThat(animator.currentFrame()).isZero();
    }

    @Test
    void nonLoopingStopsAtLastFrame() {
        Animator animator = animatorWithFrames(2, 2f);
        animator.setLoop(false);
        animator.update(0.6f);
        animator.update(0.6f);
        assertThat(animator.currentFrame()).isEqualTo(1);
        assertThat(animator.playing()).isFalse();
    }

    @Test
    void emptyAndSingleFrameAnimatorsDoNotAdvance() {
        Animator empty = new Animator();
        assertThat(empty.currentFrameAsset()).isNull();
        empty.update(1f);
        assertThat(empty.currentFrame()).isZero();

        Animator single = animatorWithFrames(1, 10f);
        single.update(1f);
        assertThat(single.currentFrame()).isZero();
        assertThat(single.currentFrameAsset()).isNotNull();
    }

    @Test
    void playAndClampControls() {
        Animator animator = animatorWithFrames(3, 10f);
        animator.setPlaying(false);
        animator.update(1f);
        assertThat(animator.currentFrame()).isZero();

        animator.setCurrentFrame(99);
        assertThat(animator.currentFrame()).isEqualTo(2);
        animator.setCurrentFrame(-5);
        assertThat(animator.currentFrame()).isZero();

        animator.frames().clear();
        animator.setCurrentFrame(2);
        assertThat(animator.currentFrame()).isZero();
        assertThat(animator.resolvedFrames()).isEmpty();
    }

    @Test
    void rejectsNonPositiveFrameRate() {
        assertThatThrownBy(() -> new Animator().setFramesPerSecond(0f))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
