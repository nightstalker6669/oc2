/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.serialization.ceres;

import li.cil.ceres.api.DeserializationVisitor;
import li.cil.ceres.api.SerializationException;
import li.cil.ceres.api.SerializationVisitor;
import li.cil.ceres.api.Serializer;
import li.cil.oc2.common.util.ItemStackUtils;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.Objects;

public final class TextComponentSerializer implements Serializer<Component> {
    @Override
    public void serialize(@Nullable final SerializationVisitor visitor, @Nullable final Class<Component> type, @Nullable final Object value) throws SerializationException {
        final String json = Component.Serializer.toJson((Component) Objects.requireNonNull(value), ItemStackUtils.getDefaultRegistries());
        Objects.requireNonNull(visitor).putObject("value", String.class, json);
    }

    @Nullable
    @Override
    public Component deserialize(@Nullable final DeserializationVisitor visitor, @Nullable final Class<Component> type, @Nullable final Object value) throws SerializationException {
        final DeserializationVisitor actualVisitor = Objects.requireNonNull(visitor);
        if (!actualVisitor.exists("value")) {
            return (Component) value;
        }

        final String json = (String) actualVisitor.getObject("value", String.class, null);
        if (json == null) {
            return (Component) value;
        }

        return Component.Serializer.fromJson(json, ItemStackUtils.getDefaultRegistries());
    }
}
