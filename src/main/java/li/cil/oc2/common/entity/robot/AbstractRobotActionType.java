/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.entity.robot;

import li.cil.oc2.common.entity.Robot;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public abstract class AbstractRobotActionType {
    private final int id;

    ///////////////////////////////////////////////////////////////////

    protected AbstractRobotActionType(final int id) {
        this.id = id;
    }

    ///////////////////////////////////////////////////////////////////

    public int getId() {
        return id;
    }

    public void initializeData(final Robot robot) {
    }

    public void performServer(final Robot robot, @Nullable final AbstractRobotAction currentAction) {
    }

    public void performClient(final Robot robot) {
    }

    public abstract AbstractRobotAction deserialize(final CompoundTag tag);
}
