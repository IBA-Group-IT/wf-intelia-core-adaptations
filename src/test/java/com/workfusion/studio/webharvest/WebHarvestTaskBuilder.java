package com.workfusion.studio.webharvest;

import com.freedomoss.crowdcontrol.webharvest.*;
import com.freedomoss.crowdcontrol.webharvest.executor.WebharvestTaskInput;
import com.freedomoss.crowdcontrol.webharvest.plugin.ocr.impl.OcrProperties;
import com.freedomoss.crowdcontrol.webharvest.plugin.security.provider.ISecureEntryProvider;
import com.freedomoss.crowdcontrol.webharvest.selenium.SeleniumServerLocation;
import com.ibagroup.wf.intelia.core.runner.RunProperties;
import com.workfusion.studio.launch.OcrPropertiesBeanChanger;
import com.workfusion.studio.mediator.settings.RuntimeSettings;
import com.workfusion.studio.mediator.webharvest.model.BotTask;
import com.workfusion.studio.mediator.webharvest.model.GlobalVariableEntry;
import com.workfusion.studio.secure.LocalSecureProvider;
import com.workfusion.utils.security.Credentials;
import com.workfusion.utils.security.DatabaseProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.webharvest.exception.PluginException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Component
public class WebHarvestTaskBuilder {
    private final OcrProperties ocrProperties;

    @Autowired
    public WebHarvestTaskBuilder(OcrProperties ocrProperties) {
        this.ocrProperties = ocrProperties;
    }

    public List<WebharvestTaskInput> buildTaskInputs(RuntimeSettings settings, List<Map<String, String>> initParams) {
        return (List) initParams.stream().map((params) -> {
            return this.buildTaskInput(settings, params);
        }).collect(Collectors.toList());
    }

    public WebharvestTaskInput buildTaskInput(RuntimeSettings settings, Map<String, String> initParams) {
        ProxySettingsDto proxySettings = null;
        if (settings.isProxyEnabled()) {
            proxySettings = new ProxySettingsDto();
            proxySettings.setServer(settings.getProxyServer());
            int proxyPort = settings.getProxyPort();
            if (proxyPort > 0) {
                proxySettings.setPort(proxyPort);
            }

            if (settings.isProxyAuthEnabled()) {
                proxySettings.setUsername(settings.getProxyUsername());
                proxySettings.setPassword(settings.getProxyPassword());
            }
        }

        SourceDto source = new SourceDto();
        source.setProxySettings(proxySettings);
        CampaignDto campaign = new CampaignDto();
        campaign.setCampaignId(10L);
        campaign.setUuid(this.generateUUID(true));
        campaign.setNewWorkflow(true);
        campaign.setMachine(true);
        campaign.setTitle("WWI TODO: Config name should be here");
        campaign.setSource(source);
        RunDto run = new RunDto();
        run.setRunId(12L);
        String runUuid = this.generateUUID(true);
        run.setUuid(runUuid);
        run.setRootRunUuid(runUuid);
        run.setSandbox(true);
        run.setSubmissionSize(1);
        if (CollectionUtils.isNotEmpty(settings.getGlobalVariableEntries())) {
            Iterator var9 = settings.getGlobalVariableEntries().iterator();

            while (var9.hasNext()) {
                GlobalVariableEntry entry = (GlobalVariableEntry) var9.next();
                run.addParameterValue(entry.getKey(), entry.getValue());
            }
        }

        SubmissionDto submission = new SubmissionDto();
        submission.setAwsHitId(333L);
        AwsHitDto awsHitDto = new AwsHitDto();
        awsHitDto.setHitId("A1BCDEF2-A345-67B8-CDE9-0F12A3456B78");
        awsHitDto.setProcessingAttempts(1);
        awsHitDto.setExternalUrl("http://externalurl/HITXXX");
        submission.setAwsHit(awsHitDto);
        WebHarvestTaskItem item = new WebHarvestTaskItem();
        item.setCampaignDto(campaign);
        item.setRun(run);
        item.setSubmission(submission);
        PreviousData previousData = this.createEmptyPreviousData();
        HitSubmissionDataItemDto dataItem = new HitSubmissionDataItemDto();
        dataItem.setId(123L);
        List<HitSubmissionDataItemValueDto> values = new ArrayList();
        if (initParams != null) {
            Iterator var15 = initParams.entrySet().iterator();

            while (var15.hasNext()) {
                Entry<String, String> entry = (Entry) var15.next();
                HitSubmissionDataItemValueDto value = new HitSubmissionDataItemValueDto();
                value.setName((String) entry.getKey());
                value.setValue((String) entry.getValue());
                values.add(value);
            }
        }

        dataItem.setItemValueList(values);
        AwsHitAssignmentDto assignment = this.createInitialAssignment(new AwsHitDto());
        WebharvestTaskInput input = new WebharvestTaskInput(campaign, run, item, previousData, dataItem, assignment);
        DatabaseProperties datastoreProperties = this.fillDatabaseProperties(settings);
        settings.isLocalDataStoresMode();
        input.setDataStoreProperties(datastoreProperties);
        Map<String, String> includeConfigs = readIncludeConfigs(settings);
        input.setIncludedConfigs(includeConfigs);
        input.setApplicationHost(this.getApplicationHost(settings));
        input.setApplicationContextPath(this.getApplicationContextPath(settings));
        Credentials userCredentials = new Credentials();
        userCredentials.setUsername(settings.getDatastoreServiceUsername());
        userCredentials.setPassword(settings.getDatastoreServicePassword());
        input.setUserInternalCredentials(userCredentials);
        String seleniumUrl = settings.getRemoteSeleniumServerUrl();
        if (seleniumUrl != null && !seleniumUrl.trim().isEmpty()) {
            settings.setLocalSeleniumMode(false);
            if (!seleniumUrl.startsWith("http://") && !seleniumUrl.startsWith("https://")) {
                seleniumUrl = "http://" + seleniumUrl;
            }

            input.setSeleniumServer(this.fillSeleniumServerLocation(seleniumUrl));
        } else {
            settings.setLocalSeleniumMode(true);
        }

        input.setS3EnpointUrl(settings.getS3EndpointUrl());
        input.setS3AccessKey(settings.getS3AccessKey());
        input.setS3SecretKey(settings.getS3SecretKey());
        input.setApplicationResourceUrl(settings.getS3EndpointUrl());
        Map<String, Entry<String, String>> s3KeyMapRes = this.convertMapToS3KeysMap(settings.getS3KeyMap());
        input.setS3KeyMap(s3KeyMapRes);
        Map<String, ISecureEntryProvider> secureProviderMap = new HashMap();
        ISecureEntryProvider provider = new LocalSecureProvider(settings.getSecureProviderMap());
        secureProviderMap.put(null, provider);
        secureProviderMap.put("localProvider", provider);
        input.setSecurityProviderMap(secureProviderMap);
        input.setOcrApiUrl(settings.getOcrApiBaseUrl());
        (new OcrPropertiesBeanChanger(this.ocrProperties)).applyFrom(settings);
        input.setOcrProperties(this.ocrProperties);

        Properties automationProperties = RunProperties.getProperties("automation/automation.properties");

        input.setAutomationUrl(automationProperties.getProperty("automationUrl"));
        input.setAutomationUser(automationProperties.getProperty("automationUser"));
        input.setAutomationPassword(automationProperties.getProperty("automationPassword"));

        input.setWfbiUrl(automationProperties.getProperty("wfbiUrl"));
        input.setWfbiUser(automationProperties.getProperty("wfbiUser"));
        input.setWfbiPassword(automationProperties.getProperty("wfbiPassword"));

        input.setTableauDbUrl(automationProperties.getProperty("tableauDbUrl"));
        input.setTableauDbUser(automationProperties.getProperty("tableauDbUser"));
        input.setTableauDbPassword(automationProperties.getProperty("tableauDbPassword"));

        return input;
    }

    private SeleniumServerLocation fillSeleniumServerLocation(String url) {
        SeleniumServerLocation seleniumServerLocation = new SeleniumServerLocation();

        URL seleniumServerUrl;
        try {
            seleniumServerUrl = new URL(url);
        } catch (MalformedURLException var5) {
            throw new PluginException(var5);
        }

        seleniumServerLocation.setProtocol(seleniumServerUrl.getProtocol());
        seleniumServerLocation.setHost(seleniumServerUrl.getHost());
        seleniumServerLocation.setPort(seleniumServerUrl.getPort());
        seleniumServerLocation.setFile(seleniumServerUrl.getPath());
        return seleniumServerLocation;
    }

    private PreviousData createEmptyPreviousData() {
        List<RunDto> runs = Collections.emptyList();
        Map<String, List<String>> allAnswers = Collections.emptyMap();
        Map<String, List<String>> approvedAnswers = Collections.emptyMap();
        return new PreviousData(allAnswers, approvedAnswers, runs);
    }

    private AwsHitAssignmentDto createInitialAssignment(AwsHitDto hit) {
        AwsHitAssignmentDto assignment = new AwsHitAssignmentDto();
        assignment.setSubmitTime(new Date());
        assignment.setAwsHit(hit);
        assignment.setAssignmentId(this.generateUUID(true));
        assignment.setAcceptTime(new Date());
        return assignment;
    }

    private DatabaseProperties fillDatabaseProperties(RuntimeSettings settings) {
        DatabaseProperties datastoreProperties = new DatabaseProperties();
        datastoreProperties.setUrl(settings.getDatastoreUrl());
        datastoreProperties.setServiceHost(settings.getDatastoreServiceUrl());
        datastoreProperties.setServiceContextPath(settings.getDatastoreServicePath());
        datastoreProperties.setUsername(settings.getDatastoreUsername());
        datastoreProperties.setPassword(settings.getDatastorePassword());
        return datastoreProperties;
    }

    private String generateUUID(boolean upperCase) {
        String uuid = UUID.randomUUID().toString();
        return upperCase ? uuid.toUpperCase() : uuid;
    }

    private static Map<String, String> readIncludeConfigs(RuntimeSettings settings) {
        Map<String, String> includeConfigs = new HashMap();
        Iterator var3 = settings.getIncludedConfigs().iterator();

        while (var3.hasNext()) {
            BotTask config = (BotTask) var3.next();

            try {
                includeConfigs.put(config.getShortName(), config.content());
            } catch (IOException var4) {
                throw new IllegalStateException(String.format("Error occurred while reading bot task: '%s'", config.getAbsolutePath()));
            }
        }

        return includeConfigs;
    }

    private Map<String, Entry<String, String>> convertMapToS3KeysMap(Map<String, String> s3KeyMapSettings) {
        Map<String, Map<String, String>> s3KeyMapWF = new HashMap();
        if (s3KeyMapSettings != null && !s3KeyMapSettings.isEmpty()) {
            Iterator var4 = s3KeyMapSettings.entrySet().iterator();

            while (var4.hasNext()) {
                Entry entry = (Entry) var4.next();
                String name = (String) entry.getKey();
                if (name.startsWith("s3/keys/")) {
                    name = name.replace("s3/keys/", "");
                    String[] split = StringUtils.split(name, "/");
                    String key = split[0];
                    Map<String, String> map = (Map) s3KeyMapWF.get(key);
                    if (map == null) {
                        map = new HashMap();
                        s3KeyMapWF.put(key, map);
                    }

                    ((Map) map).put(name, (String) entry.getValue());
                }
            }
        }

        Map<String, Entry<String, String>> s3KeyMapRes = new HashMap();
        Iterator var15 = s3KeyMapWF.entrySet().iterator();

        while (var15.hasNext()) {
            Entry<String, Map<String, String>> entry = (Entry) var15.next();
            String key = (String) entry.getKey();
            Map<String, String> value = (Map) entry.getValue();
            String access = null;
            String secret = null;
            Iterator var11 = value.entrySet().iterator();

            while (var11.hasNext()) {
                Entry<String, String> entry2 = (Entry) var11.next();
                String key1 = (String) entry2.getKey();
                if (key1.endsWith("/access-key")) {
                    access = (String) entry2.getValue();
                }

                if (key1.endsWith("/secret-key")) {
                    secret = (String) entry2.getValue();
                }
            }

            if (access != null) {
                Entry<String, String> pair = new SimpleEntry(access, secret);
                s3KeyMapRes.put(key, pair);
            }
        }

        return s3KeyMapRes;
    }

    private String getApplicationHost(RuntimeSettings settings) {
        return settings.isLocalDataStoresMode() ? "local" : "https://" + settings.getSshHost();
    }

    private String getApplicationContextPath(RuntimeSettings settings) {
        return settings.isLocalDataStoresMode() ? "local" : "/mturk-web";
    }
}
