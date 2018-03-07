package com.agilion.utils;

import com.agilion.domain.app.User;
import com.agilion.domain.networkbuilder.NetworkBuilderForm;
import com.agilion.domain.networkbuilder.TargetDeckEntry;
import com.agilion.domain.networkbuilder.datasets.DataSet;
import com.agilion.domain.networkbuilder.datasets.DataSetReference;
import com.agilion.services.files.FileStore;
import com.agilion.services.jobmanager.NetworkBuild;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public NetworkBuild convertNetworkFormToJobRequest(NetworkBuilderForm form, User user) throws Exception
    {
        // First store the files. We'll take the paths and include them in the JobRequest
        Set<String> selectorFiles = new HashSet<>();
        Set<DataSetReference> dataSetReferences = saveDatasetToFilestore(form.getDataSets());

        // Next, store the selector files.
        if (form.getTargetDeck().getSelectorFile() != null)
            selectorFiles.add(fileStore.storeFile(form.getTargetDeck().getSelectorFile().getInputStream(), UUID.randomUUID().toString()));

        //If the user manually entered in selectors, save those to a file and upload it to the filestore, too.
        if (form.getTargetDeck().getTargetDeckEntryList() != null && form.getTargetDeck().getTargetDeckEntryList().size() > 0)
            selectorFiles.add(saveSelectorsToFile(form));

        // Next, convert the date strings to dates
        Date fromDate = this.dateFormat.parse(form.getFromDate());
        Date toDate = this.dateFormat.parse(form.getToDate());

        return new NetworkBuild(form.getProjectName(), dataSetReferences, selectorFiles,
                new HashSet<String>(form.getDataSources()), fromDate, toDate, user);
    }


   // public NetworkBuild(String networkBuildName, List<DataSetReference> dataSets, Set<String> selectorFilePaths,
     //                   Set<String> dataSources, Date fromDate, Date toDate, User requestingUser) {

    private Set<DataSetReference> saveDatasetToFilestore(List<DataSet> dataSets) throws Exception
    {
        Set<DataSetReference> dataSetReferences = new HashSet<>();
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

    private String saveSelectorsToFile(NetworkBuilderForm form) throws Exception {
        InputStream stream;
        StringBuilder builder = new StringBuilder();
        for (TargetDeckEntry entry : form.getTargetDeck().getTargetDeckEntryList())
        {
            for (String selector : entry.getSelectorList().split("\\s+"))
            {
                builder.append(selector+" "+entry.getSelectorType()+"\n");
            }
        }

        return fileStore.storeFile(new ByteArrayInputStream(builder.toString().getBytes()), UUID.randomUUID().toString());
    }

}
