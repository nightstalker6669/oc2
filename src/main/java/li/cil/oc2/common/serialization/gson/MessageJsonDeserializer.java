/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.serialization.gson;

import com.google.gson.*;
import li.cil.oc2.common.bus.RPCDeviceBusAdapter;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.UUID;

public final class MessageJsonDeserializer implements JsonDeserializer<RPCDeviceBusAdapter.Message> {
    @Override
    public RPCDeviceBusAdapter.Message deserialize(@Nullable final JsonElement json, @Nullable final Type typeOfT, @Nullable final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = Objects.requireNonNull(json).getAsJsonObject();
        final JsonDeserializationContext actualContext = Objects.requireNonNull(context);
        final String messageType = jsonObject.get("type").getAsString();
        final Object messageData = switch (messageType) {
            case RPCDeviceBusAdapter.Message.MESSAGE_TYPE_LIST -> null;
            case RPCDeviceBusAdapter.Message.MESSAGE_TYPE_METHODS -> UUID.fromString(jsonObject.getAsJsonPrimitive("data").getAsString());
            case RPCDeviceBusAdapter.Message.MESSAGE_TYPE_INVOKE_METHOD -> actualContext.deserialize(jsonObject.getAsJsonObject("data"), RPCDeviceBusAdapter.MethodInvocation.class);
            default -> throw new JsonParseException(RPCDeviceBusAdapter.ERROR_UNKNOWN_MESSAGE_TYPE);
        };

        return new RPCDeviceBusAdapter.Message(messageType, messageData);
    }
}
