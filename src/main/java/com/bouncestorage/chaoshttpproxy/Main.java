/*
 * Copyright 2015 Bounce Storage, Inc. <info@bouncestorage.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bouncestorage.chaoshttpproxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import com.google.common.base.Supplier;
import com.google.common.io.Resources;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public final class Main {
    private Main() {
        throw new AssertionError("intentionally not implemented");
    }

    private static final class Options {
        @Option(name = "--address",
                usage = "Address to listen on (default 127.0.0.1)")
        private String address = "127.0.0.1";

        @Option(name = "--port", usage = "Port to listen on (default: 1080)")
        private int port = 1080;

        @Option(name = "--properties",
                usage = "Proxy configuration (defaults to 1% chance of all" +
                " failures")
        private File propertiesFile;

        @Option(name = "--version", usage = "display version")
        private boolean version;
    }

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException cle) {
            usage(parser);
        }

        if (options.version) {
            System.err.println(
                    Main.class.getPackage().getImplementationVersion());
            System.exit(0);
        }

        Properties properties = new Properties();
        if (options.propertiesFile == null) {
            try (InputStream is = Resources.asByteSource(Resources.getResource(
                    "chaos-http-proxy.conf")).openStream()) {
                properties.load(is);
            }
        } else {
            try (InputStream is = new FileInputStream(options.propertiesFile)) {
                properties.load(is);
            }
        }
        properties.putAll(System.getProperties());

        final List<Failure> failures = new ArrayList<>();
        for (String propertyName : properties.stringPropertyNames()) {
            String prefix = "com.bouncestorage.chaoshttpproxy.";
            if (!propertyName.startsWith(prefix)) {
                continue;
            }
            String failureName = propertyName.substring(prefix.length());
            Failure failure;
            try {
                failure = Failure.valueOf(failureName.toUpperCase());
            } catch (IllegalArgumentException iae) {
                System.err.println("Invalid failure: " + failureName);
                System.err.println("Valid failures:");
                for (Failure failure2 : Failure.values()) {
                    System.err.println(failure2.toString().toLowerCase());
                }
                System.exit(1);
                throw iae;
            }
            int occurrences = Integer.parseInt(properties.getProperty(
                    propertyName));
            for (int i = 0; i < occurrences; ++i) {
                failures.add(failure);
            }
        }

        URI proxyEndpoint = new URI("http", null, options.address,
                options.port, null, null, null);
        ChaosHttpProxy proxy = new ChaosHttpProxy(proxyEndpoint,
                new RandomFailureSupplier(failures));
            proxy.start();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void usage(CmdLineParser parser) {
        System.err.println("Usage: s3proxy [options...]");
        parser.printUsage(System.err);
        System.exit(1);
    }
}
