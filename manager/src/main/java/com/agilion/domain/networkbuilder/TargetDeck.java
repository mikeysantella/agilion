package com.agilion.domain.networkbuilder;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

public class TargetDeck
{
    /**
     * The list of targets, in string/type form
     */
    private List<TargetDeckEntry> targetDeckEntryList = new ArrayList<>();

    /**
     * The list of targets, in file form

     */
    private List<MultipartFile> selectorFiles;

    public List<TargetDeckEntry> getTargetDeckEntryList()
    {
        return targetDeckEntryList;
    }

    public void setTargetDeckEntryList(List<TargetDeckEntry> targetDeckEntryList) {
        this.targetDeckEntryList = targetDeckEntryList;
    }

    public List<MultipartFile> getSelectorFiles() {
        return selectorFiles;
    }

    public void setSelectorFiles(List<MultipartFile> selectorFiles) {
        this.selectorFiles = selectorFiles;
    }
}
