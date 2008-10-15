package nl.ucan.navigate;

import org.apache.commons.beanutils.*;
import org.apache.commons.beanutils.expression.Resolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.collections.CollectionUtils;

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
    private IndexPointer indexPointer;

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

    public NestedPath setPropertyInstance(PropertyInstance propertyInstance) {
        this.propertyInstance = propertyInstance;
        return this;        
    }

    public NestedPath setPropertyValue(PropertyValue propertyValue) {
        this.propertyValue = propertyValue;
        return this;
    }

    public NestedPath setIndexPointer(IndexPointer indexPointer) {
        this.indexPointer = indexPointer;
        return this;
    }

    public static NestedPath getInstance() {
        return new NestedPath();
    }
    private NestedPath() {
        this.propertyInstance = new PropertyInstance() {
            public Object indexed(Object bean, String property, int index, Object value){
                log.info("created indexed property "+property+" at "+index+" of bean "+bean+" and will be set to "+value);
                return value;
            }
            public Object simple(Object bean, String property, Object value){
                log.info("created simple property "+property+" of bean "+bean+" and will be set to "+value);
                return value;
            }
        };
        this.propertyValue = new PropertyValue() {
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
            public Object valueOf (Class clasz, String property, String value) {
                log.info("value of valueOf "+property+" will be set to "+value);
                return value;             
            }
        };
        this.indexPointer = new IndexPointer() {
            public void add(Object bean,Object instance) {
                if (bean instanceof Collection) {
                    ((Collection) bean).add(instance);
                } 
            }
            public int size(Object bean) {
                return CollectionUtils.size(bean);
            }
            public Object get(Object bean,int idx) {
               return CollectionUtils.get(bean,idx);
            }

            public int firstIndexOf(Object bean,String undeterminedIndex) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,InstantiationException, IntrospectionException {
                this.setUndeterminedIndex(undeterminedIndex);
                for(int idx = 0; idx < size(bean) ; idx++) {
                    Object object = get(bean,idx);
                    if (object != null ) {
                        if (evaluate(object,this.getUndeterminedIndex()) ) return idx;
                    }
                }
                return -1;
            }
            public void setIndex(Object bean,String undeterminedIndex) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,InstantiationException, IntrospectionException {
                this.setUndeterminedIndex(undeterminedIndex);
                Object value = propertyValue.simple(bean,this.getProperty(),this.getValue());
                pub.setProperty(bean,this.getProperty(),value);
            }
            private String undeterminedIndex;
            private void setUndeterminedIndex(String undeterminedIndex) {
                this.undeterminedIndex = undeterminedIndex;
            }
            private String getUndeterminedIndex() {
                    return this.undeterminedIndex;
            }
            private boolean evaluate(Object bean,String undeterminedIndex) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,InstantiationException, IntrospectionException  {
                this.setUndeterminedIndex(undeterminedIndex);
                String property = getProperty();
                String valueOfIndex = getValue();
                Object valueOfBean = pub.getProperty(bean,property);
                return ObjectUtils.equals(valueOfIndex,valueOfBean);
            }
            private String getProperty()  {
                Map.Entry<String,String> entry = getNamedIndex(this.getUndeterminedIndex());
                return entry.getKey();
            }
            private String getValue()  {
                Map.Entry<String,String> entry = getNamedIndex(this.getUndeterminedIndex());
                return entry.getValue();
            }
            private Map.Entry<String,String> getNamedIndex(String value) {
                final String SEP = "=";
                Map<String,String> keyValuePair = new HashMap<String,String>();
                if ( StringUtils.indexOf(value,SEP) == -1 ) return null;
                keyValuePair.put(StringUtils.substringBefore(value,SEP)
                                ,StringUtils.substringAfter(value,SEP));
                return keyValuePair.entrySet().iterator().next();
            }
        };
        this.pub = BeanUtilsBean.getInstance().getPropertyUtils();
        this.pub.setResolver(new ResolverImpl());
    }

    public void populate(Object bean, String path, Object value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,InstantiationException,IntrospectionException {
        PathContext pathContext = acquirePathContext(bean,path);
        setProperty(pathContext,value);
    }

    public Object extract(Object bean, String path) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,InstantiationException,IntrospectionException  {
        PathContext pathContext = acquirePathContext(bean,path);
        return pub.getProperty(bean,pathContext.toString());
    }

    private Object getLastInstance(PathContext pathContext) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,InstantiationException,IntrospectionException {
        if ( pathContext.getPaths().size() <= 1  ) {
            return pathContext.getBean();
        } else {
            List subListOfPaths = pathContext.getPaths().subList(0,pathContext.getPaths().size()-1);
            return pub.getProperty(pathContext.getBean(),pathContext.toString(subListOfPaths));
        }
    }
    private String getLastElement(PathContext pathContext) {
        return pathContext.getPaths().get(pathContext.getPaths().size()-1);
    }
    private void setProperty(PathContext pathContext, Object value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,InstantiationException,IntrospectionException   {
        if ( pathContext.getPaths().size() > 0 ) {
            String lastPathElement = getLastElement(pathContext);
            Object lastPathInstance = getLastInstance(pathContext);
            Resolver resolver = pub.getResolver();
            String prop = resolver.getProperty(lastPathElement);
            if(resolver.isMapped(lastPathElement)) {
                //
                // mapped property
                String key = resolver.getKey(lastPathElement);
                value = propertyValue.mapped(lastPathInstance,prop,key,value);
            } else if(resolver.isIndexed(lastPathElement)) {
                //
               // indexed property
                int idx = resolver.getIndex(lastPathElement);
                value = propertyValue.indexed(lastPathInstance,prop,idx,value);
            } else {
                //
                // simple property
                value = propertyValue.simple(lastPathInstance,lastPathElement,value);
            }
            pub.setProperty(lastPathInstance,lastPathElement,value);
        }
    }

    public PathContext acquirePathContext(Object bean, String path) throws IllegalAccessException,InvocationTargetException,InstantiationException,IntrospectionException,NoSuchMethodException {
        PathContext pathContext = new PathContext(ResolverImpl.getNested());
        Object instance = bean;
        pub.copyProperties(instance,bean);
        DynaBean dynaBean = getDynaBean(instance);
        Resolver resolver = pub.getResolver();
        for(; StringUtils.isNotBlank(path); path = resolver.remove(path))
        {
            String prop = resolver.getProperty(path);
            if ( resolver.isIndexed(path) ) {
                String undeterminedIdx = getUndeterminedIndex(path);
                int positionalIdx = getPositionalIndex(undeterminedIdx);
                if ( positionalIdx == -1 ) {                    
                    Object object = pub.getProperty(dynaBean,prop);
                    int idx = indexPointer.firstIndexOf(object,undeterminedIdx);
                    if ( idx == -1 ) {
                        Object nestedBean = ( pathContext.noPathSpecified() ? bean : pub.getProperty(bean,pathContext.toString()));
                        instance = createInstance(instance.getClass(),prop);
                        instance = propertyInstance.indexed(nestedBean,prop,indexPointer.size(object),instance);
                        indexPointer.add(object,instance);  // potentially resize collection when instance is added
                        indexPointer.setIndex(instance,undeterminedIdx); // named idx itself is a property
                        pub.setProperty(bean,( StringUtils.isBlank(pathContext.toString()) ? prop : pathContext.toString()+ResolverImpl.getNested()+prop ),object);
                        String replace = namedToPositioned(resolver.next(path),indexPointer.size(object)-1);
                        pathContext.addPart(replace);
                    }
                    else {
                        String replace = namedToPositioned(resolver.next(path),idx);
                        pathContext.addPart(replace);
                        instance = indexPointer.get(object,idx);
                    }                    

                }  else {
                    Object tmp = pub.getIndexedProperty(dynaBean,prop,positionalIdx);
                    if ( tmp == null ) {
                        Object nestedBean = ( pathContext.noPathSpecified() ? bean : pub.getProperty(bean,pathContext.toString()));
                        instance = createInstance(instance.getClass(),prop);
                        instance = propertyInstance.indexed(nestedBean,prop,positionalIdx,instance);
                        pathContext.addPart(resolver.next(path));
                        pub.setProperty(bean,pathContext.toString(),instance);
                    }
                    else {
                        pathContext.addPart(resolver.next(path));
                        instance = tmp;
                    }
                }
            }  else {
                instance = pub.getProperty(instance,prop);
                if ( instance == null ) {
                    instance = pub.getProperty(dynaBean,prop);
                    Object nestedBean = ( pathContext.noPathSpecified() ? bean : pub.getProperty(bean,pathContext.toString()));
                    instance = propertyInstance.simple(nestedBean,prop,instance);
                    pathContext.addPart(resolver.next(path));
                    String tmp = pathContext.toString();
                    pub.setProperty(bean,tmp,instance);
                } else {
                   pathContext.addPart(resolver.next(path));
                }
            }
            if ( resolver.hasNested(path))
                dynaBean = getDynaBean(instance);
         }
        pathContext.setBean(bean);
        return pathContext;
    }

    private static String namedToPositioned(String next,int position) {
        String substr = StringUtils.substringBetween(next,""+ResolverImpl.getIndexedStart(),""+ResolverImpl.getIndexedEnd());
        return StringUtils.replace(next,substr,""+position);
    }
    private static DynaBean getDynaBean(Object instance) throws IllegalAccessException,InvocationTargetException,NoSuchMethodException {
        WrapDynaClass dynaClass = WrapDynaClass.createDynaClass(instance.getClass());
        LazyDynaBean lazyBean = new LazyDynaBean(dynaClass);
        PropertyUtils.copyProperties(lazyBean,instance);
        return lazyBean;
    }

    private static Object createInstance(Class clasz, String prop) throws IntrospectionException,IllegalAccessException,InvocationTargetException,InstantiationException  {
        Class<?>[] type = getActualTypeArguments(prop,clasz);
        Constructor constructor = ConstructorUtils.getAccessibleConstructor(type[0], new Class[]{});
        return constructor.newInstance();
    }
    private static Class<?>[] getActualTypeArguments(String property,Class clasz) throws IntrospectionException {
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

    private static String getUndeterminedIndex(String expression) {
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == ResolverImpl.getNested() || c == ResolverImpl.getMappedStart() ) {
                return null;
            } else if (c == ResolverImpl.getIndexedStart()) {
                int end = expression.indexOf(ResolverImpl.getIndexedEnd(), i);
                if (end < 0) {
                    throw new IllegalArgumentException("Missing End Delimiter");
                }
                String value = expression.substring(i + 1, end);
                if (value.length() == 0) {
                    throw new IllegalArgumentException("No Index Value");
                }
                return value;
            }
        }
        return null;
    }

    private static int getPositionalIndex(String value) {
        int index = 0;
        try {
            index = Integer.parseInt(value, 10);
        } catch (Exception e) {
            return -1;
        }
        return index;
    }

    

}
