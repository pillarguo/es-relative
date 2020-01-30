package org.lz;

import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

import java.util.Map;

public class BatchRelativeFactory implements NativeScriptFactory {

    @Override
    public ExecutableScript newScript(Map<String, Object> params) {
        return new BatchRelativeScript(params);
    }

    @Override
    public boolean needsScores() {
        return false;
    }

    @Override
    public String getName() {
        return "BatchRelative";
    }
}
