package de.todo4you.todo4you.storage;

/**
 * Statistics of a single storage (in memory, DB, cloud, ...)
 */
public class StorageStatistics {
    final String name;
    final boolean connected;
    final int count;
    final int synced;

    public StorageStatistics(String name, boolean connected, int count, int synced) {
        this.name = name;
        this.connected = connected;
        this.count = count;
        this.synced = synced;
    }

    @Override
    public String toString() {
        return name + " " + (connected ? "UP" : "DOWN") + " " + synced + "/" + count;
    }
}
