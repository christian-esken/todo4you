package de.todo4you.todo4you.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import de.todo4you.todo4you.storage.caldav.CalDavConnectorHC3;
import de.todo4you.todo4you.storage.Storage;
import de.todo4you.todo4you.storage.caldav.ConnectionParameters;
import de.todo4you.todo4you.model.CompletionState;
import de.todo4you.todo4you.model.Idea;

public class TaskDAO {
    private final static String accountsFileName = "accounts.properties";
    private final static String accountsDevFileName = "accounts-dev.properties";

    private static TaskDAO instance;
    final int accountId = 1;
    private Exception lastUpstreamException = null;

    private TaskDAO() {
    };

    public static final synchronized TaskDAO instance() {
        if (instance == null) {
            instance = new TaskDAO();
        }
        return instance;
    }


    public StoreResult loadAll() {
        boolean alsoCompleted = false;
        int minusDays = 1000;
        int plusDays = 100;
        try {
            List<Idea> todosLoaded = load(minusDays, plusDays, alsoCompleted);
            return new StoreResult(todosLoaded, StoreStatus.loaded);
        } catch (Exception exc) {
            return new StoreResult(Collections.emptyList(), StoreState.ERROR, "Calendar error: " + exc.getMessage(), exc);
        }
    }

    protected List<Idea> load(int minusDays, int plusDays, boolean alsoCompleted) throws Exception {
        Storage connector = createConnector(accountId);

        LocalDate now = LocalDate.now();
        List<Idea> todosComplete = new ArrayList<>();
        List<Idea> todosNew = connector.get(now.minusDays(minusDays), now.plusDays(plusDays), !alsoCompleted);
        for (Idea idea : todosNew) {
            // Maybe remove the alsoCompleted check here. It has moved to the Connector.
            // This needs refinement. How will the user then see his heroicly completed tasks if
            // we do not load them? Also we keep them in memory after a user marked it as complete.
            // Probably we want to keep the "recently completed" tasks in memory. Or have two
            // task lists: Active (to select the 1 task from) and inactive (completed, canceled).
            if (alsoCompleted || idea.getCompletionState() != CompletionState.COMPLETED) {
                todosComplete.add(idea);
            }
        }
        return todosComplete;
    }

    private Storage createConnector(int accountId) throws IOException {
        // Primary Caldav Address
        // https://dav.example.com/owncloud/remote.php/caldav/
        // One specific calendar:
        // https://dav.example.com/owncloud/remote.php/caldav/calendars/username/calendar

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();
        try(InputStream resourceStream = loader.getResourceAsStream(accountsDevFileName)) {
            props.load(resourceStream);
        } catch (Exception exc) {
            // Issues loading the developer properties => ignore them
            props = new Properties();
        }

        if (props.isEmpty()) {
            try (InputStream resourceStream = loader.getResourceAsStream(accountsFileName)) {
                props.load(resourceStream);
            }
        }

        String type = props.getProperty("account." + accountId + ".type");
        if (!"GenericCaldav".equals(type)) {
            throw new IllegalArgumentException("Account type unsupported: " + type);
        }
        String host = props.getProperty("account." + accountId + ".host");
        int port = Integer.parseInt(props.getProperty("account." + accountId + ".port"));
        String path = props.getProperty("account." + accountId + ".path");
        String protcolString = props.getProperty("account." + accountId + ".protocol").toUpperCase();
        ConnectionParameters.HttpProtocol protocol = ConnectionParameters.HttpProtocol.valueOf(protcolString);
        String user = props.getProperty("account." + accountId + ".user");
        String password = props.getProperty("account." + accountId + ".password");


        ConnectionParameters connParam = new ConnectionParameters(ConnectionParameters.ServerType.GenericCaldav, host, port, protocol, path, user, password);
        Storage conn = new CalDavConnectorHC3(connParam);
        return conn;
    }

    public boolean insertOrUpdate(Idea task) {
        Storage connector = null;
        try {
            connector = createConnector(accountId);
            return connector.add(task);
        } catch (Exception e) {
            lastUpstreamException = e;
            return false;
        }
    }

    public boolean update(Idea task) {
        Storage connector = null;
        try {
            connector = createConnector(accountId);
            return connector.update(task);
        } catch (Exception e) {
            lastUpstreamException = e;
            return false;
        }
    }
}
