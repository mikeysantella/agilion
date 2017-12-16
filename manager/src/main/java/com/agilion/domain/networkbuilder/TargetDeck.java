package com.agilion.domain.networkbuilder;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.Target;
import java.util.List;

public class TargetDeck
{
    @NotEmpty
    private List<TargetDeckEntry> targetDeckEntryList;

    public List<TargetDeckEntry> getTargetDeckEntryList() {
        return targetDeckEntryList;
    }

    public void setTargetDeckEntryList(List<TargetDeckEntry> targetDeckEntryList) {
        this.targetDeckEntryList = targetDeckEntryList;
    }
}
