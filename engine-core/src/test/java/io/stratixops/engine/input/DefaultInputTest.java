package io.stratixops.engine.input;

import static org.assertj.core.api.Assertions.assertThat;

import io.stratixops.engine.math.Vec2;
import io.stratixops.engine.testutil.FakeInputDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Action and axis semantics frozen by ADR-0001 (F5, AC5.1-AC5.4). */
class DefaultInputTest {

    private FakeInputDevice device;
    private DefaultInput input;

    @BeforeEach
    void setUp() {
        device = new FakeInputDevice();
        input = new DefaultInput(device);
    }

    @Test
    void beginFramePollsTheDeviceOnce() {
        input.beginFrame();
        input.beginFrame();
        assertThat(device.pollCount()).isEqualTo(2);
    }

    @Test
    void duplicateAxisBindingsMergeAcrossMaps() {
        input.bind(ActionMap.def().axis("Move", Key.W, Key.A, Key.S, Key.D).build());
        input.bind(ActionMap.def().axis("Move", Key.UP, Key.LEFT, Key.DOWN, Key.RIGHT).build());

        device.hold(Key.UP);
        input.beginFrame();

        Vec2 axis = input.axis("Move");
        assertThat(axis.x).isEqualTo(0f);
        assertThat(axis.y).isEqualTo(-1f);
    }

    @Test
    void axisIsZeroAtRest() {
        input.bind(ActionMap.def().axis("Move", Key.W, Key.A, Key.S, Key.D).build());
        input.beginFrame();

        Vec2 axis = input.axis("Move");
        assertThat(axis.x).isZero();
        assertThat(axis.y).isZero();
    }

    @Test
    void diagonalIsNormalizedSoThereIsNoSpeedBoost() {
        input.bind(ActionMap.def().axis("Move", Key.W, Key.A, Key.S, Key.D).build());
        device.hold(Key.W);
        device.hold(Key.D);
        input.beginFrame();

        Vec2 axis = input.axis("Move");
        assertThat(axis.length()).isCloseTo(1f, org.assertj.core.data.Offset.offset(1e-5f));
        assertThat(axis.x).isCloseTo(0.7071f, org.assertj.core.data.Offset.offset(1e-3f));
        assertThat(axis.y).isCloseTo(-0.7071f, org.assertj.core.data.Offset.offset(1e-3f));
    }

    @Test
    void oppositeKeysCancel() {
        input.bind(ActionMap.def().axis("Move", Key.W, Key.S).build());
        device.hold(Key.W);
        device.hold(Key.S);
        input.beginFrame();

        assertThat(input.axis("Move").length()).isZero();
    }

    @Test
    void axisNormalizedForcesUnitLengthOrZero() {
        input.bind(ActionMap.def().axis("Move", Key.W, Key.A, Key.S, Key.D).build());
        device.hold(Key.A);
        input.beginFrame();

        Vec2 normalized = input.axisNormalized("Move");
        assertThat(normalized.length()).isCloseTo(1f, org.assertj.core.data.Offset.offset(1e-5f));
        assertThat(normalized.x).isEqualTo(-1f);
        assertThat(normalized.y).isZero();

        device.clear(Key.A);
        input.beginFrame();
        assertThat(input.axisNormalized("Move").length()).isZero();
    }

    @Test
    void axisReturnsAReusedInstance() {
        input.bind(ActionMap.def().axis("Move", Key.W).build());
        device.hold(Key.W);
        input.beginFrame();
        assertThat(input.axis("Move")).isSameAs(input.axis("Move"));
    }

    @Test
    void pressedIsAnEdgeAndDoesNotSurviveTheNextFrame() {
        input.bind(ActionMap.def().button("Jump", Key.SPACE).build());
        device.press(Key.SPACE);

        input.beginFrame();
        assertThat(input.pressed("Jump")).isTrue();

        input.beginFrame();
        assertThat(input.pressed("Jump")).isFalse();
    }

    @Test
    void aTapBetweenPollsIsNotLost() {
        input.bind(ActionMap.def().button("Jump", Key.SPACE).build());
        device.press(Key.SPACE);
        device.release(Key.SPACE);

        input.beginFrame();

        assertThat(input.pressed("Jump")).isTrue();
        assertThat(input.down("Jump")).isFalse();
    }

    @Test
    void downReflectsHeldState() {
        input.bind(ActionMap.def().button("Fire", Key.SPACE).build());
        device.hold(Key.SPACE);
        input.beginFrame();

        assertThat(input.down("Fire")).isTrue();
        assertThat(input.pressed("Fire")).isFalse();
    }

    @Test
    void unknownActionsAreInert() {
        input.beginFrame();
        assertThat(input.pressed("nope")).isFalse();
        assertThat(input.down("nope")).isFalse();
        assertThat(input.axis("nope").length()).isZero();
    }

    @Test
    void bindingANullMapIsIgnored() {
        input.bind(null);
        input.bind(ActionMap.def().button("Jump", Key.SPACE).build());
        device.press(Key.SPACE);
        input.beginFrame();

        assertThat(input.pressed("Jump")).isTrue();
    }

    @Test
    void holdingAKeyRepeatsDownButPressesOnlyOnce() {
        input.bind(ActionMap.def().button("Fire", Key.SPACE).build());
        device.press(Key.SPACE);

        input.beginFrame();
        assertThat(input.pressed("Fire")).isTrue();
        assertThat(input.down("Fire")).isTrue();

        for (int i = 0; i < 5; i++) {
            input.beginFrame();
            assertThat(input.pressed("Fire")).isFalse();
            assertThat(input.down("Fire")).isTrue();
        }
    }

    @Test
    void releasingAKeyClearsDown() {
        input.bind(ActionMap.def().button("Fire", Key.SPACE).build());
        device.hold(Key.SPACE);
        input.beginFrame();
        assertThat(input.down("Fire")).isTrue();

        device.release(Key.SPACE);
        input.beginFrame();

        assertThat(input.down("Fire")).isFalse();
        assertThat(input.pressed("Fire")).isFalse();
    }
}
