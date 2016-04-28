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

enum Failure {
    /** Moved Permanently. */
    HTTP_301(301),
    /** Found. */
    HTTP_302(302),
    /** See other. */
    HTTP_303(303),
    /** Temporary Redirect. */
    HTTP_307(307),
    /** Permanent Redirect. */
    HTTP_308(308),
    /** Request Timeout. */
    HTTP_408(408),
    /** Internal Server Error. */
    HTTP_500(500),
    /** Service Unavailable. */
    HTTP_503(503),
    /** Gateway Timeout. */
    HTTP_504(504),

    /** Change HTTP header to upper-case. */
    CHANGE_HEADER_CASE,
    /** Corrupt Content-MD5 header in request. */
    CORRUPT_REQUEST_CONTENT_MD5,
    /** Corrupt Content-MD5 header in response. */
    CORRUPT_RESPONSE_CONTENT_MD5,
    /** Read partial request then close socket. */
    PARTIAL_REQUEST,
    /** Write partial response then close socket. */
    PARTIAL_RESPONSE,
    /** Reorder HTTP response headers. */
    REORDER_HEADERS,
    /** Return first 10 bytes slowly, then return all bytes at full speed. */
    SLOW_RESPONSE,
    /** Never return anything. */
    TIMEOUT,

    /** Default. */
    SUCCESS;

    static final String CHAOS_CONFIG_PREFIX =
            "com.bouncestorage.chaoshttpproxy.";

    private int responseCode;

    Failure() {
        this(-1);
    }

    Failure(int responseCode) {
        this.responseCode = responseCode;
    }

    int getResponseCode() {
        return responseCode;
    }

    String toPropertyName() {
        return Failure.CHAOS_CONFIG_PREFIX + super.toString().toLowerCase();
    }

    static Failure fromPropertyName(String propertyName) {
        if (propertyName.startsWith(CHAOS_CONFIG_PREFIX)) {
            try {
                return Failure.valueOf(propertyName.substring(
                        CHAOS_CONFIG_PREFIX.length()).toUpperCase());
            } catch (IllegalArgumentException iae) {
                // handled below
            }
        }
        throw new IllegalArgumentException("Unexpected failure type: " +
                propertyName);
    }
}
