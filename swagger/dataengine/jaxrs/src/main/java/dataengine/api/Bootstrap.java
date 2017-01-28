package dataengine.api;

import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.*;

import io.swagger.models.auth.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class Bootstrap extends HttpServlet {
  @Override
  public void init(ServletConfig config) throws ServletException {
    Info info = new Info()
      .title("Swagger Server")
      .description("orchestrates backend jobs")
      .termsOfService("")
      .contact(new Contact()
        .email("envincior@deelam.net"))
      .license(new License()
        .name("LGPL 3.0")
        .url("http://www.gnu.org/licenses/lgpl-3.0.txt"));

    ServletContext context = config.getServletContext();
    Swagger swagger = new Swagger().info(info);

    new SwaggerContextService().withServletConfig(config).updateSwagger(swagger);
  }
}
