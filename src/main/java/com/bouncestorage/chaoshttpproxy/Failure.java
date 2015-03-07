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
    /** Return partially correct data then close socket. */
    PARTIAL_DATA,
    /** Reorder HTTP response headers. */
    REORDER_HEADERS,
    /** Return first 10 bytes slowly, then return all bytes at full speed. */
    SLOW_RESPONSE,
    /** Never return anything. */
    TIMEOUT,

    /** Default. */
    SUCCESS;

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
}
