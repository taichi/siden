/*
 * Copyright 2015 SATO taichi
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
package ninja.siden.jmx

import io.undertow.server.session.SessionManagerStatistics

import java.beans.ConstructorProperties

/**
 * @author taichi
 */
data class SessionMetrics
@ConstructorProperties("startTime", "createdSessionCount", "maxActiveSessions", "activeSessionCount", "expiredSessionCount", "rejectedSessionCount", "maxSessionAliveTime", "averageSessionAliveTime")
constructor(
        /**
         * @return The timestamp at which the session manager started
         */
        val startTime: Long,
        /**
         * @return The number of sessions that this session manager has created
         */
        val createdSessionCount: Long,
        /**
         * @return the maximum number of sessions this session manager supports
         */
        val maxActiveSessions: Long,
        /**
         * @return The number of active sessions
         */
        val activeSessionCount: Long,
        /**
         * @return The number of expired sessions
         */
        val expiredSessionCount: Long,
        /**
         * @return The number of rejected sessions
         */
        val rejectedSessions: Long,
        /**
         * @return The longest a session has been alive for in milliseconds
         */
        val maxSessionAliveTime: Long,
        /**
         * @return The average session lifetime in milliseconds
         */
        val averageSessionAliveTime: Long) {

    companion object {
        fun to(stats: SessionManagerStatistics): SessionMXBean {
            return object : SessionMXBean {
                override val metrics: SessionMetrics
                    get() = SessionMetrics(stats.startTime,
                            stats.createdSessionCount, stats.maxActiveSessions,
                            stats.activeSessionCount, stats.expiredSessionCount,
                            stats.rejectedSessions, stats.maxSessionAliveTime,
                            stats.averageSessionAliveTime)
            }
        }
    }
}
