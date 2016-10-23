package com.onmobile.apps.ringbacktones.v2.converter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class LowercaseEnumTypeAdapterFactory implements TypeAdapterFactory {
       
       private boolean m_caseInsensitiveRead = false;
       
       public LowercaseEnumTypeAdapterFactory() {
              super();
       }
       
       public LowercaseEnumTypeAdapterFactory(boolean caseInsensitiveRead) {
              super();
              m_caseInsensitiveRead = caseInsensitiveRead;
       }
       
       public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
              @SuppressWarnings("unchecked")
              Class<T> rawType = (Class<T>) type.getRawType();
              if (!rawType.isEnum()) {
                     return null;
              }
              
              final Map<String, T> lowercaseToConstant = new HashMap<String, T>();
              for (T constant : rawType.getEnumConstants()) {
                     lowercaseToConstant.put(toLowercase(constant), constant);
              }
              
              return new TypeAdapter<T>() {
                     @Override
                     public void write(JsonWriter out, T value) throws IOException {
                           if (value == null) {
                                  out.nullValue();
                           } else {
                                  out.value(toLowercase(value));
                           }
                     }
                     
                     @Override
                     public T read(JsonReader reader) throws IOException {
                           if (reader.peek() == JsonToken.NULL) {
                                  reader.nextNull();
                                  return null;
                           } else {
                                  String nextString = reader.nextString();
                                  if (m_caseInsensitiveRead) {
                                         nextString = nextString.toLowerCase();
                                  }
                                  return lowercaseToConstant.get(nextString);
                           }
                     }
              };
       }
       
       private String toLowercase(Object o) {
              return o.toString().toLowerCase(Locale.US);
       }
}
