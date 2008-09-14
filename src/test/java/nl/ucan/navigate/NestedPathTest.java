package nl.ucan.navigate;

import nl.ucan.navigate.util.Task;
import nl.ucan.navigate.util.Resource;

import java.util.*;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Test;

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

public class NestedPathTest {
    private static Log log = LogFactory.getLog(NestedPathTest.class);



    @Test
    public void simple() throws Exception {
        final Task simple = new Task();
        simple.setParent(new Task());
        Map propEntryMap = new HashMap();
        propEntryMap.put("parent/name","deploy project");
        NestedPath.getInstance(new PropertyInstance() {
           public Object simple(Object bean, String property, Object value) {
              Assert.assertEquals(property,"name");
              Assert.assertEquals(value,"deploy project");
              Assert.assertEquals(simple,bean);
              return value;
           }
        }).populate(simple,propEntryMap);
    }


    @Test
    public void example() throws Exception {
        Task simple = new Task();
        Map propEntryMap = new HashMap();
        propEntryMap.put("name","deploy project");
        propEntryMap.put("subTask[0]/name","write article");
        propEntryMap.put("subTask[0]/assigned/role","volunteer");
        propEntryMap.put("details(project)","beannav");
        propEntryMap.put("assigned/role","founder");
        NestedPath.getInstance().populate(simple,propEntryMap);
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
        Map extract = NestedPath.getInstance().extract(simple,xpathEntrySet);

        Assert.assertEquals(extract.get("name"),complex.getName());
        Assert.assertEquals(extract.get("subTask[0]/name"),complex.getSubTask().get(0).getName());
        Assert.assertEquals(extract.get("subTask[0]/assigned/role"),complex.getSubTask().get(0).getAssigned().getRole());
        Assert.assertEquals(extract.get("details(project)"),complex.getDetails().get("project"));
        Assert.assertEquals(extract.get("assigned/role"),complex.getAssigned().getRole());
    }


    @Test
    public void populate() throws Exception {
        Map propEntryMap = new HashMap();
        propEntryMap.put("name","promote xbean");
        propEntryMap.put("completion",0.01F);
        propEntryMap.put("assigned/role","founder");
        propEntryMap.put("subTask[0]/name","roadshow");
        propEntryMap.put("subTask[0]/assigned/role","marketing");
        propEntryMap.put("details(license)","Apache License");

        Task task = new Task();
        NestedPath.getInstance().populate(task,propEntryMap);
        Assert.assertEquals(task.getName(),"promote xbean");
        Assert.assertEquals(task.getCompletion(),0.01F);
        Assert.assertEquals(task.getAssigned().getRole(),"founder");
        Assert.assertEquals(task.getSubTask().get(0).getName(),"roadshow");
        Assert.assertEquals(task.getSubTask().get(0).getAssigned().getRole(),"marketing");
        Assert.assertEquals(task.getDetails().get("license"),"Apache License");
        //
        propEntryMap = new HashMap();
        propEntryMap.put("subTask[0]/assigned/role","founder");
        NestedPath.getInstance().populate(task,propEntryMap);
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
        Map extract = NestedPath.getInstance().extract(task,xpathEntrySet);


        Assert.assertEquals(extract.get("name"),"promote xbean");
        Assert.assertEquals(extract.get("completion"),0.01F);
        Assert.assertEquals(extract.get("assigned/role"),"founder");
        Assert.assertEquals(extract.get("subTask[0]/name"),"roadshow");
        Assert.assertEquals(extract.get("subTask[0]/assigned/role"),"marketing");
        Assert.assertEquals(extract.get("details(license)"),"Apache License");
    }


}

