package com.ibagroup.wf.intelia.core.runner;

import java.lang.reflect.InvocationTargetException;

public class Bot implements Runnable {
    private String botName;
    private String botConfigFile;
    private String botConfigDir;
    private WebHarvestMainIntegrationTestLauncher main;
    private boolean success = true;
    private String messageException;
    private String messageLineConfigException;

    public Bot(String botName, String botConfigFile, String botConfigDir) {
        this.botName = botName;
        this.botConfigFile = botConfigFile;
        this.botConfigDir = botConfigDir;
    }

    public void run() {
        try {
            String configFile = BotLauncherConfig.initialize(botName, botConfigFile, botConfigDir);

            RunProperties prop = RunProperties.getInstance();
            main = new WebHarvestMainIntegrationTestLauncher(new String[]{configFile, "3885"});

            main.launch();
        } catch (Throwable exception) {
            try {

                if (main != null) {
                    messageException = main.getStringMessageException(exception);
                    messageLineConfigException = main.getConfigLineException(exception);
                }
                exception.printStackTrace(System.err);
                success = false;

                shutdownBotContext();
                main = null;

            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace(System.err);
            }

        }
    }


    public void shutdownBotContext() throws InvocationTargetException, IllegalAccessException {
        if (main != null) {
            main.printFinishMessage(success, messageException, messageLineConfigException);
            main.stop();
            main.saveDataStoreTablesAfterLunchTast(success);
        }
    }
}
