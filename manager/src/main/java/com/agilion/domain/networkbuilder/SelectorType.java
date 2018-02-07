package com.agilion.domain.networkbuilder;

public enum SelectorType {
    MSISDN("MSISDN"),
    IMSI("IMSI"),
    EMAIL("Email Address"),
    SOCIAL_MEDIA_ID("Social Media Handle"),
    NAME("Name");

    private String displayName;

    private SelectorType(String s)
    {
        this.displayName = s;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }
}
