package de.todo4you.todo4you.caldav;
/*
import com.github.caldav4j.CalDAVCollection;
import com.github.caldav4j.CalDAVConstants;
import com.github.caldav4j.exceptions.CalDAV4JException;
import com.github.caldav4j.methods.CalDAV4JMethodFactory;
import com.github.caldav4j.model.request.CalendarQuery;
import com.github.caldav4j.util.GenerateQuery;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.VEvent;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.Iterator;
import java.util.List;

public class CalDavConnectorHC4 implements CalendarConnector {
    public static final String PROC_ID_TODO4YOU =  "-//NONSGML CalDAV4j Client//EN"; // TODO Change ID

    private final String host;
    private final int port;
    private final boolean useHttps;
    private final String scheme;
    private final String path;
    private final String user;
    private final String pass;

    public CalDavConnectorHC4(String host, int port, boolean useHttps, String path, String user, String pass) {
        this.host = host;
        this.port = port;
        this.useHttps = useHttps;
        this.scheme = useHttps ? "https" : "http";
        this.path = path;
        this.user = user;
        this.pass = pass;
    }

    public void get (LocalDate fromDate, LocalDate toDate) throws CalDAV4JException {
        HttpRequestRetryHandler retryHandler = new StandardHttpRequestRetryHandler(4, false);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(user, pass);
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);


        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setRetryHandler(retryHandler).setDefaultCredentialsProvider(credentialsProvider);
        HttpClient httpClient = builder.build();
// I tried it with zimbra - but I had no luck using google calendar


        // Proxy support? Is there an Android "get standard proxy" method?
        //httpClient.setProxy("proxy", proxyPort);

        HttpHost httpHost = new HttpHost(host, port, scheme);
        CalDAVCollection collection = new CalDAVCollection(path, httpHost, new CalDAV4JMethodFactory(), PROC_ID_TODO4YOU);

        GenerateQuery gq = new GenerateQuery();
        int from = toDay(fromDate);
        int to = toDay(toDate);
        gq.setFilter("VEVENT [" + from + "T000000Z;" + to + "T235959Z] : STATUS!=CANCELLED");
// Get the raw caldav query
// System.out.println("Query: "+ gq.prettyPrint());
        CalendarQuery calendarQuery = gq.generate();
        List<Calendar> calendars = collection.queryCalendars(httpClient, calendarQuery);

        for (Calendar calendar : calendars) {
            ComponentList componentList = calendar.getComponents().getComponents(Component.VEVENT);
            Iterator<VEvent> eventIterator = componentList.iterator();
            while (eventIterator.hasNext()) {
                VEvent ve = eventIterator.next();
                System.out.println("Event: " + ve.toString());
                System.out.println("\n\n");

            }
        }
    }

    private int toDay(LocalDate date) {
        return 10000 * date.get(ChronoField.YEAR) + 100 * date.get(ChronoField.MONTH_OF_YEAR) + date.get(ChronoField.DAY_OF_MONTH);
    }
}
*/