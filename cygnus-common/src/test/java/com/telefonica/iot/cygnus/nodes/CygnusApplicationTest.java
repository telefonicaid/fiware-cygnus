/**
 * Copyright 2017 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 */
package com.telefonica.iot.cygnus.nodes;

import static com.telefonica.iot.cygnus.utils.CommonUtilsForTests.getTestTraceHead;
import static org.junit.Assert.assertTrue;

import java.security.Permission;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.telefonica.iot.cygnus.nodes.CygnusApplication.YAFS;

/**
 *
 * @author dmartinez
 */
public class CygnusApplicationTest {

    private SecurityManager originalManager;
    private TestSecurityManager testManager;

    /**
     * Constructor.
     */
    public CygnusApplicationTest() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    } // MySQLCacheTest

    /**
     * Sets up tests by creating a unique instance of the tested class.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        System.out.println("Seting test security manager.");
        // Save original security manager.
        this.originalManager = System.getSecurityManager();
        testManager = new TestSecurityManager();
        System.setSecurityManager(testManager);

    } // setUp

    /**
     * Restore original configuration.
     */
    @After
    public void restore() {
        System.out.println("Restoring original security manager.");
        // restore original security manager.
        System.setSecurityManager(this.originalManager);
    }
    
    /**
     * Method to run into dummy threads.
     * @param name
     */
    public synchronized void dummyProccess(String name) {
        try {
            wait(3000);
            System.out.println("Thread " + name + " finishing.");
        } catch (InterruptedException e) {
            System.err.println("ERROR waiting in thread " + name);
        }
    }

    /**
     * [YAFS] -------- Test testJettyThreads.
     */
    @Test
    public void testJettyThreads() {
        
        testManager.resetExitReceived();

        System.out.println(getTestTraceHead("[testYAFS]") + "-------- Test testJettyThreads");

        Thread jetty1 = new Thread("qtp586434923-27") {
            public void run() {
                dummyProccess(this.getName());
            }
        };
        System.out.println("Starting thread qtp586434923-27");
        jetty1.start();

        Thread jetty2 = new Thread("106024875@qtp-1176250385-1") {
            public void run() {
                dummyProccess(this.getName());
            }
        };
        System.out.println("Starting thread 106024875@qtp-1176250385-1");
        jetty2.start();

        System.out.println("Starting YAFS");
        CygnusApplication.YAFS yafs = new YAFS();
        yafs.start();

        System.out.println("Stopping thread qtp586434923-27");
        synchronized (jetty1) {
            jetty1.notifyAll();
        }
        try {
            jetty1.join();
        } catch (InterruptedException e) {
            System.out.println("WARN - Error joining thread.");
        }

        System.out.println("Stopping thread 106024875@qtp-1176250385-1");
        synchronized (jetty2) {
            jetty2.notifyAll();
        }
        try {
            jetty2.join();
        } catch (InterruptedException e) {
            System.out.println("WARN - Error joining thread.");
        }

        System.out.println("Waiting for YAFS response.");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println("WARN - Error sleeping main thread.");
        }

        System.out.println("Stopping YAFS.");
        // Kill YAFS thread
        yafs.stop();

        System.out.println("Exit calls received = " + testManager.getExitReceived());
        assertTrue(!testManager.getExitReceived());
    } // testJettyThreads

    /**
     * [YAFS] -------- Test testOtherThreads.
     */
    @Test
    public void testOtherThreads() {

        testManager.resetExitReceived();

        System.out.println(getTestTraceHead("[testYAFS]") + "-------- Test testOtherThreads");

        Thread jetty1 = new Thread("VeryImportanThread") {
            public void run() {
                dummyProccess(this.getName());
            }
        };
        System.out.println("Starting thread VeryImportanThread");
        jetty1.start();

        System.out.println("Starting YAFS");
        CygnusApplication.YAFS yafs = new YAFS();
        yafs.start();

        System.out.println("Stopping thread VeryImportanThread");
        synchronized (jetty1) {
            jetty1.notifyAll();
        }
        try {
            jetty1.join();
        } catch (InterruptedException e) {
            System.out.println("WARN - Error joining thread.");
        }

        System.out.println("Waiting for YAFS response.");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println("WARN - Error sleeping main thread.");
        }

        System.out.println("Stopping YAFS.");
        // Kill YAFS thread
        yafs.stop();

        System.out.println("Exit calls received = " + testManager.getExitReceived());
        assertTrue(testManager.getExitReceived());
    } // testOtherThreads
    
    /**
     * Exception to throw on System.exit()
     */
    private static class SystemExitException extends SecurityException {
        public int exitCode = 0;

        SystemExitException(int exitCode) {
            super();
            this.exitCode = exitCode;
        }
    }

    /**
     * Security manager to avoid System.exit() calls
     */
    private static class TestSecurityManager extends SecurityManager {

        public AtomicBoolean exitReceived = new AtomicBoolean(false);

        /**
         * @return exitReceived
         */
        public boolean getExitReceived() {
            return exitReceived.get();
        }

        /**
         * 
         */
        public void resetExitReceived() {
            this.exitReceived.set(false);
        }

        @Override
        public void checkExit(int exitCode) {
            super.checkExit(exitCode);
            System.out.println("System.exit() called with code: " + exitCode);
            exitReceived.set(true);
            throw new SystemExitException(exitCode);
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
            // do nothing
        }

        @Override
        public void checkPermission(Permission perm) {
            // do nothing
        }

    }

} // CygnusApplicationTest
