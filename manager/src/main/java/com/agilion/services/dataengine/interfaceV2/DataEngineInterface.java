package com.agilion.services.dataengine.interfaceV2;

import com.agilion.services.dataengine.DataOperationReceipt;
import dataengine.api.OperationSelection;

/**
 * Created by Alex_Lappy_486 on 1/26/18.
 */
public interface DataEngineInterface
{
    public DataOperationReceipt submitDataEngineOperation(OperationSelection selection, String username, String sessionID);

    public DataOperationStatus getDataEngineOperationStatus(DataOperationReceipt id);
}
