package de.todo4you.todo4you.caldav;

import android.support.annotation.NonNull;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VToDo;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.osaf.caldav4j.CalDAVCollection;
import org.osaf.caldav4j.exceptions.CalDAV4JException;
import org.osaf.caldav4j.methods.CalDAV4JMethodFactory;
import org.osaf.caldav4j.methods.HttpClient;
import org.osaf.caldav4j.model.request.CalendarQuery;
import org.osaf.caldav4j.util.GenerateQuery;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.todo4you.todo4you.model.Todo;

/**
 *
 *         Based on the SABRE docs at http://sabre.io/dav/building-a-caldav-client/ .
 *         For adding auto-discovery see the "Discovery" section on that page.
 *
 *         PROPFIND https://dav.example.com/owncloud/remote.php/caldav/calendars/username/
 *          =>  Finds all calendars and contact bithday
 *
 *
 */
public class CalDavConnectorHC3 implements CalendarConnector {
    public static final String PROC_ID_TODO4YOU =  "-//NONSGML CalDAV4j Client//EN"; // TODO Change ID
    private final ConnectionParameters conn;


    public CalDavConnectorHC3(ConnectionParameters connectionParameters) {
        this.conn = connectionParameters;
    }

    @Override
    public List<Todo> get (LocalDate fromDate, LocalDate toDate, boolean onlyActive) throws CalDAV4JException {
        HttpClient httpClient = getHttpClient();

        // Proxy support? Is there an Android "get standard proxy" method?
        //httpClient.setProxy("proxy", proxyPort);

        CalDAVCollection collection = new CalDAVCollection(conn.path(), (HostConfiguration) httpClient.getHostConfiguration().clone(), new CalDAV4JMethodFactory(), PROC_ID_TODO4YOU);

        GenerateQuery gq = new GenerateQuery(); // gq.prettyPrint();
        int from = toDay(fromDate);
        int to = toDay(toDate);
        gq.setFilter("VTODO [" + from + "T000000Z;" + to + "T235959Z] : STATUS!=CANCELLED");

        CalendarQuery calendarQuery = gq.generate();
        List<Calendar> calendars = collection.queryCalendars(httpClient, calendarQuery);

        List<Todo> todos = new LinkedList<>();

        for (Calendar calendar : calendars) {
            if (calendar == null) {
                // Hint: In the caldav4j libarary, the getCalDAVResourceFromServer() can return null
                continue;
            }
            ComponentList<CalendarComponent> components = calendar.getComponents();
            if (components == null) {
                continue; // calendar w/o components => skip
            }
            ComponentList componentList = components.getComponents(Component.VTODO);
            if (componentList == null) {
                continue; // No VTODO component
            }
            Iterator<VToDo> eventIterator = componentList.iterator();
            while (eventIterator.hasNext()) {
                VToDo vtodo = eventIterator.next();
                todos.add(new Todo(vtodo));
            }
        }

        return todos;
    }

    @Override
    public ConnectionParameters probe(ConnectionParameters connectionParameters) {
        // no probing implemented => best set of parameters is the original one
        return connectionParameters;
    }

    @NonNull
    private HttpClient getHttpClient() {
        HttpClient httpClient = new HttpClient();
        httpClient.getHostConfiguration().setHost(conn.host(), conn.port(), conn.httpProtocol().protocol());
        UsernamePasswordCredentials httpCredentials = new UsernamePasswordCredentials(conn.user(), conn.password());
        httpClient.getState().setCredentials(AuthScope.ANY, httpCredentials);
        httpClient.getParams().setAuthenticationPreemptive(true);
        return httpClient;
    }

    private int toDay(LocalDate date) {
        return 10000 * date.get(ChronoField.YEAR) + 100 * date.get(ChronoField.MONTH_OF_YEAR) + date.get(ChronoField.DAY_OF_MONTH);
    }
}
