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
