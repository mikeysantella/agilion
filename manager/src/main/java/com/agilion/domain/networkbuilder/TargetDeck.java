package com.agilion.domain.networkbuilder;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

public class TargetDeck
{
    /**
     * The list of targets, in string/type form
     */
    @Valid
    private List<TargetDeckEntry> targetDeckEntryList = new ArrayList<>();

    /**
     * The list of targets, in file form

     */
    private MultipartFile selectorFile;

    public List<TargetDeckEntry> getTargetDeckEntryList()
    {
        return targetDeckEntryList;
    }

    public void setTargetDeckEntryList(List<TargetDeckEntry> targetDeckEntryList) {
        this.targetDeckEntryList = targetDeckEntryList;
    }

    public MultipartFile getSelectorFile() {
        return selectorFile;
    }

    public void setSelectorFile(MultipartFile selectorFile) {
        this.selectorFile = selectorFile;
    }
}
