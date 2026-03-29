/* SPDX-License-Identifier: MIT */

package li.cil.oc2.data;

import com.google.common.collect.Lists;
import li.cil.oc2.common.item.Items;
import li.cil.oc2.common.item.crafting.WrenchRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WrenchRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final Item result;
    private final int count;
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    public WrenchRecipeBuilder(final RecipeCategory category, final ItemLike result, final int count) {
        this.category = category;
        this.result = result.asItem();
        this.count = count;

        requires(Items.WRENCH.get());
    }

    public static WrenchRecipeBuilder wrenchRecipe(final RecipeCategory category, final ItemLike result) {
        return new WrenchRecipeBuilder(category, result, 1);
    }

    public static WrenchRecipeBuilder wrenchRecipe(final RecipeCategory category, final ItemLike result, final int count) {
        return new WrenchRecipeBuilder(category, result, count);
    }

    public static WrenchRecipeBuilder wrenchRecipe(final ItemLike result) {
        return wrenchRecipe(RecipeCategory.MISC, result);
    }

    public static WrenchRecipeBuilder wrenchRecipe(final ItemLike result, final int count) {
        return wrenchRecipe(RecipeCategory.MISC, result, count);
    }

    public WrenchRecipeBuilder requires(final TagKey<Item> tag) {
        return requires(Ingredient.of(tag));
    }

    public WrenchRecipeBuilder requires(final ItemLike item) {
        return requires(item, 1);
    }

    public WrenchRecipeBuilder requires(final ItemLike item, final int quantity) {
        for (int i = 0; i < quantity; i++) {
            requires(Ingredient.of(item));
        }

        return this;
    }

    public WrenchRecipeBuilder requires(final Ingredient ingredient) {
        return requires(ingredient, 1);
    }

    public WrenchRecipeBuilder requires(final Ingredient ingredient, final int quantity) {
        for (int i = 0; i < quantity; i++) {
            ingredients.add(ingredient);
        }

        return this;
    }

    @Override
    public WrenchRecipeBuilder unlockedBy(final String name, final Criterion<?> criterion) {
        criteria.put(name, criterion);
        return this;
    }

    @Override
    public WrenchRecipeBuilder group(@Nullable final String value) {
        group = value;
        return this;
    }

    public WrenchRecipeBuilder setGroup(@Nullable final String value) {
        return group(value);
    }

    @Override
    public Item getResult() {
        return result;
    }

    @Override
    public void save(final RecipeOutput recipeOutput, final ResourceLocation id) {
        ensureValid(id);

        final Advancement.Builder advancementBuilder = recipeOutput.advancement()
            .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .requirements(AdvancementRequirements.Strategy.OR);
        criteria.forEach(advancementBuilder::addCriterion);

        final NonNullList<Ingredient> recipeIngredients = NonNullList.create();
        recipeIngredients.addAll(ingredients);

        final AdvancementHolder advancement = advancementBuilder.build(id.withPrefix("recipes/" + category.getFolderName() + "/"));
        final ItemStack resultStack = new ItemStack(result, count);
        final WrenchRecipe recipe = new WrenchRecipe(
            Objects.requireNonNullElse(group, ""),
            RecipeBuilder.determineBookCategory(category),
            resultStack,
            recipeIngredients
        );
        recipeOutput.accept(id, recipe, advancement);
    }

    private void ensureValid(final ResourceLocation id) {
        if (criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }
}
