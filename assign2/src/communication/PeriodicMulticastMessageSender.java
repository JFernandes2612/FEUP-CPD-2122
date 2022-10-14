package communication;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import store.Store;

public class PeriodicMulticastMessageSender {

    private final ScheduledExecutorService scheduledExecutorService;
    private final Store node;

    public PeriodicMulticastMessageSender(Store node) {
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.node = node;
    }

    public void stopLoop() {
        this.scheduledExecutorService.shutdownNow();
    }

    public void updateMembershipPeriodically() {
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                final List<String> clusterIDs = node.getClusterIDs();
                final String id = node.getID();

                if (clusterIDs.size() != 1) {
                    if (clusterIDs.get(0).equals(id)) {
                        node.sendMulticastMembership();
                    }
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }
}
