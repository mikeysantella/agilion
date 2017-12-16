package com.agilion.domain.networkbuilder;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class TargetDeckEntry
{
    /**
     * The entire selector list, delimited by newlines.
     */
    private String selectorList;

    /**
     * The type of selector
     */
    private SelectorType selectorType;

    /**
     * A file containing selectors
     */
    private MultipartFile uploadedSelectorFile;
}
