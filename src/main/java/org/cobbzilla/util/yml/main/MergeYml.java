package org.cobbzilla.util.yml.main;

import org.cobbzilla.util.yml.YmlMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * (c) Copyright 2013 Jonathan Cobb
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class MergeYml {

    private static final Logger LOG = LoggerFactory.getLogger(MergeYml.class);

    public static void main (String[] args) throws Exception {
        System.out.println(new YmlMerger().mergeToString(args));
    }

}
