package com.ibagroup.wf.intelia.core.runner;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SecureStorage {

    private final String  storagePath;

    public SecureStorage(String storagePath){
        this.storagePath = storagePath;
    }

    public ArrayList<Map> getData() {
        CSVReader reader = null;
        ArrayList<Map> secureStorageData = new ArrayList<>();
        try {
            reader = new CSVReader(new FileReader(this.storagePath));
            String[] line;
            while ((line = reader.readNext()) != null) {
                HashMap<String,String> secureStorageRecord = new HashMap<>();
                secureStorageRecord.put("alias", line[0]);
                secureStorageRecord.put("key", line[1]);
                secureStorageRecord.put("value", line[2]);
                secureStorageData.add(secureStorageRecord);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return secureStorageData;

    }

}

