package com.telefonica.iot.cygnus.management;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.regex.Pattern;

public class PatternTypeAdapter extends TypeAdapter<Pattern> {

    @Override
    public void write(JsonWriter out, Pattern pattern) throws IOException {
        if (pattern == null) {
            out.nullValue();
            return;
        }
        out.value(pattern.pattern());
    }

    @Override
    public Pattern read(JsonReader in) throws IOException {
        String patternString = in.nextString();
        return Pattern.compile(patternString);
    }
}
