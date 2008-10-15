package nl.ucan.navigate.example;

import java.util.Date;
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
public class Resource {
    private String  name;
    private String	role;
    private Date    startDate;
    private Resource managedBy;
    private List<Resource> managerOf = new ArrayList<Resource>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Resource getManagedBy() { return managedBy; }
    public void setManagedBy(Resource managedBy) { this.managedBy = managedBy; }

    public List<Resource> getManagerOf() { return managerOf; }
    public void setManagerOf(List<Resource> managerOf) { this.managerOf = managerOf; }
}
