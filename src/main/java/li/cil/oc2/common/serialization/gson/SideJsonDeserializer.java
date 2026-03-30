/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.serialization.gson;

import com.google.gson.*;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import li.cil.oc2.api.util.Side;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Objects;

public final class SideJsonDeserializer implements JsonDeserializer<Side> {
    @Override
    public Side deserialize(@Nullable final JsonElement json, @Nullable final Type typeOfT, @Nullable final JsonDeserializationContext context) throws JsonParseException {
        if (json == null) {
            throw new JsonParseException("Missing side payload.");
        }
        if (json.isJsonPrimitive()) {
            final JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
            if (jsonPrimitive.isNumber()) {
                return Side.values()[jsonPrimitive.getAsNumber().intValue()];
            }
        }

        return (Side) Objects.requireNonNull(TypeAdapters.ENUM_FACTORY.create(null, TypeToken.get(Objects.requireNonNull(typeOfT)))).fromJsonTree(json);
    }
}
