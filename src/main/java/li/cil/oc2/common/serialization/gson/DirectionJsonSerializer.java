/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.serialization.gson;

import com.google.gson.*;
import net.minecraft.core.Direction;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public final class DirectionJsonSerializer implements JsonDeserializer<Direction>, JsonSerializer<Direction> {
    @Nullable
    @Override
    public Direction deserialize(@Nullable final JsonElement json, @Nullable final Type typeOfT, @Nullable final JsonDeserializationContext context) throws JsonParseException {
        if (json == null) {
            return null;
        }
        if (!json.isJsonPrimitive()) {
            return null;
        }

        final JsonPrimitive primitive = json.getAsJsonPrimitive();

        if (primitive.isString()) {
            final Direction direction = Direction.byName(json.getAsString());
            if (direction != null) {
                return direction;
            }
        }

        if (primitive.isNumber()) {
            return Direction.from3DDataValue(json.getAsInt());
        }

        return null;
    }

    @Override
    public JsonElement serialize(@Nullable final Direction src, @Nullable final Type typeOfSrc, @Nullable final JsonSerializationContext context) {
        return src != null ? new JsonPrimitive(src.toString()) : JsonNull.INSTANCE;
    }
}
