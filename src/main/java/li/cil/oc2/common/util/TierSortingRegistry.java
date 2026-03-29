package li.cil.oc2.common.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public final class TierSortingRegistry {
    private TierSortingRegistry() {
    }

    public static ResourceLocation getName(final Tier tier) {
        if (tier == Tiers.WOOD) return ResourceLocation.fromNamespaceAndPath("minecraft", "wood");
        if (tier == Tiers.STONE) return ResourceLocation.fromNamespaceAndPath("minecraft", "stone");
        if (tier == Tiers.IRON) return ResourceLocation.fromNamespaceAndPath("minecraft", "iron");
        if (tier == Tiers.GOLD) return ResourceLocation.fromNamespaceAndPath("minecraft", "gold");
        if (tier == Tiers.DIAMOND) return ResourceLocation.fromNamespaceAndPath("minecraft", "diamond");
        if (tier == Tiers.NETHERITE) return ResourceLocation.fromNamespaceAndPath("minecraft", "netherite");
        return ResourceLocation.fromNamespaceAndPath("minecraft", "wood");
    }

    @Nullable
    public static Tier byName(final ResourceLocation name) {
        if (name == null) return null;
        return switch (name.getPath()) {
            case "stone" -> Tiers.STONE;
            case "iron" -> Tiers.IRON;
            case "gold" -> Tiers.GOLD;
            case "diamond" -> Tiers.DIAMOND;
            case "netherite" -> Tiers.NETHERITE;
            case "wood" -> Tiers.WOOD;
            default -> null;
        };
    }

    public static boolean isCorrectTierForDrops(final Tier tier, final BlockState state) {
        return !state.requiresCorrectToolForDrops() || !state.is(tier.getIncorrectBlocksForDrops());
    }
}
