package net.deelam.vertx.jobboard;

import net.deelam.vertx.AbstractServerVerticle;

public class JobMarketVerticle extends AbstractServerVerticle{

  private final JobBoard market=new JobBoard("");
  
  public JobMarketVerticle(String serviceType, String inboxAddressBase) {
    super(serviceType, inboxAddressBase);
  }

  @Override
  protected void registerMsgHandlers() {
    // TODO Auto-generated method stub
    
  }

}
