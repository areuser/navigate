package nl.ucan.navigate;

import org.apache.commons.beanutils.*;
import org.apache.commons.beanutils.expression.Resolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.*;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.util.*;


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
 * since  : 0.2.5
  */

public class NestedPath {
    private static Log log = LogFactory.getLog(NestedPath.class);
    private PropertyUtilsBean pub;
    private PropertyInstance propertyInstance;
    private PropertyValue propertyValue;


    public static void setNested(char nested) {
        ResolverImpl.setNested(nested);
    }

    public static void setMappedStart(char mappedStart) {
        ResolverImpl.setMappedStart(mappedStart);
    }

    public static void setMappedEnd(char mappedEnd) {
        ResolverImpl.setMappedEnd(mappedEnd);
    }

    public static void setIndexedStart(char indexedStart) {
        ResolverImpl.setIndexedStart(indexedStart);
    }

    public static void setIndexedEnd(char indexedEnd) {
        ResolverImpl.setIndexedEnd(indexedEnd);
    }
    public static NestedPath getInstance(PropertyInstance propertyInstance) {
        return new NestedPath(propertyInstance,null);
    }
    public static NestedPath getInstance(PropertyValue propertyValue) {
        return new NestedPath(null,propertyValue);
    }
    public static NestedPath getInstance(PropertyInstance propertyInstance,PropertyValue propertyValue) {
        return new NestedPath(propertyInstance,propertyValue);
    }
    public static NestedPath getInstance() {
        return new NestedPath(null,null);
    }

    private NestedPath(PropertyInstance propertyInstance, PropertyValue propertyValue) {
        if ( propertyInstance == null ) {
            propertyInstance = new PropertyInstance() {
                public Object indexed(Object bean, String property, int index, Object value){
                    log.info("created indexed property "+property+" at "+index+" of bean "+bean+" and will be set to "+value);
                    return value;
                }
                public Object simple(Object bean, String property, Object value){
                    log.info("created simple property "+property+" of bean "+bean+" and will be set to "+value);
                    return value;
                }
            };
        }
        if ( propertyValue == null ) {
            propertyValue = new PropertyValue() {
                public Object indexed(Object bean, String property, int index, Object value){
                    log.info("value of indexed property "+property+" at "+index+" of bean "+bean+" will be set to "+value);
                    return value;
                }
                public Object mapped(Object bean, String property, Object key, Object value){
                    log.info("value of mapped property "+property+" at "+key+" of bean "+bean+" will be set to "+value);
                    return value;
                }
                public Object simple(Object bean, String property, Object value){
                    log.info("value of simple property "+property+" of bean "+bean+" will be set to "+value);
                    return value;
                }
            };
        }
        this.pub = BeanUtilsBean.getInstance().getPropertyUtils();
        this.pub.setResolver(new ResolverImpl());
        this.propertyInstance = propertyInstance;
        this.propertyValue = propertyValue;
    }

    private NestedPath(PropertyValue propertyValue) {
        if ( propertyValue == null ) {
            propertyValue = new PropertyValue() {
                public Object indexed(Object bean, String property, int index, Object value){
                    log.info("value of indexed property "+property+" at "+index+" of bean "+bean+" will be set to "+value);
                    return value;
                }
                public Object mapped(Object bean, String property, Object key, Object value){
                    log.info("value of mapped property "+property+" at "+key+" of bean "+bean+" will be set to "+value);
                    return value;
                }
                public Object simple(Object bean, String property, Object value){
                    log.info("value of simple property "+property+" of bean "+bean+" will be set to "+value);
                    return value;
                }
            };
        }
        this.pub = BeanUtilsBean.getInstance().getPropertyUtils();
        this.pub.setResolver(new ResolverImpl());
        this.propertyValue = propertyValue;
    }

    public void populate(Object bean, Map<String,Object> pathValues) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,InstantiationException,IntrospectionException {
        Iterator<Map.Entry<String,Object>> itPathValues = pathValues.entrySet().iterator();
        while(itPathValues.hasNext()) {
            Map.Entry<String,Object> entry = (Map.Entry)itPathValues.next();
            String path = entry.getKey();
            Object value = entry.getValue();
            bean = acquireBean(bean,path);
            setProperty(bean,path,value);
        }
    }
    public Map extract(Object bean, Set<String> paths) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,InstantiationException,IntrospectionException  {
        Map extracted = new HashMap();
        for(String path : paths) {
            bean = acquireBean(bean,path);
            Object value = pub.getProperty(bean,path);
            extracted.put(path, value);
        }
        return extracted;
    }

    private void setProperty(Object bean, String path, Object value)
        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        Object   drilldownBean = bean;
        String   drilldownPath = new String (path);
        Resolver resolver  = pub.getResolver();
        for(; resolver.hasNested(drilldownPath); drilldownPath = resolver.remove(drilldownPath))
        {
            drilldownBean = pub.getProperty(drilldownBean, resolver.next(drilldownPath));
        }
        String prop = resolver.getProperty(drilldownPath);
        if(resolver.isMapped(drilldownPath)) {
            //
            // mapped property
            String key = resolver.getKey(drilldownPath);
            value = propertyValue.mapped(drilldownBean,prop,key,value);
        } else if(resolver.isIndexed(drilldownPath)) {
            //
           // indexed property
            int idx = resolver.getIndex(drilldownPath);
            value = propertyValue.indexed(drilldownBean,prop,idx,value);
        } else {
            //
            // simple property
            value = propertyValue.simple(drilldownBean,drilldownPath,value);
        }
        pub.setProperty(bean,path,value);
    }

    private Object acquireBean(Object bean, String path) throws IllegalAccessException,InvocationTargetException,InstantiationException,IntrospectionException,NoSuchMethodException {
        Object instance = bean;
        pub.copyProperties(instance,bean);
        DynaBean dynaBean = getDynaBean(instance);
        Resolver resolver = pub.getResolver();
        String masterPath = "" ;
        for(; resolver.hasNested(path); path = resolver.remove(path))
        {
            masterPath += resolver.next(path);
            String prop = resolver.getProperty(path);
            if ( resolver.isIndexed(path) ) {
                int idx = resolver.getIndex(path);
                Object tmp = pub.getIndexedProperty(dynaBean,prop,idx);
                if ( tmp == null ) {
                    instance = createIndexedInstance(instance.getClass(),prop);
                    pub.setIndexedProperty(dynaBean,prop,idx,instance);
                    pub.setProperty(bean,masterPath,instance);
                }
                else instance = tmp;
            }  else {
                instance = pub.getProperty(instance,prop);
                if ( instance == null ) {
                    instance = pub.getProperty(dynaBean,prop);
                    pub.setProperty(bean,masterPath,instance);
                }
            }
            dynaBean = getDynaBean(instance);
            masterPath += ResolverImpl.getNested();
         }
        return bean;
    }

    private DynaBean getDynaBean(Object instance) throws IllegalAccessException,InvocationTargetException,NoSuchMethodException {
        WrapDynaClass dynaClass = WrapDynaClass.createDynaClass(instance.getClass());
        LazyDynaBean lazyBean = new LazyDynaBean(dynaClass);
        PropertyUtils.copyProperties(lazyBean,instance);
        return lazyBean;
    }
    private Object createIndexedInstance(Class clasz, String prop) throws IntrospectionException,IllegalAccessException,InvocationTargetException,InstantiationException  {
        Class<?>[] type = getActualTypeArguments(prop,clasz);
        Constructor constructor = ConstructorUtils.getAccessibleConstructor(type[0], new Class[]{});
        return constructor.newInstance();
    }
    private Class<?>[] getActualTypeArguments(String property,Class clasz) throws IntrospectionException {
		List<Class<?>> types = new ArrayList<Class<?>>();
		PropertyDescriptor propertyDescriptor = new PropertyDescriptor(property, clasz);
		Method method = propertyDescriptor.getReadMethod();
		ParameterizedType genericReturnType = (ParameterizedType)method.getGenericReturnType();
		Type[] actualTypeArguments = genericReturnType.getActualTypeArguments();
        for ( int i=0 ; i < actualTypeArguments.length ; i ++) {
        	Class<?> type = (Class<?>)(actualTypeArguments[i]);
        	types.add(type);
        }
        return types.toArray(new Class<?>[types.size()]);
	}
}
