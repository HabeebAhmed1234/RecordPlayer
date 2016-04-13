package com.cromiumapps.musicplayer.gson;

import android.net.Uri;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by Habeeb Ahmed on 7/29/2015.
 */
public class UriTypeAdapter implements JsonDeserializer<Uri>, JsonSerializer<Uri> {

    @Override
    public Uri deserialize(final JsonElement src, final Type srcType,final JsonDeserializationContext context) throws JsonParseException {
        if (src == null || src.isJsonNull()) return null;
        return Uri.parse(src.toString());
    }

    @Override
    public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
        return src == null ? null : new JsonPrimitive(src.toString());
    }
}