package de.todo4you.todo4you.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UidFactory {
    static UidFactory instance;
    Map<String, Boolean> uids = new ConcurrentHashMap<String, Boolean>();

    public static synchronized UidFactory instance() {
        if (instance == null) {
            instance = new UidFactory();
        }
        return instance;
    }

    private UidFactory() {
    }

    public String produceNewUid() {
        for (int i=0; i<100; i++) {
            String uuid = UUID.randomUUID().toString();
            if (!uids.containsKey(uuid)) {
                uids.put(uuid, Boolean.TRUE);
                return uuid;
            }
        }
        throw new IllegalStateException("Cannot create uid");
    }

    /**
     * Sets the reserved UID's to the ones from the given Set. This should be called at the
     * start of the application to reserve the UID's from the loaded Tasks.
     * @param uids
     */
    public void setReservedUids(Map<String, Boolean> uids) {
        Map<String, Boolean> uidsNew = new ConcurrentHashMap<>();
        uidsNew.putAll(uids);
        this.uids = uidsNew;
    }

}
