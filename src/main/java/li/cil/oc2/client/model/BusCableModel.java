/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.model;

import li.cil.oc2.api.API;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.SimpleModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public record BusCableModel(BlockModel baseModel) implements IUnbakedGeometry<BusCableModel> {
    private static final ResourceLocation BUS_CABLE_STRAIGHT_MODEL = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/cable_straight");
    private static final ResourceLocation BUS_CABLE_SUPPORT_MODEL = ResourceLocation.fromNamespaceAndPath(API.MOD_ID, "block/cable_support");

    ///////////////////////////////////////////////////////////////////

    @Override
    public BakedModel bake(final IGeometryBakingContext context, final ModelBaker bakery, final Function<Material, TextureAtlasSprite> spriteGetter, final ModelState modelTransform, final ItemOverrides overrides) {
        final BakedModel bakedBaseModel = baseModel.bake(bakery, spriteGetter, modelTransform);
        final BakedModel[] straightModelByAxis = {
            requireNonNull(bakery.bake(BUS_CABLE_STRAIGHT_MODEL, rotated(modelTransform, BlockModelRotation.X0_Y90), spriteGetter)),
            requireNonNull(bakery.bake(BUS_CABLE_STRAIGHT_MODEL, rotated(modelTransform, BlockModelRotation.X90_Y0), spriteGetter)),
            requireNonNull(bakery.bake(BUS_CABLE_STRAIGHT_MODEL, modelTransform, spriteGetter))
        };
        final BakedModel[] supportModelByFace = {
            requireNonNull(bakery.bake(BUS_CABLE_SUPPORT_MODEL, rotated(modelTransform, BlockModelRotation.X270_Y0), spriteGetter)), // -y
            requireNonNull(bakery.bake(BUS_CABLE_SUPPORT_MODEL, rotated(modelTransform, BlockModelRotation.X90_Y0), spriteGetter)), // +y
            requireNonNull(bakery.bake(BUS_CABLE_SUPPORT_MODEL, rotated(modelTransform, BlockModelRotation.X0_Y180), spriteGetter)), // -z
            requireNonNull(bakery.bake(BUS_CABLE_SUPPORT_MODEL, modelTransform, spriteGetter)), // +z
            requireNonNull(bakery.bake(BUS_CABLE_SUPPORT_MODEL, rotated(modelTransform, BlockModelRotation.X0_Y90), spriteGetter)), // -x
            requireNonNull(bakery.bake(BUS_CABLE_SUPPORT_MODEL, rotated(modelTransform, BlockModelRotation.X0_Y270), spriteGetter)) // +x
        };

        return new BusCableBakedModel(bakedBaseModel, straightModelByAxis, supportModelByFace);
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> modelGetter, final IGeometryBakingContext context) {
        baseModel.resolveParents(modelGetter);
    }

    private static ModelState rotated(final ModelState modelTransform, final ModelState rotation) {
        return new SimpleModelState(modelTransform.getRotation().compose(rotation.getRotation()), modelTransform.isUvLocked());
    }
}
