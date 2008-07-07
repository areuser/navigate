package nl.ucan.navigate;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.WrapDynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

public class GenericTypeUtil {
    public static Class<?>[] getActualTypeArguments(String property,Class clasz) throws IntrospectionException {
		List<Class<?>> types = new ArrayList<Class<?>>();
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(property, clasz);
		Method method = propertyDescriptor.getReadMethod();
		ParameterizedType genericReturnType = (ParameterizedType)method.getGenericReturnType();
		Type[] actualTypeArguments = genericReturnType.getActualTypeArguments();
        for ( int i=0 ; i < actualTypeArguments.length ; i ++) {
        	Class<?> type = (Class<?>)(actualTypeArguments[i]); // could be class or interface
        	types.add(type);
        }
        return (Class<?>[])types.toArray(new Class<?>[types.size()]);
	}
    public static boolean isEmpty(Object bean,String[] exclude) {
        boolean isEmpty = true;
        //
        Collection excludes = new ArrayList(( exclude == null ? new ArrayList() : Arrays.asList(exclude) ));
        excludes.add("class"); // class is a standard property that is being excluded
        DynaBean dynaBean = new WrapDynaBean(bean);
        DynaClass dynaClass = dynaBean.getDynaClass();
        DynaProperty[] dynaProperties = dynaClass.getDynaProperties();
        for ( DynaProperty property : dynaProperties ){
            if ( excludes.contains(property.getName())) continue;
            if ( property.isIndexed() ) {
                if ( property.getClass().isArray() ) {
                    Object[] value = (Object[])dynaBean.get(property.getName());
                    isEmpty &= ( value == null || value.length == 0 );
                } else {
                    Collection value = (Collection)dynaBean.get(property.getName());
                    isEmpty &= ( value == null || value.size() == 0 );
                }
            } else if ( property.isMapped()) {
                Map value = (Map)dynaBean.get(property.getName());
                isEmpty &= ( value == null || value.size() == 0 );
            } else {
                Object value = dynaBean.get(property.getName());
                isEmpty &= ( value == null );
            }
        }
        //
        return isEmpty;
    }    
}
