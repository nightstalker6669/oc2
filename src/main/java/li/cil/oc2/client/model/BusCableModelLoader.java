/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public final class BusCableModelLoader implements IGeometryLoader<BusCableModel> {
    @Override
    public BusCableModel read(final JsonObject modelContents, final JsonDeserializationContext context) {
        return new BusCableModel(context.deserialize(modelContents, BlockModel.class));
    }
}
