/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.integration.jade;

import li.cil.oc2.common.block.ChargerBlock;
import li.cil.oc2.common.block.ComputerBlock;
import li.cil.oc2.common.block.ProjectorBlock;
import li.cil.oc2.common.entity.Robot;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class OC2JadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(final IWailaClientRegistration registration) {
        registration.registerBlockComponent(MachineBlockComponentProvider.INSTANCE, ComputerBlock.class);
        registration.registerBlockComponent(MachineBlockComponentProvider.INSTANCE, ChargerBlock.class);
        registration.registerBlockComponent(MachineBlockComponentProvider.INSTANCE, ProjectorBlock.class);
        registration.registerEntityComponent(MachineEntityComponentProvider.INSTANCE, Robot.class);
    }
}
