/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor("mainCamera")
    Camera oc2$getMainCamera();

    @Accessor("mainCamera")
    void oc2$setMainCamera(Camera camera);
}
