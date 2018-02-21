package com.agilion.domain.networkbuilder;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
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

    public String getSelectorList() {
        return selectorList;
    }

    public void setSelectorList(String selectorList) {
        this.selectorList = selectorList;
    }

    public SelectorType getSelectorType() {
        return selectorType;
    }

    public void setSelectorType(SelectorType selectorType) {
        this.selectorType = selectorType;
    }
}
