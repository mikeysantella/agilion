package com.agilion.domain.networkbuilder.datasets;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alex_Lappy_486 on 2/20/18.
 */
public class DataSetReference
{
    private String nodelistLocation;

    private String edgelistLocation;

    public DataSetReference(String nodelistLocation, String edgelistLocation) {
        this.nodelistLocation = nodelistLocation;
        this.edgelistLocation = edgelistLocation;
    }

    public String getNodelistLocation() {

        return nodelistLocation;
    }

    public void setNodelistLocation(String nodelistLocation) {
        this.nodelistLocation = nodelistLocation;
    }

    public String getEdgelistLocation() {
        return edgelistLocation;
    }

    public void setEdgelistLocation(String edgelistLocation) {
        this.edgelistLocation = edgelistLocation;
    }

    public List<String> getAllFiles()
    {
        List<String> list = new LinkedList<>();
        if (StringUtils.isNotBlank(nodelistLocation))
            list.add(nodelistLocation);

        if (StringUtils.isNotBlank(edgelistLocation))
            list.add(edgelistLocation);

        return list;
    }
}
