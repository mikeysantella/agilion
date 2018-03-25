package com.agilion.services.files;

import java.io.InputStream;

/**
 * An interface for storing/retrieving files. Useful if we're going to be using some kind of hosted filesystem for
 * interactions between the UI/Data Engine (S3, HDFS, etc).
 */
public interface FileStore
{
    /**
     * This method should write the input stream to a file located at the path described by the destination argument.
     *
     * @param io
     * @return
     */
    public String storeFile(InputStream io, String destination) throws Exception;

    public InputStream getFile(String location) throws Exception;
}
