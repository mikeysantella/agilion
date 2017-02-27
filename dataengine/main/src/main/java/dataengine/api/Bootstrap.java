package dataengine.api;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import dataengine.main.MainJetty;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Swagger;

/**
 * For Gretty to work for this project,
 * this class overrides the same class in Swagger's dataengine-api.
 */
public class Bootstrap extends HttpServlet {
  @Override
  public void init(ServletConfig config) throws ServletException {
    System.out.println("==== Bootstrap.init()");
    Info info = new Info()
      .title("Swagger Server")
      .description("orchestrates backend jobs")
      .termsOfService("")
      .contact(new Contact()
        .email("agilion@deelam.net"))
      .license(new License()
        .name("LGPL 3.0")
        .url("http://www.gnu.org/licenses/lgpl-3.0.txt"));

    ServletContext context = config.getServletContext();
    Swagger swagger = new Swagger().info(info);

    new SwaggerContextService().withServletConfig(config).updateSwagger(swagger);

    MainJetty.injectVertx(true);
  }
}
