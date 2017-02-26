package dataengine.main;

import org.glassfish.jersey.server.ResourceConfig;

public class MainJerseyRestResource extends ResourceConfig {
  
  public MainJerseyRestResource() {
    
    //setClassLoader(MainJerseyRestResource.class.getClassLoader());
    
    // REST service operations
    packages("dataengine.api");
    
    // GSON message body writer/reader
    //packages(GsonProvider.class.getPackage().getName());
    //register(new GuiceFeature(injector));
    
    // settings copied from web.xml
    property("jersey.config.server.provider.classnames", 
        org.glassfish.jersey.media.multipart.MultiPartFeature.class.getCanonicalName());
    property("jersey.config.server.wadl.disableWadl", "true");
    property("jersey.config.server.wadl.disableWadl", "true");
  }
  
  /*
   * Easiest way to get the ServiceLocator without it being injected...
   * Make a Feature to be registered and intercept the GuiceBridge here.
   */
//  @RequiredArgsConstructor
//  static class GuiceFeature implements Feature {
//    final Injector injector;
//    @Override
//    public boolean configure(FeatureContext context) {
//      ServiceLocator locator = ServiceLocatorProvider.getServiceLocator(context);
//      GuiceBridge.getGuiceBridge().initializeGuiceBridge(locator);
//      GuiceIntoHK2Bridge guiceBridge = locator.getService(GuiceIntoHK2Bridge.class);
//      guiceBridge.bridgeGuiceInjector(injector);
//      return true;
//    }
//  }
}

