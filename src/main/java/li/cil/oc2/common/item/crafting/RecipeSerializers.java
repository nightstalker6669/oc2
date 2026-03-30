/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item.crafting;

import li.cil.oc2.common.util.RegistryUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RecipeSerializers {
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = RegistryUtils.getInitializerFor(Registries.RECIPE_SERIALIZER);

    ///////////////////////////////////////////////////////////////////

    public static final DeferredHolder<RecipeSerializer<?>, WrenchRecipe.Serializer> WRENCH = RECIPE_SERIALIZERS.register("wrench", () -> WrenchRecipe.Serializer.INSTANCE);

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
    }
}
