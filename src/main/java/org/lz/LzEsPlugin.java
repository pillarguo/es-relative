package org.lz;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.NativeScriptFactory;

import java.util.Arrays;
import java.util.List;

public class LzEsPlugin  extends Plugin implements ScriptPlugin {

    private final static Logger LOGGER = LogManager.getLogger(LzEsPlugin.class);


    @Override
    public List<NativeScriptFactory> getNativeScripts(){
        return Arrays.asList(new BatchRelativeFactory());
    }
}
