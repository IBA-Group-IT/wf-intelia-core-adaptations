package com.ibagroup.wf.intelia.core.runner;

import com.google.gson.GsonBuilder;
import com.workfusion.common.utils.GsonUtils;
import com.workfusion.studio.mediator.settings.RuntimeSettings;
import com.workfusion.studio.mediator.webharvest.model.BotTask;
import com.workfusion.studio.mediator.webharvest.model.LocalSecureEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

public class BotLauncherConfig {

    public static final String TEST_RESOURCES_WFS_DATA_TO_RUN = "wfs-data/";

    public static String initialize(String botName, String botConfigFile) throws IOException, SAXException, ParserConfigurationException {
        return initialize(botName, botConfigFile, "src/main/resources/configs");
    }

    public static String initialize(String botName, String botConfigFile, String botConfigDir) throws IOException, SAXException, ParserConfigurationException {

        String PATH_TO_BOTCONFIG_PROPERTIES = TEST_RESOURCES_WFS_DATA_TO_RUN + botConfigFile + ".json";
        String PATH_TO_BOT_CONFIG_SER = TEST_RESOURCES_WFS_DATA_TO_RUN + botConfigFile + ".ser";

        ConfigParser parser = new ConfigParser();

        parser.setPossibleConfigurations(botConfigDir);

        parser.setIncludedConfigs(botName);

        System.out.println("### list of used bot's configs for running bot[" + botName + "] -> " + parser.getIncludesList());

        ArrayList<String> includesList = parser.getIncludesList();

        Map<String, String> possibleConfigurations = parser.getPossibleConfigurations();

        RuntimeSettings runtimeSettings = null;
        try {
            runtimeSettings = GsonUtils.GSON.fromJson(FileUtils.readFileToString(new File(PATH_TO_BOTCONFIG_PROPERTIES), "UTF-8"), RuntimeSettings.class);
        } catch (IOException e) {
            RunProperties runProperties = RunProperties.getInstance();
            runProperties.loadProperties(botName);
            runtimeSettings = new RuntimeSettings();

            runtimeSettings.setIncludedConfigs(getBotIncludeConfigs(includesList, possibleConfigurations));
            runtimeSettings.setDatastoreUsername(runProperties.getProperty("datastoreUsername"));
            runtimeSettings.setDatastorePassword(runProperties.getProperty("datastorePassword"));
            runtimeSettings.setDatastoreLocation(runProperties.getProperty("datastoreLocation"));
            runtimeSettings.setDatastoreServiceUrl(runProperties.getProperty("datastoreServiceUrl"));
            runtimeSettings.setRemoteSeleniumServerUrl(runProperties.getProperty("remoteSeleniumServerUrl"));
            runtimeSettings.setDatastoreUrl(runProperties.getProperty("datastoreUrl"));
            runtimeSettings.setLocalDataStoresMode(Boolean.parseBoolean(runProperties.getProperty("localDataStoresMode")));
            runtimeSettings.setLocalSeleniumMode(Boolean.parseBoolean(runProperties.getProperty("localSeleniumMode")));
            runtimeSettings.setDatastoreServiceUsername(runProperties.getProperty("datastoreServiceUsername"));
            runtimeSettings.setDatastoreServicePassword(runProperties.getProperty("datastoreServicePassword"));
            runtimeSettings.setProxyEnabled(Boolean.parseBoolean(runProperties.getProperty("proxyEnabled")));
            runtimeSettings.setWorkingPath(runProperties.getProperty("workingPath"));
            runtimeSettings.setInputDataFilePath(runProperties.getProperty("inputDataFilePath"));
            runtimeSettings.setS3AccessKey(runProperties.getProperty("s3AccessKey"));
            runtimeSettings.setS3EndpointUrl(runProperties.getProperty("s3EndpointUrl"));
            runtimeSettings.setS3SecretKey(runProperties.getProperty("s3SecretKey"));

            runtimeSettings.setSecureProviderMap(getSecureEntryMap(runProperties.getProperty("secureStorage")));

            //extra v9
            Properties props = initLogDefaultProperties();
            runtimeSettings.setLogProperties(props);

            //Global Vars
            runtimeSettings.setGlobalVariableEntries(runProperties.getGlobalVariables());

            String runtimeSettingsJson = GsonUtils.GSON_PRETTY_PRINT.toJson(runtimeSettings);
            FileUtils.writeStringToFile(new File(PATH_TO_BOTCONFIG_PROPERTIES), runtimeSettingsJson, "UTF-8");
            System.err.println(runtimeSettingsJson);
            throw new IllegalArgumentException("There is no " + PATH_TO_BOTCONFIG_PROPERTIES + " exist, creating a new one. Please change the setting according to you configuration.");
        }

        File file = getBotFilePathFromResources(botConfigDir, botName);

        System.out.println("### Start to load bot from file -> " + file);

        BotTask botTask = new BotTask(file);
        runtimeSettings.setMainConfig(botTask);

        FileOutputStream fos = new FileOutputStream(new File(PATH_TO_BOT_CONFIG_SER));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(runtimeSettings);
        oos.close();
        fos.close();

        String runtimeSettingsJson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(runtimeSettings);

        FileUtils.writeStringToFile(new File(PATH_TO_BOTCONFIG_PROPERTIES), runtimeSettingsJson, "UTF-8");
        System.out.println(runtimeSettingsJson);

        return PATH_TO_BOT_CONFIG_SER;
    }

    private static File getBotFilePathFromResources(String configDir, String botName) {

        Collection<File> botConigurations = FileUtils.listFiles(
                new File(configDir),
                new NameFileFilter(botName + ".xml"),
                DirectoryFileFilter.DIRECTORY
        );
        return (botConigurations.size() > 0) ? botConigurations.iterator().next() : new File(configDir + "\\" + botName);
    }

    private static Properties initLogDefaultProperties() {
        Properties props = new Properties();
        props.setProperty("log4j.rootLogger", "INFO, CONSOLE, FILE");
        props.setProperty("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.CONSOLE.layout.ConversionPattern", "[%p] %d %c %M - %m%n");
        props.setProperty("log4j.logger.org.webharvest.runtime.Scraper", "WARN");
        props.setProperty("log4j.logger.com.workfusion.studio.datastore.LocalDataStoreDao", "WARN");

        props.setProperty("log4j.appender.FILE", "org.apache.log4j.FileAppender");
        props.setProperty("log4j.appender.FILE.file", (new File("target", "webharvest_execution.log")).getAbsolutePath());
        props.setProperty("log4j.appender.FILE.layout", "org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.FILE.layout.ConversionPattern", "[%p] %d %c %M - %m%n");
        // props.setProperty("log4j.logger.com.workfusion.studio.launch", "INFO");
        return props;
    }


    private static ArrayList<BotTask> getBotIncludeConfigs(ArrayList<String> includesList, Map<String, String> possibleConfigurations) {
        ArrayList<BotTask> includes = new ArrayList<>();
        if (includesList.size() > 1) {
            for (int i = 1; i <= includesList.size() - 1; i++) {
                includes.add(new BotTask(new File(possibleConfigurations.get(includesList.get(i)))));
            }
        }
        return includes;
    }

    private static Map<String, LocalSecureEntry> getSecureEntryMap(String secureStoragePath) {
        SecureStorage secureStorage = new SecureStorage(secureStoragePath);
        secureStorage.getData();
        Map<String, LocalSecureEntry> secureEntryMap = new HashMap<>();

        for (Map secureRecord : secureStorage.getData()) {
            LocalSecureEntry secureEntry = new LocalSecureEntry(
                    (String) secureRecord.get("alias"),
                    (String) secureRecord.get("key"),
                    (String) secureRecord.get("value"));
            secureEntryMap.put((String) secureRecord.get("alias"), secureEntry);
        }
        return secureEntryMap;
    }

}
