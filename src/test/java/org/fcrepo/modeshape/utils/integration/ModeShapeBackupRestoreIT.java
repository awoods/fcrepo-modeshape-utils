/*
 * Licensed to DuraSpace under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * DuraSpace licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fcrepo.modeshape.utils.integration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.fcrepo.modeshape.utils.ModeShapeBackupRestore;
import org.junit.Test;
import org.modeshape.jcr.value.binary.BinaryStoreException;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author awoods
 */
public class ModeShapeBackupRestoreIT {

    private ModeShapeBackupRestore modeshape;

    @Test
    public void testGetChildren() throws IOException, RepositoryException, InterruptedException {
        // Known number of containers from test data
        final int numContainers = 4;

        // Known number of binaries from test data
        final int numBinaries = 3;

        // Perform backup followed by restore
        final File repoConfig = new File("target/test-classes/repository-test.json");
        final File backupDirectory = new File("target/test-backup");

        // Perform backup
        modeshape = new ModeShapeBackupRestore(repoConfig, backupDirectory, true);
        binaryExists(true);

        // Ensure there are numContainers + numBinaries number of resources (plus system-resource)
        assertEquals(numContainers + numBinaries + 1, getChildren());
        modeshape.run();

        Thread.sleep(1000);

        // Remove test data
        final File testData = new File("target/test-classes/fcrepo4-data");
        FileUtils.forceDelete(testData);

        // Perform restore
        modeshape = new ModeShapeBackupRestore(repoConfig, backupDirectory, false);
        modeshape.run();

        Thread.sleep(1000);

        // Ensure there are numContainers + numBinaries number of resources (plus system-resource)
        modeshape = new ModeShapeBackupRestore(repoConfig, backupDirectory, false);
        assertEquals(numContainers + numBinaries + 1, getChildren());

        // Ensure binaries were not restored!
        binaryExists(false);
    }

    private void binaryExists(final boolean expected) throws RepositoryException, IOException {
        // Known resource in test data
        final String imageName = "image2";

        final Node contentNode = getRootNode().getNode(imageName).getNode("jcr:content");
        final Binary binary = contentNode.getProperty("jcr:data").getBinary();

        boolean exists;
        try {
            final String content = IOUtils.toString(binary.getStream());
            assertTrue("Length: " + content.length(), content.length() > 0);
            exists = true;
        } catch (final BinaryStoreException e) {
            exists = false;
        }

        assertEquals(expected, exists);
    }

    private int getChildren() throws RepositoryException {
        final StringBuilder count = new StringBuilder();
        getRootNode().getNodes().forEachRemaining(n -> count.append("x"));
        return count.length();
    }

    private Node getRootNode() throws RepositoryException {
        return modeshape.getRepositoryManager().getWorkspace().getSession().getRootNode();
    }

}
