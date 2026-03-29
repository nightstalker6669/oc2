/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import li.cil.oc2.common.util.ItemStackUtils;
import li.cil.oc2.common.util.TooltipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ModBlockItem extends BlockItem {
    public ModBlockItem(final Block block, final Properties properties) {
        super(block, properties);
    }

    public ModBlockItem(final Block block) {
        this(block, createProperties());
    }

    ///////////////////////////////////////////////////////////////////

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, final Item.TooltipContext context, final List<Component> tooltip, final TooltipFlag flag) {
        TooltipUtils.tryAddDescription(stack, tooltip);
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(final BlockPos pos, final Level level, @Nullable final Player player, final ItemStack stack, final BlockState state) {
        if (super.updateCustomBlockEntityTag(pos, level, player, stack, state)) {
            return true;
        }

        if (level.getServer() == null) {
            return false;
        }

        final CompoundTag legacyBlockEntityData = ItemStackUtils.getLegacyBlockEntityDataTag(stack);
        if (legacyBlockEntityData.isEmpty()) {
            return false;
        }

        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }

        if (!level.isClientSide() && blockEntity.onlyOpCanSetNbt() && (player == null || !player.canUseGameMasterBlocks())) {
            return false;
        }

        return CustomData.of(legacyBlockEntityData).loadInto(blockEntity, level.registryAccess());
    }

    ///////////////////////////////////////////////////////////////////

    protected static Properties createProperties() {
        return new Properties();
    }
}
