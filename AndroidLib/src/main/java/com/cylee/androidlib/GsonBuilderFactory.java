package com.cylee.androidlib;

import com.google.jtm.Gson;
import com.google.jtm.GsonBuilder;
import com.google.jtm.TypeAdapter;
import com.google.jtm.TypeAdapterFactory;
import com.google.jtm.reflect.TypeToken;
import com.google.jtm.stream.JsonReader;
import com.google.jtm.stream.JsonToken;
import com.google.jtm.stream.JsonWriter;

import java.io.IOException;

/**
 * Created by fanxi on 13-12-9.
 *
 * json 格式处理工具集
 */
public class GsonBuilderFactory {
    private static volatile Gson instance;
    /**
     * 创建可解析泛型与boolean 类型转化的GsonBuilder
     * @return GsonBuilder
     * */
    public static Gson createBuilder(){
        if(instance == null){
            synchronized (GsonBuilderFactory.class){
                if(instance == null){
                    GsonBuilder builder = new GsonBuilder();
                    builder.registerTypeAdapter(Boolean.class, booleanAsIntAdapter);
                    builder.registerTypeAdapter(boolean.class, booleanAsIntAdapter);
                    builder.registerTypeAdapterFactory(new TypeAdapterFactory() {
                        @Override
                        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> tTypeToken) {
                            final Class<T> rawType = (Class<T>) tTypeToken.getRawType();
                            final T[] types = rawType.getEnumConstants();
                            if (rawType.isEnum()) {
                                return new TypeAdapter<T>() {
                                    public void write(JsonWriter out, T value) throws IOException {
                                        if (value == null) {
                                            out.nullValue();
                                        } else {
                                            int index = 0;
                                            for (int i = 0; i < types.length; i++) {
                                                if (types[i].toString().equals(value.toString())) {
                                                    index = i;
                                                    break;
                                                }
                                            }
                                            out.value(index);
                                        }
                                    }

                                    public T read(JsonReader reader) throws IOException {
                                        if (reader.peek() == JsonToken.NULL) {
                                            reader.nextNull();
                                            return null;
                                        } else {
                                            T result;
                                            try {
                                                result = types[reader.nextInt()];
                                            } catch (ArrayIndexOutOfBoundsException e) {
                                                // 枚举类型，第一个值作为默认值
                                                result = types[0];
                                            }

                                            return result;
                                        }
                                    }
                                };
                            }
                            return null;
                        }
                    });
                    instance = builder.create();
                }
            }
        }
        return instance;
    }

    private static final TypeAdapter<Boolean> booleanAsIntAdapter = new TypeAdapter<Boolean>() {
        @Override
        public void write(JsonWriter jsonWriter, Boolean aBoolean) throws IOException {
            if (null == aBoolean) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(aBoolean);
            }
        }

        @Override
        public Boolean read(JsonReader jsonReader) throws IOException {
            JsonToken peek = jsonReader.peek();
            switch (peek) {
                case BOOLEAN:
                    return jsonReader.nextBoolean();
                case NULL:
                    jsonReader.nextNull();
                    return null;
                case NUMBER:
                    return jsonReader.nextInt() != 0;
                case STRING:
                    return Boolean.parseBoolean(jsonReader.nextString());
                default:
                    throw new IllegalStateException("Expected BOOLEAN or NUMBER but was " + peek);
            }
        }
    };
}