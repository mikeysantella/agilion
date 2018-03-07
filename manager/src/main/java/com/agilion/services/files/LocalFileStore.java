package com.agilion.services.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This implementation of Filestore stores and retrieves files locally. Note that all locations passed in as arguments
 * should be relative to the root directory passed in to the constructor.
 */
public class LocalFileStore implements FileStore
{
    private String rootDir;

    public LocalFileStore(String rootDirectory)
    {
        this.rootDir = rootDirectory;

        new File(this.rootDir).mkdirs();
    }

    /**
     * NOTE: This method closes the input stream!
     * @param io
     * @param destination
     * @return
     * @throws IOException
     */
    @Override
    public String storeFile(InputStream io, String destination) throws IOException {
        String finalResult = null;
        Path targetPath = Paths.get(getFullPath(destination));

        Files.copy(io, targetPath);
        finalResult = targetPath.toUri().toASCIIString();

        try
        {
            io.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return finalResult;
    }

    @Override
    public InputStream getFile(String location) throws IOException {
        return Files.newInputStream(Paths.get(getFullPath(location)));
    }

    private String getFullPath(String dest)
    {
        return rootDir+"/"+dest;
    }
}
