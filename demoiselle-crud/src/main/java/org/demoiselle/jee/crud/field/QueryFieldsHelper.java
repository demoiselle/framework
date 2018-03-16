package org.demoiselle.jee.crud.field;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Subgraph;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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

    public static TypedQuery createFilteredQuery(EntityManager em, CriteriaQuery criteriaQuery, Class<?> entityClass, FieldsContext fieldsContext) {
        if (fieldsContext.isFieldsEnabled()) {
            return createFilteredQuery(em, criteriaQuery, entityClass, excludeNonEntityFields(em.getMetamodel(), entityClass, fieldsContext.getFlatFields()));
        }
        return em.createQuery(criteriaQuery);
    }

    private static List<String> excludeNonEntityFields(Metamodel metaModel, Class<?> entityClass, List<String> flatFields) {
        return flatFields.stream()
                .filter(field -> isEntityField(metaModel, entityClass, field))
                .collect(Collectors.toList());
    }

    private static boolean isEntityField(Metamodel metaModel, Class<?> entityClass, String field) {
        if (!field.contains(".")) {
            return metaModel.entity(entityClass).getAttributes().stream().anyMatch(attr -> attr.getName().toLowerCase() == field);
        } else {
            Class<?> currClass = entityClass;
            for (String currField : StringUtils.split(field, ".")) {
                if (!metaModel.entity(currClass).getAttributes().stream().anyMatch(attr -> attr.getName().toLowerCase() == currField)) {
                    return false;
                }
                currClass = metaModel.entity(currClass).getAttribute(currField).getJavaType();
            }
            return true;
        }
    }

    public static TypedQuery createFilteredQuery(EntityManager em, CriteriaQuery criteriaQuery, Class<?> entityClass, Collection<String> fields) {

        EntityGraph graph = generateEntityGraphForQuery(em, entityClass, fields);
        TypedQuery query = em.createQuery(criteriaQuery);
        query.setHint("javax.persistence.loadgraph", graph)
                .setHint("javax.persistence.loadgraph", graph);
        return query;
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
