/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.serialization.ceres;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import li.cil.ceres.api.DeserializationVisitor;
import li.cil.ceres.api.SerializationException;
import li.cil.ceres.api.SerializationVisitor;
import li.cil.ceres.api.Serializer;

import javax.annotation.Nullable;
import java.util.Objects;

public final class JsonArraySerializer implements Serializer<JsonArray> {
    @Override
    public void serialize(@Nullable final SerializationVisitor visitor, @Nullable final Class<JsonArray> type, @Nullable final Object value) throws SerializationException {
        final JsonArray jsonArray = (JsonArray) Objects.requireNonNull(value);
        Objects.requireNonNull(visitor).putObject("value", String.class, jsonArray.toString());
    }

    @Nullable
    @Override
    public JsonArray deserialize(@Nullable final DeserializationVisitor visitor, @Nullable final Class<JsonArray> type, @Nullable final Object value) throws SerializationException {
        final DeserializationVisitor actualVisitor = Objects.requireNonNull(visitor);
        JsonArray array = (JsonArray) value;
        if (!actualVisitor.exists("value")) {
            return array;
        }

        final String jsonString = (String) actualVisitor.getObject("value", String.class, null);
        if (jsonString == null) {
            return null;
        }

        if (array == null) {
            array = new JsonArray();
        }

        while (array.size() > 0) {
            array.remove(array.size() - 1);
        }

        array.addAll(JsonParser.parseString(jsonString).getAsJsonArray());

        return array;
    }
}
