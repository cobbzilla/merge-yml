package org.cobbzilla.util.yml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * (c) Copyright 2013 Jonathan Cobb
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class YmlMerger {

    private static final Logger LOG = LoggerFactory.getLogger(YmlMerger.class);

    private final Yaml yaml = new Yaml();

    public Map<String, Object> merge(String[] files) throws IOException {
        Map<String, Object> mergedResult = new LinkedHashMap<String, Object>();
        for (String file : files) {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                final Map<String, Object> yamlContents = (Map<String, Object>) yaml.load(in);
                merge_internal(mergedResult, yamlContents);
                LOG.info("loaded YML from "+file+": "+yamlContents);

            } finally {
                if (in != null) in.close();
            }
        }
        return mergedResult;
    }

    private void merge_internal(Map<String, Object> mergedResult, Map<String, Object> yamlContents) {

        for (String key : yamlContents.keySet()) {
            Object yamlValue = yamlContents.get(key);
            Object existingValue = mergedResult.get(key);
            if (existingValue != null) {
                if (yamlValue instanceof Map) {
                    if (existingValue instanceof Map) {
                        merge_internal((Map<String, Object>) existingValue, (Map<String, Object>)  yamlValue);
                    } else if (existingValue instanceof String) {
                        throw new IllegalArgumentException("Cannot merge complex element into a simple element: "+key);
                    } else {
                        throw new IllegalArgumentException("Cannot merge element of unknown type: "+key);
                    }

                } else if (yamlValue instanceof String ||
                           yamlValue instanceof Boolean ||
                           yamlValue instanceof Double ||
                           yamlValue instanceof Integer) {
                    LOG.info("overriding value of "+key+" with value "+yamlValue);
                    mergedResult.put(key, yamlValue);

                } else {
                    throw new IllegalArgumentException("Cannot merge element of unknown type: "+key);
                }

            } else {
                if (yamlValue instanceof Map || yamlValue instanceof String) {
                    LOG.info("adding new key->value: "+key+"->"+yamlValue);
                    mergedResult.put(key, yamlValue);
                } else {
                    throw new IllegalArgumentException("Cannot merge element of unknown type: "+key);
                }
            }
        }
    }

    public String mergeToString(String[] files) throws IOException {
        return toString(merge(files));
    }

    public String toString(Map<String, Object> merged) {
        return yaml.dump(merged);
    }

}
