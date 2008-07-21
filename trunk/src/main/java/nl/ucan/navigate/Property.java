package nl.ucan.navigate;/*
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
public abstract class Property {
    public boolean aquire(Object bean,String path) { return true; }
    public boolean set(Object bean,String path,Object value) { return true; }
    public Object indexed(Object bean, String property, int index, Object value) { return value; }
    public Object mapped(Object bean, String name, Object key, Object value) { return value; }
    public Object simple(Object bean, String property, Object value) { return value; }
}
