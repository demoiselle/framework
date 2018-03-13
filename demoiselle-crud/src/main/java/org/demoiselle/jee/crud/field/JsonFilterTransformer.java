package org.demoiselle.jee.crud.field;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.monitorjbl.json.JsonView;
import com.monitorjbl.json.JsonViewModule;
import com.monitorjbl.json.Match;
import io.swagger.util.Json;

public class JsonFilterTransformer implements Function {
    private Class resultClass;
    private String[] fields;

    private static final JsonViewModule JSON_VIEW_MODULE = new JsonViewModule();
    public JsonFilterTransformer(Class resultClass, String... fields) {
        this.resultClass = resultClass;
        this.fields = fields;
    }

    public JsonFilterTransformer(Class resultClass, List<String> fields) {
        this(resultClass, fields.toArray(new String[fields.size()]));
    }

    @Override
    public Object apply(Object o) {
        try {
            Json.mapper().registerModule(JSON_VIEW_MODULE);
            String value = Json.mapper()
                    .writeValueAsString(
                            JsonView.with(o)
                                    .onClass(resultClass,
                                            Match.match()
                                                    .exclude("*")
                                                    .include(fields)));
            return Json.mapper().readValue(value, Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
