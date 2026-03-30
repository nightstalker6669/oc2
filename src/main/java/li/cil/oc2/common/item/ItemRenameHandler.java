/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import li.cil.oc2.api.API;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;

public final class ItemRenameHandler {
    private static final Map<String, String> RENAMES = Util.make(() -> {
        final Map<String, String> map = new HashMap<>();

        map.put("hard_drive_buildroot", "hard_drive_custom");
        map.put("flash_memory_buildroot", "flash_memory_custom");

        return map;
    });

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
    }

    static void registerAliases(final DeferredRegister<Item> register) {
        RENAMES.forEach((from, to) -> register.addAlias(
            ResourceLocation.fromNamespaceAndPath(API.MOD_ID, from),
            ResourceLocation.fromNamespaceAndPath(API.MOD_ID, to)
        ));
    }
}
