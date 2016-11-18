package com.ccl.elasticsearch.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.joda.time.DateTime;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author dean
 * @date 2016/3/15.
 */
public class ElasticSearchAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    BlockingQueue<ILoggingEvent> blockingQueue;

    /**
     * The default buffer size.
     */
    public static final int DEFAULT_QUEUE_SIZE = 256;
    int queueSize = DEFAULT_QUEUE_SIZE;

    private String index;

    Worker worker = new Worker();

    @Override
    public void start() {
        if (queueSize < 1) {
            addError("Invalid queue size [" + queueSize + "]");
            return;
        }
        blockingQueue = new ArrayBlockingQueue<>(queueSize);

        worker.setDaemon(true);
        worker.setName("ElasticSearchAppender-Worker-" + worker.getName());
        // make sure this instance is marked as "started" before staring the worker Thread
        super.start();
        worker.start();
    }

    @Override
    public void stop() {
        if (!isStarted())
            return;

        // mark this appender as stopped so that Worker can also processPriorToRemoval if it is invoking aii.appendLoopOnAppenders
        // and sub-appenders consume the interruption
        super.stop();

        // interrupt the worker thread so that it can terminate. Note that the interruption can be consumed
        // by sub-appenders
        worker.interrupt();
        try {
            worker.join(1000);
        } catch (InterruptedException e) {
            addError("Failed to join worker thread", e);
        }
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        try {
            blockingQueue.put(eventObject);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    class Worker extends Thread {

        public void run() {

            // loop while the parent is started
            while (isStarted()) {
                try {
                    ILoggingEvent eventObject = blockingQueue.take();
                    insert(eventObject);
                } catch (InterruptedException ie) {
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for (ILoggingEvent eventObject : blockingQueue) {
                insert(eventObject);
            }
        }
    }

    private void insert(ILoggingEvent eventObject) {
        DateTime dateTime = new DateTime();
        ElasticsearchClient.getConnection().index(index + "_" + dateTime.getYear(), "log", null, eventObject.getMessage());
    }
}
