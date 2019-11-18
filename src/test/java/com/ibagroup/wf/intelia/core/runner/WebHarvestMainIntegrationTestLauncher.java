package com.ibagroup.wf.intelia.core.runner;

import com.workfusion.studio.datastore.DataStoreDao;
import com.workfusion.studio.launch.WebHarvestMainLauncher;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WebHarvestMainIntegrationTestLauncher {
    private WebHarvestMainLauncher launcher;

    public WebHarvestMainIntegrationTestLauncher(String[] args) {
        launcher = new WebHarvestMainLauncher(args);
    }

    public void launch() throws IllegalAccessException {
        Field springContextField = ReflectionUtils.findField(launcher.getClass(), "springContext");
        springContextField.setAccessible(true);
        DataStoreDao dataStoreDao = ((ClassPathXmlApplicationContext) springContextField.get(launcher)).getBean(DataStoreDao.class);

        dataStoreDao.getAllDataStoresTables().stream().forEach(datastoreTableInfo -> {
            String tableName = datastoreTableInfo.getTableName();
            if (tableName.startsWith("ds_")) {
                dataStoreDao.dropTable(tableName.substring(3, tableName.length()));
            }
        });

        launcher.launch();
    }

    public void stop() {
        Method stopMethod = ReflectionUtils.findMethod(launcher.getClass(), "stop");
        stopMethod.setAccessible(true);

        ReflectionUtils.invokeMethod(stopMethod, launcher);

        stopMethod.setAccessible(false);
    }

    public void printFinishMessage(boolean success, String messageException, String messageLineConfigException) throws InvocationTargetException, IllegalAccessException {
        Method printFinishMessageMethod = ReflectionUtils.findMethod(launcher.getClass(), "printFinishMessage", boolean.class, String.class, String.class);
        printFinishMessageMethod.setAccessible(true);

        printFinishMessageMethod.invoke(launcher, success, messageException, messageLineConfigException);

        printFinishMessageMethod.setAccessible(false);
    }

    public void saveDataStoreTablesAfterLunchTast(boolean success) throws InvocationTargetException, IllegalAccessException {
        Method saveDataStoreTablesAfterLunchTast = ReflectionUtils.findMethod(launcher.getClass(), "saveDataStoreTablesAfterLunchTast", boolean.class);
        saveDataStoreTablesAfterLunchTast.setAccessible(true);

        saveDataStoreTablesAfterLunchTast.invoke(launcher, success);

        saveDataStoreTablesAfterLunchTast.setAccessible(false);
    }

    public String getStringMessageException(Throwable exception) throws InvocationTargetException, IllegalAccessException {
        Method getStringMessageException = ReflectionUtils.findMethod(launcher.getClass(), "getStringMessageException", Throwable.class);
        getStringMessageException.setAccessible(true);

        String result = (String) getStringMessageException.invoke(launcher, exception);

        getStringMessageException.setAccessible(false);

        return result;
    }

    public String getConfigLineException(Throwable exception) throws InvocationTargetException, IllegalAccessException {
        Method getConfigLineException = ReflectionUtils.findMethod(launcher.getClass(), "getConfigLineException", Throwable.class);
        getConfigLineException.setAccessible(true);

        String result = (String) getConfigLineException.invoke(launcher, exception);

        getConfigLineException.setAccessible(false);

        return result;
    }
}
