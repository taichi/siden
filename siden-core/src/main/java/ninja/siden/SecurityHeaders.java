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
package ninja.siden;

import io.undertow.util.HttpString;

/**
 * @author taichi
 * @see <a href="https://www.owasp.org/index.php/List_of_useful_HTTP_headers">List_of_useful_HTTP_headers</a>
 */
public interface SecurityHeaders {

	String REQUESTED_WITH_STRING = "X-Requested-With";

	String STRICT_TRANSPORT_SECURITY_STRING = "Strict-Transport-Security";
	String FRAME_OPTIONS_STRING = "X-Frame-Options";
	String XSS_PROTECTION_STRING = "X-XSS-Protection";
	String CONTENT_TYPE_OPTIONS_STRING = "X-Content-Type-Options";
	String CONTENT_SECURITY_POLICY_STRING = "Content-Security-Policy";
	String CONTENT_SECURITY_POLICY_REPORT_ONLY_STRING = "Content-Security-Policy-Report-Only";

	HttpString REQUESTED_WITH = new HttpString(REQUESTED_WITH_STRING);

	HttpString STRICT_TRANSPORT_SECURITY = new HttpString(
			STRICT_TRANSPORT_SECURITY_STRING);
	HttpString FRAME_OPTIONS = new HttpString(FRAME_OPTIONS_STRING);
	HttpString XSS_PROTECTION = new HttpString(XSS_PROTECTION_STRING);
	HttpString CONTENT_TYPE_OPTIONS = new HttpString(
			CONTENT_TYPE_OPTIONS_STRING);
	HttpString CONTENT_SECURITY_POLICY = new HttpString(
			CONTENT_SECURITY_POLICY_STRING);
	HttpString CONTENT_SECURITY_REPORT_ONLY_POLICY = new HttpString(
			CONTENT_SECURITY_POLICY_REPORT_ONLY_STRING);

}
