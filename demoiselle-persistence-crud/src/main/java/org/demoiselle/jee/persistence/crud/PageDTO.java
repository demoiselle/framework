/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.crud;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("rawtypes")
public class PageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long total;

    private int from;

    private int size;

    private String fields;

    private String search;

    private List content;

    public PageDTO(List content, int from, int size, long total) {
        super();
        this.from = from;
        this.size = size;
        this.total = total;
        this.content = content;
    }

    public PageDTO(List content, int from, int size, long total, String fields, String search) {
        super();
        this.from = from;
        this.size = size;
        this.total = total;
        this.fields = fields;
        this.search = search;
        this.content = content;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public List getContent() {
        return content;
    }

    public void setContent(List content) {
        this.content = content;
    }

}
