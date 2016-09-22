package io.oddworks.device.model;

import android.support.test.runner.AndroidJUnit4;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class EventTest {
    private String id = "123";
    private String typeEvent = "event";
    private Event event = new Event(id, typeEvent);
    private String title = "Odd the Great and Powerful";
    private String description = "Odd is good! Odd is great!";
    private String category = "categorically";
    private String source = "asourceisasourceofcourseofcourse";
    private String url = "http://oddnetworks.com/foo.html";
    private DateTime createdAt = new DateTime("2015-12-01T01:45:21Z");
    private DateTime dateTimeStart = new DateTime("2016-02-13T08:00:00-0600");
    private DateTime dateTimeEnd = new DateTime("2016-02-14T17:59:00-0600");
    private String location = "Las Vegas, NV";
    private MediaImage mediaImage = new MediaImage("a", "b", "c", "d", "e", "f", "e", "g", "h", "i", "j", "k", "l", "m");
    private HashMap<String, Object> attributes = new HashMap<>();

    @Before
    public void beforeEach() {
        attributes.put("title", title);
        attributes.put("description", description);
        attributes.put("mediaImage", mediaImage);
        attributes.put("url", url);
        attributes.put("category", category);
        attributes.put("source", source);
        attributes.put("mediaImage", mediaImage);
        attributes.put("createdAt", createdAt);
        attributes.put("dateTimeStart", dateTimeStart);
        attributes.put("dateTimeEnd", dateTimeEnd);
        attributes.put("location", location);
        event.setAttributes(attributes);
    }

    @Test
    public void testGetIdentifier() throws Exception {
        assertThat(event.getIdentifier(), is(instanceOf(Identifier.class)));
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals(id, event.getId());
    }

    @Test
    public void testGetType() throws Exception {
        assertEquals(typeEvent, event.getType());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals(title, event.getTitle());
    }

    @Test
    public void testGetDescription() throws Exception {
        assertEquals(description, event.getDescription());
    }

    @Test
    public void testGetMediaImage() throws Exception {
        assertEquals(mediaImage, event.getMediaImage());
    }

    @Test
    public void testGetUrl() throws Exception {
        assertEquals(url, event.getUrl());
    }

    @Test
    public void testGetCategory() throws Exception {
        assertEquals(category, event.getCategory());
    }

    @Test
    public void testGetSource() throws Exception {
        assertEquals(source, event.getSource());
    }

    @Test
    public void testGetCreatedAt() throws Exception {
        assertEquals(createdAt, event.getCreatedAt());
    }

    @Test
    public void testGetDateTimeStart() throws Exception {
        assertEquals(dateTimeStart, event.getDateTimeStart());
    }

    @Test
    public void testGetDateTimeEnd() throws Exception {
        assertEquals(dateTimeEnd, event.getDateTimeEnd());
    }

    @Test
    public void testGetLocation() throws Exception {
        assertEquals(location, event.getLocation());
    }

    @Test
    public void testHasStartAndEndDateTimeFalse() throws Exception {
        Event badEvent = new Event(id, typeEvent);
        assertFalse(badEvent.hasStartAndEndDateTime());
    }

    @Test
    public void testHasStartAndEndDateTimeTrue() throws Exception {
        assertTrue(event.hasStartAndEndDateTime());
    }

    @Test
    public void testIsMultiDayEventTrue() throws Exception {
        assertTrue(event.isMultiDayEvent());
    }

    @Test
    public void testIsMultiDayEventFalse() throws Exception {
        Event nonMultiDayEvent = new Event(id, typeEvent);
        attributes.put("dateTimeStart", new DateTime("2013-01-01T00:00:00Z"));
        attributes.put("dateTimeEnd", new DateTime("2013-01-01T23:59:59Z"));
        nonMultiDayEvent.setAttributes(attributes);

        assertFalse(nonMultiDayEvent.isMultiDayEvent());
    }

    @Test
    public void testSetAttributesWhenNotSet() throws Exception {
        Event event = new Event(id, typeEvent);

        assertEquals(null, event.getTitle());
        assertEquals(null, event.getDescription());
        assertEquals(null, event.getMediaImage());
        assertEquals(null, event.getUrl());
        assertEquals(null, event.getCategory());
        assertEquals(null, event.getSource());
        assertEquals(null, event.getCreatedAt());
        assertEquals(null, event.getDateTimeStart());
        assertEquals(null, event.getDateTimeEnd());
        assertEquals(null, event.getLocation());
    }

    @Test
    public void testSetAttributesWhenSet() throws Exception {
        Event event = new Event(id, typeEvent);
        event.setAttributes(attributes);

        assertEquals(title, event.getTitle());
        assertEquals(description, event.getDescription());
        assertEquals(mediaImage, event.getMediaImage());
        assertEquals(url, event.getUrl());
        assertEquals(category, event.getCategory());
        assertEquals(source, event.getSource());
        assertEquals(createdAt, event.getCreatedAt());
        assertEquals(dateTimeStart, event.getDateTimeStart());
        assertEquals(dateTimeEnd, event.getDateTimeEnd());
        assertEquals(location, event.getLocation());
    }

    @Test
    public void testGetAttributes() throws Exception {
        assertEquals(attributes, event.getAttributes());
    }

    @Test
    public void isPresentableShouldReturnTrue() {
        Event event = new Event(id, typeEvent);
        assertThat(event.isPresentable(), is(true));
    }

    @Test
    public void toPresentableFieldsShouldEqualMediasFields() {
        Event event = new Event(id, typeEvent);
        event.setAttributes(attributes);
        Presentable presentable = event.toPresentable();

        assertThat("titles should match", presentable.getTitle(), is(equalTo(event.getTitle())));
        assertThat("descriptions should match", presentable.getDescription(), is(equalTo(event.getDescription())));
        assertThat("MediaImages should match", presentable.getMediaImage(), is(equalTo(event.getMediaImage())));
    }
}
