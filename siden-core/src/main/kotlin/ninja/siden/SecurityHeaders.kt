/*
 * Copyright 2014 SATO taichi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ninja.siden

import io.undertow.util.HttpString

/**
 * @author taichi
 * *
 * @see [List_of_useful_HTTP_headers](https://www.owasp.org/index.php/List_of_useful_HTTP_headers)
 */
interface SecurityHeaders {
    companion object {

        val REQUESTED_WITH_STRING = "X-Requested-With"

        val STRICT_TRANSPORT_SECURITY_STRING = "Strict-Transport-Security"
        val FRAME_OPTIONS_STRING = "X-Frame-Options"
        val XSS_PROTECTION_STRING = "X-XSS-Protection"
        val CONTENT_TYPE_OPTIONS_STRING = "X-Content-Type-Options"
        val CONTENT_SECURITY_POLICY_STRING = "Content-Security-Policy"
        val CONTENT_SECURITY_POLICY_REPORT_ONLY_STRING = "Content-Security-Policy-Report-Only"

        val REQUESTED_WITH = HttpString(REQUESTED_WITH_STRING)

        val STRICT_TRANSPORT_SECURITY = HttpString(
                STRICT_TRANSPORT_SECURITY_STRING)
        val FRAME_OPTIONS = HttpString(FRAME_OPTIONS_STRING)
        val XSS_PROTECTION = HttpString(XSS_PROTECTION_STRING)
        val CONTENT_TYPE_OPTIONS = HttpString(
                CONTENT_TYPE_OPTIONS_STRING)
        val CONTENT_SECURITY_POLICY = HttpString(
                CONTENT_SECURITY_POLICY_STRING)
        val CONTENT_SECURITY_REPORT_ONLY_POLICY = HttpString(
                CONTENT_SECURITY_POLICY_REPORT_ONLY_STRING)
    }

}
