package dataengine.workers;

import java.util.Collection;

import dataengine.api.Operation;

public interface Worker_I {

  String name();

  Collection<Operation> operations();

}
