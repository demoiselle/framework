/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.entity;

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
    
}
