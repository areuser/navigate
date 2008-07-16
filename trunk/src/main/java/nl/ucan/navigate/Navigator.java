package nl.ucan.navigate;

import nl.ucan.navigate.convertor.ValueConvertor;

import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.expression.Resolver;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;/*
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
 * since  : 0.2.4
  */

public class Navigator {
    private static Log log = LogFactory.getLog(Navigator.class);
    private PropertyUtilsBean pub;
    private Property property;
    private boolean ignore = true;
    public static Navigator getInstance(Property property) {
        return new Navigator(property);
    }
    public static Navigator getInstance() {
        return getInstance(new Property() {
            public Object indexed(Object bean, String name, int index, Object value){
                log.debug("indexed property "+name+" at "+index+" of bean "+bean+" will be set to "+value);
                return value;
            }
            public Object mapped(Object bean, String name, Object key, Object value){
                log.debug("mapped property "+name+" at "+key+" of bean "+bean+" will be set to "+value);
                return value;
            }
            public Object simple(Object bean, String name, Object value){
                log.debug("simple property "+name+" of bean "+bean+" will be set to "+value);
                return value;
            }
        });
    }
    public Navigator silent(boolean ignore) {
        this.ignore = ignore;
        return this;
    }
    private Navigator(Property property) {
        this.pub = new PropertyUtilsBean();
        this.pub.setResolver(new ResolverImpl());
        this.property = property;       
    }


     private Object expandCollection(Object collection, int size) {
             if (collection == null) {
                 return null;
             }
             else if (collection.getClass().isArray()) {
                 Object bigger = Array.newInstance(collection.getClass().getComponentType(),size);
                 System.arraycopy(collection,0,bigger,0,Array.getLength(collection));
                 return bigger;
             }
             else if (collection instanceof Collection) {
                 while (((Collection) collection).size() < size) {
                     ((Collection) collection).add(null);
                 }
                 return collection;
             }
             else {
                 throw new IllegalStateException("Cannot turn "+collection.getClass().getName()+ " into a collection of size "+ size);
             }
    }

    private Object createIndexedInstance(Object bean, String prop) throws IntrospectionException,InstantiationException,IllegalAccessException  {
        Class<?>[] clasz = GenericTypeUtil.getActualTypeArguments(prop,bean.getClass());
        return clasz[0].newInstance();
    }
    private Object createSimpleInstance(Object bean,String prop) throws IntrospectionException,InstantiationException,IllegalAccessException, InvocationTargetException {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(prop, bean.getClass());
        Constructor constructor = ConstructorUtils.getAccessibleConstructor(propertyDescriptor.getPropertyType(), new Class[]{});
        return constructor.newInstance();
    }

    private Object aquireNestedPath(Object bean,String path) throws NoSuchMethodException, IntrospectionException,InstantiationException,IllegalAccessException,InvocationTargetException{
        property.aquire(bean,path);
        Object nestedBean = bean;
        Resolver resolver = pub.getResolver();
         if ( !resolver.hasNested(path)) {
             if(resolver.isIndexed(path)) {
                // indexed property
                 String prop = resolver.getProperty(path);
                 int idx = resolver.getIndex(path);
                 Object instance = createIndexedInstance(nestedBean,prop);
                 try {
                     pub.setIndexedProperty(nestedBean,prop,idx,instance);
                 } catch( IndexOutOfBoundsException e) {
                     //
                     // size of collection is too small
                     // therefore expand collection to make it bigger
                     Object small  = pub.getProperty(nestedBean,prop);
                     Object bigger = expandCollection(small,idx+1);
                     pub.setProperty(nestedBean,prop,bigger);
                 }
             } 
         } else {
             for(; resolver.hasNested(path); path = resolver.remove(path))
             {
                String next = resolver.next(path);
                Object object = null;
                try {
                     object = pub.getProperty(bean,next);
                } catch( IndexOutOfBoundsException e) {
                    //
                    // size of collection is too small
                    // therefore expand collection to make it bigger
                    String prop = resolver.getProperty(path);
                    int idx = resolver.getIndex(path);
                    Object small  = pub.getProperty(nestedBean,prop);
                    Object bigger = expandCollection(small,idx+1);
                    pub.setProperty(nestedBean,prop,bigger);
                    object = pub.getProperty(bean,next);
                }
                if ( object == null && resolver.hasNested(path)) {
                    // nesting will fail
                    if(resolver.isMapped(next)) {
                        // mapped property is not supported for path expansion
                        throw new NestedNullException("Null property value for '" + path + "' on bean class '" + bean.getClass() + "'");
                    } else if(resolver.isIndexed(next)) {
                       // indexed property
                        String prop = resolver.getProperty(path);
                        int idx = resolver.getIndex(path);
                        Object instance = createIndexedInstance(nestedBean,prop);
                        instance = property.indexed(nestedBean,prop,idx,instance);
                        pub.setIndexedProperty(nestedBean,prop,idx,instance);                        
                        object = pub.getProperty(bean,next);
                    } else {
                        // simple property
                       String prop = resolver.getProperty(path);
                       Object instance = createSimpleInstance(nestedBean,prop);
                        instance = property.simple(nestedBean,prop,instance);
                        pub.setProperty(nestedBean,prop,instance);                       
                       object = pub.getProperty(bean,next);
                    }
                }
                nestedBean = object;
             }             
         }
        return bean;
    }

    private void setProperty(Object bean, String path, Object value)
        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        if(bean == null)
            throw new IllegalArgumentException("No bean specified");
        if(path == null)
            throw new IllegalArgumentException("No path specified for bean class '" + bean.getClass() + "'");
        //
        property.set(bean,path,value);
        Object   drilldown = bean;
        String   subpath   = new String (path);
        Resolver resolver  = pub.getResolver();
        String   next      = null;
        for(; resolver.hasNested(subpath); subpath = resolver.remove(subpath))
        {
            next = resolver.next(subpath);
            Object nestedBean = pub.getProperty(drilldown, next);
            if(nestedBean == null)
                throw new NestedNullException("Null property value for '" + subpath + "' on bean class '" + bean.getClass() + "'");
            drilldown = nestedBean;
        }
        String prop = resolver.getProperty(subpath);
        if(resolver.isMapped(subpath)) {
            // mapped property
            String key = resolver.getKey(subpath);
            value = property.mapped(drilldown,prop,key,value);
        } else if(resolver.isIndexed(subpath)) {
           // indexed property
            int idx = resolver.getIndex(subpath);
            value = property.indexed(drilldown,prop,idx,value);
        } else {
            // simple property
            value = property.simple(drilldown,subpath,value);
        }
        //
        pub.setProperty(bean,path,value);
    }

    public void populate(Object dest, Map origPathValues) throws NoSuchMethodException, IntrospectionException,InstantiationException,IllegalAccessException,InvocationTargetException {
        Iterator itPathValues = origPathValues.entrySet().iterator();
        while(itPathValues.hasNext()) {
            Map.Entry entry = (Map.Entry)itPathValues.next();
            String path = (String)entry.getKey();
            Object value = entry.getValue();
            if ( value != null ) {
                try {
                    dest = aquireNestedPath(dest,path);
                    setProperty(dest,path,value);
                } catch(Exception e) {
                   if ( ignore ) {
                       log.error("population of data for path "+path+" returned exception",e);
                   } else {
                        throw new IllegalStateException("extraction of data for path "+path+" returned exception",e);
                   }

                }
            }
        }
    }
    public Map extract(Object bean, Set paths)  {
        Map extracted = new HashMap();
        for(Object path : paths) {
            Object value = null;
            try {
                bean = aquireNestedPath(bean,(String)path);
                value = pub.getProperty(bean,(String)path);                            
            } catch(Exception e) {
               if ( ignore ) {
                   log.error("extraction of data for path "+path+" returned exception",e);
               } else {
                    throw new IllegalStateException("extraction of data for path "+path+" returned exception",e);   
               }

            }
            extracted.put(path, value);
        }
        return extracted;
    }
}
