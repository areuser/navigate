package nl.ucan.navigate;

import nl.ucan.navigate.util.Task;
import nl.ucan.navigate.util.Resource;

import java.util.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.expression.Resolver;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
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
        PropertyValue propertyValue = new PropertyValue() {
           public Object simple(Object bean, String property, Object value) {
              Assert.assertEquals(property,"name");
              Assert.assertEquals(value,"deploy project");
              Assert.assertEquals(simple,bean);
              return value;
           }
        };
        NestedPath.getInstance().setPropertyValue(propertyValue).populate(simple,"parent.name","deploy project");
    }


    @Test
    public void example() throws Exception {
        Task simple = new Task();
        NestedPath nestedPath = NestedPath.getInstance();
        nestedPath.populate(simple,"name","deploy project");
        nestedPath.populate(simple,"subTask[0].name","write article");
        nestedPath.populate(simple,"subTask[0].assigned.role","volunteer");
        nestedPath.populate(simple,"subTask[1]",null);
        nestedPath.populate(simple,"details(project)","beannav");
        nestedPath.populate(simple,"assigned.role","founder");
        nestedPath.populate(simple,"classification[1]","oss");
        nestedPath.populate(simple,"classification[0]","java");

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
        complex.setClassification(new String[]{"java","oss"});


        Assert.assertEquals(simple.getName(),complex.getName());
        Assert.assertEquals(simple.getSubTask().get(0).getName(),complex.getSubTask().get(0).getName());
        Assert.assertEquals(simple.getSubTask().get(0).getAssigned().getRole(),complex.getSubTask().get(0).getAssigned().getRole());
        Assert.assertEquals(simple.getDetails().get("project"),complex.getDetails().get("project"));
        Assert.assertEquals(simple.getAssigned().getRole(),complex.getAssigned().getRole());
        Assert.assertEquals(nestedPath.extract(simple,"name"),complex.getName());
        Assert.assertEquals(nestedPath.extract(simple,"subTask[name=write article].name"),complex.getSubTask().get(0).getName());
        Assert.assertEquals(nestedPath.extract(simple,"subTask[0].assigned.role"),complex.getSubTask().get(0).getAssigned().getRole());
        Assert.assertEquals(nestedPath.extract(simple,"details(project)"),complex.getDetails().get("project"));
        Assert.assertEquals(nestedPath.extract(simple,"assigned.role"),complex.getAssigned().getRole());
        Assert.assertEquals(nestedPath.extract(simple,"classification[0]"),complex.getClassification()[0]);
        Assert.assertEquals(nestedPath.extract(simple,"classification[1]"),complex.getClassification()[1]);
    }


    @Test
    public void populate() throws Exception {
        Task task = new Task();
        NestedPath nestedPath = NestedPath.getInstance();
        nestedPath.populate(task,"name","promote xbean");
        nestedPath.populate(task,"completion",0.01F);
        nestedPath.populate(task,"assigned.role","founder");
        nestedPath.populate(task,"subTask[0].name","roadshow");
        nestedPath.populate(task,"subTask[0].assigned.role","marketing");
        nestedPath.populate(task,"details(license)","Apache License");

        Assert.assertEquals(task.getName(),"promote xbean");
        Assert.assertEquals(task.getCompletion(),0.01F);
        Assert.assertEquals(task.getAssigned().getRole(),"founder");
        Assert.assertEquals(task.getSubTask().get(0).getName(),"roadshow");
        Assert.assertEquals(task.getSubTask().get(0).getAssigned().getRole(),"marketing");
        Assert.assertEquals(task.getDetails().get("license"),"Apache License");
        //
        nestedPath.populate(task,"subTask[name=roadshow].assigned.role","founder");
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

        NestedPath nestedPath = NestedPath.getInstance();
        Assert.assertEquals(nestedPath.extract(task,"name"),"promote xbean");
        Assert.assertEquals(nestedPath.extract(task,"completion"),0.01F);
        Assert.assertEquals(nestedPath.extract(task,"assigned.role"),"founder");
        Assert.assertEquals(nestedPath.extract(task,"subTask[0].name"),"roadshow");
        Assert.assertEquals(nestedPath.extract(task,"subTask[name=roadshow].assigned.role"),"marketing");
        Assert.assertEquals(nestedPath.extract(task,"details(license)"),"Apache License");
    }

}

