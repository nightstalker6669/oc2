/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.serialization.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public final class UnsignedByteArrayJsonSerializer implements JsonSerializer<byte[]> {
    @Override
    public JsonElement serialize(@Nullable final byte[] src, @Nullable final Type typeOfSrc, @Nullable final JsonSerializationContext context) {
        final JsonArray json = new JsonArray();
        if (src == null) {
            return json;
        }
        for (final byte b : src) {
            json.add(b & 0xFF);
        }
        return json;
    }
}
