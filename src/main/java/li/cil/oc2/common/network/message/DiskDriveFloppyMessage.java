/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.network.message;

import li.cil.oc2.common.blockentity.DiskDriveBlockEntity;
import li.cil.oc2.common.network.MessageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class DiskDriveFloppyMessage extends AbstractMessage {
    private BlockPos pos;
    private CompoundTag data;

    ///////////////////////////////////////////////////////////////////

    public DiskDriveFloppyMessage(final DiskDriveBlockEntity diskDrive) {
        this.pos = diskDrive.getBlockPos();
        final var level = diskDrive.getLevel();
        this.data = diskDrive.getFloppy().isEmpty() || level == null
            ? new CompoundTag()
            : li.cil.oc2.common.util.ItemStackUtils.save(diskDrive.getFloppy(), level.registryAccess());
    }

    public DiskDriveFloppyMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        final CompoundTag data = buffer.readNbt();
        this.data = data != null ? data : new CompoundTag();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeNbt(data);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        MessageUtils.withClientBlockEntityAt(pos, DiskDriveBlockEntity.class,
            diskDrive -> {
                final var level = diskDrive.getLevel();
                diskDrive.setFloppyClient(level != null
                    ? li.cil.oc2.common.util.ItemStackUtils.parse(level.registryAccess(), data)
                    : ItemStack.EMPTY);
            });
    }
}
