/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.entity.robot;

import li.cil.oc2.common.entity.Entities;
import li.cil.oc2.common.entity.Robot;
import li.cil.oc2.common.util.NBTTagIds;
import li.cil.oc2.common.util.NBTUtils;
import li.cil.oc2.common.util.TickUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Objects;

public final class RobotMovementAction extends AbstractRobotAction {
    public static final double TARGET_EPSILON = 0.0001;

    ///////////////////////////////////////////////////////////////////

    private static final float MOVEMENT_SPEED = 1f / TickUtils.toTicks(Duration.ofSeconds(1)); // blocks per tick

    private static final String DIRECTION_TAG_NAME = "direction";
    private static final String ORIGIN_TAG_NAME = "origin";
    private static final String START_TAG_NAME = "start";
    private static final String TARGET_TAG_NAME = "target";

    ///////////////////////////////////////////////////////////////////

    @Nullable private MovementDirection direction;
    @Nullable private BlockPos origin;
    @Nullable private BlockPos start;
    @Nullable private BlockPos target;
    @Nullable private Vec3 targetPos;

    ///////////////////////////////////////////////////////////////////

    public RobotMovementAction(final MovementDirection direction) {
        super(RobotActions.MOVEMENT);
        this.direction = direction.resolve();
    }

    RobotMovementAction(final CompoundTag tag) {
        super(RobotActions.MOVEMENT, tag);
    }

    ///////////////////////////////////////////////////////////////////

    public static Vec3 getTargetPositionInBlock(final BlockPos position) {
        return Vec3.atBottomCenterOf(position).add(0, 0.5f * (1 - Entities.ROBOT.get().getHeight()), 0);
    }

    public static void moveTowards(final Robot robot, final Vec3 targetPosition) {
        Vec3 delta = targetPosition.subtract(robot.position());
        if (delta.lengthSqr() > MOVEMENT_SPEED * MOVEMENT_SPEED) {
            delta = delta.normalize().scale(MOVEMENT_SPEED);
        }

        robot.move(MoverType.SELF, delta);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public void initialize(final Robot robot) {
        if (origin == null || start == null || target == null) {
            origin = robot.blockPosition();
            start = origin;
            target = start;
            final MovementDirection direction = this.direction;
            if (direction != null) {
                BlockPos currentTarget = Objects.requireNonNull(target);
                switch (direction) {
                    case UPWARD, upward, up, u -> currentTarget = currentTarget.relative(Direction.UP);
                    case DOWNWARD, downward, down, d -> currentTarget = currentTarget.relative(Direction.DOWN);
                    case FORWARD, forward, ahead, f -> currentTarget = currentTarget.relative(robot.getDirection());
                    case BACKWARD, backward, back, b -> currentTarget = currentTarget.relative(robot.getDirection().getOpposite());
                }
                target = currentTarget;
            }
        }

        final BlockPos currentTarget = Objects.requireNonNull(target);
        targetPos = getTargetPositionInBlock(currentTarget);
        robot.getEntityData().set(Robot.TARGET_POSITION, currentTarget);
    }

    @Override
    public RobotActionResult perform(final Robot robot) {
        final Vec3 targetPos = this.targetPos;
        if (targetPos == null) {
            throw new IllegalStateException();
        }

        validateTarget(robot);

        moveAndResolveCollisions(robot);

        if (robot.position().distanceToSqr(targetPos) < TARGET_EPSILON) {
            if (Objects.equals(target, origin)) {
                return RobotActionResult.FAILURE; // Collided and returned.
            } else {
                return RobotActionResult.SUCCESS; // Made it to new location.
            }
        }

        return RobotActionResult.INCOMPLETE;
    }

    @Override
    public CompoundTag serialize() {
        final CompoundTag tag = super.serialize();

        NBTUtils.putEnum(tag, DIRECTION_TAG_NAME, direction);
        if (origin != null) {
            tag.put(ORIGIN_TAG_NAME, NbtUtils.writeBlockPos(origin));
        }
        if (start != null) {
            tag.put(START_TAG_NAME, NbtUtils.writeBlockPos(start));
        }
        if (target != null) {
            tag.put(TARGET_TAG_NAME, NbtUtils.writeBlockPos(target));
        }

        return tag;
    }

    @Override
    public void deserialize(final CompoundTag tag) {
        super.deserialize(tag);

        final MovementDirection direction = NBTUtils.getEnum(tag, DIRECTION_TAG_NAME, MovementDirection.class);
        this.direction = (direction != null ? direction : MovementDirection.FORWARD).resolve();
        if (tag.contains(ORIGIN_TAG_NAME, NBTTagIds.TAG_COMPOUND)) {
            origin = NbtUtils.readBlockPos(tag, ORIGIN_TAG_NAME).orElse(null);
        }
        if (tag.contains(START_TAG_NAME, NBTTagIds.TAG_COMPOUND)) {
            start = NbtUtils.readBlockPos(tag, START_TAG_NAME).orElse(null);
        }
        if (tag.contains(TARGET_TAG_NAME, NBTTagIds.TAG_COMPOUND)) {
            target = NbtUtils.readBlockPos(tag, TARGET_TAG_NAME).orElse(null);
            if (target != null) {
                targetPos = getTargetPositionInBlock(target);
            }
        }
    }

    private void moveAndResolveCollisions(final Robot robot) {
        if (start == null || target == null || targetPos == null) {
            return;
        }

        final BlockPos currentStart = Objects.requireNonNull(start);
        final BlockPos currentTarget = Objects.requireNonNull(target);
        final Vec3 currentTargetPos = Objects.requireNonNull(targetPos);

        moveTowards(robot, currentTargetPos);

        final boolean didCollide = robot.horizontalCollision || robot.verticalCollision;
        final long gameTime = robot.level().getGameTime();
        if (didCollide && !robot.level().isClientSide()
            && robot.getLastPistonMovement() < gameTime - 1) {
            start = currentTarget;
            target = currentStart;
            targetPos = getTargetPositionInBlock(currentStart);
            robot.getEntityData().set(Robot.TARGET_POSITION, currentStart);
        }
    }

    private void validateTarget(final Robot robot) {
        final BlockPos currentPosition = robot.blockPosition();
        if (start == null || Objects.equals(currentPosition, start) ||
            target == null || Objects.equals(currentPosition, target)) {
            return;
        }

        final BlockPos currentStart = Objects.requireNonNull(start);
        final BlockPos currentTarget = Objects.requireNonNull(target);

        // Got pushed out of our original path. Adjust start and target by the least offset.
        final BlockPos fromStart = currentPosition.subtract(currentStart);
        final BlockPos fromTarget = currentPosition.subtract(currentTarget);

        final int deltaStart = fromStart.getX() + fromStart.getY() + fromStart.getZ();
        final int deltaTarget = fromTarget.getX() + fromTarget.getY() + fromTarget.getZ();

        if (deltaStart < deltaTarget) {
            start = currentPosition;
            target = currentTarget.offset(fromStart);
        } else {
            start = currentStart.offset(fromTarget);
            target = currentPosition;
        }

        final BlockPos updatedTarget = Objects.requireNonNull(target);
        targetPos = getTargetPositionInBlock(updatedTarget);
        robot.getEntityData().set(Robot.TARGET_POSITION, updatedTarget);
    }
}
