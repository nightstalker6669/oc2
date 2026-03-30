/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.serialization.gson;

import com.google.gson.*;
import li.cil.oc2.common.bus.RPCDeviceBusAdapter;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.UUID;

public final class MethodInvocationJsonDeserializer implements JsonDeserializer<RPCDeviceBusAdapter.MethodInvocation> {
    @Override
    public RPCDeviceBusAdapter.MethodInvocation deserialize(@Nullable final JsonElement json, @Nullable final Type typeOfT, @Nullable final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = Objects.requireNonNull(json).getAsJsonObject();
        final JsonDeserializationContext actualContext = Objects.requireNonNull(context);
        final UUID deviceId = actualContext.deserialize(jsonObject.get("deviceId"), UUID.class);
        final String methodName = jsonObject.get("name").getAsString();
        final JsonElement parameters = jsonObject.get("parameters");
        return new RPCDeviceBusAdapter.MethodInvocation(deviceId, methodName, parameters != null && parameters.isJsonArray() ? parameters.getAsJsonArray() : new JsonArray());
    }
}
