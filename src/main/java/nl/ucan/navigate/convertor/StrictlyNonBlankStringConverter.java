package nl.ucan.navigate.convertor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.beanutils.converters.AbstractConverter;
import org.apache.commons.lang.StringUtils;
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

public class StrictlyNonBlankStringConverter extends AbstractConverter {
    private static Log log = LogFactory.getLog(StrictlyNonBlankStringConverter.class);
    public StrictlyNonBlankStringConverter() {
        super(String.class);
    }
    public StrictlyNonBlankStringConverter(Object defaultValue) {
        super(String.class, defaultValue);
    }

    protected String convertToString(Object value) throws Throwable {
        return (String)convertToType(String.class,value);
    }
    protected Object convertToType(Class type, Object value) throws Throwable {
        if ( value == null ) {
            return null;
        } else {
            String string = value.toString();
            // if a String is whitespace, empty ("") or null then return null
            if ( StringUtils.isBlank(string)) {
                log.debug("value is whitespace, empty or null ");
                log.debug("Converted to String value null ");
                return null;
            }
            else {
                log.debug("value is no whitespace, empty or null ");
                log.debug("Converted to String value "+string);
                return string;
            }
        }
    }
}

