package com.agilion.domain.networkbuilder;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.lang.annotation.Target;
import java.util.Date;
import java.util.List;

public class NetworkBuilderForm
{
    @NotNull
    @NotBlank
    private String projectName;

    @NotNull
    private TargetDeck targetDeck;

    private Date fromDate;

    private Date toDate;

    private List<String> dataSourceSelectionStrings;

    private List<MultipartFile> dataFiles;
}
