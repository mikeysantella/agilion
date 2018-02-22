package com.agilion.domain.networkbuilder.datasets;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Alex_Lappy_486 on 2/15/18.
 *
 * This class models a dataset sent by the user on the NetworkBuild page. A Dataset is a grouping of a "nodelist" file
 * and an "edgelist" file. An edgelist file describes communication between nodes, while a nodelist describes detailed
 * information about each node.
 */
public class DataSet
{
    private MultipartFile nodelist;

    private MultipartFile edgelist;

    public MultipartFile getNodelist() {
        return nodelist;
    }

    public void setNodelist(MultipartFile nodelist) {
        this.nodelist = nodelist;
    }

    public MultipartFile getEdgelist() {
        return edgelist;
    }

    public void setEdgelist(MultipartFile edgelist) {
        this.edgelist = edgelist;
    }
}
