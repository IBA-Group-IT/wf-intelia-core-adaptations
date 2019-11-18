package com.ibagroup.wf.intelia.core.runner;


import com.workfusion.studio.mediator.webharvest.model.GlobalVariableEntry;

import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class RunProperties {

    public static final String PATH_TO_BOT_CONFIG_DIR = "src/main/resources/configs";
    public static final String TEST_RESOURCES_WFS_DATA_TO_RUN = "wfs-data/";

    public static final String PATH_TO_WFS_DATA_TEST_DATASTORE = TEST_RESOURCES_WFS_DATA_TO_RUN + "datastore";
    public static final String PATH_TO_WFS_DATA_TEST_INPUT = TEST_RESOURCES_WFS_DATA_TO_RUN + "input/my_input_data.csv";
    public static final String PATH_TO_WFS_DATA_TEST_SECURE = TEST_RESOURCES_WFS_DATA_TO_RUN + "secure/secureStorage.csv";
    public static final String PATH_TO_OUTPUT = TEST_RESOURCES_WFS_DATA_TO_RUN + "output";
    public static final String PATH_TO_WFS_DATA_TEST_GLOBAL_VARS = TEST_RESOURCES_WFS_DATA_TO_RUN + "global/globalVars.properties";

    private static volatile RunProperties instance;
    private final Properties properties;

    public RunProperties() {
        this.properties = new Properties();
    }

    public static RunProperties getInstance() {
        RunProperties localInstance = instance;
        if (localInstance == null) {
            synchronized (RunProperties.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new RunProperties();
                }
            }
        }
        return localInstance;
    }

    /**
     * @param botName
     * @return
     * @throws IOException
     * @see com.workfusion.studio.mediator.settings.RuntimeSettings
     */
    public boolean loadProperties(String botName) throws IOException {

        String PATH_TO_BOTCONFIG_PROPERTIES = TEST_RESOURCES_WFS_DATA_TO_RUN + "/" + botName + ".properties";
        String PATH_TO_BOT_CONFIG_SER = TEST_RESOURCES_WFS_DATA_TO_RUN + "/" + botName + ".ser";
        InputStream input = null;
        try {
            input = new FileInputStream(PATH_TO_BOTCONFIG_PROPERTIES);
            properties.load(input);

            properties.setProperty("botConfigDir", PATH_TO_BOT_CONFIG_DIR);
            properties.setProperty("serializedConfig", PATH_TO_BOT_CONFIG_SER);
            properties.setProperty("datastoreLocation", PATH_TO_WFS_DATA_TEST_DATASTORE);
            //properties.setProperty("inputDataFilePath", PATH_TO_WFS_DATA_TEST_INPUT);
            properties.setProperty("secureStorage", PATH_TO_WFS_DATA_TEST_SECURE);
            properties.setProperty("workingPath", PATH_TO_OUTPUT);

        } catch (FileNotFoundException ex) {

            //System.out.println("Sorry, unable to find " + PATH_TO_BOTCONFIG_PROPERTIES + ". File run.properties was generated in " + TEST_RESOURCES_WFS_DATA_TO_RUN + " folder with default values.");

            //OutputStream output = new FileOutputStream(PATH_TO_BOTCONFIG_PROPERTIES);
            properties.setProperty("botConfigDir", PATH_TO_BOT_CONFIG_DIR);
            properties.setProperty("serializedConfig", PATH_TO_BOT_CONFIG_SER);
            properties.setProperty("datastoreLocation", PATH_TO_WFS_DATA_TEST_DATASTORE);
            properties.setProperty("inputDataFilePath", PATH_TO_WFS_DATA_TEST_INPUT);
            properties.setProperty("secureStorage", PATH_TO_WFS_DATA_TEST_SECURE);
            properties.setProperty("workingPath", PATH_TO_OUTPUT);

            properties.setProperty("datastoreUsername", "sa");
            properties.setProperty("datastorePassword", "");
            properties.setProperty("datastoreServiceUrl",
                    "jdbc:postgresql://localhost:5432?useLegacyDatetimeCode=false&serverTimezone=UTC&characterEncoding=UTF-8&rewriteBatchedStatements=true");
            properties.setProperty("remoteSeleniumServerUrl", "http://localhost:4444/wd/hub");
            properties.setProperty("datastoreUrl", "jdbc:h2:mem:datastoredb;MODE=PostgreSQL;DATABASE_TO_UPPER=FALSE");
            properties.setProperty("localDataStoresMode", "true");
            properties.setProperty("localSeleniumMode", "false");
            properties.setProperty("datastoreServiceUsername", "mturk");
            properties.setProperty("datastoreServicePassword", "mturk");
            properties.setProperty("proxyEnabled", "false");
            properties.setProperty("launcherPort", "3885");

            properties.setProperty("s3EndpointUrl", "s3EndpointUrl");
            properties.setProperty("s3AccessKey", "s3AccessKey");
            properties.setProperty("s3SecretKey", "s3SecretKey");

            properties.setProperty("ocrApiBaseUrl", "ocrApiBaseUrl");
            properties.setProperty("ocrJwtIssuer", "ocrJwtIssuer");
            properties.setProperty("ocrJwtSecret", "ocrJwtSecret");

            //properties.store(output, null);

            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    public static Set<GlobalVariableEntry> getGlobalVariables() throws IOException {

        Set<GlobalVariableEntry> globalVariableEntries = new HashSet<>();
        File globVarsFileCsv = new File(PATH_TO_WFS_DATA_TEST_GLOBAL_VARS);

        if (globVarsFileCsv.exists()) {

            Properties properties = new Properties();
            properties.load(new FileReader(PATH_TO_WFS_DATA_TEST_GLOBAL_VARS));

            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);

                globalVariableEntries.add(new GlobalVariableEntry(key, value));
            }
        }
        return globalVariableEntries;
    }

    public static Properties getProperties(String fileName) {

        String pathToFile = TEST_RESOURCES_WFS_DATA_TO_RUN + fileName;
        Properties properties = new Properties();
        File file = new File(pathToFile);
        try {
            properties.load(new FileReader(pathToFile));
        } catch (IOException e) {
            System.out.println("There is not file for properties to load, skipped: " + pathToFile);
        }
        return properties;
    }
}
