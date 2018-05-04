package org.demoiselle.jee.wrapper;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.ParameterizedType;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;
import static javax.ws.rs.core.HttpHeaders.USER_AGENT;
import javax.ws.rs.core.MultivaluedMap;

import org.demoiselle.jee.core.exception.DemoiselleException;
import org.demoiselle.jee.wrapper.security.Authentication;
import org.demoiselle.jee.wrapper.security.Credentials;

public abstract class AbstractAPIWrapper<T, I> {

    private final Class<T> entityClass;

    protected abstract String resourceApi();

    protected abstract String resourceAuth();

    protected abstract String resourceUser();

    protected abstract String resourcePassword();

    protected abstract Boolean isAuthenticated();

    private Logger logger = Logger.getLogger(this.getClass().getName());

    @SuppressWarnings("unchecked")
    public AbstractAPIWrapper() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    protected Authentication getAuthentication() {
        Credentials cred = new Credentials();
        cred.setUsername(resourceUser());
        cred.setPassword(resourcePassword());
        return new Gson().fromJson(post(Boolean.FALSE, resourceAuth(), new Gson().toJson(cred)), Authentication.class);
    }

    public T post(T entity) {
        try {
            return new Gson().fromJson(post(isAuthenticated(), resourceApi(), new Gson().toJson(entity)), entityClass);
        } catch (JsonSyntaxException e) {
            throw new DemoiselleException("Não foi possível salvar", e);
        }
    }

    public T get(I id) {
        try {
            return new Gson().fromJson(get(isAuthenticated(), resourceApi() + id.toString()), entityClass);
        } catch (JsonSyntaxException e) {
            throw new DemoiselleException("Não foi possível salvar", e);
        }
    }

    public T patch(I id, T entity) {
        try {
            throw new UnsupportedOperationException("Not supported yet.");
        } catch (final Exception e) {
            throw new DemoiselleException("Não foi possível salvar", e);
        }
    }

    public T put(T entity) {
        try {
            throw new UnsupportedOperationException("Not supported yet.");
        } catch (Exception e) {
            throw new DemoiselleException("Não foi possível salvar", e);
        }
    }

    public void delete(I id) {
        try {
            throw new UnsupportedOperationException("Not supported yet.");
        } catch (Exception e) {
            throw new DemoiselleException("Não foi possível excluir", e);
        }
    }

    public List<T> queryString(MultivaluedMap map) {
        List<T> list = new Gson().fromJson(get(isAuthenticated(), resourceApi() + parser(map)), new TypeToken<List<T>>() {
        }.getType());
        if (list != null) {
            return list;
        } else {
            throw new DemoiselleException("Erro no servidor ");
        }
    }

    public List<T> path(String path) {
        List<T> list = new Gson().fromJson(get(isAuthenticated(), resourceApi() + path), new TypeToken<List<T>>() {
        }.getType());
        if (list != null) {
            return list;
        } else {
            throw new DemoiselleException("Erro no servidor ");
        }
    }

    public List<T> params(String... params) {
        List<T> list = new Gson().fromJson(get(isAuthenticated(), resourceApi() + parser(params)), new TypeToken<List<T>>() {
        }.getType());
        if (list != null) {
            return list;
        } else {
            throw new DemoiselleException("Erro no servidor ");
        }
    }

    private String parser(MultivaluedMap map) {
        StringBuilder sb = new StringBuilder();

        sb.append("?");
        if (map != null) {
            Iterator it = map.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                sb.append(key)
                        .append("=")
                        .append(map.get(key))
                        .append("&");
            }
        }

        return sb.toString();
    }

    private String parser(String... params) {
        StringBuilder sb = new StringBuilder();

        for (String param : params) {
            sb.append(param).append("/");
        }

        return sb.toString();
    }

    private String post(Boolean isAuthenticated, String resource, String body) {
        try {

            int responseCode = 0;
            StringBuilder response = new StringBuilder();

            URL obj = new URL(resource);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            if (isAuthenticated) {
                Authentication auth = getAuthentication();
                con.setRequestProperty("Authorization", auth.getType() + " " + auth.getKey());
            }

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(con.getOutputStream());
            outputStreamWriter.write(body);
            outputStreamWriter.flush();
            responseCode = con.getResponseCode();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            if (responseCode == 200) {
                return response.toString();
            }

        } catch (MalformedURLException ex) {
            logger.log(SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(SEVERE, null, ex);
        }
        return null;
    }

    private String get(Boolean isAuthenticated, String path) {
        try {

            StringBuilder response = new StringBuilder();

            URL obj = new URL(path);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Content-Type", "application/json");

            if (isAuthenticated) {
                Authentication auth = getAuthentication();
                con.setRequestProperty("Authorization", auth.getType() + " " + auth.getKey());
            }

            con.setDoOutput(true);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            if (con.getResponseCode() >= 200 && con.getResponseCode() <= 210) {
                return response.toString();
            }

        } catch (MalformedURLException ex) {
            logger.log(SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(SEVERE, null, ex);
        }
        return null;
    }
}
