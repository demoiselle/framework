/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.entity;

/**
 * @author SERPRO
 *
 */
public class AddressModelForTest {
    
    private Long id;
    private String address;
    private String street;
    private CountryModelForTest country;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public CountryModelForTest getCountry() {
        return country;
    }
    public void setCountry(CountryModelForTest country) {
        this.country = country;
    }
    public String getStreet() {
        return street;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    @Override
    public String toString() {
        return "AddressModelForTest [id=" + id + ", address=" + address + ", street=" + street + ", country=" + country
                + "]";
    }
}
