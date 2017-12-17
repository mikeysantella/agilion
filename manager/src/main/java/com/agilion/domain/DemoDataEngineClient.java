package com.agilion.domain;

import com.agilion.services.DataEngineClient;
import jersey.repackaged.com.google.common.collect.Lists;

import java.util.List;

public class DemoDataEngineClient implements DataEngineClient
{
    public List<String> getSelectorTypes()
    {
        return Lists.newArrayList("MSISDN", "IMSI", "Social Media Handle");
    }
}
