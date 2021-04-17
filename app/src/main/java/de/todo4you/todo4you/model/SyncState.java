package de.todo4you.todo4you.model;

import de.todo4you.todo4you.storage.DataOrigin;

public class SyncState {
    private volatile boolean inSyncWithDeviceStore;
    private volatile boolean inSyncWithCloudStore;

    private SyncState(boolean inSyncWithCloudStore, boolean inSyncWithDeviceStore)
    {
       this.inSyncWithCloudStore = inSyncWithCloudStore;
       this.inSyncWithDeviceStore = inSyncWithDeviceStore;
    }

    public static SyncState buildNew() {
        return new SyncState(false, false);
    }

    public static SyncState buildFrom(DataOrigin dataOrigin) {
        return new SyncState(dataOrigin == DataOrigin.CloudStore, dataOrigin == DataOrigin.DeviceStore);
    }

    public void insyncReset(boolean inSyncWithCloudstore) {
        this.inSyncWithCloudStore = inSyncWithCloudstore;
        this.inSyncWithDeviceStore = false;
    }
    public boolean inSyncWithDeviceStore() {
        return inSyncWithDeviceStore;
    }
    public boolean inSyncWithCloudStore() {
        return inSyncWithCloudStore;
    }

    public void setInSyncWithCloudStore() {
        inSyncWithCloudStore = true;
    }

    public void setInSyncWithDeviceStore() {
        inSyncWithDeviceStore =true;
    }
}
