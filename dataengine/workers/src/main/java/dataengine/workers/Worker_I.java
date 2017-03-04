package dataengine.workers;

import java.util.ArrayList;
import java.util.Collection;

import dataengine.api.Operation;

public interface Worker_I {

  String name();

  Operation operation();
  
  default Collection<Operation> operations(){
   ArrayList<Operation> list = new ArrayList<>();
   list.add(operation());
   return list;
  }

}
