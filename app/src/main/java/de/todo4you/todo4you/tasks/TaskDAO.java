package de.todo4you.todo4you.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import de.todo4you.todo4you.caldav.CalDavConnectorHC3;
import de.todo4you.todo4you.caldav.CalendarConnector;
import de.todo4you.todo4you.caldav.ConnectionParameters;
import de.todo4you.todo4you.model.CompletionState;
import de.todo4you.todo4you.model.Todo;

public class TaskDAO {
    private final static String accountsFileName = "accounts.properties";
    private final static String accountsDevFileName = "accounts-dev.properties";

    private static TaskDAO instance;

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
        int minusDays = 100;
        int plusDays = 100;
        try {
            List<Todo> todosLoaded = load(minusDays, plusDays, alsoCompleted);
            return new StoreResult(todosLoaded, StoreState.LOADED);
        } catch (Exception exc) {
            return new StoreResult(Collections.emptyList(), StoreState.ERROR, "Calendar error: " + exc.getMessage(), exc);
        }
    }

    protected List<Todo> load(int minusDays, int plusDays, boolean alsoCompleted) throws Exception {
        int accountId = 1;
        CalendarConnector connector = createConnector(accountId);

        LocalDate now = LocalDate.now();
        List<Todo> todosComplete = new ArrayList<>();
        List<Todo> todosNew = connector.get(now.minusDays(minusDays), now.plusDays(plusDays), !alsoCompleted);
        for (Todo todo : todosNew) {
            if (alsoCompleted || todo.getCompletionState() != CompletionState.COMPLETED) {
                todosComplete.add(todo);
            }
        }
        return todosComplete;
    }

    private CalendarConnector createConnector(int accountId) throws IOException {
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
        CalendarConnector conn = new CalDavConnectorHC3(connParam);
        return conn;
    }

}
