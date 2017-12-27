package com.agilion.utils;

import com.agilion.domain.networkbuilder.NetworkBuilderForm;
import com.agilion.domain.networkbuilder.TargetDeckEntry;
import com.agilion.services.files.FileStore;
import com.agilion.services.jobmanager.JobRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * This class converts the NetworkBuilderForm object, uploaded by users on the "Network Builder page", into a JobRequest,
 * which commands the JobManagerServer to start processing/gathering data.
 */
public class NetworkFormToJobRequestConverter
{
    private FileStore fileStore;

    public NetworkFormToJobRequestConverter(FileStore store)
    {
        this.fileStore = store;
    }

    public JobRequest convertNetworkFormToJobRequest(NetworkBuilderForm form) throws Exception
    {
        // First store the files. We'll take the paths and include them in the JobRequest
        List<String> dataFiles = new LinkedList<>();
        List<String> selectorFiles = new LinkedList<>();

        for (MultipartFile multipartFile : form.getDataFiles())
        {
            dataFiles.add(fileStore.storeFile(multipartFile.getInputStream(), UUID.randomUUID().toString()));
        }
        for (MultipartFile multipartFile : form.getTargetDeck().getSelectorFiles())
        {
            selectorFiles.add(fileStore.storeFile(multipartFile.getInputStream(), UUID.randomUUID().toString()));
        }

        // Set the dates as parameters
        Map<String, Object> params = new HashMap<>();
        params.put("fromDate", form.getFromDate());
        params.put("toDate", form.getToDate());

        if (form.getTargetDeck().getTargetDeckEntryList().size() < 1)
        {
            return new JobRequest(form.getProjectName(), selectorFiles, dataFiles, form.getDataSources(), params);
        }
        else
        {
            Map<String, List<String>> selectorMap = buildSelectorMap(form);
            return new JobRequest(form.getProjectName(), selectorMap, dataFiles, form.getDataSources(), params);
        }
    }

    public List<String> parseSelectorStringIntoList(String selectorStr)
    {
        return Arrays.asList(selectorStr.split("\\s+"));
    }

    public Map<String, List<String>> buildSelectorMap(NetworkBuilderForm form)
    {
        Map<String, List<String>> selectorMap = new HashMap<>();
        for (TargetDeckEntry entry : form.getTargetDeck().getTargetDeckEntryList())
        {
            String selectorType = entry.getSelectorType().toString();
            if (!selectorMap.containsKey(selectorType))
                selectorMap.put(selectorType, new LinkedList<>());

            List<String> selectors = parseSelectorStringIntoList(entry.getSelectorList());
            selectorMap.get(selectorType).addAll(selectors);
        }

        return selectorMap;
    }
}
