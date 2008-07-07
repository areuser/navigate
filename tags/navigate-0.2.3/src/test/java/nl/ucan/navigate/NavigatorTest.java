package nl.ucan.navigate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.beanutils.*;

import java.beans.IntrospectionException;
import java.util.*;
import java.lang.reflect.InvocationTargetException;

import nl.ucan.navigate.util.Task;
import nl.ucan.navigate.util.Resource;
import nl.ucan.navigate.convertor.EventHandler;
import nl.ucan.navigate.convertor.DefaultDirtyBeanConvertor;
import nl.ucan.navigate.convertor.DirtyBeanConvertor;
import nl.ucan.navigate.convertor.DefaultValueConvertor;
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

    @Before
    public void setup() {
    }

    @After
    public void teardown() {
    }

    @Test
    public void nullity() throws IntrospectionException {
        Task simple = new Task();
        Object[][] xpathPopEntries = new Object[][]{
            {"id","null"}
        };
        Map xpathEntryMap =  MapUtils.putAll(new HashMap(), xpathPopEntries);
        Navigator.populate(simple,xpathEntryMap);
    }

    @Test
    public void enrich()  {
        try {
            Task simple = new Task();
            Map<Navigator.Event, EventHandler> handler = new HashMap<Navigator.Event, EventHandler>();
            DirtyBeanConvertor ddbc = new DirtyBeanConvertor() {
                public Object evaluate(Object bean, String property, Object value) {
                    try {
                        Object version = BeanUtils.getProperty(bean,"version");
                        Object id = BeanUtils.getProperty(bean,"id");
                        if ( version != null && id != null ) {
                            if (GenericTypeUtil.isEmpty(bean,new String[]{"id","version"})) {
                                Task simple = (Task)ConstructorUtils.invokeConstructor(bean.getClass(),null);
                                simple.setName("piet");
                                simple.getSubTask().add(new Task());
                                // simple.setDueDate(new Date());
                                // simple.setStartDate(new Date());
                                //
                                // load bean from persisted store

                                //
                                // copy properties from loaded bean to transient bean in memory
                                PropertyUtilsBean pub = new PropertyUtilsBean();
                                pub.copyProperties(bean,simple);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); 
                    } 
                    return value;
                }            
            };
            Object[][] xpathPopEntries = new Object[][]{
                {"id","1"}
                ,{"version","1"}
//                  ,{"name","deploy project"}                    
//                ,{"subTask[1]/name","write article"}
//                ,{"subTask[1]/assigned/role","volunteer"}
//                ,{"details[@name='project']","beannav"}
//                ,{"assigned/role","founder"}
            };
            Map xpathEntryMap =  MapUtils.putAll(new HashMap(), xpathPopEntries);
            Navigator.populate(simple,xpathEntryMap,new DefaultValueConvertor(), ddbc);


            // Task dummy = new Task();
            // dummy.setName("task");
            // dummy = (Task)genericService.merge(dummy);
            // Long id = dummy.getId();
            // Long version = dummy.getVersion();
            //
            // als alleen id en version gegeven, voer dan load uit van object
            // als meer gegeven, laat dan als het is
//            Task test = new Task();
//            test.setId(1L);
//            test.setDetails(new HashMap());
//            List list = new ArrayList();
//            list.add(new Task());
//            test.setSubTask(new ArrayList(list));
//            int j = 2;
           //  Map map = PropertyUtils.describe(test);

            // als index property -> size = 0
            // als mapped property -> size =  0
            // als simple property -> waarde null
            
//>
//> The get(property, index) does either ....
//>    return ((List)values.get(name)).get(index)
//> or....
//>    return Array.get(values.get(name), index)
//>
//> The get(name, key) does....
//>    return ((Map)values.get(name)).get(key)

            //



        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void test() throws IntrospectionException {
        Task simple = new Task();
        Map<Navigator.Event, EventHandler> handler = new HashMap<Navigator.Event, EventHandler>();
        handler.put(Navigator.Event.AddedIndexedProperty, new EventHandler(){
            public void on(Object[] o) {
                 Task task = (Task)o[0];
                 String property = (String)o[1]; // subtask
                 Integer index = (Integer)o[2]; // 0
                 Task  value = (Task)o[3];
            }
        });
        Object[][] xpathPopEntries = new Object[][]{
            {"name","deploy project"}
            ,{"subTask[1]/name","write article"}
            ,{"subTask[1]/assigned/role","volunteer"}
            ,{"details[@name='project']","beannav"}
            ,{"assigned/role","founder"}
        };
        Map xpathEntryMap =  MapUtils.putAll(new HashMap(), xpathPopEntries);
        Navigator.populate(simple,xpathEntryMap,handler);
    }

    @Test
    public void example() throws IntrospectionException {
        Task simple = new Task();
        Object[][] xpathPopEntries = new Object[][]{
            {"name","deploy project"}
            ,{"subTask[1]/name","write article"}
            ,{"subTask[1]/assigned/role","volunteer"}
            ,{"details[@name='project']","beannav"}
            ,{"assigned/role","founder"}
        };
        Map xpathEntryMap =  MapUtils.putAll(new HashMap(), xpathPopEntries);
        Navigator.populate(simple,xpathEntryMap);
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
                ,"subTask[1]/name"
                ,"subTask[1]/assigned/role"
                ,"details[@name='project']"
                ,"assigned/role"
        }; 
        Set<String> xpathEntrySet = new HashSet(Arrays.asList(xpathExtEntries));
        Map extract = Navigator.extract(simple,xpathEntrySet);

        Assert.assertEquals(extract.get("name"),complex.getName());
        Assert.assertEquals(extract.get("subTask[1]/name"),complex.getSubTask().get(0).getName());
        Assert.assertEquals(extract.get("subTask[1]/assigned/role"),complex.getSubTask().get(0).getAssigned().getRole());
        Assert.assertEquals(extract.get("details[@name='project']"),complex.getDetails().get("project"));
        Assert.assertEquals(extract.get("assigned/role"),complex.getAssigned().getRole());
    }


    @Test
    public void populate() throws IntrospectionException {
        Object[][] xpathPopEntries = new Object[][]{
                {"name","promote xbean"}
                ,{"completion",0.01F}
                ,{"assigned/role","founder"}
                ,{"subTask[1]/name","roadshow"}
                ,{"subTask[1]/assigned/role","marketing"}
                ,{"details[@name='license']","Apache License"}
        };
        Map xpathEntryMap =  MapUtils.putAll(new HashMap(), xpathPopEntries);        
        Task task = (Task) Navigator.populate(new Task(),xpathEntryMap);
        Assert.assertEquals(task.getName(),"promote xbean");
        Assert.assertEquals(task.getCompletion(),0.01F);
        Assert.assertEquals(task.getAssigned().getRole(),"founder");
        Assert.assertEquals(task.getSubTask().get(0).getName(),"roadshow");
        Assert.assertEquals(task.getSubTask().get(0).getAssigned().getRole(),"marketing");
        Assert.assertEquals(task.getDetails().get("license"),"Apache License");
        //
        xpathPopEntries = new Object[][]{
                {"subTask[assigned/role='marketing']/assigned/role","founder"}
        };
        xpathEntryMap =  MapUtils.putAll(new HashMap(), xpathPopEntries);        
        task = (Task) Navigator.populate(task,xpathEntryMap);
        Assert.assertEquals(task.getSubTask().get(0).getAssigned().getRole(),"founder");
    }

    @Test
    public void extract() throws IntrospectionException {
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
                ,"subTask[1]/name"
                ,"subTask[1]/assigned/role"
                ,"details[@name='license']"                
        };
        Set<String> xpathEntrySet = new HashSet(Arrays.asList(xpathExtEntries));
        Map extract = Navigator.extract(task,xpathEntrySet);


        Assert.assertEquals(extract.get("name"),"promote xbean");
        Assert.assertEquals(extract.get("completion"),0.01F);
        Assert.assertEquals(extract.get("assigned/role"),"founder");
        Assert.assertEquals(extract.get("subTask[1]/name"),"roadshow");
        Assert.assertEquals(extract.get("subTask[1]/assigned/role"),"marketing");
        Assert.assertEquals(extract.get("details[@name='license']"),"Apache License");
    }


}

