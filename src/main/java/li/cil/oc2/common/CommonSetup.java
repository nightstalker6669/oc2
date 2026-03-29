/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common;

import li.cil.oc2.common.bus.device.rpc.RPCMethodParameterTypeAdapters;
import li.cil.oc2.common.network.Network;
import li.cil.oc2.common.util.ServerScheduler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public final class CommonSetup {
    @SubscribeEvent
    public static void handleSetupEvent(final FMLCommonSetupEvent event) {
        Network.initialize();
        RPCMethodParameterTypeAdapters.initialize();
        ServerScheduler.initialize();
    }
}
