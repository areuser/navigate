package nl.ucan.navigate;

import junit.framework.Assert;
import org.apache.commons.collections.MapUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.util.*;

import nl.ucan.navigate.util.Task;
import nl.ucan.navigate.util.Resource;/*
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
    public void example() throws IntrospectionException {
        Task simple = new Task();
        Navigator.populate(simple,new Object[][]{
            {"name","deploy project"}
            ,{"subTask[1]/name","write article"}
            ,{"subTask[1]/assigned/role","volunteer"}
            ,{"details[@name='project']","beannav"}
            ,{"assigned/role","founder"}
        });

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

        Map extract = Navigator.extract(simple,new String[]{
                "name"
                ,"subTask[1]/name"
                ,"subTask[1]/assigned/role"
                ,"details[@name='project']"
                ,"assigned/role"
        });

        Assert.assertEquals(extract.get("name"),complex.getName());
        Assert.assertEquals(extract.get("subTask[1]/name"),complex.getSubTask().get(0).getName());
        Assert.assertEquals(extract.get("subTask[1]/assigned/role"),complex.getSubTask().get(0).getAssigned().getRole());
        Assert.assertEquals(extract.get("details[@name='project']"),complex.getDetails().get("project"));
        Assert.assertEquals(extract.get("assigned/role"),complex.getAssigned().getRole());
    }


    @Test
    public void populate() throws IntrospectionException {
        Map  init = MapUtils.putAll(new HashMap(), new Object[][]{
                {"name","promote xbean"}
                ,{"completion",0.01F}
                ,{"assigned/role","founder"}
                ,{"subTask[1]/name","roadshow"}
                ,{"subTask[1]/assigned/role","marketing"}
                ,{"details[@name='license']","Apache License"}
        });
        Task task = (Task) Navigator.populate(new Task(),init);
        Assert.assertEquals(task.getName(),"promote xbean"); 
        Assert.assertEquals(task.getCompletion(),0.01F);
        Assert.assertEquals(task.getAssigned().getRole(),"founder");
        Assert.assertEquals(task.getSubTask().get(0).getName(),"roadshow");
        Assert.assertEquals(task.getSubTask().get(0).getAssigned().getRole(),"marketing");
        Assert.assertEquals(task.getDetails().get("license"),"Apache License");
        //
        Map  modified = MapUtils.putAll(new HashMap(), new Object[][]{
                {"subTask[assigned/role='marketing']/assigned/role","founder"}
        });
        task = (Task) Navigator.populate(task,modified);
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

        Map extract = Navigator.extract(task, new HashSet(Arrays.asList(
           "name"
           ,"completion"
           ,"assigned/role"               
           ,"subTask[1]/name"
           ,"subTask[1]/assigned/role"
           ,"details[@name='license']"
        )));

        Assert.assertEquals(extract.get("name"),"promote xbean");
        Assert.assertEquals(extract.get("completion"),0.01F);
        Assert.assertEquals(extract.get("assigned/role"),"founder");
        Assert.assertEquals(extract.get("subTask[1]/name"),"roadshow");
        Assert.assertEquals(extract.get("subTask[1]/assigned/role"),"marketing");
        Assert.assertEquals(extract.get("details[@name='license']"),"Apache License");  
    }


}
