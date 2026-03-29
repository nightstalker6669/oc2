package li.cil.oc2.common.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public interface DyeableLeatherItem {
    int DEFAULT_LEATHER_COLOR = 0xA06540;

    default boolean hasCustomColor(final ItemStack stack) {
        return stack.has(DataComponents.DYED_COLOR);
    }

    default int getColor(final ItemStack stack) {
        return DyedItemColor.getOrDefault(stack, DEFAULT_LEATHER_COLOR);
    }

    default void setColor(final ItemStack stack, final int color) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color, true));
    }

    default void clearColor(final ItemStack stack) {
        stack.remove(DataComponents.DYED_COLOR);
    }
}
