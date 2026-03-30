/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.bus;

import li.cil.oc2.api.API;
import li.cil.oc2.api.bus.DeviceBusElement;
import li.cil.oc2.api.bus.device.Device;
import li.cil.oc2.api.bus.device.ItemDevice;
import li.cil.oc2.api.bus.device.object.Callback;
import li.cil.oc2.api.bus.device.object.ObjectDevice;
import li.cil.oc2.api.bus.device.provider.BlockDeviceProvider;
import li.cil.oc2.api.bus.device.provider.BlockDeviceQuery;
import li.cil.oc2.api.bus.device.provider.ItemDeviceProvider;
import li.cil.oc2.api.bus.device.provider.ItemDeviceQuery;
import li.cil.oc2.api.util.Invalidatable;
import li.cil.oc2.common.Constants;
import li.cil.oc2.common.bus.device.provider.Providers;
import li.cil.oc2.common.capabilities.Capabilities;
import li.cil.oc2.common.capabilities.CapabilityProvider;
import li.cil.oc2.common.capabilities.CapabilityRef;
import li.cil.oc2.common.registry.RegistryView;
import li.cil.oc2.common.util.LazyValue;
import li.cil.sedna.api.device.serial.SerialDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class BlockDeviceBusControllerTests {
    public static final ResourceLocation TEST_PROVIDER_REGISTRY_NAME = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "test");

    private FakeLevel fakeLevel;
    private LevelAccessor level;

    ///////////////////////////////////////////////////////////////////

    @BeforeEach
    public void setupEach() {
        final RegistryView<BlockDeviceProvider> blockDeviceProviderRegistry = createBlockDeviceProviderRegistry();
        final RegistryView<ItemDeviceProvider> itemDeviceProviderRegistry = createItemDeviceProviderRegistry();
        Providers.setBlockDeviceProviderRegistryOverride(() -> blockDeviceProviderRegistry);
        Providers.setItemDeviceProviderRegistryOverride(() -> itemDeviceProviderRegistry);

        fakeLevel = new FakeLevel();
        level = fakeLevel.getLevel();
    }

    @AfterEach
    public void teardownEach() {
        Providers.setBlockDeviceProviderRegistryOverride(null);
        Providers.setItemDeviceProviderRegistryOverride(null);
    }

    @Test
    public void busTouchingUnloadedChunkStaysIncomplete() {
        final BlockPos posAtChunkEdge = new BlockPos(0, 0, 0);
        final BlockDeviceBusController busController = new TestBusControllerBlockEntity(posAtChunkEdge).getBusController();

        busController.scan();

        assertEquals(CommonDeviceBusController.BusState.INCOMPLETE, busController.getState());
    }

    @Test
    public void busNotTouchingUnloadedChunkCompletes() {
        final BlockPos posInsideChunk = new BlockPos(8, 0, 8);
        final BlockDeviceBusController busController = new TestBusControllerBlockEntity(posInsideChunk).getBusController();

        busController.scan();

        assertEquals(CommonDeviceBusController.BusState.READY, busController.getState());
    }

    @Test
    public void busControllerIgnoresNonAccessibleBusElements() {
        final BlockPos controllerPos = new BlockPos(8, 0, 8);
        final TestBusControllerBlockEntity busController = new TestBusControllerBlockEntity(controllerPos);

        final BlockPos elementPos = controllerPos.east();
        final TestBusElementBlockEntity busElement = new TestBusElementBlockEntity(elementPos);
        busElement.setSideEnabled(Direction.WEST, false);

        busController.getBusController().scan();

        assertEquals(CommonDeviceBusController.BusState.READY, busController.getBusController().getState());

        assertFalse(busElement.getBusElement().getControllers().contains(busController.getBusController()));
        assertFalse(busController.getBusController().getElements().contains(busElement.getBusElement()));
    }

    @Test
    public void busControllerDetectsBusElements() {
        final BlockPos controllerPos = new BlockPos(8, 0, 8);
        final BlockDeviceBusController busController = new TestBusControllerBlockEntity(controllerPos).getBusController();

        final BlockPos elementPos = controllerPos.east();
        final TestBusElementBlockEntity busElementInfo = new TestBusElementBlockEntity(elementPos);
        final DeviceBusElement busElement = busElementInfo.getBusElement();

        busController.scan();

        assertTrue(busElement.getControllers().contains(busController));
        assertTrue(busController.getElements().contains(busElement));
    }

    @Test
    public void devicesInUnloadedChunksAreMarkedAsUnloaded() {
        final BlockPos elementPos = new BlockPos(0, 0, 0);
        final TestBusElementBlockEntity busElementInfo = new TestBusElementBlockEntity(elementPos);
        final TestBlockDeviceBusElement busElement = busElementInfo.getBusElement();

        busElement.updateDevicesForNeighbor(Direction.WEST);
        verify(busElement, atLeastOnce()).setEntriesForGroupUnloaded(Direction.WEST.get3DDataValue());
    }

    @Test
    public void unloadedDeviceIsRemovedFromElement() {
        final BlockPos elementPos = new BlockPos(0, 0, 8);
        final TestBusElementBlockEntity busElementInfo = new TestBusElementBlockEntity(elementPos);

        final BlockPos devicePos = elementPos.west();
        final TestDeviceBlockEntity deviceBlockEntity = new TestDeviceBlockEntity(devicePos);

        busElementInfo.getBusElement().updateDevicesForNeighbor(Direction.WEST);
        assertTrue(busElementInfo.getBusElement().getDevices().contains(deviceBlockEntity.getObjectDevice()));

        fakeLevel.setChunkLoaded(new ChunkPos(devicePos), false);

        busElementInfo.getBusElement().updateDevicesForNeighbor(Direction.WEST);
        assertFalse(busElementInfo.getBusElement().getDevices().contains(deviceBlockEntity.getObjectDevice()));
    }

    @Test
    public void devicesInLoadedChunksAreCollected() {
        final BlockPos elementPos = new BlockPos(0, 0, 0);
        final TestBusElementBlockEntity busElementInfo = new TestBusElementBlockEntity(elementPos);
        final TestBlockDeviceBusElement busElement = busElementInfo.getBusElement();

        final BlockPos devicePos = elementPos.east();
        final TestDeviceBlockEntity deviceBlockEntity = new TestDeviceBlockEntity(devicePos);

        busElement.updateDevicesForNeighbor(Direction.EAST);
        verify(busElement, atLeastOnce()).setEntriesForGroup(eq(Direction.EAST.get3DDataValue()), any());
        assertTrue(busElement.getDevices().contains(deviceBlockEntity.getObjectDevice()));
    }

    @Test
    public void equalDevicesAreIgnored() {
        final BlockPos elementPos = new BlockPos(0, 0, 0);
        final TestBusElementBlockEntity busElementInfo = new TestBusElementBlockEntity(elementPos);
        final TestBlockDeviceBusElement busElement = busElementInfo.getBusElement();

        final BlockPos devicePos = elementPos.east();
        final TestDeviceBlockEntity deviceBlockEntity = new TestDeviceBlockEntity(devicePos);

        busElement.updateDevicesForNeighbor(Direction.EAST);

        assertTrue(busElement.getDevices().contains(deviceBlockEntity.getObjectDevice()));

        final ObjectDevice equalDevice = new ObjectDevice(deviceBlockEntity.getTestDevice());
        deviceBlockEntity.setObjectDevice(equalDevice);

        busElement.updateDevicesForNeighbor(Direction.EAST);

        assertTrue(busElement.getDevices().contains(deviceBlockEntity.getObjectDevice()));
        assertNotSame(busElement.getDevices().stream().findFirst().orElseThrow(), equalDevice);
    }

    @Test
    public void busControllerDetectsDevices() {
        final BlockPos controllerPos = new BlockPos(8, 0, 8);
        final BlockDeviceBusController busController = new TestBusControllerBlockEntity(controllerPos).getBusController();

        final BlockPos elementPos = controllerPos.east();
        final TestBusElementBlockEntity busElementInfo = new TestBusElementBlockEntity(elementPos);

        final BlockPos devicePos = elementPos.east();
        final TestDeviceBlockEntity deviceBlockEntity = new TestDeviceBlockEntity(devicePos);

        busElementInfo.getBusElement().updateDevicesForNeighbor(Direction.EAST);
        busController.scan();

        assertTrue(busController.getDevices().contains(deviceBlockEntity.getObjectDevice()));
    }

    @Test
    public void devicesGetSerializedWhenUnloadedAndDeserializedWhenLoaded() {
        final BlockPos controllerPos = new BlockPos(1, 0, 8);
        final BlockDeviceBusController busController = new TestBusControllerBlockEntity(controllerPos).getBusController();

        final BlockPos elementPos = controllerPos.west();
        final TestBusElementBlockEntity busElementInfo = new TestBusElementBlockEntity(elementPos);

        final BlockPos devicePos = elementPos.west();
        final TestDeviceBlockEntity deviceBlockEntity = new TestDeviceBlockEntity(devicePos);

        fakeLevel.setChunkLoaded(new ChunkPos(devicePos), false);
        busController.scheduleBusScan();

        final RPCDeviceBusAdapter rpcDeviceBusAdapter = new RPCDeviceBusAdapter(mock(SerialDevice.class));
        busController.onBeforeDeviceScan.add(rpcDeviceBusAdapter::pause);
        busController.onAfterDeviceScan.add(event -> rpcDeviceBusAdapter.resume(busController, event.didDevicesChange()));

        busController.scan();

        // Reminder: missing chunk -> bus scan cannot complete.
        assertEquals(CommonDeviceBusController.BusState.INCOMPLETE, busController.getState());

        final ObjectDevice objectDevice = spy(deviceBlockEntity.getObjectDevice());
        deviceBlockEntity.setObjectDevice(objectDevice);

        // Initialize with unloaded chunk.
        busElementInfo.getBusElement().updateDevicesForNeighbor(Direction.WEST);

        assertFalse(busController.getDevices().contains(objectDevice));

        verify(objectDevice, never()).mount();
        verify(objectDevice, never()).unmount();
        verify(objectDevice, never()).dispose();
        verify(objectDevice, never()).serializeNBT();
        verify(objectDevice, never()).deserializeNBT(any());

        // Load device chunk.
        fakeLevel.setChunkLoaded(new ChunkPos(devicePos), true);
        busController.scheduleBusScan();
        busElementInfo.getBusElement().updateDevicesForNeighbor(Direction.WEST);

        busController.scan();
        assertEquals(CommonDeviceBusController.BusState.READY, busController.getState());

        assertTrue(busController.getDevices().contains(objectDevice));

        rpcDeviceBusAdapter.mountDevices();

        verify(objectDevice, times(1)).mount();
        verify(objectDevice, never()).unmount();
        verify(objectDevice, never()).dispose();
        verify(objectDevice, never()).serializeNBT();
        verify(objectDevice, never()).deserializeNBT(any()); // no state to deserialize

        // Unload device chunk.
        fakeLevel.setChunkLoaded(new ChunkPos(devicePos), false);
        busController.scheduleBusScan();
        busElementInfo.getBusElement().updateDevicesForNeighbor(Direction.WEST);

        busController.scan();
        assertEquals(CommonDeviceBusController.BusState.INCOMPLETE, busController.getState());

        assertFalse(busController.getDevices().contains(objectDevice));

        verify(objectDevice, times(1)).mount();
        verify(objectDevice, times(1)).unmount();
        verify(objectDevice, never()).dispose();
        verify(objectDevice, times(1)).serializeNBT();
        verify(objectDevice, never()).deserializeNBT(any());
    }

    // Different load states and how removals effect state. Adds are uninteresting,
    // because we need a fully loaded state before anything happens here.

    // Loaded: [ ] Controller, [ ] Element, [ ] Device
    //  -> No interaction possible.

    // Loaded: [ ] Controller, [ ] Element, [x] Device
    //  -> Removing Device:
    //      -> Provider#dispose() when Element is loaded.

    @Test
    public void providerDisposeIsCalledWhenDeviceIsRemovedWhileElementIsUnloaded() {
        final BlockPos elementPos = new BlockPos(0, 0, 8);
        TestBusElementBlockEntity busElementInfo = new TestBusElementBlockEntity(elementPos);

        final BlockPos devicePos = elementPos.west();
        final TestDeviceBlockEntity deviceBlockEntity = new TestDeviceBlockEntity(devicePos);

        final ObjectDevice objectDevice = spy(deviceBlockEntity.getObjectDevice());
        deviceBlockEntity.setObjectDevice(objectDevice);

        busElementInfo.getBusElement().updateDevicesForNeighbor(Direction.WEST);

        assertTrue(busElementInfo.getBusElement().getDevices().contains(deviceBlockEntity.getObjectDevice()));
        verify(objectDevice, never()).mount();
        verify(objectDevice, never()).unmount();
        verify(objectDevice, never()).dispose();
        verify(objectDevice, never()).serializeNBT();
        verify(objectDevice, never()).deserializeNBT(any());

        final CompoundTag data = busElementInfo.getBusElement().save();
        verify(objectDevice, times(1)).serializeNBT();

        fakeLevel.setChunkLoaded(new ChunkPos(elementPos), false);
        fakeLevel.removeBlockEntity(elementPos);

        fakeLevel.removeBlockEntity(devicePos);

        fakeLevel.setChunkLoaded(new ChunkPos(elementPos), true);
        busElementInfo = new TestBusElementBlockEntity(elementPos);
        busElementInfo.getBusElement().load(data);

        busElementInfo.getBusElement().updateDevicesForNeighbor(Direction.WEST);

        final BlockDeviceProvider provider = Providers.blockDeviceProviderRegistry().getValue(TEST_PROVIDER_REGISTRY_NAME);
        verify(provider, times(1)).unmount(any(), any());
    }

    // Loaded: [ ] Controller, [x] Element, [ ] Device
    //  -> Removing Element:
    //      -> Provider#dispose()

    @Test
    public void providerDisposeIsCalledWhenElementIsRemovedWhileDeviceIsUnloaded() {
        final BlockPos elementPos = new BlockPos(0, 0, 8);
        final TestBusElementBlockEntity busElementInfo = new TestBusElementBlockEntity(elementPos);

        final BlockPos devicePos = elementPos.west();
        final TestDeviceBlockEntity deviceBlockEntity = new TestDeviceBlockEntity(devicePos);

        final ObjectDevice objectDevice = spy(deviceBlockEntity.getObjectDevice());
        deviceBlockEntity.setObjectDevice(objectDevice);

        busElementInfo.getBusElement().updateDevicesForNeighbor(Direction.WEST);

        assertTrue(busElementInfo.getBusElement().getDevices().contains(deviceBlockEntity.getObjectDevice()));
        verify(objectDevice, never()).mount();
        verify(objectDevice, never()).unmount();
        verify(objectDevice, never()).dispose();
        verify(objectDevice, never()).serializeNBT();
        verify(objectDevice, never()).deserializeNBT(any());

        fakeLevel.setChunkLoaded(new ChunkPos(devicePos), false);
        busElementInfo.getBusElement().updateDevicesForNeighbor(Direction.WEST);
        assertFalse(busElementInfo.getBusElement().getDevices().contains(deviceBlockEntity.getObjectDevice()));
        verify(objectDevice, never()).mount();
        verify(objectDevice, never()).unmount();
        verify(objectDevice, never()).dispose();
        verify(objectDevice, times(1)).serializeNBT();
        verify(objectDevice, never()).deserializeNBT(any());

        fakeLevel.removeBlockEntity(elementPos);
        busElementInfo.getBusElement().setRemoved();

        final BlockDeviceProvider provider = Providers.blockDeviceProviderRegistry().getValue(TEST_PROVIDER_REGISTRY_NAME);
        verify(provider, times(1)).unmount(any(), any());
    }

    // Loaded: [ ] Controller, [x] Element, [x] Device
    //  -> Removing Element:
    //      -> Device#dispose()

    @Test
    public void deviceIsDisposedWhenElementIsRemoved() {
        final BlockPos elementPos = new BlockPos(8, 0, 8);
        final TestBusElementBlockEntity busElementInfo = new TestBusElementBlockEntity(elementPos);

        final BlockPos devicePos = elementPos.west();
        final TestDeviceBlockEntity deviceBlockEntity = new TestDeviceBlockEntity(devicePos);

        final ObjectDevice objectDevice = spy(deviceBlockEntity.getObjectDevice());
        deviceBlockEntity.setObjectDevice(objectDevice);

        busElementInfo.getBusElement().updateDevicesForNeighbor(Direction.WEST);

        assertTrue(busElementInfo.getBusElement().getDevices().contains(deviceBlockEntity.getObjectDevice()));
        verify(objectDevice, never()).mount();
        verify(objectDevice, never()).unmount();
        verify(objectDevice, never()).dispose();
        verify(objectDevice, never()).serializeNBT();
        verify(objectDevice, never()).deserializeNBT(any());

        fakeLevel.removeBlockEntity(elementPos);
        busElementInfo.getBusElement().setRemoved();

        verify(objectDevice, times(1)).dispose();

        final BlockDeviceProvider provider = Providers.blockDeviceProviderRegistry().getValue(TEST_PROVIDER_REGISTRY_NAME);
        verify(provider, never()).unmount(any(), any());
    }

    //  -> Removing Device:
    //      -> Device#dispose()

    @Test
    public void deviceIsDisposedWhenDeviceIsRemoved() {
        final BlockPos elementPos = new BlockPos(8, 0, 8);
        final TestBusElementBlockEntity busElementInfo = new TestBusElementBlockEntity(elementPos);

        final BlockPos devicePos = elementPos.west();
        final TestDeviceBlockEntity deviceBlockEntity = new TestDeviceBlockEntity(devicePos);

        final ObjectDevice objectDevice = spy(deviceBlockEntity.getObjectDevice());
        deviceBlockEntity.setObjectDevice(objectDevice);

        busElementInfo.getBusElement().updateDevicesForNeighbor(Direction.WEST);

        assertTrue(busElementInfo.getBusElement().getDevices().contains(deviceBlockEntity.getObjectDevice()));
        verify(objectDevice, never()).mount();
        verify(objectDevice, never()).unmount();
        verify(objectDevice, never()).dispose();
        verify(objectDevice, never()).serializeNBT();
        verify(objectDevice, never()).deserializeNBT(any());

        fakeLevel.removeBlockEntity(devicePos);
        busElementInfo.getBusElement().updateDevicesForNeighbor(Direction.WEST);

        verify(objectDevice, times(1)).dispose();

        final BlockDeviceProvider provider = Providers.blockDeviceProviderRegistry().getValue(TEST_PROVIDER_REGISTRY_NAME);
        verify(provider, never()).unmount(any(), any());
    }

    // Loaded: [x] Controller, [ ] Element, [ ] Device
    //  -> Removing Controller:
    //      -> Edge-case: suspended Devices will *not* be disposed. If a new controller
    //         picks them up, they will resume under the assumption that they're managed
    //         by the same controller as before their previous unmount.

    // Loaded: [x] Controller, [ ] Element, [x] Device
    //  -> Removing Controller:
    //      -> Edge-case: suspended Devices will *not* be disposed. If a new controller
    //         picks them up, they will resume under the assumption that they're managed
    //         by the same controller as before their previous unmount.
    //  -> Removing Device:
    //      -> Provider#dispose() when Element is loaded.

    // Same as providerDisposeIsCalledWhenDeviceIsRemovedWhileElementIsUnloaded()

    // Loaded: [x] Controller, [x] Element, [ ] Device
    //  -> Removing Controller:
    //      -> Edge-case: suspended Devices will *not* be disposed. If a new controller
    //         picks them up, they will resume under the assumption that they're managed
    //         by the same controller as before their previous unmount.
    //  -> Removing Element:
    //      -> Provider#dispose()

    // Same as providerDisposeIsCalledWhenElementIsRemovedWhileDeviceIsUnloaded()

    // Loaded: [x] Controller, [x] Element, [x] Device
    //  -> Removing Controller:
    //      -> Stop VM if running.
    //  -> Stopping VM:
    //      -> Device#unmount(), Device#dispose()

    // Handled in Computer/Robot, too much pain to try to mock this.

    //  -> Removing Element:
    //      -> Device#unmount() if mounted, Device#dispose()
    //  -> Removing Device:
    //      -> Device#unmount() if mounted, Device#dispose()
    //  -> Unloading Controller, Element or Device:
    //      -> Device#unmount() if mounted.

    // TODO

    // Last case (all loaded) is the only case where the bus can be complete, so
    // also the only case where Devices can possibly be mounted.

    // In all but the last case (all loaded) it makes no difference if there's more
    // loaded/unloaded elements in the chain. However, in the last case it does:
    //  -> Removing intermediate element:
    //      -> Edge-case: suspended Devices will *not* be disposed. If a new controller
    //         picks them up, they will resume under the assumption that they're managed
    //         by the same controller as before their previous unmount.


    ///////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    private static RegistryView<BlockDeviceProvider> createBlockDeviceProviderRegistry() {
        final RegistryView<BlockDeviceProvider> registry = mock(RegistryView.class);

        final Map<ResourceLocation, BlockDeviceProvider> blockDeviceProviders = new HashMap<>();
        blockDeviceProviders.put(TEST_PROVIDER_REGISTRY_NAME, spy(new TestBlockDeviceProvider()));

        when(registry.getValues()).thenReturn(blockDeviceProviders.values());
        when(registry.getKey(notNull())).then(a -> blockDeviceProviders.entrySet().stream()
            .filter(entry -> entry.getValue() == a.getArgument(0))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null));
        when(registry.getValue(notNull())).then(a -> blockDeviceProviders.get(a.<ResourceLocation>getArgument(0)));

        return registry;
    }

    @SuppressWarnings("unchecked")
    private static RegistryView<ItemDeviceProvider> createItemDeviceProviderRegistry() {
        final RegistryView<ItemDeviceProvider> registry = mock(RegistryView.class);

        final Map<ResourceLocation, ItemDeviceProvider> itemDeviceProviders = new HashMap<>();
        itemDeviceProviders.put(TEST_PROVIDER_REGISTRY_NAME, spy(new TestItemDeviceProvider()));

        when(registry.getValues()).thenReturn(itemDeviceProviders.values());
        when(registry.getKey(notNull())).then(a -> itemDeviceProviders.entrySet().stream()
            .filter(entry -> entry.getValue() == a.getArgument(0))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null));
        when(registry.getValue(notNull())).then(a -> itemDeviceProviders.get(a.<ResourceLocation>getArgument(0)));

        return registry;
    }

    ///////////////////////////////////////////////////////////////////

    private static final class FakeLevel {
        private final Level level = mock(Level.class);
        private final HashMap<BlockPos, BlockEntity> blockEntities = new HashMap<>();
        private final HashSet<ChunkPos> loadedChunks = new HashSet<>();

        public FakeLevel() {
            when(level.getBlockEntity(any())).then(a -> blockEntities.get(a.<BlockPos>getArgument(0)));
            when(level.isClientSide()).thenReturn(false);

            when(level.hasChunk(anyInt(), anyInt())).then(a -> {
                final int chunkX = a.getArgument(0);
                final int chunkZ = a.getArgument(1);
                return loadedChunks.contains(new ChunkPos(chunkX, chunkZ));
            });
        }

        public Level getLevel() {
            return level;
        }

        public void addBlockEntity(final BlockEntity blockEntity) {
            blockEntities.put(blockEntity.getBlockPos(), blockEntity);
        }

        public void removeBlockEntity(final BlockPos pos) {
            blockEntities.remove(pos);
        }

        public void setChunkLoaded(final ChunkPos chunkPos, final boolean loaded) {
            if (loaded) {
                loadedChunks.add(chunkPos);
            } else {
                loadedChunks.remove(chunkPos);
            }
        }
    }

    private class TestBlockEntity {
        private final TestCapabilityBlockEntity blockEntity;

        public TestBlockEntity(final BlockPos pos) {
            blockEntity = new TestCapabilityBlockEntity(pos);
            blockEntity.setLevel(fakeLevel.getLevel());

            fakeLevel.addBlockEntity(blockEntity);
            fakeLevel.setChunkLoaded(new ChunkPos(pos), true);
        }

        public TestCapabilityBlockEntity getBlockEntity() {
            return blockEntity;
        }
    }

    private final class TestCapabilityBlockEntity extends BlockEntity implements CapabilityProvider {
        private BiFunction<CapabilityRef<?>, Direction, LazyValue<?>> capabilityProvider = (capability, side) -> LazyValue.empty();

        public TestCapabilityBlockEntity(final BlockPos pos) {
            super(null, pos, null);
        }

        public void setCapabilityProvider(final BiFunction<CapabilityRef<?>, Direction, LazyValue<?>> capabilityProvider) {
            this.capabilityProvider = capabilityProvider;
        }

        @Override
        public boolean isValidBlockState(@Nullable final BlockState state) {
            return true;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> LazyValue<T> getCapability(final CapabilityRef<T> capability, @Nullable final Direction side) {
            final LazyValue<?> value = capabilityProvider.apply(capability, side);
            return value != null ? value.cast() : LazyValue.empty();
        }
    }

    private class TestBusElementBlockEntity extends TestBlockEntity {
        private final TestBlockDeviceBusElement busElement;
        private final boolean[] enabledSides = new boolean[Constants.BLOCK_FACE_COUNT];

        public TestBusElementBlockEntity(final BlockPos pos) {
            super(pos);
            busElement = spy(new TestBlockDeviceBusElement(level, pos));
            getBlockEntity().setCapabilityProvider((capability, side) -> {
                if (capability == Capabilities.deviceBusElement() && side != null && enabledSides[side.get3DDataValue()]) {
                    return LazyValue.of(() -> busElement);
                }
                return LazyValue.empty();
            });
            Arrays.fill(enabledSides, true);
        }

        public TestBlockDeviceBusElement getBusElement() {
            return busElement;
        }

        public void setSideEnabled(final Direction side, final boolean value) {
            enabledSides[side.get3DDataValue()] = value;
        }
    }

    private class TestBusControllerBlockEntity extends TestBusElementBlockEntity {
        private final BlockDeviceBusController busController;

        public TestBusControllerBlockEntity(final BlockPos pos) {
            super(pos);
            busController = new BlockDeviceBusController(getBusElement(), 0, getBlockEntity());
        }

        public BlockDeviceBusController getBusController() {
            return busController;
        }
    }

    private class TestDeviceBlockEntity extends TestBlockEntity {
        private final TestDevice testDevice;
        private ObjectDevice objectDevice;

        public TestDeviceBlockEntity(final BlockPos pos) {
            super(pos);
            testDevice = new TestDevice();
            objectDevice = new ObjectDevice(testDevice);
            getBlockEntity().setCapabilityProvider((capability, side) ->
                capability == Capabilities.device() ? LazyValue.of(() -> objectDevice) : LazyValue.empty());
        }

        public TestDevice getTestDevice() {
            return testDevice;
        }

        public ObjectDevice getObjectDevice() {
            return objectDevice;
        }

        public void setObjectDevice(final ObjectDevice device) {
            this.objectDevice = device;
        }
    }

    private static final class TestBlockDeviceBusElement extends AbstractBlockDeviceBusElement {
        private final LevelAccessor level;
        private final BlockPos blockPos;

        public TestBlockDeviceBusElement(final LevelAccessor level, final BlockPos blockPos) {
            this.level = level;
            this.blockPos = blockPos;
        }

        @Override
        public LevelAccessor getLevel() {
            return level;
        }

        @Override
        public BlockPos getPosition() {
            return blockPos;
        }

        @Override
        protected void collectSyntheticDevices(final LevelAccessor level, final BlockPos pos, @Nullable final Direction side, final HashSet<BlockEntry> entries) {
        }
    }

    private static class TestBlockDeviceProvider implements BlockDeviceProvider {
        @Override
        public Invalidatable<Device> getDevice(final BlockDeviceQuery query) {
            final LevelAccessor level = query.getLevel();
            final BlockEntity blockEntity = level.getBlockEntity(query.getQueryPosition());
            if (blockEntity != null) {
                final Optional<Device> optional = Capabilities.getCapability(blockEntity, Capabilities.device(), null).resolve();
                return optional.map(Invalidatable::of).orElseGet(Invalidatable::empty);
            }
            return Invalidatable.empty();
        }
    }

    private static class TestItemDeviceProvider implements ItemDeviceProvider {
        @Override
        public Optional<ItemDevice> getDevice(final ItemDeviceQuery query) {
            return Optional.empty();
        }
    }

    public static class TestDevice {
        @Callback
        public int test() {
            return 42;
        }
    }
}
