/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.serialization.gson;

import com.google.gson.*;
import li.cil.oc2.api.bus.device.rpc.RPCMethod;
import li.cil.oc2.api.bus.device.rpc.RPCParameter;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Objects;

public final class RPCMethodJsonSerializer implements JsonSerializer<RPCMethod> {
    @Override
    public JsonElement serialize(@Nullable final RPCMethod method, @Nullable final Type typeOfMethod, @Nullable final JsonSerializationContext context) {
        final RPCMethod actualMethod = Objects.requireNonNull(method);
        final JsonObject methodJson = new JsonObject();
        methodJson.addProperty("name", actualMethod.getName());
        methodJson.addProperty("returnType", actualMethod.getReturnType().getSimpleName());

        actualMethod.getDescription().ifPresent(s -> methodJson.addProperty("description", s));
        actualMethod.getReturnValueDescription().ifPresent(s -> methodJson.addProperty("returnValueDescription", s));

        final JsonArray parametersJson = new JsonArray();
        methodJson.add("parameters", parametersJson);

        final RPCParameter[] parameters = actualMethod.getParameters();
        for (final RPCParameter parameter : parameters) {
            final JsonObject parameterJson = new JsonObject();

            parameter.getName().ifPresent(s -> parameterJson.addProperty("name", s));
            parameter.getDescription().ifPresent(s -> parameterJson.addProperty("description", s));

            final Class<?> type = parameter.getType();
            parameterJson.addProperty("type", type.getSimpleName());

            parametersJson.add(parameterJson);
        }

        return methodJson;
    }
}
