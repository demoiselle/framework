package org.demoiselle.jee.crud.entity;/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */

import org.demoiselle.jee.crud.entity.AddressModelForTest;
import org.demoiselle.jee.crud.entity.UserType;

/**
 * 
 * @author SERPRO
 *
 */
public class UserModelForTest {
    
    private Long id;
    private String name;
    private String mail;
    private Integer age;
    private UserType userType;
    private AddressModelForTest address;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public AddressModelForTest getAddress() {
        return address;
    }

    public void setAddress(AddressModelForTest address) {
        this.address = address;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    @Override
    public String toString() {
        return "UserModelForTest [id=" + id + ", name=" + name + ", mail=" + mail + ", age=" + age + ", address="
                + address + ", userType = "+userType+"]";
    }
    
}
