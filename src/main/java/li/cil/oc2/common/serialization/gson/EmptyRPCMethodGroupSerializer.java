/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.serialization.gson;

import com.google.gson.*;
import li.cil.oc2.common.bus.RPCDeviceBusAdapter;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Objects;

public final class EmptyRPCMethodGroupSerializer implements JsonSerializer<RPCDeviceBusAdapter.EmptyMethodGroup> {
    @Override
    public JsonElement serialize(@Nullable final RPCDeviceBusAdapter.EmptyMethodGroup methodGroup, @Nullable final Type typeOfMethodGroup, @Nullable final JsonSerializationContext context) {
        final RPCDeviceBusAdapter.EmptyMethodGroup actualMethodGroup = Objects.requireNonNull(methodGroup);
        final JsonObject parameterJson = new JsonObject();
        parameterJson.addProperty("name", "...");

        final JsonArray parametersJson = new JsonArray();
        parametersJson.add(parameterJson);

        final JsonObject methodGroupJson = new JsonObject();
        methodGroupJson.addProperty("name", actualMethodGroup.name());
        methodGroupJson.add("parameters", parametersJson);

        return methodGroupJson;
    }
}
