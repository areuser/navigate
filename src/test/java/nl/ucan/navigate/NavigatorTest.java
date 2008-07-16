package nl.ucan.navigate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.beanutils.*;
import org.apache.commons.beanutils.expression.Resolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.collections.MapUtils;

import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import nl.ucan.navigate.util.Task;
import nl.ucan.navigate.util.Resource;
import junit.framework.Assert;/*
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

public class NavigatorTest {
    private static Log log = LogFactory.getLog(NavigatorTest.class);

    @Test
    public void simple() throws Exception {
        final Task simple = new Task();
        simple.setParent(new Task());
        Object[][] propEntries = new Object[][]{
            {"parent/name","deploy project"}
        };
        Map propEntryMap =  MapUtils.putAll(new HashMap(), propEntries);
        Navigator.getInstance(new Property() {
           public Object simple(Object bean, String property, Object value) {
              Assert.assertEquals(property,"name");
              Assert.assertEquals(value,"deploy project");
              Assert.assertEquals(simple,bean);
              return value;
           }
        }).populate(simple,propEntryMap);
    }

    @Test
    public void date() throws Exception {
        final Task simple = new Task();
        simple.setParent(new Task());
        Object[][] propEntries = new Object[][]{
            {"parent/dueDate",""}
        };
        Map propEntryMap =  MapUtils.putAll(new HashMap(), propEntries);
        Navigator.getInstance(new Property() {
           public Object simple(Object bean, String property, Object value) {
               try {
                   PropertyUtilsBean prop = new PropertyUtilsBean();
                   PropertyDescriptor desc = prop.getPropertyDescriptor(bean,property);
                   boolean isDate = Date.class.isAssignableFrom(desc.getPropertyType());
               } catch (Exception e) {
                   e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
               }
               return value;
           }
        }).populate(simple,propEntryMap);
    }

    @Test
    public void example() throws Exception {
        Task simple = new Task();
        Object[][] propEntries = new Object[][]{
            {"name","deploy project"}
            ,{"subTask[0]/name","write article"}
            ,{"subTask[0]/assigned/role","volunteer"}
            ,{"details(project)","beannav"}
            ,{"assigned/role","founder"}
        };
        Map propEntryMap =  MapUtils.putAll(new HashMap(), propEntries);
        Navigator.getInstance().populate(simple,propEntryMap);
        Task complex = new Task();
        complex.setName("deploy project");
        List<Task> subTasks = complex.getSubTask();
        Task subTask = new Task();
        subTask.setName("write article");
        Resource volunteer = new Resource();
        volunteer.setRole("volunteer");
        subTask.setAssigned(volunteer);
        subTasks.add(subTask);
        Map<String,String> details = complex.getDetails();
        details.put("project","beannav");
        Resource founder = new Resource();
        founder.setRole("founder");
        complex.setAssigned(founder);

        Assert.assertEquals(simple.getName(),complex.getName());
        Assert.assertEquals(simple.getSubTask().get(0).getName(),complex.getSubTask().get(0).getName());
        Assert.assertEquals(simple.getSubTask().get(0).getAssigned().getRole(),complex.getSubTask().get(0).getAssigned().getRole());
        Assert.assertEquals(simple.getDetails().get("project"),complex.getDetails().get("project"));
        Assert.assertEquals(simple.getAssigned().getRole(),complex.getAssigned().getRole());

        Object[] xpathExtEntries = new String[]{
                "name"
                ,"subTask[0]/name"
                ,"subTask[0]/assigned/role"
                ,"details(project)"
                ,"assigned/role"
        };
        Set<String> xpathEntrySet = new HashSet(Arrays.asList(xpathExtEntries));
        Map extract = Navigator.getInstance().extract(simple,xpathEntrySet);

        Assert.assertEquals(extract.get("name"),complex.getName());
        Assert.assertEquals(extract.get("subTask[0]/name"),complex.getSubTask().get(0).getName());
        Assert.assertEquals(extract.get("subTask[0]/assigned/role"),complex.getSubTask().get(0).getAssigned().getRole());
        Assert.assertEquals(extract.get("details(project)"),complex.getDetails().get("project"));
        Assert.assertEquals(extract.get("assigned/role"),complex.getAssigned().getRole());
    }


    @Test
    public void populate() throws Exception {
        Object[][] xpathPopEntries = new Object[][]{
                {"name","promote xbean"}
                ,{"completion",0.01F}
                ,{"assigned/role","founder"}
                ,{"subTask[0]/name","roadshow"}
                ,{"subTask[0]/assigned/role","marketing"}
                ,{"details(license)","Apache License"}
        };
        Map xpathEntryMap =  MapUtils.putAll(new HashMap(), xpathPopEntries);
        Task task = new Task();
        Navigator.getInstance().populate(task,xpathEntryMap);
        Assert.assertEquals(task.getName(),"promote xbean");
        Assert.assertEquals(task.getCompletion(),0.01F);
        Assert.assertEquals(task.getAssigned().getRole(),"founder");
        Assert.assertEquals(task.getSubTask().get(0).getName(),"roadshow");
        Assert.assertEquals(task.getSubTask().get(0).getAssigned().getRole(),"marketing");
        Assert.assertEquals(task.getDetails().get("license"),"Apache License");
        //
        xpathPopEntries = new Object[][]{
                {"subTask[0]/assigned/role","founder"}
        };
        xpathEntryMap =  MapUtils.putAll(new HashMap(), xpathPopEntries);
        Navigator.getInstance().populate(task,xpathEntryMap);
        Assert.assertEquals(task.getSubTask().get(0).getAssigned().getRole(),"founder");
    }

    @Test
    public void extract() throws Exception {
        Task task = new Task();
        task.setName("promote xbean");
        task.setCompletion(0.01F);
        Resource founder = new Resource();
        founder.setRole("founder");
        task.setAssigned(founder);
        List<Task> subTasks = task.getSubTask();
        Task subTask = new Task();
        subTask.setName("roadshow");
        Resource marketing = new Resource();
        marketing.setRole("marketing");
        subTask.setAssigned(marketing);
        subTasks.add(subTask);
        task.getDetails().put("license","Apache License");

        Object[] xpathExtEntries = new String[]{
                "name"
                ,"completion"
                ,"assigned/role"
                ,"subTask[0]/name"
                ,"subTask[0]/assigned/role"
                ,"details(license)"
        };
        Set<String> xpathEntrySet = new HashSet(Arrays.asList(xpathExtEntries));
        Map extract = Navigator.getInstance().extract(task,xpathEntrySet);


        Assert.assertEquals(extract.get("name"),"promote xbean");
        Assert.assertEquals(extract.get("completion"),0.01F);
        Assert.assertEquals(extract.get("assigned/role"),"founder");
        Assert.assertEquals(extract.get("subTask[0]/name"),"roadshow");
        Assert.assertEquals(extract.get("subTask[0]/assigned/role"),"marketing");
        Assert.assertEquals(extract.get("details(license)"),"Apache License");
    }


}

