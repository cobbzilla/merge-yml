package org.cobbzilla.util.yml.main;

import org.cobbzilla.util.yml.YmlMerger;

/**
 * (c) Copyright 2013 Jonathan Cobb
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class MergeYml {

    public static void main (String[] args) throws Exception {
        System.out.println(new YmlMerger().mergeToString(args));
    }

}
