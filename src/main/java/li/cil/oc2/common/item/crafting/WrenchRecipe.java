/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import li.cil.oc2.common.integration.Wrenches;
import li.cil.oc2.common.util.ItemStackUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public final class WrenchRecipe extends ShapelessRecipe {
    private static final int MAX_INGREDIENTS = 9;

    public WrenchRecipe(final String group, final CraftingBookCategory category, final ItemStack result, final NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
    }

    public WrenchRecipe(final ShapelessRecipe recipe, final HolderLookup.Provider registries) {
        this(recipe.getGroup(), recipe.category(), recipe.getResultItem(registries), recipe.getIngredients());
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingInput input) {
        final NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int slot = 0; slot < input.size(); slot++) {
            final ItemStack stack = input.getItem(slot);
            if (stack.hasCraftingRemainingItem()) {
                result.set(slot, stack.getCraftingRemainingItem());
            } else if (Wrenches.isWrench(stack)) {
                final ItemStack copy = stack.copy();
                copy.setCount(1);
                result.set(slot, copy);
            }
        }

        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static final class Serializer implements RecipeSerializer<WrenchRecipe> {
        private static final MapCodec<WrenchRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.getGroup()),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(WrenchRecipe::category),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.getResultItem(ItemStackUtils.getDefaultRegistries())),
            Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(
                values -> {
                    final Ingredient[] ingredients = values.toArray(Ingredient[]::new);
                    if (ingredients.length == 0) {
                        return DataResult.error(() -> "No ingredients for shapeless recipe");
                    }
                    if (ingredients.length > MAX_INGREDIENTS) {
                        return DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: %s".formatted(MAX_INGREDIENTS));
                    }
                    return DataResult.success(NonNullList.of(Ingredient.EMPTY, ingredients));
                },
                DataResult::success
            ).forGetter(WrenchRecipe::getIngredients)
        ).apply(instance, WrenchRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, WrenchRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::toNetwork,
            Serializer::fromNetwork
        );
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {
        }

        @Override
        public MapCodec<WrenchRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, WrenchRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static WrenchRecipe fromNetwork(final RegistryFriendlyByteBuf buffer) {
            final String group = buffer.readUtf();
            final CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            final int ingredientCount = buffer.readVarInt();
            final NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientCount, Ingredient.EMPTY);
            ingredients.replaceAll(ignored -> Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            final ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            return new WrenchRecipe(group, category, result, ingredients);
        }

        private static void toNetwork(final RegistryFriendlyByteBuf buffer, final WrenchRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            buffer.writeVarInt(recipe.getIngredients().size());

            for (final Ingredient ingredient : recipe.getIngredients()) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }

            ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(ItemStackUtils.getDefaultRegistries()));
        }
    }
}
