/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2017, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.restcomm.cache;

public class CacheExecutorConfiguration {

    private Integer numberOfThreads;
    private Long terminationTimeout;
    private Long commandTimeout;

    public CacheExecutorConfiguration(Integer numberOfThreads, Long terminationTimeout, Long commandTimeout) {
        this.numberOfThreads = numberOfThreads;
        this.terminationTimeout = terminationTimeout;
        this.commandTimeout = commandTimeout;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("numberOfThreads:" + numberOfThreads).append(",");
        sb.append("terminationTimeout:" + terminationTimeout).append(",");
        sb.append("commandTimeout:" + commandTimeout);
        return sb.toString();
    }

    public Integer getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(Integer numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }

    public Long getTerminationTimeout() {
        return terminationTimeout;
    }

    public void setTerminationTimeout(Long terminationTimeout) {
        this.terminationTimeout = terminationTimeout;
    }

    public Long getCommandTimeout() {
        return commandTimeout;
    }

    public void setCommandTimeout(Long commandTimeout) {
        this.commandTimeout = commandTimeout;
    }
}
