package com.agilion.services;

/**
 * Created by Alex_Lappy_486 on 3/18/17.
 */
public enum DataIngestJobType
{
    QUERY("API Query"), FILE_UPLOAD("File Upload");

    private DataIngestJobType(String display)
    {
        this.displayName = display;
    }

    private String displayName;

    public String getDisplayName()
    {
        return this.displayName;
    }
}
