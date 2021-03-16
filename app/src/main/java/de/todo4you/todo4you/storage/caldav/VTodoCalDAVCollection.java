package de.todo4you.todo4you.storage.caldav;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.osaf.caldav4j.CalDAVCollection;
import org.osaf.caldav4j.CalDAVResource;
import org.osaf.caldav4j.exceptions.BadStatusException;
import org.osaf.caldav4j.exceptions.CalDAV4JException;
import org.osaf.caldav4j.exceptions.ResourceOutOfDateException;
import org.osaf.caldav4j.methods.CalDAV4JMethodFactory;
import org.osaf.caldav4j.methods.HttpClient;
import org.osaf.caldav4j.methods.PutMethod;
import org.osaf.caldav4j.util.CaldavStatus;
import org.osaf.caldav4j.util.UrlUtils;

import static org.osaf.caldav4j.util.ICalendarUtils.getUIDValue;
import static org.osaf.caldav4j.util.ICalendarUtils.hasProperty;
import static org.osaf.caldav4j.util.UrlUtils.stripHost;

/**
 * A CalDAVCollection subclass that implements VToDo modification support
 */
class VTodoCalDAVCollection extends CalDAVCollection {
    public VTodoCalDAVCollection(String path,
                                 HostConfiguration hostConfiguration,
                                 CalDAV4JMethodFactory methodFactory, String prodId) {
        super(path, hostConfiguration, methodFactory, prodId);
    }

    /**
     * Replaces the VToDo with the uid on httpClient
     * @param httpClient
     * @param vToDo
     * @param timezone
     * @param collection
     * @throws CalDAV4JException
     */
    public void updateMasterTodo(HttpClient httpClient, VToDo vToDo, VTimeZone timezone, CalDAVCollection collection)
            throws CalDAV4JException, IllegalStateException {
        String uid = getUIDValue(vToDo);
        CalDAVResource resource = getCalDAVResourceByUID(httpClient, Component.VTODO, uid);
        Calendar calendar = resource.getCalendar();

        //let's find the master event first!
        VToDo originalVToDo = getMasterTodo(calendar, uid);
        if (originalVToDo == null) {
            throw new IllegalStateException("VToDo cannot be updated. uid=" + uid);
            // Client
        }

        calendar.getComponents().remove(originalVToDo);
        calendar.getComponents().add(vToDo);

        putX(httpClient, calendar,
                stripHost(resource.getResourceMetadata().getHref()),
                resource.getResourceMetadata().getETag());
    }

    /**
     * Returns the "master" VEvent - one that does not have a RECURRENCE-ID
     *
     * @param calendar Calendar from where the Master Event is supposed to be retrieved
     * @param uid UID of the VEvent
     * @return VEvent that does not have Recurrence ID
     */
    public VToDo getMasterTodo(Calendar calendar, String uid){
        ComponentList clist = calendar.getComponents().getComponents(Component.VTODO);
        for (Object o : clist){
            VToDo curEvent = (VToDo) o;
            String curUid = getUIDValue(curEvent);
            // Checking RECURRENCE_ID does not hurt, but may not be sensible on a VTODO.
            if (uid.equals(curUid) && !hasProperty(curEvent, Property.RECURRENCE_ID) ){
                return curEvent;
            }
        }
        return null;
    }

    /**
     * @param httpClient the httpClient which will make the request
     * @param calendar iCal body to place on the server
     * @param path Path to the new/old resource
     * @param etag ETag if updation of calendar has to take place.
     * @throws CalDAV4JException on error
     */
    void putX(HttpClient httpClient, Calendar calendar, String path,
             String etag)
            throws CalDAV4JException {
        PutMethod putMethod = getMethodFactory().createPutMethod();
        putMethod.addEtag(etag);
        putMethod.setPath(path);
        putMethod.setIfMatch(true);
        putMethod.setRequestBody(calendar);
        try {
            httpClient.executeMethod(getHostConfiguration(), putMethod);
            int statusCode = putMethod.getStatusCode();
            switch(statusCode) {
                case CaldavStatus.SC_NO_CONTENT:
                case CaldavStatus.SC_CREATED:
                    break;
                case CaldavStatus.SC_PRECONDITION_FAILED:
                    throw new ResourceOutOfDateException("Etag was not matched: "+ etag);
                default:
                    throw new BadStatusException(statusCode, putMethod.getName(), path);
            }

            Header h = putMethod.getResponseHeader("ETag");

            String newEtag = null;
            if (h != null) {
                newEtag = h.getValue();
            } else {
                newEtag = getETagbyMultiget(httpClient, path);
            }
            cache.putResource(new CalDAVResource(calendar, newEtag, getHrefX(putMethod.getPath())));
        } catch (ResourceOutOfDateException e){
            throw e;
        } catch (BadStatusException e){
            throw e;
        } catch (Exception e){
            throw new CalDAV4JException("Problem executing put method",e);
        } finally {
            putMethod.releaseConnection();
        }
    }


    /**
     * TODO check hostConfiguration.getUri()
     * @param path
     * @return the URI of the path resource
     *
     * XXX maybe it will be faster to write down the whole url including port...
     */
    String getHrefX(String path){
        HostConfiguration hostConfiguration = getHostConfiguration();
        int port = hostConfiguration.getPort();
        String scheme = hostConfiguration.getProtocol().getScheme();
        String portString = "";
        if ( (port != 80 && "http".equals(scheme)) ||
                (port != 443 && "https".equals(scheme))
        ) {
            portString = ":" + port;
        }

        return UrlUtils.removeDoubleSlashes(
                String.format("%s://%s%s/%s", scheme,
                        hostConfiguration.getHost(),
                        portString, path )
        );
    }
}
