/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.util;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Provides a safe wrapper around any kind of {@link VertexConsumer} implementation so that
 * call-chaining still works as expected.
 * <p>
 * This is primarily a workaround for the broken chaining in sprite-expanding consumers that may
 * return the wrapped consumer instead of themselves.
 */
public record ChainableVertexConsumer(VertexConsumer inner) implements VertexConsumer {
    @Override
    public VertexConsumer addVertex(final float x, final float y, final float z) {
        inner.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(final int r, final int g, final int b, final int a) {
        inner.setColor(r, g, b, a);
        return this;
    }

    @Override
    public VertexConsumer setUv(final float u, final float v) {
        inner.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(final int u, final int v) {
        inner.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(final int u, final int v) {
        inner.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(final float x, final float y, final float z) {
        inner.setNormal(x, y, z);
        return this;
    }
}
