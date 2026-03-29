package li.cil.oc2.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

public final class GuiComponent {
    private GuiComponent() {
    }

    public static void blit(final PoseStack stack, final int x, final int y, final int u, final int v, final int width, final int height, final int textureWidth, final int textureHeight) {
        final float u0 = u / (float) textureWidth;
        final float u1 = (u + width) / (float) textureWidth;
        final float v0 = v / (float) textureHeight;
        final float v1 = (v + height) / (float) textureHeight;
        final Matrix4f pose = stack.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        final BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.addVertex(pose, x, y + height, 0).setUv(u0, v1);
        builder.addVertex(pose, x + width, y + height, 0).setUv(u1, v1);
        builder.addVertex(pose, x + width, y, 0).setUv(u1, v0);
        builder.addVertex(pose, x, y, 0).setUv(u0, v0);
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }
}
