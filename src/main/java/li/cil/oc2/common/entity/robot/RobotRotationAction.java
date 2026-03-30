/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.entity.robot;

import li.cil.oc2.common.entity.Robot;
import li.cil.oc2.common.util.NBTTagIds;
import li.cil.oc2.common.util.NBTUtils;
import li.cil.oc2.common.util.TickUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Objects;

public final class RobotRotationAction extends AbstractRobotAction {
    public static final float TARGET_EPSILON = 0.0001f;

    ///////////////////////////////////////////////////////////////////

    private static final float ROTATION_SPEED = 90f / TickUtils.toTicks(Duration.ofSeconds(1)); // degrees per tick

    private static final String DIRECTION_TAG_NAME = "direction";
    private static final String TARGET_TAG_NAME = "start";

    ///////////////////////////////////////////////////////////////////

    @Nullable private RotationDirection direction;
    @Nullable private Direction target;

    ///////////////////////////////////////////////////////////////////

    public RobotRotationAction(final RotationDirection direction) {
        super(RobotActions.ROTATION);
        this.direction = direction.resolve();
    }

    RobotRotationAction(final CompoundTag tag) {
        super(RobotActions.ROTATION, tag);
    }

    ///////////////////////////////////////////////////////////////////

    public static void rotateTowards(final Robot robot, final Direction targetRotation) {
        robot.setYRot(Mth.approachDegrees(robot.getYRot(), targetRotation.toYRot(), ROTATION_SPEED));
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public void initialize(final Robot robot) {
        if (target == null) {
            Direction currentTarget = robot.getDirection();
            if (direction != null) {
                switch (Objects.requireNonNull(direction.resolve())) {
                    case LEFT, left, l -> currentTarget = currentTarget.getCounterClockWise();
                    case RIGHT, right, r -> currentTarget = currentTarget.getClockWise();
                }
            }
            target = currentTarget;
        }

        robot.getEntityData().set(Robot.TARGET_DIRECTION, Objects.requireNonNull(target));
    }

    @Override
    public RobotActionResult perform(final Robot robot) {
        final Direction target = this.target;
        if (target == null) {
            throw new IllegalStateException();
        }

        rotateTowards(robot, target);

        if (Mth.degreesDifferenceAbs(robot.getYRot(), target.toYRot()) < TARGET_EPSILON) {
            return RobotActionResult.SUCCESS;
        }

        return RobotActionResult.INCOMPLETE;
    }

    @Override
    public CompoundTag serialize() {
        final CompoundTag tag = super.serialize();

        NBTUtils.putEnum(tag, DIRECTION_TAG_NAME, direction);
        if (target != null) {
            NBTUtils.putEnum(tag, TARGET_TAG_NAME, target);
        }

        return tag;
    }

    @Override
    public void deserialize(final CompoundTag tag) {
        super.deserialize(tag);

        final RotationDirection direction = NBTUtils.getEnum(tag, DIRECTION_TAG_NAME, RotationDirection.class);
        this.direction = (direction != null ? direction : RotationDirection.LEFT).resolve();
        if (tag.contains(TARGET_TAG_NAME, NBTTagIds.TAG_INT)) {
            target = NBTUtils.getEnum(tag, TARGET_TAG_NAME, Direction.class);
        }
    }
}
