/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.integration.jade;

import li.cil.oc2.common.block.ChargerBlock;
import li.cil.oc2.common.block.ComputerBlock;
import li.cil.oc2.common.block.ProjectorBlock;
import li.cil.oc2.common.entity.Robot;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import javax.annotation.Nullable;
import java.util.Objects;

@WailaPlugin
public final class OC2JadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(@Nullable final IWailaClientRegistration registration) {
        final IWailaClientRegistration actualRegistration = Objects.requireNonNull(registration);
        actualRegistration.registerBlockComponent(MachineBlockComponentProvider.INSTANCE, ComputerBlock.class);
        actualRegistration.registerBlockComponent(MachineBlockComponentProvider.INSTANCE, ChargerBlock.class);
        actualRegistration.registerBlockComponent(MachineBlockComponentProvider.INSTANCE, ProjectorBlock.class);
        actualRegistration.registerEntityComponent(MachineEntityComponentProvider.INSTANCE, Robot.class);
    }
}
