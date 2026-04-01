/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Test entity for mergeHalf() unit tests.
 * Contains fields with various JPA annotations to exercise all code paths:
 * - @Id field (should be used for WHERE clause, not SET)
 * - @Column(updatable=true) field (should be included in SET)
 * - @Column(updatable=false) field (should be excluded from SET)
 * - @ManyToOne field (should be included regardless of @Column)
 * - Field without @Column or @ManyToOne (should be excluded)
 */
public class MergeHalfEntityForTest {

    @Id
    private Long id;

    @Column(updatable = true)
    private String name;

    @Column(updatable = false)
    private String createdBy;

    @ManyToOne
    private CountryModelForTest country;

    @Column(updatable = true)
    private Integer age;

    /** Field without @Column or @ManyToOne — should be excluded by mergeHalf() */
    private String unmapped;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public CountryModelForTest getCountry() { return country; }
    public void setCountry(CountryModelForTest country) { this.country = country; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getUnmapped() { return unmapped; }
    public void setUnmapped(String unmapped) { this.unmapped = unmapped; }
}
