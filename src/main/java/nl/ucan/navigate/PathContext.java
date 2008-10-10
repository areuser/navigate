package nl.ucan.navigate;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;

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
public class PathContext {
    private Object bean;
    private List<String> paths;
    private char nested;

    public PathContext(char nested) {
        this.paths = new ArrayList<String>();
        this.nested = nested;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void addPart(String path) {
        this.paths.add(path);
    }

    public boolean noPathSpecified() {
        return this.paths.isEmpty();
    }

    public static String toString(PathContext context) {
        String string = "";
        for(int idx=0 ; idx < context.paths.size() ; idx ++ ) {
            if ( idx == 0 ) {
                string = context.paths.get(idx);
            } else {
                string += context.nested+context.paths.get(idx);
            }
        }
        return string;
    }
    public String toString(List<String> subListOfPaths) {
        PathContext tmp = new PathContext(this.nested);
        tmp.getPaths().addAll(subListOfPaths);
        return tmp.toString();        
    }

    public String toString() {
       return PathContext.toString(this);
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }
        
}
