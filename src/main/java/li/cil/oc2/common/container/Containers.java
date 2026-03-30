/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.container;

import li.cil.oc2.common.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Containers {
    private static final DeferredRegister<MenuType<?>> CONTAINERS = RegistryUtils.getInitializerFor(Registries.MENU);

    ///////////////////////////////////////////////////////////////////

    public static final DeferredHolder<MenuType<?>, MenuType<ComputerInventoryContainer>> COMPUTER = CONTAINERS.register("computer", () -> IMenuTypeExtension.create(ComputerInventoryContainer::createClient));
    public static final DeferredHolder<MenuType<?>, MenuType<ComputerTerminalContainer>> COMPUTER_TERMINAL = CONTAINERS.register("computer_terminal", () -> IMenuTypeExtension.create(ComputerTerminalContainer::createClient));
    public static final DeferredHolder<MenuType<?>, MenuType<RobotInventoryContainer>> ROBOT = CONTAINERS.register("robot", () -> IMenuTypeExtension.create(RobotInventoryContainer::createClient));
    public static final DeferredHolder<MenuType<?>, MenuType<RobotTerminalContainer>> ROBOT_TERMINAL = CONTAINERS.register("robot_terminal", () -> IMenuTypeExtension.create(RobotTerminalContainer::createClient));
    public static final DeferredHolder<MenuType<?>, MenuType<NetworkTunnelContainer>> NETWORK_TUNNEL = CONTAINERS.register("network_tunnel", () -> IMenuTypeExtension.create(NetworkTunnelContainer::createClient));

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
    }
}
