package com.agilion.domain.networkbuilder.datasets;

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
}
