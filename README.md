Chaos HTTP Proxy
================
Introduce failures into HTTP requests via a
[proxy server](http://en.wikipedia.org/wiki/Proxy_server).
This can uncover error handling bugs in HTTP clients.
Andrew Gaul at Bounce Storage <gaul@bouncestorage.com> originally wrote
Chaos HTTP Proxy.

Features
--------
Chaos HTTP Proxy can trigger many different failures:

* [change case of HTTP header name](http://stackoverflow.com/questions/5258977/are-http-headers-case-sensitive)
* Content-MD5 request corruption
* Content-MD5 response corruption
* client timeout, HTTP 408
* redirects (temporary and permanent)
* reorder response headers
* server connection break, i.e., short read and write
* server errors: HTTP 500, 503, and 504
* server timeout

Installation
------------
Users can
[download releases](https://github.com/bouncestorage/chaos-http-proxy/releases)
from GitHub.
One can also build the project by running `mvn package` which produces a
binary at `target/chaos-http-proxy`.
Chaos HTTP Proxy requires Java 8 to run.

Examples
--------
Linux and Mac OS X users can run Chaos HTTP Proxy via the executable jar:

```
chmod +x chaos-http-proxy
chaos-http-proxy --properties chaos-http-proxy.conf
```

Windows users must explicitly invoke java:

```
java -jar chaos-http-proxy --properties chaos-http-proxy.conf
```

An example using `curl`:

```
curl --fail --proxy http://localhost:1080/ http://google.com/
curl: (22) The requested URL returned error: 500 Server Error
```

Configuring Failure Rates
-------------------------

The configuration file determines all possible results for an HTTP request.
Entries take the form `[response_type]=[n]`, where `[n]` is an integral value
that determines the relative occurrence likelihood of each response type.
For example, the following configuration gives a 1% chance of responses
failing with a `500 Internal Error` response:

```
com.bouncestorage.chaoshttpproxy.http_500=1
com.bouncestorage.chaoshttpproxy.success=99
```

[Sample configuration](https://github.com/bouncestorage/chaos-http-proxy/blob/master/src/main/resources/chaos-http-proxy.conf)

Chaos HTTP Proxy accepts configuration at invocation time via the `--properties`
flag and at run-time via the `/chaos/api` endpoint:

```
curl --request POST --upload-file chaos-http-proxy.conf http://localhost:1080/chaos/api
curl http://localhost:1080/chaos/api
com.bouncestorage.chaoshttpproxy.success=100
```

Limitations
-----------
* lacks HTTP authentication
* lacks HTTPS support

References
----------
* [Charles Web Debugging Proxy](http://www.charlesproxy.com/) - allows interactive modification of HTTP requests and responses
* [Chaos Monkey](https://github.com/Netflix/SimianArmy) - inspiration for Chaos HTTP Proxy
* [Hamms](https://github.com/kevinburke/hamms) - designed to elicit failures in your HTTP Client, similar to httpbin
* [httpbin](http://httpbin.org/) - HTTP Request & Response Service which can deterministically exercise HTTP functionality
* [pathod](http://pathod.net/docs/pathod) - programmable HTTP server
* [toxiproxy](https://github.com/Shopify/toxiproxy) - A proxy to simulate network and system conditions
* [toxy](https://github.com/h2non/toxy) - Hackable HTTP proxy to simulate server failure scenarios and unexpected network conditions
* [Vaurien](https://github.com/mozilla-services/vaurien) - Chaos TCP Proxy

License
-------
Copyright (C) 2015-2016 Bounce Storage

Licensed under the Apache License, Version 2.0
