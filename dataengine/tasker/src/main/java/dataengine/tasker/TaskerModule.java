package dataengine.tasker;

import static java.util.stream.Collectors.toList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.Connection;
import javax.jms.JMSException;
import org.apache.curator.framework.CuratorFramework;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import dataengine.apis.CommunicationConsts;
import dataengine.apis.RpcClientProvider;
import dataengine.apis.SessionsDB_I;
import dataengine.apis.Tasker_I;
import dataengine.tasker.jobcreators.AddSourceDataset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.deelam.activemq.rpc.ActiveMqRpcServer;
import net.deelam.activemq.rpc.AmqComponentSubscriber;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
final class TaskerModule extends AbstractModule {
  final String jobCreatorsString;
  
  @Override
  protected void configure() {
    requireBinding(Connection.class);
    requireBinding(CuratorFramework.class); // for DispatcherComponentListener
    requireBinding(Key.get(new TypeLiteral<RpcClientProvider<SessionsDB_I>>() {}));
    
//    bind(Properties.class).toInstance(properties);

    install(new FactoryModuleBuilder()
        .implement(JobListener_I.class, TaskerJobListener.class)
        .build(JobListener_I.Factory.class));
    
    install(new FactoryModuleBuilder()
        .build(JobProcessingEntry.Factory.class));
    
    // See http://stackoverflow.com/questions/14781471/guice-differences-between-singleton-class-and-singleton
    bind(Tasker_I.class).to(TaskerService.class);
    bind(TaskerService.class).in(Singleton.class);
  }

  
  @Provides
  List<JobsCreator_I> getJobCreators(Injector injector) {
    // read jobCreators from file
    String jobCreatorsStr=jobCreatorsString;
    if(jobCreatorsStr==null) {
      String[] defaultJobCreatorClasses={
        AddSourceDataset.class.getCanonicalName()
      };
      jobCreatorsStr = String.join(" ",defaultJobCreatorClasses);
      log.info("Using default jobCreators: {}", jobCreatorsStr);
    }
    List<String> classes = Arrays.asList(jobCreatorsStr.split(" "));
    List<JobsCreator_I> jobCreators = classes.stream().map(jcClassName -> {
      try {
        @SuppressWarnings("unchecked")
        Class<? extends JobsCreator_I> addSrcDataClazz = (Class<? extends JobsCreator_I>) Class.forName(jcClassName);
        JobsCreator_I jc = injector.getInstance(addSrcDataClazz);
        return jc;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).collect(toList());
    return jobCreators;
  }

  static void deployTasker(Injector injector) throws JMSException {
    TaskerService taskerSvc = injector.getInstance(TaskerService.class);
    
    log.info("AMQ: SERV: Deploying RPC service for TaskerService: {} ", taskerSvc); 
    taskerSvc.setTaskerRpcAddr(CommunicationConsts.TASKER_RPCADDR);
    injector.getInstance(ActiveMqRpcServer.class).start(taskerSvc.getTaskerRpcAddr(), taskerSvc, true);
    
    Connection connection=injector.getInstance(Connection.class);
    new AmqComponentSubscriber(connection, "Tasker", 
        CommunicationConsts.RPC_ADDR, taskerSvc.getTaskerRpcAddr(),
        CommunicationConsts.COMPONENT_TYPE, "Tasker");
  }
  
  static DispatcherComponentListener deployDispatcherListener(Injector injector, String dispatcherTypeZkPath) {
    // detection via Zookeeper
    DispatcherComponentListener dispatcherListener = injector.getInstance(DispatcherComponentListener.class);
    log.info("ZK: SERV: Deploying DispatcherComponentListener for TaskerService: {} ", dispatcherListener); 
    dispatcherListener.start(dispatcherTypeZkPath);
    return dispatcherListener;
  }

}
