package com.agilion.services.dataengine;

import java.util.List;
import java.util.Map;

public interface DataEngineClient
{
    public List<String> getSelectorTypes();

    public List<String> getDataSources();

    public void startNetworkBuild(String sessionID, String username, List<String> dataFilePaths, Map<String, Object> params) throws Exception;
}
