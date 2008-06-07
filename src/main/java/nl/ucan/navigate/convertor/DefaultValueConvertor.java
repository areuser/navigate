package nl.ucan.navigate.convertor;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.FloatConverter;
import org.apache.commons.beanutils.converters.SqlDateConverter;

import java.util.Date;
/*
 * Copyright 2007-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * author : Arnold Reuser
 * since  : 0.1
  */

public class DefaultValueConvertor implements ValueConvertor {
    private static ConvertUtilsBean valueConvertor = new ConvertUtilsBean();
    static {
        valueConvertor.register(new LongConverter(null), Long.class);
        valueConvertor.register(new LongConverter(null), Long.TYPE);
        valueConvertor.register(new IntegerConverter(null), Integer.TYPE);
        valueConvertor.register(new IntegerConverter(null), Integer.class);
        valueConvertor.register(new FloatConverter(null), Float.TYPE);
        valueConvertor.register(new FloatConverter(null), Float.class);
        valueConvertor.register(new SqlDateConverter(null), Date.class);
        valueConvertor.register(new StrictlyNonBlankStringConverter(null), String.class);
    }
    public Object evaluate(String xpath, Object value) {
        return valueConvertor.convert(value);
    }
    public Object evaluate(String xpath, Object value,Class clasz) {
        return valueConvertor.convert(value,clasz);        
    }
}
