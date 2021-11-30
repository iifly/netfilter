package io.zhile.research.ja.netfilter;

import io.zhile.research.ja.netfilter.commons.ConfigDetector;
import io.zhile.research.ja.netfilter.commons.ConfigParser;
import io.zhile.research.ja.netfilter.commons.DebugInfo;
import io.zhile.research.ja.netfilter.models.FilterConfig;
import io.zhile.research.ja.netfilter.plugin.PluginManager;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class Initializer {
    public static void init(String args, Instrumentation inst, File currentDirectory) {
        File configFile = ConfigDetector.detect(currentDirectory, args);
        if (null == configFile) {
            DebugInfo.output("Could not find any configuration files.");
        } else {
            DebugInfo.output("Current config file: " + configFile.getPath());
        }

        try {
            FilterConfig.setCurrent(new FilterConfig(ConfigParser.parse(configFile)));
        } catch (Exception e) {
            DebugInfo.output(e.getMessage());
        }

        PluginManager.getInstance().loadPlugins(inst, currentDirectory);

        for (Class<?> c : inst.getAllLoadedClasses()) {
            try {
                inst.retransformClasses(c);
            } catch (UnmodifiableClassException e) {
                // ok, ok. just ignore
            }
        }

        inst.addTransformer(Dispatcher.getInstance(), true);
    }
}
