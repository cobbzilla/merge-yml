package org.cobbzilla.util.yml;

import com.github.mustachejava.DefaultMustacheFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * (c) Copyright 2013-2015 Jonathan Cobb
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class YmlMerger
{

    private static final Logger LOG = LoggerFactory.getLogger(YmlMerger.class);
    public static final DefaultMustacheFactory DEFAULT_MUSTACHE_FACTORY = new DefaultMustacheFactory();

    private final Yaml snakeYaml;
    private Map<String, Object> variablesToReplace = new HashMap<String, Object>();

    public YmlMerger()
    {
        // See https://github.com/spariev/snakeyaml/blob/master/src/test/java/org/yaml/snakeyaml/DumperOptionsTest.java
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setPrettyFlow(true);
        //dumperOptions.setCanonical(true);
        dumperOptions.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.snakeYaml = new Yaml(dumperOptions);
    }

    public YmlMerger setVariablesToReplace(Map<String, String> vars) {
        this.variablesToReplace.clear();
        this.variablesToReplace.putAll(vars);
        return this;
    }



    public Map<String, Object> mergeYamlFiles(String[] pathsStr) throws IOException {
        return mergeYamlFiles(stringsToPaths(pathsStr));
    }

    /**
     * Merges the files at given paths to a map representing the resulting YAML structure.
     */
    public Map<String, Object> mergeYamlFiles(List<Path> paths) throws IOException
    {
        Map<String, Object> mergedResult = new LinkedHashMap<String, Object>();
        for (Path yamlFilePath : paths) {
            InputStream in = null;
            try {
                File file = yamlFilePath.toFile();
                if (!file.exists())
                    throw new FileNotFoundException("YAML file to merge not found: " + file.getCanonicalPath());

                // Read the YAML file into a String
                in = new FileInputStream(file);
                final String entireFile = IOUtils.toString(in);

                // Substitute variables. TODO: This should be done by a resolver when parsing.
                int bufferSize = entireFile.length() + 100;
                final StringWriter writer = new StringWriter(bufferSize);
                DEFAULT_MUSTACHE_FACTORY.compile(new StringReader(entireFile), "yaml-mergeYamlFiles-" + System.currentTimeMillis()).execute(writer, variablesToReplace);

                // Parse the YAML.
                String yamlString = writer.toString();
                final Map<String, Object> yamlToMerge = (Map<String, Object>) this.snakeYaml.load(yamlString);

                // Merge into results map.
                mergeStructures(mergedResult, yamlToMerge);
                LOG.debug("Loaded YAML from " + yamlFilePath + ": " + yamlToMerge);
            }
            finally {
                if (in != null) in.close();
            }
        }
        return mergedResult;
    }

    @SuppressWarnings("unchecked")
    private void mergeStructures(Map<String, Object> targetTree, Map<String, Object> sourceTree)
    {
        if (sourceTree == null)  return;

        for (String key : sourceTree.keySet()) {

            Object yamlValue = sourceTree.get(key);
            if (yamlValue == null) {
                addToMergedResult(targetTree, key, yamlValue);
                continue;
            }

            Object existingValue = targetTree.get(key);
            if (existingValue != null) {
                if (yamlValue instanceof Map) {
                    if (existingValue instanceof Map) {
                        mergeStructures((Map<String, Object>) existingValue, (Map<String, Object>) yamlValue);
                    }
                    else if (existingValue instanceof String) {
                        throw new IllegalArgumentException("Cannot mergeYamlFiles complex element into a simple element: " + key);
                    }
                    else {
                        throw unknownValueType(key, yamlValue);
                    }
                }
                else if (yamlValue instanceof List) {
                    mergeLists(targetTree, key, yamlValue);

                }
                else if (yamlValue instanceof String
                        || yamlValue instanceof Boolean
                        || yamlValue instanceof Double
                        || yamlValue instanceof Integer)
                {
                    LOG.debug("Overriding value of " + key + " with value " + yamlValue);
                    addToMergedResult(targetTree, key, yamlValue);

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
                    addToMergedResult(targetTree, key, yamlValue);
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


    public String mergeToString(List<Path> filesToMerge) throws IOException
    {
        Map<String, Object> merged = mergeYamlFiles(filesToMerge);
        return exportToString(merged);
    }

    public String exportToString(Map<String, Object> merged)
    {
        return snakeYaml.dump(merged);
    }

    // Util methods

    public static List<Path> stringsToPaths(String[] pathsStr) {
        Set<Path> paths = new LinkedHashSet<>();
        for (String pathStr : pathsStr) {
            paths.add(Paths.get(pathStr));
        }
        List<Path> pathsList = new ArrayList<>(paths.size());
        pathsList.addAll(paths);
        return pathsList;
    }

}
