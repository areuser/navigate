package nl.ucan.navigate.example;

import nl.ucan.navigate.NestedPath;
import nl.ucan.navigate.PropertyInstance;
import nl.ucan.navigate.PropertyValue;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
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
* since  : 
*/

public class Answer {
    public Resource robert = new Resource();
    public Resource scott  = null;
    public Answer() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,InstantiationException, IntrospectionException {
        populate(robert,new Object[][] {{"name","Robert"}
                                        ,{"role","Application Director"}
                                        ,{"managerOf[name=Scott].role","Technical Project Lead"}
                                        ,{"managerOf[name=Scott].managerOf[name=Bob].role","Lead Developer"}
                                        ,{"managerOf[name=Scott].managerOf[name=Bob].managerOf[name=Adam].role","Developer"}
                                        ,{"managerOf[name=Scott].managerOf[name=Bob].managerOf[name=Anna].role","Developer"}
                                        }

        );
        scott = (Resource)NestedPath.getInstance().extract(robert,"managerOf[name=Scott]");
    }

    public static void  populate ( Resource bean , Object[][] values) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,InstantiationException, IntrospectionException {
        for (Object[] valuePair : values) {
            NestedPath.getInstance().populate(bean,(String)valuePair[0],valuePair[1]);
        }
    }


    public static void main(String[] s) {
        try {
            Answer answer = new Answer();
            answer.answerQuestions();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void answerQuestions() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,InstantiationException, IntrospectionException {
        //  Scott asks Adam what his role is on this project
        String roleOfAdam;
        List<Resource> team = scott.getManagerOf();
        for ( Resource member1 : team ) {
            if ( "Bob".equals(member1.getName())) {
                team = member1.getManagerOf();
                for ( Resource member2 : team ) {
                    if ( "Adam".equals(member2.getName())) {
                        roleOfAdam = member2.getRole();
                    }
                }
            }
        }

        //  Scott asks Adam what his role is on this project
        roleOfAdam  = (String)NestedPath.getInstance().extract(scott,"managerOf[name=Bob].managerOf[name=Adam].role");

        Resource anna = (Resource)NestedPath.getInstance().extract(scott,"managerOf[name=Bob].managerOf[name=Anna]");
        // Scott asks Anna what the name is of her manager.
        try {
            String nameOfManager = anna.getManagedBy().getName();
        } catch(NullPointerException e ) {
            e.printStackTrace();
        }

        // Scott asks Anna what the name is of her manager.
        String nameOfManager = (String)NestedPath.getInstance().extract(anna,"managedBy.name");

        // Scott promotes Bob from lead developer to architect
        // Scott adds Tania as a developer to Bob his team
        team = scott.getManagerOf();
        for ( Resource member : team ) {
            if ( "Bob".equals(member.getName())) {
                member.setRole("architect");

                Resource tania = new Resource();
                tania.setName("Tania");
                tania.setRole("developer");
                member.getManagerOf().add(tania);
            }
        }

        // Scott promotes Bob from lead developer to architect
        NestedPath.getInstance().populate(scott,"managerOf[name=Bob].role","architect");

        // Scott adds Tania as a developer to Bob his team
        NestedPath.getInstance().populate(scott,"managerOf[name=Bob].managerOf[name=Tania].role","developer");

        // NestedPath extension to implement association when instance is created by using PropertyInstance  
        NestedPath nestedPath = NestedPath.getInstance().setPropertyInstance(new PropertyInstance() {
            public Object indexed(Object bean, String property, int index, Object value) {
                if ( "managerOf".equalsIgnoreCase(property)) {
                    try {
                        NestedPath.getInstance().populate(value,"managedBy",bean);
                    } catch(Exception e ) {
                        throw new IllegalStateException(e);
                    }
                }
                return value;
            }
        });


        //  PropertyInstance is called when a new instance is created, therefore recreate the instance
        nestedPath.populate(scott,"managerOf[name=Bob].managerOf[name=Tania]",null);
        nestedPath.populate(scott,"managerOf[name=Bob].managerOf[name=Tania].role","developer");

        // NestedPath extension to implement association when property value is set using PropertyValue
        nestedPath = NestedPath.getInstance().setPropertyValue(new PropertyValue() {
            public Object indexed(Object bean, String property, int index, Object value) {
                if ( "managerOf".equalsIgnoreCase(property)) {
                    try {
                        NestedPath.getInstance().populate(value,"managedBy",bean);
                    } catch(Exception e ) {
                        throw new IllegalStateException(e);
                    }
                }
                return value;
            }
        });


        //  PropertyInstance is called when a new instance is created, therefore recreate the instance
        nestedPath.populate(robert,"managerOf[name=Scott]",scott);

    }

}
