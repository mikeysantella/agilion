package dataengine.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.google.inject.Injector;

import dataengine.api.Bootstrap;
import lombok.extern.slf4j.Slf4j;

// TODO: 2: run without gretty
@Slf4j
public class MainJetty {
  public static void main(String[] args) throws Exception {
    MainJetty main = new MainJetty();
    
    Server jettyServer = main.startServer(8080, 8083, "/server/deelam/DataEngine/0.0.1", 
        null, null, false);
    
    try {
      jettyServer.start();
      jettyServer.join();
  } finally {
      jettyServer.destroy();
  }
  }
  
  protected Properties props=new Properties();
  Injector injector;

  private Server startServer(int port, int sslPort, String contextPath,
      String keyStoreFile, String keyStorePwd, boolean validateCerts) throws FileNotFoundException {
    final ResourceConfig rc = new MainJerseyRestResource();
    ServletContainer servletCont = new ServletContainer(rc);
    ServletHolder servletHolder = new ServletHolder(servletCont);

    Server server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    
    ServletContextHandler context = new ServletContextHandler(server, contextPath);
    context.addServlet(servletHolder, "/*");
    server.setHandler(context);
    
    if(keyStoreFile!=null){
      File keystoreFile = new File(keyStoreFile);
      if(!keystoreFile.exists())
        throw new FileNotFoundException("Cannot find "+keystoreFile.getAbsolutePath());
      
      String keyStorePath=keystoreFile.getAbsolutePath();
      log.info("keyStorePath={}",keyStorePath);
      if(keyStorePwd!=null)
        log.info("keyStorePwd.length={}",keyStorePwd.length());

      HttpConfiguration https = new HttpConfiguration();
      https.addCustomizer(new SecureRequestCustomizer());
      SslContextFactory sslContextFactory = new SslContextFactory();
      sslContextFactory.setKeyStorePath(keyStorePath);
      if(keyStorePwd!=null){
        sslContextFactory.setKeyStorePassword(keyStorePwd);
        sslContextFactory.setKeyManagerPassword(keyStorePwd);
      }
      log.info("validateCerts={}",validateCerts);
      sslContextFactory.setValidateCerts(validateCerts);
      ServerConnector sslConnector = new ServerConnector(server,
          new SslConnectionFactory(sslContextFactory, "http/1.1"),
          new HttpConnectionFactory(https));
      sslConnector.setPort(sslPort);
      server.setConnectors(new Connector[] { connector, sslConnector });
    } else {
      server.setConnectors(new Connector[] { connector });
    }
    log.info("Starting server");
    return server;
  }
}
