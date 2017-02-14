/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * This class helps the CRUD feature to hold the fields on a Tree structure.
 * 
 * @author SERPRO
 */
public class TreeNodeField<T, K> {

    private T key;
    private K value;
    private TreeNodeField<T, K> parent;
    private List<TreeNodeField<T, K>> children;

    public TreeNodeField(T key, K value) {
        this.key = key;
        this.value = value;
        this.children = new LinkedList<>();
    }

    public TreeNodeField<T, K> addChild(T key, K value) {
        TreeNodeField<T, K> childNode = new TreeNodeField<T, K>(key, value);
        childNode.parent = this;
        this.children.add(childNode);
        return childNode;
    }
    
    public TreeNodeField<T, K> getParent() {
        return this.parent;
    }
    
    public T getKey() {
        return this.key;
    }
    
    public K getValue() {
        return this.value;
    }

    public List<TreeNodeField<T, K>> getChildren() {
        return this.children;
    }
    
    public TreeNodeField<T, K> getChildByKey(T key){
        return getChildren().stream()
                .filter( (child) -> child.getKey().equals(key))
                .findAny()
                .orElse(null);
    }
    
    public Boolean containsKey(T key){
        return getChildren().stream()
                .filter( (child) -> child.getKey().equals(key)).count() > 0;
    }

    @Override
    public String toString() {
        return "TreeNodeField [key=" + key + ", value=" + value + ", children=" + children + "]";
    }
    
}
