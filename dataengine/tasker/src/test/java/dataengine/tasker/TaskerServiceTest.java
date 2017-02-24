package dataengine.tasker;

import java.util.function.Supplier;

import javax.inject.Singleton;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

import dataengine.apis.OperationsRegistry_I;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import dataengine.apis.VerticleConsts;
import net.deelam.vertx.jobboard.DepJobService_I;

public class TaskerServiceTest {

  @Before
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(
        new AbstractModule() {
          @Override
          protected void configure() {

            // OperationsRegistry_I used by clients
            bind(OperationsRegistry_I.class).to(OperationsRegistryRpcService.class);
            bind(OperationsRegistryRpcService.class).in(Singleton.class);
            // See http://stackoverflow.com/questions/14781471/guice-differences-between-singleton-class-and-singleton

            // OperationsRegistryVerticle to which operations are registered by providers (ie, Workers)
            OperationsRegistryVerticle opsRegVert = new OperationsRegistryVerticle(VerticleConsts.opsRegBroadcastAddr);
            bind(OperationsRegistryVerticle.class).toInstance(opsRegVert);

            // 
            bind(JobListener.class).in(Singleton.class);

            bind(Tasker_I.class).to(TaskerService.class);
            bind(TaskerService.class).in(Singleton.class);
          }
          
          @Provides @Singleton
          Supplier<SessionsDB_I> getSessionsDBClient() {
            return null; //getClientSupplierFor(SessionsDB_I.class, VerticleConsts.sessionDbBroadcastAddr); // blocks
          }
          
          @Provides @Singleton
          Supplier<DepJobService_I> getDepJobServiceClient() {
            return  null; //getClientSupplierFor(DepJobService_I.class, VerticleConsts.depJobMgrBroadcastAddr); // blocks
          }
        });
  }

  @Test
  public void test() {
    //fail("Not yet implemented");
  }

}
