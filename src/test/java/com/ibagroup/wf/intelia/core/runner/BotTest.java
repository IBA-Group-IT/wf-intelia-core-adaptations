package com.ibagroup.wf.intelia.core.runner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.Charsets;
import org.junit.Before;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class BotTest {

    public void runBC(String botName) throws InvocationTargetException, IllegalAccessException {
        runBC(botName, botName);
    }

    public void runBC(String botName, String botConfigFile) throws InvocationTargetException, IllegalAccessException {
        runBC(botName, botName, "src/main/resources/configs");
    }

    public void runBC(String botName, String botConfigFile, String botConfigDir) throws InvocationTargetException, IllegalAccessException {
        Bot bot = new Bot(botName, botConfigFile, botConfigDir);
        bot.run();
        bot.shutdownBotContext();
    }

    @Before
    public void cleanup() throws IOException {
        Files.walk(Paths.get("wfs-data/datastore"))
                .filter(p ->
                        !Files.isDirectory(p) && p.toString().endsWith("_wfs-working-copy.csv")
                ).forEach(p -> p.toFile().delete());
    }

    public File getLastOutputFile() throws Exception {
        File outputFolder = new File("wfs-data\\output");
        File[] outputFiles = outputFolder.listFiles();
        Arrays.sort(outputFiles, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });
        File lastOutput = outputFiles[0];
        System.out.println("File to test: " + lastOutput.getName());
        return lastOutput;
    }

    public List<CSVRecord> getLastOutput() throws Exception {
        List<CSVRecord> result = new ArrayList<>();

        try (CSVParser parser = new CSVParser(
                new InputStreamReader(new FileInputStream(getLastOutputFile()), Charsets.toCharset("UTF-8")),
                CSVFormat.RFC4180.withHeader().withSkipHeaderRecord(true))) {
            for (CSVRecord record : parser) {
                result.add(record);
            }
        }

        return result;
    }

}