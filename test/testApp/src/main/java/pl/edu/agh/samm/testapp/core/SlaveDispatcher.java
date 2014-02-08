package pl.edu.agh.samm.testapp.core;

import java.io.Serializable;

public class SlaveDispatcher extends LoggingClass implements Runnable, Stoppable, Serializable {

    private static final long serialVersionUID = -8745913791313753914L;
    private static final long SLAVE_WAIT_TIME = 1000;
    private ExpressionGenerator master;
    private SlaveManager resolver;
    private boolean running = true;

    public SlaveDispatcher(ExpressionGenerator master, SlaveManager resolver) {
        this.master = master;
        this.resolver = resolver;
    }

    public void run() {
        long iteration = 0;
        while (running) {
            logMessage("Distributor: Iteration " + iteration++);
            if (resolver.getSlavesCount() > 0) {
                logMessage("Distributor: Waiting for data...");
                String expression = master.getNewExpression();

                logMessage("Distributor: queue size: " + master.getQueueLength());

                boolean scheduled = false;
                ISlave firstSlaveInLoop = null;
                boolean firstSlave = true;
                while (!scheduled && running) {
                    ISlave slave = resolver.getNextSlave();

                    if (slave == null) {
                        logMessage("ERROR", "Can't schedule expression: " + expression + "!");
                        master.giveBack(expression);
                        break;
                    } else {
                        logMessage("Sending expression " + iteration + " to " + slave.getSlaveId());
                        if (slave.canTakeMore()) {
                            slave.scheduleIntegration(expression);
                            scheduled = true;
                        } else {
                            logMessage("Slave " + slave + " has enough");
                        }
                    }

                    if (!firstSlave && firstSlaveInLoop == slave) {
                        try {
                            logMessage("No more slaves with free capacity!");
                            Thread.sleep(SLAVE_WAIT_TIME);
                        } catch (InterruptedException e) {
                            // no problem
                        }
                    }

                    if (firstSlave) {
                        firstSlaveInLoop = slave;
                        firstSlave = false;
                    }
                }
            } else {
                logMessage("No slaves! Waiting...");
                try {
                    Thread.sleep(SLAVE_WAIT_TIME);
                } catch (InterruptedException e) {
                    // nothing happens
                }
            }
        }
    }

    public void stopExecution() {
        running = false;
    }
}
