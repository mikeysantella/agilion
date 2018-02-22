package com.agilion.services.dataengine;

import com.agilion.domain.networkbuilder.datasets.DataSet;
import com.agilion.domain.networkbuilder.datasets.DataSetReference;
import dataengine.api.Operation;

import java.util.List;
import java.util.Map;

/**
 * Defines operations for communicating with the data engine.
 */
public interface DataEngineClient
{
    /**
     * Returns all
     * @return
     */
    public List<String> getSelectorTypes();

    public List<String> getDataSources();

    public List<Operation> listOperations();

    public DataOperationReceipt startNetworkBuild(String sessionID, String username, List<DataSetReference> datasets,
                                                  Map<String, Object> params)
            throws Exception;

    public boolean networkBuildIsDone(DataOperationReceipt receipt);
}
