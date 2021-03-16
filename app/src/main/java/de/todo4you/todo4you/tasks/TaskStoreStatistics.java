package de.todo4you.todo4you.tasks;

import de.todo4you.todo4you.storage.StorageStatistics;

public class TaskStoreStatistics {
    final StorageStatistics memory;
    final StorageStatistics cloud;
    final StorageStatistics device;

    public TaskStoreStatistics(StorageStatistics memory, StorageStatistics cloud, StorageStatistics device) {
        this.memory = memory;
        this.cloud = cloud;
        this.device = device;
    }

    @Override
    public String toString() {
        return memory + ","  + cloud + "," + device;
    }
}
