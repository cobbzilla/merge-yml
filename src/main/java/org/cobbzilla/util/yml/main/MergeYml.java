package org.cobbzilla.util.yml.main;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.cobbzilla.util.yml.YmlMerger;

/**
 * (c) Copyright 2013 Jonathan Cobb
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class MergeYml {

    public static void main (String[] args) throws Exception {

        YmlMerger yamlMerger = new YmlMerger();
        yamlMerger.setVariablesToReplace(System.getenv());

        Set<Path> filesToMerge = new LinkedHashSet<>();

        //Arrays.asList(args).stream().map(Paths::get).collect(Collectors.toSet());
        for (String arg : args) {
            filesToMerge.add(Paths.get(arg));
        }

        String resultingYaml = yamlMerger.mergeToString(filesToMerge);

        System.out.println(resultingYaml);
    }

}
