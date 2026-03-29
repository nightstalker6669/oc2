/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import li.cil.oc2.api.API;
import net.minecraft.Util;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ItemRenameHandler {
    private static final Map<String, Supplier<Item>> RENAMES = Util.make(() -> {
        final Map<String, Supplier<Item>> map = new HashMap<>();

        map.put("hard_drive_buildroot", Items.HARD_DRIVE_CUSTOM::get);
        map.put("flash_memory_buildroot", Items.FLASH_MEMORY_CUSTOM::get);

        return map;
    });

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
    }
}
