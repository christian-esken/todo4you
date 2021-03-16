package de.todo4you.todo4you.tasks;

public class StoreStatus {
    public final static StoreStatus loading = new StoreStatus(StoreState.LOADING);
    public final static StoreStatus loaded = new StoreStatus(StoreState.LOADED);

    public final StoreState status;
    public final String userErrorMessaage;
    public final Exception exception;

    public StoreStatus(StoreState status, String userErrorMessaage, Exception exception) {
        this.status = status;
        this.userErrorMessaage = userErrorMessaage;
        this.exception = exception;
    }

    public StoreStatus(StoreState status) {
        this(status, null, null);
    }
}
