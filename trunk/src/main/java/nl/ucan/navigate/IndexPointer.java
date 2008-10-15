package nl.ucan.navigate;

import java.util.List;
import java.lang.reflect.InvocationTargetException;
import java.beans.IntrospectionException;

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
public abstract class IndexPointer {
    public abstract int firstIndexOf(Object bean,String undeterminedIndex) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,InstantiationException, IntrospectionException;
    public abstract void setIndex(Object bean,String undeterminedIndex) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,InstantiationException, IntrospectionException;
    public abstract void add(Object bean,Object instance);
    public abstract int size(Object bean);
    public abstract Object get(Object bean,int idx);
}
