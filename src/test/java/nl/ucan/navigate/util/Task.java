package nl.ucan.navigate.util;

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
 * since  : 0.1
  */
public class Task {
      private String name;
      private Date   startDate;
      private Date   dueDate;
      private Float  completion;
      private Resource  assigned;
      private List<Task> subTask = new ArrayList<Task>();
      private Map<String,String>  details = new HashMap<String,String>();

    public Task(){}
    
    public Task(String name, Date startDate, Date dueDate) {
        this.name = name;
        this.startDate = startDate;
        this.dueDate = dueDate;
    }

    public Map<String,String> getDetails() {
        return details;
    }

    public void setDetails(Map<String,String> details) {
        this.details = details;
    }

    public Resource getAssigned() {
        return assigned;
    }

    public void setAssigned(Resource assigned) {
        this.assigned = assigned;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Float getCompletion() {
        return completion;
    }

    public void setCompletion(Float completion) {
        this.completion = completion;
    }

    public List<Task> getSubTask() {
        return subTask;
    }

    public void setSubTask(List<Task> subTask) {
        this.subTask = subTask;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        if (name != null ? !name.equals(task.name) : task.name != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (dueDate != null ? dueDate.hashCode() : 0);
        result = 31 * result + (completion != null ? completion.hashCode() : 0);
        result = 31 * result + (subTask != null ? subTask.hashCode() : 0);
        return result;
    }
}
