package com.agilion.utils;

import com.agilion.domain.networkbuilder.NetworkBuilderForm;
import com.agilion.domain.networkbuilder.TargetDeckEntry;
import com.agilion.domain.networkbuilder.datasets.DataSet;
import com.agilion.domain.networkbuilder.datasets.DataSetReference;
import com.agilion.services.files.FileStore;
import com.agilion.services.jobmanager.NetworkBuildingRequest;
import dataengine.api.Dataset;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.DateFormat;
import java.util.*;

/**
 * This class converts the NetworkBuilderForm object, uploaded by users on the "Network Builder page", into a JobRequest,
 * which commands the JobManagerServer to start processing/gathering data.
 */
public class NetworkFormToJobRequestConverter
{
    private FileStore fileStore;

    private DateFormat dateFormat;

    public NetworkFormToJobRequestConverter(FileStore store, DateFormat dateFormat)
    {
        this.fileStore = store;
        this.dateFormat = dateFormat;
    }

    public NetworkBuildingRequest convertNetworkFormToJobRequest(NetworkBuilderForm form, String user) throws Exception
    {
        // First store the files. We'll take the paths and include them in the JobRequest
        List<String> selectorFiles = new LinkedList<>();
        List<DataSetReference> dataSetReferences;

        dataSetReferences = saveDatasetToFilestore(form.getDataSets());

        if (form.getTargetDeck().getSelectorFile() != null)
            selectorFiles.add(fileStore.storeFile(form.getTargetDeck().getSelectorFile().getInputStream(), UUID.randomUUID().toString()));

        // Next, convert the date strings to dates
        Date fromDate = this.dateFormat.parse(form.getFromDate());
        Date toDate = this.dateFormat.parse(form.getToDate());

        Map<String, List<String>> selectorMap = buildSelectorMap(form);
        return new NetworkBuildingRequest(form.getProjectName(), selectorMap, selectorFiles, dataSetReferences,
                form.getDataSources(), fromDate, toDate, user);
    }

    private List<DataSetReference> saveDatasetToFilestore(List<DataSet> dataSets) throws Exception
    {
        List<DataSetReference> dataSetReferences = new LinkedList<>();
        if (dataSets != null)
        {
            for (DataSet dataset : dataSets)
            {
                String nodelistLoc = this.fileStore.storeFile(dataset.getNodelist().getInputStream(), UUID.randomUUID().toString());
                String edgelistLoc = this.fileStore.storeFile(dataset.getEdgelist().getInputStream(), UUID.randomUUID().toString());
                dataSetReferences.add(new DataSetReference(nodelistLoc, edgelistLoc));
            }
        }

        return dataSetReferences;
    }

    private List<String> parseSelectorStringIntoList(String selectorStr)
    {
        return Arrays.asList(selectorStr.split("\\s+"));
    }

    private Map<String, List<String>> buildSelectorMap(NetworkBuilderForm form)
    {
        Map<String, List<String>> selectorMap = new HashMap<>();
        if (form.getTargetDeck().getTargetDeckEntryList() != null && form.getTargetDeck().getTargetDeckEntryList().size() > 0)
        {
            for (TargetDeckEntry entry : form.getTargetDeck().getTargetDeckEntryList())
            {
                String selectorType = entry.getSelectorType().toString();
                if (!selectorMap.containsKey(selectorType))
                    selectorMap.put(selectorType, new LinkedList<>());

                List<String> selectors = parseSelectorStringIntoList(entry.getSelectorList());
                selectorMap.get(selectorType).addAll(selectors);
            }

        }
        return selectorMap;
    }
}
