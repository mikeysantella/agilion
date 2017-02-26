package dataengine.workers;

import java.util.Collection;

import dataengine.api.Operation;

public interface Worker_I {

  String getName();

  Collection<Operation> getOperations();

}
