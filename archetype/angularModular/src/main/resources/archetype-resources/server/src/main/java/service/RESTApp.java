package ${package}.service;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Info;
import io.swagger.models.License;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 *
 * @author 70744416353
 */
@ApplicationPath("api")
public class RESTApp extends Application {

    /**
     *
     */
    public RESTApp() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setBasePath("/bookmark/api");
        beanConfig.setResourcePackage("${package}.service");
        Info info = new Info();
        info.setDescription("Sistema de exemplo do Demoiselle 2.5 com AngularJS.");
        info.setTitle("App");
        beanConfig.setInfo(info);
        beanConfig.setScan(true);
    }
}
