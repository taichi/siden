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
package ninja.siden.jmx;

import io.undertow.server.session.SessionManagerStatistics;

import java.beans.ConstructorProperties;

/**
 * @author taichi
 */
public class SessionMetrics {

    final long startTime;
    final long createdSessionCount;
    final long maxActiveSessions;
    final long activeSessionCount;
    final long expiredSessionCount;
    final long rejectedSessionCount;
    final long maxSessionAliveTime;
    final long averageSessionAliveTime;

    @ConstructorProperties({"startTime", "createdSessionCount",
            "maxActiveSessions", "activeSessionCount", "expiredSessionCount",
            "rejectedSessionCount", "maxSessionAliveTime",
            "averageSessionAliveTime"})
    public SessionMetrics(long startTime, long createdSessionCount,
                          long maxActiveSessions, long activeSessionCount,
                          long expiredSessionCount, long rejectedSessionCount,
                          long maxSessionAliveTime, long averageSessionAliveTime) {
        super();
        this.startTime = startTime;
        this.createdSessionCount = createdSessionCount;
        this.maxActiveSessions = maxActiveSessions;
        this.activeSessionCount = activeSessionCount;
        this.expiredSessionCount = expiredSessionCount;
        this.rejectedSessionCount = rejectedSessionCount;
        this.maxSessionAliveTime = maxSessionAliveTime;
        this.averageSessionAliveTime = averageSessionAliveTime;
    }

    public static SessionMXBean to(SessionManagerStatistics stats) {
        return () -> new SessionMetrics(stats.getStartTime(),
                stats.getCreatedSessionCount(), stats.getMaxActiveSessions(),
                stats.getActiveSessionCount(), stats.getExpiredSessionCount(),
                stats.getRejectedSessions(), stats.getMaxSessionAliveTime(),
                stats.getAverageSessionAliveTime());
    }

    /**
     * @return The number of sessions that this session manager has created
     */
    public long getCreatedSessionCount() {
        return this.createdSessionCount;
    }

    /**
     * @return the maximum number of sessions this session manager supports
     */
    public long getMaxActiveSessions() {
        return this.maxActiveSessions;
    }

    /**
     * @return The number of active sessions
     */
    public long getActiveSessionCount() {
        return this.activeSessionCount;
    }

    /**
     * @return The number of expired sessions
     */
    public long getExpiredSessionCount() {
        return this.expiredSessionCount;
    }

    /**
     * @return The number of rejected sessions
     */
    public long getRejectedSessions() {
        return this.rejectedSessionCount;
    }

    /**
     * @return The longest a session has been alive for in milliseconds
     */
    public long getMaxSessionAliveTime() {
        return this.maxSessionAliveTime;
    }

    /**
     * @return The average session lifetime in milliseconds
     */
    public long getAverageSessionAliveTime() {
        return this.averageSessionAliveTime;
    }

    /**
     * @return The timestamp at which the session manager started
     */
    public long getStartTime() {
        return this.startTime;
    }
}
