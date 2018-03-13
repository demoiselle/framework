package org.demoiselle.jee.crud.field;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Subgraph;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.demoiselle.jee.crud.fields.FieldsContext;

public class QueryFieldsHelper {

    private static class SubgraphForPath {
        private final String parentPath;
        private final String currPath;
        private final Subgraph subgraph;

        public SubgraphForPath(String parentPath, String currPath, Subgraph subgraph) {
            this.parentPath = parentPath;
            this.currPath = currPath;
            this.subgraph = subgraph;
        }

        public String getParentPath() {
            return parentPath;
        }

        public String getCurrPath() {
            return currPath;
        }

        public Subgraph getSubgraph() {
            return subgraph;
        }
    }

    public static void configEntityGraphHints(EntityManager em, Query query, Class<?> entityClass, FieldsContext fieldsContext) {
        if (fieldsContext.isFieldsEnabled()) {
            configEntityGraphHints(em, query, entityClass, fieldsContext.getFlatFields());
        }
    }

    public static void configEntityGraphHints(EntityManager em, Query query, Class<?> entityClass, Collection<String> fields) {
        EntityGraph graph = generateEntityGraphForQuery(em, entityClass, fields);
        query.setHint("javax.persistence.loadgraph", em)
                .setHint("javax.persistence.loadgraph", em);
    }

    private static EntityGraph generateEntityGraphForQuery(EntityManager em,
                                                   Class<?> entityClass,
                                                   Collection<String> fields) {
        EntityGraph graph = em.createEntityGraph(entityClass);
        Map<String, Subgraph> subgraphMap = new HashMap<>();
        for (String field : fields) {
            if (field.contains(".")) {
                SubgraphForPath subgraphForPath = processSubgraphPath(graph, subgraphMap, field, "", null);
                while(subgraphForPath.getCurrPath().contains(".")) {
                    subgraphForPath = processSubgraphPath(graph, subgraphMap, subgraphForPath.getCurrPath(), subgraphForPath.getParentPath(), subgraphForPath.getSubgraph());
                }
                subgraphForPath.getSubgraph().addAttributeNodes(subgraphForPath.getCurrPath());
            } else {
                graph.addAttributeNodes(field);
            }
        }
        return graph;
    }

    private static SubgraphForPath processSubgraphPath(EntityGraph graph, Map<String, Subgraph> subgraphMap, String fieldPath, String parentPath, Subgraph parentSubgraph) {
        int firstDotIndex = fieldPath.indexOf(".");
        String firstField = fieldPath.substring(0, firstDotIndex);
        String restField = fieldPath.substring(firstDotIndex+1);
        String subgraphFullPath;
        if (!parentPath.isEmpty()) {
            subgraphFullPath = parentPath + "." + firstField;
        } else {
            subgraphFullPath = firstField;
        }

        Subgraph subgraph = getSubgraphForFullPath(subgraphFullPath, subgraphMap, graph, parentSubgraph);
        return new SubgraphForPath(subgraphFullPath, restField, subgraph);
    }

    private static Subgraph getSubgraphForFullPath(String subgraphFullPath, Map<String, Subgraph> subgraphMap, EntityGraph graph, Subgraph parentSubgraph) {
        String lastField;
        if (subgraphFullPath.contains(".")) {
            lastField = subgraphFullPath.substring(subgraphFullPath.lastIndexOf(".")+1);
        } else {
            lastField = subgraphFullPath;
        }
        if (subgraphMap.containsKey(subgraphFullPath)) {
            return subgraphMap.get(subgraphFullPath);
        } else {
            Subgraph subgraph;
            if (parentSubgraph != null) {
                subgraph = parentSubgraph.addSubgraph(lastField);
            } else {
                subgraph = graph.addSubgraph(lastField);
            }
            subgraphMap.put(subgraphFullPath, subgraph);
            return subgraph;
        }
    }
}
