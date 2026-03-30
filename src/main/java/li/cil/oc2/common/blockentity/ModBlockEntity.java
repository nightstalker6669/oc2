/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.blockentity;

import li.cil.oc2.api.API;
import li.cil.oc2.common.capabilities.CapabilityRef;
import li.cil.oc2.common.util.LazyValue;
import li.cil.oc2.common.util.LazyValueUtils;
import li.cil.oc2.common.util.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class ModBlockEntity extends BlockEntity {
    private final Runnable onWorldUnloaded = this::onWorldUnloaded;
    private final HashMap<CapabilityCacheKey, LazyValue<?>> capabilityCache = new HashMap<>();
    private boolean needsWorldUnloadEvent;
    private boolean isUnloaded;
    @Nullable private HolderLookup.Provider currentRegistries;

    ///////////////////////////////////////////////////////////////////

    protected ModBlockEntity(final BlockEntityType<?> blockEntityType, final BlockPos pos, final BlockState state) {
        super(blockEntityType, pos, state);
    }

    ///////////////////////////////////////////////////////////////////

    @Nonnull
    public <T> LazyValue<T> getCapability(final CapabilityRef<T> capability, @Nullable final Direction side) {
        if (!isValid()) {
            return LazyValue.empty();
        }

        final CapabilityCacheKey key = new CapabilityCacheKey(capability, side);
        LazyValue<?> value;
        if (capabilityCache.containsKey(key)) {
            value = capabilityCache.get(key);
        } else {
            value = LazyValue.empty();
        }

        if (!value.isPresent()) {
            final ArrayList<T> list = new ArrayList<>();
            collectCapabilities(new CapabilityCollector() {
                @SuppressWarnings("unchecked")
                @Override
                public <TOffered> void offer(final CapabilityRef<TOffered> offeredCapability, final TOffered instance) {
                    if (offeredCapability == capability) {
                        list.add((T) instance);
                    }
                }
            }, side);

            if (!list.isEmpty()) {
                final T instance = list.get(0);
                value = LazyValue.of(() -> instance);
            } else {
                value = LazyValue.empty();
            }

            if (value.isPresent()) {
                capabilityCache.put(key, value);
                LazyValueUtils.addWeakListener(value, capabilityCache, (map, optional) -> map.remove(key, optional));
            }
        }

        return value.cast();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        final var level = this.level;
        if (level == null) {
            return;
        }

        if (level.isClientSide()) {
            loadClient();
        } else {
            loadServer();

            if (needsWorldUnloadEvent) {
                ServerScheduler.scheduleOnUnload(level, onWorldUnloaded);
            }
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded(); // -> invalidateCaps()
        onUnload(false);
        isUnloaded = true;
    }

    public void onWorldUnloaded() {
        invalidateCaps();
        onUnload(false);
    }

    @Override
    public void setRemoved() {
        super.setRemoved(); // -> invalidateCaps()
        if (!isUnloaded) {
            onUnload(true);
        }
    }

    public boolean isValid() {
        return !isRemoved() && !isUnloaded;
    }

    ///////////////////////////////////////////////////////////////////

    protected <T> void invalidateCapability(final CapabilityRef<T> capability, @Nullable final Direction direction) {
        final CapabilityCacheKey key = new CapabilityCacheKey(capability, direction);
        final LazyValue<?> value = capabilityCache.get(key);
        if (value != null) {
            value.invalidate();
        }
    }

    public void invalidateCaps() {
        invalidateCapabilities();

        // Copy values because invalidate callback will modify map (removes invalidated entry).
        for (final LazyValue<?> capability : new ArrayList<>(capabilityCache.values())) {
            capability.invalidate();
        }
    }

    @Override
    protected final void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        currentRegistries = registries;
        try {
            super.saveAdditional(tag, registries);
            saveAdditional(tag);
        } finally {
            currentRegistries = null;
        }
    }

    @Override
    protected final void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        currentRegistries = registries;
        try {
            super.loadAdditional(tag, registries);
            load(tag);
        } finally {
            currentRegistries = null;
        }
    }

    @Override
    public final CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        currentRegistries = registries;
        try {
            return getUpdateTag();
        } finally {
            currentRegistries = null;
        }
    }

    @Override
    public final void handleUpdateTag(final CompoundTag tag, final HolderLookup.Provider registries) {
        currentRegistries = registries;
        try {
            handleUpdateTag(tag);
        } finally {
            currentRegistries = null;
        }
    }

    protected void onUnload(final boolean isRemove) {
        final var level = this.level;
        if (level != null && !level.isClientSide()) {
            unloadServer(isRemove);
            ServerScheduler.cancelOnUnload(level, onWorldUnloaded);
        }
    }

    protected void setNeedsLevelUnloadEvent() {
        needsWorldUnloadEvent = true;
    }

    public CompoundTag getUpdateTag() {
        return new CompoundTag();
    }

    public void handleUpdateTag(final CompoundTag tag) {
        load(tag);
    }

    protected void saveAdditional(final CompoundTag tag) {
    }

    public void load(final CompoundTag tag) {
    }

    protected void collectCapabilities(final CapabilityCollector collector, @Nullable final Direction direction) {
    }

    protected final HolderLookup.Provider getRegistries() {
        if (currentRegistries != null) {
            return currentRegistries;
        }
        if (level != null) {
            return level.registryAccess();
        }
        throw new IllegalStateException("No registry provider available.");
    }

    protected void loadClient() {
    }

    protected void loadServer() {
    }

    protected void unloadServer(final boolean isRemove) {
    }

    ///////////////////////////////////////////////////////////////////

    @FunctionalInterface
    protected interface CapabilityCollector {
        <T> void offer(CapabilityRef<T> capability, T instance);
    }

    ///////////////////////////////////////////////////////////////////

    private record CapabilityCacheKey(CapabilityRef<?> capability, @Nullable Direction direction) { }
}
