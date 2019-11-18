package com.ibagroup.wf.intelia.core.runner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.xerces.dom.DeferredElementImpl;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigParser {

    private ArrayList<String> includesList = new ArrayList<>();
    private Map<String, String> possibleConfigurations = new HashMap<>();


    public void setPossibleConfigurations(String configDir) {

        System.out.println("### Directory with Bots -> " + configDir);

        Collection<File> botConigurations = FileUtils.listFiles(
                new File(configDir),
                new RegexFileFilter("^(.*?.xml)"),
                DirectoryFileFilter.DIRECTORY
        );

        botConigurations.forEach(configPath -> {
                    Pattern pattern = Pattern.compile("^(.*)\\/(.*)(\\..*)$");
                    Matcher matcher = pattern.matcher(configPath.toString().replace("\\", "/"));
                    if (matcher.find()) {
                        possibleConfigurations.put(matcher.group(2), configPath.toString());
                    }
                }
        );

        System.out.println("### system found bot's configs -> " + possibleConfigurations.keySet());
    }

    public void setIncludedConfigs(String configName) throws ParserConfigurationException, IOException, SAXException {
        includesList.add(configName);
        ArrayList<String> includes = addIncludedConfigs(configName);

        //empty
        includesList.addAll(includes);


        if (!includes.isEmpty()) {
            includes.forEach(include -> {
                try {
                    ArrayList<String> nestedIncluds = addIncludedConfigs(include);
                    if (!nestedIncluds.isEmpty()) {
                        nestedIncluds.forEach(nested -> {
                            try {
                                setIncludedConfigs(nested);
                            } catch (ParserConfigurationException | IOException | SAXException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (IOException | SAXException | ParserConfigurationException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    private ArrayList<String> addIncludedConfigs(String configName) throws IOException, SAXException, ParserConfigurationException {
        ArrayList<String> includeConfigs = new ArrayList<>();
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.parse(new File(possibleConfigurations.get(configName)));
        NodeList includes = doc.getElementsByTagName("include-config");
        for (int i = 0; i < includes.getLength(); i++) {
            includeConfigs.add(((DeferredElementImpl) includes.item(i)).getAttribute("code"));
        }

        return includeConfigs;
    }

    public ArrayList<String> getIncludesList() {
        return includesList;
    }

    public Map<String, String> getPossibleConfigurations() {
        return possibleConfigurations;
    }
}
