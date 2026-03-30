/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.serialization.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import li.cil.oc2.common.bus.RPCDeviceBusAdapter;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Objects;

public final class RPCDeviceWithIdentifierJsonSerializer implements JsonSerializer<RPCDeviceBusAdapter.RPCDeviceWithIdentifier> {
    @Override
    public JsonElement serialize(@Nullable final RPCDeviceBusAdapter.RPCDeviceWithIdentifier src, @Nullable final Type typeOfSrc, @Nullable final JsonSerializationContext context) {
        final RPCDeviceBusAdapter.RPCDeviceWithIdentifier actualSource = Objects.requireNonNull(src);
        final JsonSerializationContext actualContext = Objects.requireNonNull(context);
        final JsonObject deviceJson = new JsonObject();
        deviceJson.add("deviceId", actualContext.serialize(actualSource.identifier()));
        deviceJson.add("typeNames", actualContext.serialize(actualSource.device().getTypeNames()));

        return deviceJson;
    }
}
