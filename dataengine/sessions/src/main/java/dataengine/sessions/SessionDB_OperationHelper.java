package dataengine.sessions;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static net.deelam.graph.GrafTxn.tryOn;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.frames.FramedTransactionalGraph;

import dataengine.api.OperationSelection;
import dataengine.api.OperationSelectionMap;
import dataengine.sessions.frames.OperationSelectionFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SessionDB_OperationHelper {
  private final FramedTransactionalGraph<TransactionalGraph> graph;

  static final String OPERATION_PARAMS_PROPPREFIX = "params.";

  OperationSelectionFrame addOperationNode(String opSelId, OperationSelection op) {
    log.info("SESS: addOperationNode: '{}' id={}", opSelId, op.getId());
    checkNotNull(opSelId);
    return tryOn(graph, graph -> {
      OperationSelectionFrame of = graph.getVertex(opSelId, OperationSelectionFrame.class);
      if (of == null) {
        of = graph.addVertex(opSelId, OperationSelectionFrame.class);
        of.setOperationId(op.getId());
        if (op.getParams() != null)
          SessionDB_FrameHelper.saveMapAsProperties(op.getParams(),
              of.asVertex(), OPERATION_PARAMS_PROPPREFIX);
        
        if(op.getSubOperationSelections()!=null){
          for(Map.Entry<String,OperationSelection> entry:op.getSubOperationSelections().entrySet()){
            OperationSelectionFrame subOpFrame = addOperationNode(opSelId+"."+entry.getKey(), entry.getValue());
            of.addSubOperation(subOpFrame);
          }
        }
        return of;
      } else {
        throw new IllegalArgumentException("OperationSelection.id already exists: " + opSelId);
      }
    });

  }

  public static OperationSelection toOperationSelection(OperationSelectionFrame of) {
    OperationSelectionMap subOperationSelections=new OperationSelectionMap();
    for(Iterator<OperationSelectionFrame> itr = of.getSubOperations().iterator();itr.hasNext();){
      OperationSelectionFrame subOF=itr.next();
      OperationSelection subOpSel=toOperationSelection(subOF);
      subOperationSelections.put(subOpSel.getId(), subOpSel);
    };
    if(subOperationSelections.isEmpty())
      subOperationSelections=null;
    return new OperationSelection()
        .id(of.getOperationId())
        .params(SessionDB_FrameHelper.loadPropertiesAsMap(of.asVertex(), OPERATION_PARAMS_PROPPREFIX))
        .subOperationSelections(subOperationSelections);
  }

  static List<OperationSelection> toOperationSelections(Iterable<OperationSelectionFrame> requests) {
    return stream(requests.spliterator(), false)
        .sorted(SessionDB_FrameHelper.createdTimeComparator)
        .map(req -> toOperationSelection(req))
        .collect(toList());
  }
}
