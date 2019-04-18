/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.sling.feature.cpconverter.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.apache.jackrabbit.vault.fs.io.Archive;
import org.apache.jackrabbit.vault.fs.io.Archive.Entry;
import org.apache.sling.feature.ArtifactId;
import org.apache.sling.feature.Extension;
import org.apache.sling.feature.ExtensionType;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.cpconverter.ContentPackage2FeatureModelConverter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SystemUsersEntryHandlerTest {

    private SystemUsersEntryHandler systemUsersEntryHandler;

    @Before
    public void setUp() {
        systemUsersEntryHandler = new SystemUsersEntryHandler();
    }

    @After
    public void tearDown() {
        systemUsersEntryHandler = null;
    }

    @Test
    public void doesNotMatch() {
        assertFalse(systemUsersEntryHandler.matches("/this/is/a/path/not/pointing/to/a/valid/configuration.asd"));
    }

    @Test
    public void matches() {
        assertTrue(systemUsersEntryHandler.matches("/home/users/system/asd-share-commons/asd-index-definition-reader/.content.xml"));
        assertTrue(systemUsersEntryHandler.matches("jcr_root/home/users/system/asd-share-commons/asd-index-definition-reader/.content.xml"));
    }

    @Test
    public void parseSystemUser() throws Exception {
        String path = "jcr_root/home/users/system/asd-share-commons/asd-index-definition-reader/.content.xml";
        Extension repoinitExtension = parseAndSetRepoinit(path);

        assertNotNull(repoinitExtension);
        assertEquals(ExtensionType.TEXT, repoinitExtension.getType());
        assertTrue(repoinitExtension.isRequired());
        assertEquals("create service user asd-share-commons-asd-index-definition-reader-service", repoinitExtension.getText());
    }

    @Test
    public void unrecognisedSystemUserJcrNode() throws Exception {
        String path = "jcr_root/home/users/system/asd-share-commons/asd-index-definition-invalid/.content.xml";
        Extension repoinitExtension = parseAndSetRepoinit(path);
        assertNull(repoinitExtension);
    }

    private Extension parseAndSetRepoinit(String path) throws Exception {
        Archive archive = mock(Archive.class);
        Entry entry = mock(Entry.class);

        when(archive.openInputStream(entry)).thenReturn(getClass().getResourceAsStream(path));

        Feature feature = new Feature(new ArtifactId("org.apache.sling", "org.apache.sling.cp2fm", "0.0.1", null, null));
        ContentPackage2FeatureModelConverter converter = spy(ContentPackage2FeatureModelConverter.class);
        when(converter.getTargetFeature()).thenReturn(feature);

        systemUsersEntryHandler.handle(path, archive, entry, converter);

        return feature.getExtensions().getByName("repoinit");
    }

}