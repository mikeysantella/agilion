package com.agilion.domain.mock;

import com.agilion.services.DataEngineClient;
import jersey.repackaged.com.google.common.collect.Lists;

import java.util.List;

public class MockDataEngineClient implements DataEngineClient
{
    public List<String> getSelectorTypes()
    {
        return Lists.newArrayList("MSISDN", "IMSI", "Social Media Handle");
    }

    public List<String> getDataSources(){
        return Lists.newArrayList("Facebook", "DeviantArt", "Reddit", "MeleeItOnMe");
    }
}
