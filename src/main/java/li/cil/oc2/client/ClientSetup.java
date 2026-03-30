/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client;

import li.cil.oc2.api.API;
import li.cil.oc2.api.bus.device.DeviceType;
import li.cil.oc2.client.gui.*;
import li.cil.oc2.client.item.CustomItemColors;
import li.cil.oc2.client.item.CustomItemModelProperties;
import li.cil.oc2.client.model.BusCableModelLoader;
import li.cil.oc2.client.renderer.entity.RobotWithoutLevelRenderer;
import li.cil.oc2.client.renderer.BusInterfaceNameRenderer;
import li.cil.oc2.client.renderer.ProjectorDepthRenderer;
import li.cil.oc2.client.renderer.blockentity.*;
import li.cil.oc2.client.renderer.color.BusCableBlockColor;
import li.cil.oc2.client.renderer.entity.RobotRenderer;
import li.cil.oc2.client.renderer.entity.model.RobotModel;
import li.cil.oc2.common.block.Blocks;
import li.cil.oc2.common.blockentity.BlockEntities;
import li.cil.oc2.common.bus.device.DeviceTypes;
import li.cil.oc2.common.container.Containers;
import li.cil.oc2.common.entity.Entities;
import li.cil.oc2.common.item.Items;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = API.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void handleSetupEvent(final FMLClientSetupEvent event) {
        BusInterfaceNameRenderer.initialize();

        event.enqueueWork(() -> {
            CustomItemModelProperties.initialize();
            NeoForge.EVENT_BUS.register(ProjectorDepthRenderer.class);
        });
    }

    @SubscribeEvent
    public static void handleRegisterBlockColors(final RegisterColorHandlersEvent.Block event) {
        event.register(new BusCableBlockColor(), Blocks.BUS_CABLE.get());
    }

    @SubscribeEvent
    public static void handleRegisterItemColors(final RegisterColorHandlersEvent.Item event) {
        CustomItemColors.register(event);
    }

    @SubscribeEvent
    public static void handleRegisterGeometryLoaders(final ModelEvent.RegisterGeometryLoaders event) {
        event.register(Blocks.BUS_CABLE.getId(), new BusCableModelLoader());
    }

    @SubscribeEvent
    public static void handleRegisterMenuScreens(final RegisterMenuScreensEvent event) {
        event.register(Containers.COMPUTER.get(), ComputerContainerScreen::new);
        event.register(Containers.COMPUTER_TERMINAL.get(), ComputerTerminalScreen::new);
        event.register(Containers.ROBOT.get(), RobotContainerScreen::new);
        event.register(Containers.ROBOT_TERMINAL.get(), RobotTerminalScreen::new);
        event.register(Containers.NETWORK_TUNNEL.get(), NetworkTunnelScreen::new);
    }

    @SubscribeEvent
    public static void handleRegisterClientExtensions(final RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new RobotWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
            }
        }, Items.ROBOT.get());
    }

    @SubscribeEvent
    public static void handleEntityRendererRegisterEvent(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntities.COMPUTER.get(), ComputerRenderer::new);
        event.registerBlockEntityRenderer(BlockEntities.DISK_DRIVE.get(), DiskDriveRenderer::new);
        event.registerBlockEntityRenderer(BlockEntities.CHARGER.get(), ChargerRenderer::new);
        event.registerBlockEntityRenderer(BlockEntities.NETWORK_CONNECTOR.get(), NetworkConnectorRenderer::new);
        event.registerBlockEntityRenderer(BlockEntities.PROJECTOR.get(), ProjectorRenderer::new);
        event.registerEntityRenderer(Entities.ROBOT.get(), RobotRenderer::new);
    }

    @SubscribeEvent
    public static void handleRegisterLayerDefinitionsEvent(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RobotModel.ROBOT_MODEL_LAYER, RobotModel::createRobotLayer);
    }
}
