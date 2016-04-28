/*
 * Copyright 2015-2016 Bounce Storage, Inc. <info@bouncestorage.com>
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
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

        ByteSource byteSource;
        if (options.propertiesFile == null) {
            byteSource = Resources.asByteSource(Resources.getResource(
                        "chaos-http-proxy.conf"));
        } else {
            byteSource = Files.asByteSource(options.propertiesFile);
        }

        ChaosConfig config;
        try (InputStream is = byteSource.openStream()) {
            config = ChaosConfig.loadFromPropertyStream(is);
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
            System.exit(1);
            return;
        }

        URI proxyEndpoint = new URI("http", null, options.address,
                options.port, null, null, null);
        ChaosHttpProxy proxy = new ChaosHttpProxy(proxyEndpoint, config);
        try {
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
