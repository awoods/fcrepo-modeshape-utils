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
package org.fcrepo.modeshape.utils;

import org.modeshape.jcr.JcrRepository;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.RepositoryConfiguration;
import org.modeshape.jcr.api.BackupOptions;
import org.modeshape.jcr.api.Problems;
import org.modeshape.jcr.api.RepositoryManager;
import org.modeshape.jcr.api.Session;
import org.modeshape.schematic.document.ParsingException;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.FileNotFoundException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This utility exposes backup/restore capability directly to ModeShape
 *
 * @author awoods
 * @since 2017-04-30
 */
public class ModeShapeBackupRestore {

    private static final Logger LOGGER = getLogger(ModeShapeBackupRestore.class);

    private ModeShapeEngine engine;
    private JcrRepository repository;
    private Session session;

    private File backupDirectory;
    private boolean backup;

    /**
     * Constructor
     */
    public ModeShapeBackupRestore(final File repoConfig, final File backupDirectory, final boolean backup) {
        startEngine(repoConfig);
        this.backupDirectory = backupDirectory;
        this.backup = backup;
    }

    /**
     * This method runs the backup/restore
     */
    public void run() {
        try {
            doRun();
        } finally {
            stop();
        }
    }

    private void stop() {
        engine.shutdown();
    }

    private void doRun() {
        final RepositoryManager repoMgr = getRepositoryManager();
        final Problems problems;

        try {
            if (backup) {
                problems = repoMgr.backupRepository(backupDirectory, new ExcludeBinaries());
                LOGGER.info("Successful backup!");
            } else {
                problems = repoMgr.restoreRepository(backupDirectory);
                LOGGER.info("Successful restore!");
            }
        } catch (RepositoryException e) {
            throw new RuntimeException("Error performing " + (backup ? "backup" : "restore") + e.getMessage());
        }

        if (problems.size() > 0) {
            throwProblems(problems);
        }
    }

    /**
     * Visible For Testing     *
     *
     * @return ModeShape RepositoryManager
     */
    public RepositoryManager getRepositoryManager() {
        session = login();
        try {
            return session.getWorkspace().getRepositoryManager();
        } catch (RepositoryException e) {
            throw new RuntimeException("Unable to get repository manager: " + e.getMessage());
        }
    }

    private Session login() {
        try {
            return repository.login();
        } catch (RepositoryException e) {
            throw new RuntimeException("Unable to login: " + e.getMessage());
        }
    }

    private void throwProblems(final Problems problems) {
        final StringBuilder msg = new StringBuilder("There were problems: ");
        problems.forEach(p -> msg.append(p.getMessage()));
        throw new RuntimeException(msg.toString());
    }


    private void startEngine(final File repoConfig) {
        final RepositoryConfiguration config;
        try {
            config = RepositoryConfiguration.read(repoConfig);

        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + repoConfig + ", " + e.getMessage());
        } catch (ParsingException e) {
            throw new RuntimeException("Invalid configuration: " + repoConfig + ", " + e.getMessage());
        }

        engine = new ModeShapeEngine();
        engine.start();
        try {
            repository = engine.deploy(config);

            // next line ensures that repository starts before the factory is used.
            final org.modeshape.common.collection.Problems problems = repository.getStartupProblems();
            for (final org.modeshape.common.collection.Problem p : problems) {
                LOGGER.error("ModeShape Start Problem: {}", p.getMessageString());
            }
        } catch (RepositoryException e) {
            throw new RuntimeException("Error ");
        } catch (Exception e) {
            throw new RuntimeException("Unknown error: " + e.getMessage());
        }
    }

    private class ExcludeBinaries extends BackupOptions {
        @Override
        public boolean includeBinaries() {
            return false;
        }
    }

    /**
     * This is the 'main' method for running as a standalone executable
     *
     * @param args from command line
     */
    public static void main(final String[] args) {
        if (args.length != 3 || (!args[2].equalsIgnoreCase("b") && !args[2].equalsIgnoreCase("r"))) {
            final StringBuilder msg = new StringBuilder("There must be 3 arguments!");
            msg.append("\nUsage: java -jar modeshape-utils.jar <path-to-repository.json> <path-to-backup-directory> ");
            msg.append("<b|r>");
            msg.append("\n-- Where 'b' indicates a backup and 'r' a restore");
            LOGGER.error(msg.toString());
            return;
        }

        final File repoConfig = new File(args[0]);
        final File backupDirectory = new File(args[1]);
        final boolean backup = args[2].equalsIgnoreCase("b");

        final ModeShapeBackupRestore modeshape = new ModeShapeBackupRestore(repoConfig, backupDirectory, backup);
        modeshape.run();
    }
}
