package com.google.gson;

import def.js.JSON;
import jsweet.util.Lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Gson implements JsonSerializationContext, JsonDeserializationContext {
    private final Map<Class<?>, JsonSerializer<?>> serializers;
    private final Map<Class<?>, JsonDeserializer<?>> deserializers;
    Gson(Map<Class<?>, JsonSerializer<?>> serializers, Map<Class<?>, JsonDeserializer<?>> deserializers) {
        this.serializers = serializers;
        this.deserializers = deserializers;
    }

    public String toJson(Object src) {
        return serialize(src).toString();
    }

    public <T> T fromJson(String json, Class<T> classOfT) {
        return deserialize(new JsonElement(JSON.parse(json)), classOfT);
    }

    @Override
    public JsonElement serialize(Object src) {
        return serialize(src, src.getClass());
    }

    @Override
    public JsonElement serialize(Object src, Class<?> typeOfSrc) {
        JsonSerializer<?> jsonSerializer = serializers.get(typeOfSrc);
        if (jsonSerializer != null) {
            return jsonSerializer.serialize(Lang.any(src), Lang.any(typeOfSrc), this);
        }

        if (src instanceof JsonElement) {
            return (JsonElement) src;
        }

        return new JsonElement(src);
    }

    @Override
    public <T> T deserialize(JsonElement je, Class<T> classOfT) throws JsonParseException {
        JsonDeserializer<?> jsonDeserializer = deserializers.get(classOfT);
        if (jsonDeserializer != null)
            return Lang.any(jsonDeserializer.deserialize(je, Lang.any(classOfT), this));

        if (Object.class == classOfT)
            return Lang.any(je.o);
        if (String.class == classOfT)
            return Lang.any(je.o.toString());

        if (JsonElement.class == classOfT) {
            return Lang.any(je);
        }
        if (JsonArray.class == classOfT) {
            return Lang.any(je.getAsJsonArray());
        }
        if (JsonNull.class == classOfT) {
            if (je.o == null)
                return Lang.any(JsonNull.INSTANCE);

            throw new IllegalStateException("Not a JSON null: " + je.toString());
        }
        if (JsonObject.class == classOfT) {
            return Lang.any(je.getAsJsonObject());
        }
        if (JsonPrimitive.class == classOfT) {
            return Lang.any(je.getAsJsonPrimitive());
        }

        if (List.class == classOfT || ArrayList.class == classOfT) {
            JsonArray ja = je.getAsJsonArray();
            List<Object> res = new ArrayList<>(ja.size());
            for (int i = 0; i < ja.size(); ++i)
                res.add(ja.get(i));
            return Lang.any(res);
        }

        if (Map.class == classOfT || HashMap.class == classOfT) {
            JsonObject ja = je.getAsJsonObject();
            HashMap<String, Object> res = new HashMap<>();
            for (String key : ja.keySet())
                res.put(key, ja.get(key));
            return Lang.any(res);
        }

        if (Number.class == classOfT ||
                Float.class == classOfT ||
                Integer.class == classOfT ||
                Double.class == classOfT ||
                Long.class == classOfT)
            return Lang.any(je.getAsJsonPrimitive().getAsDouble());

        if (Boolean.class == classOfT)
            return Lang.any(je.getAsJsonPrimitive().getAsBoolean());

        return Lang.any(je.o);
    }
}
