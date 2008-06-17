package nl.ucan.navigate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPropertyPointer;
import org.apache.commons.collections.MapUtils;

import java.util.*;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import nl.ucan.navigate.convertor.DefaultValueConvertor;
import nl.ucan.navigate.convertor.ValueConvertor;

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
public class Navigator {
        private static Log log = LogFactory.getLog(Navigator.class);
        public static Object populate(Object bean, Object[][] expPathValues) throws IntrospectionException {
            return populate(bean,MapUtils.putAll(new HashMap(),expPathValues));
        }

        public static Object populate(Object bean, Map expPathValues) throws IntrospectionException {
            return populate(bean,expPathValues, new DefaultValueConvertor());
        }
        public static Object populate(Object bean,Map expPathValues, ValueConvertor valueConvertor) throws IntrospectionException {
            JXPathContext context = JXPathContext.newContext(bean);
            context.setFactory(new BeanPropertyFactory());
            context.setLenient(true); //suppress JPathException for inexistent paths

            // 1. Navigator and visit xpath node and create required path of beans and collections if value != null
            Iterator itExpPathValue = expPathValues.entrySet().iterator();
            while(itExpPathValue.hasNext()) {
                Map.Entry entry = (Map.Entry)itExpPathValue.next();
                String expression = (String)entry.getKey();
                Object value = entry.getValue();
                NodePointer np = (NodePointer)context.getPointer(expression);
                value = valueConvertor.evaluate(expression,value);
                 if (!np.isActual() && value != null ) {
                     context.createPath(expression);
                 }
            }
            // 2. Set value xpath node; ignore xpath nodes of inexistent paths
            itExpPathValue = expPathValues.entrySet().iterator();
            while(itExpPathValue.hasNext()) {
                Map.Entry entry = (Map.Entry)itExpPathValue.next();
                String expression = (String)entry.getKey();
                Object value = entry.getValue();
                NodePointer np = (NodePointer)context.getPointer(expression);
                if ( np.isActual()) {
                    Class<?> clasz = getActualTypeArgument(np);
                    value = valueConvertor.evaluate(expression,value,clasz);
                    np.setValue(value);
                }
            }

            return bean;
        }

        public static Map extract(Object bean, Object[] expressions) throws IntrospectionException {
            return extract(bean,new HashSet(Arrays.asList(expressions)));
        }

        public static Map extract(Object bean, Set<String> expressions) {
            Map extracted = new HashMap();
            JXPathContext context = JXPathContext.newContext(bean);
            context.setFactory(new BeanPropertyFactory());
            context.setLenient(true); //suppress JPathException and return null for inexistent paths
            for(String expression : expressions) {
                NodePointer np = (NodePointer)context.getPointer(expression);
                if (np.isActual()) {
                    extracted.put(expression, np.getValue());
                } else {
                    extracted.put(expression, null);
                }
            }
            return extracted;
        }
        private static Class<?> getActualTypeArgument(Pointer p) throws IntrospectionException {
            Class<?> clasz = null;
            if ( p instanceof BeanPropertyPointer) {
                BeanPropertyPointer bp = (BeanPropertyPointer)p;
                Object baseValue = bp.getBaseValue();
                if ( baseValue != null && baseValue instanceof Collection ) {
                    Class<?>[] clasza = GenericTypeUtil.getActualTypeArguments(bp.getPropertyName(),bp.getBean().getClass());
                    if ( clasza.length > 0 ) clasz = clasza[0];
                } else if ( baseValue != null && baseValue instanceof Map ) {
                    Class<?>[] clasza = GenericTypeUtil.getActualTypeArguments(bp.getPropertyName(),bp.getBean().getClass());
                    if ( clasza.length > 1 ) clasz = clasza[1];
                } else {
                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor(bp.getPropertyName(), bp.getBean().getClass());
                    Method method = propertyDescriptor.getReadMethod();
                    clasz = method.getReturnType();
                }
                return clasz;
            } else {
                NodePointer np = (NodePointer)p;
                if ( np != null ) return getActualTypeArgument(np.getImmediateParentPointer());
                else return null;
            }
        }


        private static class BeanPropertyFactory extends AbstractFactory {
             public boolean createObject(JXPathContext context,
                Pointer pointer, Object parent, String name, int index){
                 // The parameters may describe a collection element or an individual object.
                 // It is up to the factory to infer which one it is. If it is a collection, the factory should check if the collection exists.
                 // If not, it should create the collection. Then it should create the index'th element of the collection and return true.
                try {
                    if ( pointer instanceof BeanPropertyPointer) {
                        BeanPropertyPointer beanPropertyPointer = (BeanPropertyPointer)pointer;
                        if (beanPropertyPointer.isCollection()) {
                            // a property representing a collection/map should already be initialized
                            // therefore do not initialize the collection/map itself
                            // but initialize the element of the collection at the specified index
                            // TODO currently only a List of beans is supported!
                            if ( beanPropertyPointer.getBaseValue() instanceof List) {
                                Class<?>[] clasz = GenericTypeUtil.getActualTypeArguments(name,parent.getClass());
                                List value = (List)beanPropertyPointer.getBaseValue();
                                while(value.size() < index+1) {
                                    Object instance = clasz[0].newInstance();
                                    value.add(instance);
                                }
                                value.set(index, clasz[0].newInstance());
                                BeanUtils.setProperty(parent, name, value);
                            }
                        } else {
                            // a property representing a bean could be null
                            // therefore initialize property
                            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(name, parent.getClass());
                            Constructor constructor = ConstructorUtils.getAccessibleConstructor(propertyDescriptor.getPropertyType(), new Class[]{});
                            if ( constructor != null ) {
                                Object instance = constructor.newInstance();
                                BeanUtils.setProperty(parent, name,instance );
                            }
                        }
                    }
                    return true;
                } catch(Exception e) {
                    e.printStackTrace();
                    return false;
                }
         }
        }
    }
