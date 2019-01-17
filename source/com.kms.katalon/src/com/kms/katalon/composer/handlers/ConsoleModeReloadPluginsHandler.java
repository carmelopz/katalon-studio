package com.kms.katalon.composer.handlers;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.kms.katalon.constants.StringConstants;
import com.kms.katalon.logging.LogUtil;
import com.kms.katalon.plugin.models.KStoreApiCredentials;
import com.kms.katalon.plugin.models.ResultItem;
import com.kms.katalon.plugin.service.PluginService;

public class ConsoleModeReloadPluginsHandler {

    public void reload(String apiKey) {
        try {
            LogUtil.printOutputLine("Start reloading plugins...");
            
            KStoreApiCredentials credentials = new KStoreApiCredentials();
            credentials.setApiKey(apiKey);
            List<ResultItem> reloadPluginResults = PluginService.getInstance().reloadPlugins(credentials,
                    new NullProgressMonitor());
            reloadPluginResults.stream().forEach(result -> {
                String pluginName = result.getPlugin().getProduct().getName();
                if (result.isPluginInstalled()) {
                    LogUtil.printOutputLine(
                            MessageFormat.format(StringConstants.MSG_PLUGIN_HAS_BEEN_INSTALLED, pluginName));
                } else {
                    LogUtil.printOutputLine(
                            MessageFormat.format(StringConstants.MSG_PLUGIN_HAS_BEEN_UNINSTALLED, pluginName));
                }
            });
        } catch (Exception e) {
            LogUtil.printAndLogError(e, "Failed to reload plugins.");
        }
    }
}
