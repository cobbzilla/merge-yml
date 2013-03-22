package org.cobbzilla.util.yml;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import static junit.framework.Assert.*;

/**
 * (c) Copyright 2013 Jonathan Cobb
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class YmlMergerTest {

    private static final Logger LOG = LoggerFactory.getLogger(YmlMergerTest.class);

    public static final String YML_1 = getResourceFile("test1.yml");
    public static final String YML_2 = getResourceFile("test2.yml");
    public static final String YML_NULL = getResourceFile("test-null.yml");
    public static final String YML_COLON = getResourceFile("test-colon.yml");

    private final Yaml yaml = new Yaml();
    private final YmlMerger merger = new YmlMerger();

    @Test
    public void testMerge2Files () throws Exception {
        final Map<String, Object> merged = merger.merge(new String[]{YML_1, YML_2});
        Map<String, Object> dbconfig;
        dbconfig = (Map<String, Object>) merged.get("database");
        assertEquals("wrong user", dbconfig.get("user"), "alternate-user");
        assertEquals("wrong db url", dbconfig.get("url"), "jdbc:mysql://localhost:3306/some-db");

        final String mergedYmlString = merger.toString(merged);
        LOG.info("resulting YML=\n"+ mergedYmlString);
        final Map<String, Object> reloadedYaml = (Map<String, Object>) yaml.load(mergedYmlString);
        dbconfig = (Map<String, Object>) reloadedYaml.get("database");
        assertEquals("wrong user", dbconfig.get("user"), "alternate-user");
        assertEquals("wrong db url", dbconfig.get("url"), "jdbc:mysql://localhost:3306/some-db");
        Map<String, Object> dbProperties = (Map<String, Object>) dbconfig.get("properties");
        assertEquals("wrong db url", dbProperties.get("hibernate.dialect"), "org.hibernate.dialect.MySQL5InnoDBDialect");
    }

    @Test
    public void testMergeFileIntoSelf () throws Exception {
        final Map<String, Object> merged = merger.merge(new String[]{YML_1, YML_1});
        final Map<String, Object> dbconfig = (Map<String, Object>) merged.get("database");
        assertEquals("wrong user", dbconfig.get("user"), "some-user");
        assertEquals("wrong db url", dbconfig.get("url"), "jdbc:mysql://localhost:3306/some-db");
    }

    @Test
    public void testNullValue () throws Exception {
        final Map<String, Object> merged = merger.merge(new String[]{YML_NULL});
        assertNotNull(merged.get("prop1"));
        assertNull(merged.get("prop2"));
    }

    @Test
    public void testSubstitutionValueWithColon () throws Exception {
        final Map<String, Object> merged = new YmlMerger(Collections.singletonMap("ENV_VAR", "localhost")).merge(new String[]{YML_COLON});
        final Map<String, Object> hash = (Map<String, Object>) merged.get("memcache");
        assertEquals(hash.get("one_key"), "value1");
        assertEquals(hash.get("another_key"), "localhost:22133");
        assertEquals(hash.get("some_other_key"), "value2");
    }

    public static String getResourceFile(String file) {
        return new File(System.getProperty("user.dir")+"/src/test/resources/"+ file).getAbsolutePath();
    }

}
