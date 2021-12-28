/*
 * Copyright [2021-2021] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.cosid.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import me.ahoo.cosid.IdConverter;
import me.ahoo.cosid.converter.Radix62IdConverter;
import me.ahoo.cosid.converter.ToStringIdConverter;

import java.io.IOException;
import java.util.Objects;

/**
 * @author ahoo wang
 */
public class AsStringSerializer extends JsonSerializer<Long> implements ContextualSerializer {

    private static final AsStringSerializer TO_STRING = new AsStringSerializer();

    private static final AsStringSerializer DEFAULT_RADIX = new AsStringSerializer(Radix62IdConverter.INSTANCE);
    private static final AsStringSerializer DEFAULT_RADIX_PAD_START = new AsStringSerializer(Radix62IdConverter.PAD_START);

    private final IdConverter converter;

    public AsStringSerializer() {
        this(ToStringIdConverter.INSTANCE);
    }

    public AsStringSerializer(IdConverter converter) {
        this.converter = converter;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        AsString asString = property.getAnnotation(AsString.class);
        if (Objects.isNull(asString)) {
            return TO_STRING;
        }
        if (AsString.Type.TO_STRING.equals(asString.value())) {
            return TO_STRING;
        }

        if (Radix62IdConverter.MAX_CHAR_SIZE != asString.radixCharSize()) {
            IdConverter idConverter = new Radix62IdConverter(asString.radixPadStart(), asString.radixCharSize());
            return new AsStringSerializer(idConverter);
        }

        return asString.radixPadStart() ? DEFAULT_RADIX_PAD_START : DEFAULT_RADIX;
    }

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (Objects.isNull(value)) {
            gen.writeNull();
        } else {
            String valueStr = converter.asString(value);
            gen.writeString(valueStr);
        }
    }
}
