package org.cobbzilla.util.yml;

import com.github.mustachejava.DefaultMustacheFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * (c) Copyright 2013-2015 Jonathan Cobb
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class YmlMerger
{

    private static final Logger LOG = LoggerFactory.getLogger(YmlMerger.class);
    public static final DefaultMustacheFactory DEFAULT_MUSTACHE_FACTORY = new DefaultMustacheFactory();

    private final Yaml yaml = new Yaml();
    private Map<String, Object> variablesToReplace = new HashMap<String, Object>();

    public YmlMerger setVariablesToReplace(Map<String, String> vars) {
        this.variablesToReplace.clear();
        this.variablesToReplace.putAll(vars);
        return this;
    }



    public Map<String, Object> mergeYamlFiles(String[] pathsStr) throws IOException {
        return mergeYamlFiles(stringsToPaths(pathsStr));
    }

    public Map<String, Object> mergeYamlFiles(Set<Path> paths) throws IOException
    {
        Map<String, Object> mergedResult = new LinkedHashMap<String, Object>();
        for (Path yamlFilePath : paths) {
            InputStream in = null;
            try {
                File file = yamlFilePath.toFile();
                if (!file.exists())
                    throw new FileNotFoundException("YAML file to merge not found: " + file.getCanonicalPath());

                // read the file into a String
                in = new FileInputStream(file);
                final String entireFile = IOUtils.toString(in);

                // Substitute variables
                int bufferSize = entireFile.length() + 100;
                final StringWriter writer = new StringWriter(bufferSize);
                DEFAULT_MUSTACHE_FACTORY.compile(new StringReader(entireFile), "yaml-mergeYamlFiles-" + System.currentTimeMillis()).execute(writer, variablesToReplace);

                // load the YAML file
                final Map<String, Object> yamlContents = (Map<String, Object>) yaml.load(writer.toString());

                // mergeYamlFiles into results map
                merge_internal(mergedResult, yamlContents);
                LOG.debug("Loaded YAML from " + yamlFilePath + ": " + yamlContents);
            }
            finally {
                if (in != null) in.close();
            }
        }
        return mergedResult;
    }

    @SuppressWarnings("unchecked")
    private void merge_internal(Map<String, Object> mergedResult, Map<String, Object> yamlContents)
    {
        if (yamlContents == null)  return;

        for (String key : yamlContents.keySet()) {

            Object yamlValue = yamlContents.get(key);
            if (yamlValue == null) {
                addToMergedResult(mergedResult, key, yamlValue);
                continue;
            }

            Object existingValue = mergedResult.get(key);
            if (existingValue != null) {
                if (yamlValue instanceof Map) {
                    if (existingValue instanceof Map) {
                        merge_internal((Map<String, Object>) existingValue, (Map<String, Object>) yamlValue);
                    }
                    else if (existingValue instanceof String) {
                        throw new IllegalArgumentException("Cannot mergeYamlFiles complex element into a simple element: " + key);
                    }
                    else {
                        throw unknownValueType(key, yamlValue);
                    }
                }
                else if (yamlValue instanceof List) {
                    mergeLists(mergedResult, key, yamlValue);

                }
                else if (yamlValue instanceof String
                        || yamlValue instanceof Boolean
                        || yamlValue instanceof Double
                        || yamlValue instanceof Integer)
                {
                    LOG.debug("Overriding value of " + key + " with value " + yamlValue);
                    addToMergedResult(mergedResult, key, yamlValue);

                }
                else {
                    throw unknownValueType(key, yamlValue);
                }

            }
            else {
                if (yamlValue instanceof Map
                        || yamlValue instanceof List
                        || yamlValue instanceof String
                        || yamlValue instanceof Boolean
                        || yamlValue instanceof Integer
                        || yamlValue instanceof Double)
                {
                    LOG.debug("Adding new key->value: " + key + " -> " + yamlValue);
                    addToMergedResult(mergedResult, key, yamlValue);
                }
                else {
                    throw unknownValueType(key, yamlValue);
                }
            }
        }
    }

    private static IllegalArgumentException unknownValueType(String key, Object yamlValue)
    {
        final String msg = "Cannot mergeYamlFiles element of unknown type: " + key + ": " + yamlValue.getClass().getName();
        LOG.error(msg);
        return new IllegalArgumentException(msg);
    }

    private static Object addToMergedResult(Map<String, Object> mergedResult, String key, Object yamlValue)
    {
        return mergedResult.put(key, yamlValue);
    }

    @SuppressWarnings("unchecked")
    private static void mergeLists(Map<String, Object> mergedResult, String key, Object yamlValue)
    {
        if (!(yamlValue instanceof List && mergedResult.get(key) instanceof List)) {
            throw new IllegalArgumentException("Cannot mergeYamlFiles a list with a non-list: " + key);
        }

        List<Object> originalList = (List<Object>) mergedResult.get(key);
        originalList.addAll((List<Object>) yamlValue);
    }


    public String mergeToString(Set<Path> filesToMerge) throws IOException
    {
        Map<String, Object> merged = mergeYamlFiles(filesToMerge);
        return exportToString(merged);
    }

    public String exportToString(Map<String, Object> merged)
    {
        return yaml.dump(merged);
    }

    // Util methods

    public static Set<Path> stringsToPaths(String[] pathsStr) {
        Set<Path> paths = new LinkedHashSet<>();
        for (String pathStr : pathsStr) {
            paths.add(Paths.get(pathStr));
        }
        return paths;
    }

}
