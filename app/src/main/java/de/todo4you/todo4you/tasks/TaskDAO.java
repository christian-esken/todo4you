package de.todo4you.todo4you.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
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
    List<Todo> todos = new ArrayList<>();
    long lastLoaded = 0;

    private TaskDAO() {
    };

    public static final synchronized TaskDAO instance() {
        if (instance == null) {
            instance = new TaskDAO();
        }
        return instance;
    }

    public List<Todo> get() {
        return todos;
    }

    public List<Todo> getWithRefresh() throws Exception {
        if (needsRefresh()) {
            boolean alsoCompleted = false;
            int minusDays = 100;
            int plusDays = 100;
            load(minusDays, plusDays, alsoCompleted);
            lastLoaded = System.currentTimeMillis();
        }
        return todos;
    }

    /**
     *
     * @return true if data is older than 1 minute or was never loaded before
     */
    private boolean needsRefresh() {
        if (lastLoaded == 0)
            return true;

        return  lastLoaded + 60_000 < System.currentTimeMillis();
    }

    protected void load(int minusDays, int plusDays, boolean alsoCompleted) throws Exception {
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
        todos = todosComplete;
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

    public Todo findBySummary(Object taskObject) {
        if (!(taskObject instanceof String)) {
            return null;
        }
        String taskDescription = (String)taskObject;
        for (Todo todo : todos) {
            if (todo.getSummary() == null) {
                continue; // no summary
            }
            if (taskDescription.startsWith(todo.getSummary())) {
                return todo;
            }
        }
        return null;
    }
}
