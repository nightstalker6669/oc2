# OC2 Addon API Reference

This document lists the supported addon-facing API surface for the current `1.21.1-neoforge` port.

Scope rules for this reference:

- Included: types under `li.cil.oc2.api` and the explicit IMC constant in `li.cil.oc2.api.API`.
- Excluded: `li.cil.oc2.common.*`, renderer internals, registry holder internals, and other implementation classes.
- `package-info.java` files are not listed individually. They only provide package-level docs and defaults.

If you are integrating with OC2 from another mod, treat anything outside `li.cil.oc2.api` as unstable unless OC2 explicitly documents it elsewhere.

## Quick integration map

Use one of these entry paths:

- Expose block-attached devices: implement `li.cil.oc2.api.bus.device.provider.BlockDeviceProvider`.
- Expose item-attached devices: implement `li.cil.oc2.api.bus.device.provider.ItemDeviceProvider`.
- Expose high-level callable methods: implement `li.cil.oc2.api.bus.device.rpc.RPCDevice`, or more commonly wrap a Java object in `li.cil.oc2.api.bus.device.object.ObjectDevice`.
- Expose low-level hardware: implement `li.cil.oc2.api.bus.device.vm.VMDevice`.
- Add extra RPC parameter serializers: send IMC using `li.cil.oc2.api.API.IMC_ADD_RPC_METHOD_PARAMETER_TYPE_ADAPTER`.
- Expose network, robot, redstone, or terminal-state hooks: implement the relevant interface from `li.cil.oc2.api.capabilities` and expose it via your own NeoForge capability registration.
- Add your own bus segments or VM containers: implement the bus interfaces in `li.cil.oc2.api.bus`.

## Stable contract and registration points

### `li.cil.oc2.api.API`

Use: global OC2 constants.

Supported constants:

- `String MOD_ID`
- `String IMC_ADD_RPC_METHOD_PARAMETER_TYPE_ADAPTER`

Notes:

- `IMC_ADD_RPC_METHOD_PARAMETER_TYPE_ADAPTER` expects an IMC payload supplier that produces `li.cil.oc2.api.imc.RPCMethodParameterTypeAdapter`.

### `li.cil.oc2.api.util.Registries`

Use: registry keys for OC2-managed custom registries.

Supported constants:

- `ResourceKey<Registry<BlockDeviceProvider>> BLOCK_DEVICE_PROVIDER`
- `ResourceKey<Registry<ItemDeviceProvider>> ITEM_DEVICE_PROVIDER`
- `ResourceKey<Registry<BlockDeviceData>> BLOCK_DEVICE_DATA`
- `ResourceKey<Registry<Firmware>> FIRMWARE`

Notes:

- These keys are the current registration path for addon providers and data entries on NeoForge.

## Primary supported entrypoints

### Block devices

Primary type: `li.cil.oc2.api.bus.device.provider.BlockDeviceProvider`

Use this when a block in the world should expose one or more OC2 devices.

Calls:

- `Invalidatable<Device> getDevice(BlockDeviceQuery query)`
- `default void unmount(BlockDeviceQuery query, CompoundTag tag)`

Behavior requirements:

- For identical query and world state, return the same device instance when possible.
- If you cannot return the same instance, return equal instances with stable `equals()` / `hashCode()`.
- Return `Invalidatable.empty()` when no device is available.

Supporting query type: `li.cil.oc2.api.bus.device.provider.BlockDeviceQuery`

Calls:

- `LevelAccessor getLevel()`
- `BlockPos getQueryPosition()`
- `@Nullable Direction getQuerySide()`

Supporting wrapper: `li.cil.oc2.api.util.Invalidatable<T>`

Calls:

- `static <T> Invalidatable<T> empty()`
- `static <T> Invalidatable<T> of(T value)`
- `T get()`
- `boolean isPresent()`
- `void ifPresent(Consumer<T> consumer)`
- `<U> Invalidatable<U> mapWithDependency(Function<T, U> mapper)`
- `void invalidate()`
- `ListenerToken addListener(Consumer<Invalidatable<T>> listener)`

Nested type:

- `Invalidatable.ListenerToken`
  - `void removeListener()`

### Item devices

Primary type: `li.cil.oc2.api.bus.device.provider.ItemDeviceProvider`

Use this when an item inserted into a computer or robot should expose one or more OC2 devices.

Calls:

- `Optional<ItemDevice> getDevice(ItemDeviceQuery query)`
- `default int getEnergyConsumption(ItemDeviceQuery query)`
- `default void unmount(@Nullable ItemDeviceQuery query, CompoundTag tag)`

Supporting query type: `li.cil.oc2.api.bus.device.provider.ItemDeviceQuery`

Calls:

- `Optional<BlockEntity> getContainerBlockEntity()`
- `Optional<Entity> getContainerEntity()`
- `ItemStack getItemStack()`

Item-specific device type: `li.cil.oc2.api.bus.device.ItemDevice`

Calls:

- `default void exportToItemStack(CompoundTag nbt)`
- `default void importFromItemStack(CompoundTag nbt)`

Notes:

- Use `exportToItemStack()` / `importFromItemStack()` for state that should survive the item leaving and re-entering a machine.
- Slot compatibility is controlled by `DeviceType` item tags.

### High-level RPC devices

Primary type: `li.cil.oc2.api.bus.device.rpc.RPCDevice`

Use this when you want the VM to call named methods on your device.

Calls:

- `List<String> getTypeNames()`
- `List<RPCMethodGroup> getMethodGroups()`
- `default void mount()`
- `default void unmount()`

Base device type: `li.cil.oc2.api.bus.device.Device`

Calls:

- `default void dispose()`
- `default CompoundTag serializeNBT(HolderLookup.Provider provider)`
- `default CompoundTag serializeNBT()`
- `default void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag)`
- `default void deserializeNBT(CompoundTag tag)`

Method group type: `li.cil.oc2.api.bus.device.rpc.RPCMethodGroup`

Calls:

- `String getName()`
- `default Set<RPCMethod> getOverloads()`
- `Optional<RPCMethod> findOverload(RPCInvocation invocation)`

Method type: `li.cil.oc2.api.bus.device.rpc.RPCMethod`

Calls:

- `boolean isSynchronized()`
- `Class<?> getReturnType()`
- `RPCParameter[] getParameters()`
- `@Nullable Object invoke(RPCInvocation invocation) throws Throwable`
- `default Optional<String> getDescription()`
- `default Optional<String> getReturnValueDescription()`
- inherited `getName()`, `getOverloads()`, `findOverload(...)`

Invocation type: `li.cil.oc2.api.bus.device.rpc.RPCInvocation`

Calls:

- `JsonArray getParameters()`
- `Gson getGson()`
- `Optional<Object[]> tryDeserializeParameters(RPCParameter... parameterTypes)`

Parameter type: `li.cil.oc2.api.bus.device.rpc.RPCParameter`

Calls:

- `Class<?> getType()`
- `default Optional<String> getName()`
- `default Optional<String> getDescription()`

Convenience base class: `li.cil.oc2.api.bus.device.rpc.AbstractRPCMethod`

Use:

- Subclass it when you want a manual `RPCMethod` implementation without re-implementing the basic metadata accessors.

Subclass entrypoint:

- `protected abstract @Nullable Object invoke(Object... parameters) throws Throwable`

Reflection-based convenience path: `li.cil.oc2.api.bus.device.object.ObjectDevice`

Use:

- Wrap a Java object and let OC2 turn `@Callback` methods into an `RPCDevice`.

Constructors:

- `ObjectDevice(Object object, List<String> typeNames)`
- `ObjectDevice(Object object, String... typeNames)`
- `ObjectDevice(Object object, @Nullable String typeName)`
- `ObjectDevice(Object object)`

Calls:

- inherited `getTypeNames()`, `getMethodGroups()`, `mount()`, `unmount()`, `dispose()`

Reflection helper: `li.cil.oc2.api.bus.device.object.Callbacks`

Calls:

- `static List<RPCMethodGroup> collectMethods(Object methodContainer)`
- `static boolean hasMethods(Object object)`

Annotations and helper interfaces for `ObjectDevice`:

- `li.cil.oc2.api.bus.device.object.Callback`
  - `boolean synchronize() default true`
  - `String name() default ""`
  - `String description() default ""`
  - `String returnValueDescription() default ""`
- `li.cil.oc2.api.bus.device.object.Parameter`
  - `String value()`
  - `String description() default ""`
- `li.cil.oc2.api.bus.device.object.NamedDevice`
  - `Collection<String> getDeviceTypeNames()`
- `li.cil.oc2.api.bus.device.object.LifecycleAwareDevice`
  - `default void onDeviceMounted()`
  - `default void onDeviceUnmounted()`
  - `default void onDeviceDisposed()`
- `li.cil.oc2.api.bus.device.object.DocumentedDevice`
  - `void getDeviceDocumentation(DeviceVisitor visitor)`
  - nested `DeviceVisitor`
    - `CallbackVisitor visitCallback(String callbackName)`
  - nested `CallbackVisitor`
    - `CallbackVisitor description(String value)`
    - `CallbackVisitor returnValueDescription(String value)`
    - `CallbackVisitor parameterDescription(String parameterName, String value)`

### Low-level VM devices

Primary type: `li.cil.oc2.api.bus.device.vm.VMDevice`

Use this when you want to attach memory-mapped hardware directly to the VM.

Calls:

- `VMDeviceLoadResult mount(VMContext context)`
- `void unmount()`
- inherited `dispose()` and NBT serialization methods from `Device`

Load result: `li.cil.oc2.api.bus.device.vm.VMDeviceLoadResult`

Factory methods:

- `static VMDeviceLoadResult success()`
- `static VMDeviceLoadResult fail()`

Calls:

- `boolean wasSuccessful()`
- `VMDeviceLoadResult withErrorMessage(Component value)`
- `@Nullable Component getErrorMessage()`

Marker subtype: `li.cil.oc2.api.bus.device.vm.FirmwareLoader`

Use:

- Marker for VM devices that provide required firmware early in startup.

VM context: `li.cil.oc2.api.bus.device.vm.context.VMContext`

Calls:

- `MemoryMap getMemoryMap()`
- `InterruptController getInterruptController()`
- `MemoryRangeAllocator getMemoryRangeAllocator()`
- `InterruptAllocator getInterruptAllocator()`
- `MemoryAllocator getMemoryAllocator()`
- `VMLifecycleEventBus getEventBus()`

Allocators and event bus:

- `li.cil.oc2.api.bus.device.vm.context.MemoryAllocator`
  - `boolean claimMemory(int size)`
- `li.cil.oc2.api.bus.device.vm.context.MemoryRangeAllocator`
  - `boolean claimMemoryRange(long address, MemoryMappedDevice device)`
  - `OptionalLong claimMemoryRange(MemoryMappedDevice device)`
- `li.cil.oc2.api.bus.device.vm.context.InterruptAllocator`
  - `boolean claimInterrupt(int interrupt)`
  - `OptionalInt claimInterrupt()`
- `li.cil.oc2.api.bus.device.vm.context.VMLifecycleEventBus`
  - `void register(Object subscriber)`

Lifecycle events:

- `li.cil.oc2.api.bus.device.vm.event.VMInitializingEvent`
  - record component: `long programStartAddress`
- `li.cil.oc2.api.bus.device.vm.event.VMResumedRunningEvent`
  - no fields
- `li.cil.oc2.api.bus.device.vm.event.VMSynchronizeEvent`
  - no fields
- `li.cil.oc2.api.bus.device.vm.event.VMInitializationException`
  - constructors:
    - `VMInitializationException(Component message)`
    - `VMInitializationException()`
  - calls:
    - `Optional<Component> getErrorMessage()`

### Capabilities and utility types

These are stable interface contracts. OC2 may look for them in specific integration paths, but OC2 does not expose a generic stable capability token API under `li.cil.oc2.api`.

- `li.cil.oc2.api.capabilities.NetworkInterface`
  - `@Nullable byte[] readEthernetFrame()`
  - `void writeEthernetFrame(NetworkInterface source, byte[] frame, int timeToLive)`
- `li.cil.oc2.api.capabilities.RedstoneEmitter`
  - `int getRedstoneOutput()`
- `li.cil.oc2.api.capabilities.Robot`
  - `ItemStackHandler getInventory()`
  - `int getSelectedSlot()`
  - `void setSelectedSlot(int value)`
- `li.cil.oc2.api.capabilities.TerminalUserProvider`
  - `Iterable<Player> getTerminalUsers()`

Item slot typing:

- `li.cil.oc2.api.bus.device.DeviceType`
  - `ResourceKey<Registry<DeviceType>> REGISTRY`
  - `TagKey<Item> getTag()`
  - `ResourceLocation getBackgroundIcon()`
  - `Component getName()`
- `li.cil.oc2.api.bus.device.DeviceTypes`
  - built-in constants:
    - `MEMORY`
    - `HARD_DRIVE`
    - `FLASH_MEMORY`
    - `CARD`
    - `ROBOT_MODULE`
    - `FLOPPY`
    - `NETWORK_TUNNEL`

Directional helper enums:

- `li.cil.oc2.api.util.Side`
  - canonical values: `DOWN`, `UP`, `NORTH`, `SOUTH`, `WEST`, `EAST`
  - aliases: `down`, `d`, `up`, `u`, `north`, `n`, `back`, `b`, `south`, `s`, `front`, `f`, `west`, `w`, `left`, `l`, `east`, `e`, `right`, `r`
  - `Direction getDirection()`
- `li.cil.oc2.api.util.RobotOperationSide`
  - canonical values: `FRONT`, `UP`, `DOWN`
  - aliases: `front`, `f`, `up`, `u`, `down`, `d`
  - `static Direction toGlobal(Entity entity, @Nullable RobotOperationSide side)`

### Custom bus topology APIs

Most addon mods do not need this section. It is only relevant if you are implementing your own OC2 bus cable, bus segment, or VM container.

- `li.cil.oc2.api.bus.DeviceBus`
  - `Collection<Device> getDevices()`
  - `void scheduleScan()`
- `li.cil.oc2.api.bus.DeviceBusElement`
  - `void addController(DeviceBusController controller)`
  - `void removeController(DeviceBusController controller)`
  - `Collection<DeviceBusController> getControllers()`
  - `Optional<Collection<LazyOptional<DeviceBusElement>>> getNeighbors()`
  - `Collection<Device> getLocalDevices()`
  - `Optional<UUID> getDeviceIdentifier(Device device)`
  - `default double getEnergyConsumption()`
- `li.cil.oc2.api.bus.BlockDeviceBusElement`
  - `@Nullable LevelAccessor getLevel()`
  - `BlockPos getPosition()`
- `li.cil.oc2.api.bus.DeviceBusController`
  - nested enum `ScanReason`
    - `BUS_CHANGE`
    - `BUS_ERROR`
  - `void scheduleBusScan(ScanReason reason)`
  - `default void scheduleBusScan()`
  - `void scanDevices()`
  - `Set<Device> getDevices()`
  - `Set<UUID> getDeviceIdentifiers(Device device)`

### Data registries and IMC

Block device base data:

- `li.cil.oc2.api.bus.device.data.BlockDeviceData`
  - `BlockDevice getBlockDevice()`
  - `Component getDisplayName()`

Firmware data:

- `li.cil.oc2.api.bus.device.data.Firmware`
  - `boolean run(MemoryMap memory, long startAddress)`
  - `Component getDisplayName()`

IMC payload:

- `li.cil.oc2.api.imc.RPCMethodParameterTypeAdapter`
  - record components:
    - `Class<?> type`
    - `Object typeAdapter`

## Current NeoForge examples

### Register a block device provider

```java
import li.cil.oc2.api.util.Registries;
import li.cil.oc2.api.util.Invalidatable;
import li.cil.oc2.api.bus.device.Device;
import li.cil.oc2.api.bus.device.object.Callback;
import li.cil.oc2.api.bus.device.object.ObjectDevice;
import li.cil.oc2.api.bus.device.provider.BlockDeviceProvider;
import li.cil.oc2.api.bus.device.provider.BlockDeviceQuery;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class MyOc2Integration {
    private static final DeferredRegister<BlockDeviceProvider> BLOCK_DEVICE_PROVIDERS =
        DeferredRegister.create(Registries.BLOCK_DEVICE_PROVIDER, "my_mod");

    public static void init(final IEventBus modBus) {
        BLOCK_DEVICE_PROVIDERS.register("my_block_device", MyBlockDeviceProvider::new);
        BLOCK_DEVICE_PROVIDERS.register(modBus);
    }

    public static final class MyBlockDeviceProvider extends ForgeRegistryEntry<BlockDeviceProvider> implements BlockDeviceProvider {
        @Override
        public Invalidatable<Device> getDevice(final BlockDeviceQuery query) {
            if (!shouldExposeDevice(query)) {
                return Invalidatable.empty();
            }

            return Invalidatable.of(new ObjectDevice(new MyDevice(), "my_device"));
        }
    }

    public static final class MyDevice {
        @Callback(synchronize = false)
        public int square(final int value) {
            return value * value;
        }
    }

    private static boolean shouldExposeDevice(final BlockDeviceQuery query) {
        return true;
    }
}
```

### Register an item device provider

```java
import li.cil.oc2.api.util.Registries;
import li.cil.oc2.api.bus.device.ItemDevice;
import li.cil.oc2.api.bus.device.object.ObjectDevice;
import li.cil.oc2.api.bus.device.provider.ItemDeviceProvider;
import li.cil.oc2.api.bus.device.provider.ItemDeviceQuery;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Optional;

public final class MyOc2Items {
    private static final DeferredRegister<ItemDeviceProvider> ITEM_DEVICE_PROVIDERS =
        DeferredRegister.create(Registries.ITEM_DEVICE_PROVIDER, "my_mod");

    public static void init(final IEventBus modBus) {
        ITEM_DEVICE_PROVIDERS.register("my_item_device", MyItemDeviceProvider::new);
        ITEM_DEVICE_PROVIDERS.register(modBus);
    }

    public static final class MyItemDeviceProvider extends ForgeRegistryEntry<ItemDeviceProvider> implements ItemDeviceProvider {
        @Override
        public Optional<ItemDevice> getDevice(final ItemDeviceQuery query) {
            if (!query.getItemStack().is(MyItems.MY_CARD)) {
                return Optional.empty();
            }

            return Optional.of(new ObjectDevice(new MyCallbacks(), "my_item_device"));
        }

        @Override
        public int getEnergyConsumption(final ItemDeviceQuery query) {
            return 2;
        }
    }

    public static final class MyCallbacks {
    }
}
```

If you need item-backed persisted state, return your own class that implements `ItemDevice` and, if needed, `RPCDevice` or `VMDevice`. `ObjectDevice` already implements `ItemDevice`, but it does not provide custom `exportToItemStack()` or `importFromItemStack()` behavior by itself.

### Implement a VM device

```java
import li.cil.oc2.api.bus.device.vm.VMDevice;
import li.cil.oc2.api.bus.device.vm.VMDeviceLoadResult;
import li.cil.oc2.api.bus.device.vm.context.VMContext;
import li.cil.sedna.api.device.MemoryMappedDevice;

public final class MyVmDevice implements VMDevice {
    @Override
    public VMDeviceLoadResult mount(final VMContext context) {
        final MemoryMappedDevice device = createDevice();
        return context.getMemoryRangeAllocator().claimMemoryRange(device).isPresent()
            ? VMDeviceLoadResult.success()
            : VMDeviceLoadResult.fail();
    }

    @Override
    public void unmount() {
    }

    private static MemoryMappedDevice createDevice() {
        throw new UnsupportedOperationException();
    }
}
```

### Send an RPC parameter type adapter over IMC

```java
import li.cil.oc2.api.API;
import li.cil.oc2.api.imc.RPCMethodParameterTypeAdapter;
import net.neoforged.fml.InterModComms;

public final class MyOc2Imc {
    public static void send() {
        InterModComms.sendTo(
            API.MOD_ID,
            API.IMC_ADD_RPC_METHOD_PARAMETER_TYPE_ADAPTER,
            () -> new RPCMethodParameterTypeAdapter(MyType.class, new MyTypeAdapter())
        );
    }
}
```

## What is intentionally not in the stable addon contract

- Anything under `li.cil.oc2.common.*`
- Internal provider registries and registry holder classes
- Renderer, GUI, and networking implementation classes
- A generic public `DEVICE_CAPABILITY` token

If you need functionality that is only reachable through internal OC2 classes, document that as an explicit hard dependency on current OC2 internals, not as stable API usage.
