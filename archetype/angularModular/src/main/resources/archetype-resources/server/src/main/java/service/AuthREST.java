package ${package}.service;

import br.gov.frameworkdemoiselle.security.AuthenticationException;
import br.gov.frameworkdemoiselle.security.Credentials;
import br.gov.frameworkdemoiselle.security.LoggedIn;
import br.gov.frameworkdemoiselle.security.SecurityContext;
import br.gov.frameworkdemoiselle.transaction.Transactional;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ValidatePayload;
import ${package}.util.Util;
import io.swagger.annotations.Api;
import java.io.Serializable;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import javax.ws.rs.core.Response;

/**
 *
 * @author 70744416353
 */
@Api(value = "auth")
@Path("auth")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class AuthREST implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SecurityContext securityContext;

    /**
     *
     * @param data
     * @return
     */
    @POST
    @ValidatePayload
    @Transactional
    public Response login(CredentialsData data) {
        Credentials credentials = Beans.getReference(Credentials.class);
        credentials.setUsername(data.username);
        credentials.setPassword(Util.MD5(data.password));

        try {
            securityContext.login();
        } catch (AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok().header("Access-Control-Expose-Headers", "Set-Token").build();
    }

    /**
     *
     */
    public static class CredentialsData {

        /**
         *
         */
        @NotNull(message = "{required.field}")
        @Size(min = 1, message = "{required.field}")
        public String username;

        /**
         *
         */
        @NotNull(message = "{required.field}")
        @Size(min = 1, message = "{required.field}")
        public String password;
    }
}
