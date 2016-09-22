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
public class MediaTest {
    private String id = "123";
    private String typeVideo = "video";
    private String title = "Odd the Great and Powerful";
    private String subtitle = null;
    private String description = "Odd is good! Odd is great!";
    private DateTime releaseDate = new DateTime("2013-07-04T12:30:59Z");
    private String url = "http://oddnetworks.com";
    private int duration = 2345;
    private MediaAd mediaAd = new MediaAd();
    private MediaImage mediaImage = new MediaImage("a", "b", "c", "d", "e", "f", "e", "g", "h", "i", "j", "k", "l", "m");
    private Media video = new Media(id, typeVideo);
    private HashMap<String, Object> attributes = new HashMap<>();

    @Before
    public void beforeEach() {
        attributes.put("title", title);
        attributes.put("subtitle", subtitle);
        attributes.put("description", description);
        attributes.put("releaseDate", releaseDate);
        attributes.put("url", url);
        attributes.put("duration", duration);
        attributes.put("mediaAd", mediaAd);
        attributes.put("mediaImage", mediaImage);
        video.setAttributes(attributes);
    }

    @Test
    public void testGetIdentifier() throws Exception {
        assertThat(video.getIdentifier(), is(instanceOf(Identifier.class)));
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals(id, video.getId());
    }

    @Test
    public void testGetType() throws Exception {
        assertEquals(typeVideo, video.getType());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals(title, video.getTitle());
    }

    @Test
    public void testGetDescription() throws Exception {
        assertEquals(description, video.getDescription());
    }

    @Test
    public void testGetReleaseDate() throws Exception {
        assertEquals(releaseDate, video.getReleaseDate());
    }

    @Test
    public void testGetMediaImage() throws Exception {
        assertEquals(mediaImage, video.getMediaImage());
    }

    @Test
    public void testGetDuration() throws Exception {
        assertEquals(Integer.toString(duration), Integer.toString(video.getDuration()));
    }

    @Test
    public void testGetUrl() throws Exception {
        assertEquals(url, video.getUrl());
    }

    @Test
    public void testGetMediaAd() throws Exception {
        assertEquals(mediaAd, video.getMediaAd());
    }

    @Test
    public void testIsLiveTrue() throws Exception {
        Media liveStream = new Media(id, "liveStream");
        liveStream.setAttributes(attributes);
        assertTrue(liveStream.isLive());
    }

    @Test
    public void testIsLiveFalse() throws Exception {
        assertFalse(video.isLive());
    }

    @Test
    public void testSetAttributesWhenNotSet() throws Exception {
        Media media = new Media(id, typeVideo);

        assertEquals(null, media.getTitle());
        assertEquals(null, media.getDescription());
        assertEquals(null, media.getReleaseDate());
        assertEquals(null, media.getMediaImage());
        assertEquals(Integer.toString(0), Integer.toString(media.getDuration()));
        assertEquals(null, media.getUrl());
        assertEquals(null, media.getMediaAd());
    }

    @Test
    public void testSetAttributesWhenSet() throws Exception {
        Media media = new Media(id, typeVideo);
        media.setAttributes(attributes);

        assertEquals(title, media.getTitle());
        assertEquals(description, video.getDescription());
        assertEquals(releaseDate, video.getReleaseDate());
        assertEquals(mediaImage, video.getMediaImage());
        assertEquals(Integer.toString(duration), Integer.toString(video.getDuration()));
        assertEquals(url, video.getUrl());
        assertEquals(mediaAd, video.getMediaAd());
    }

    @Test
    public void testGetAttributes() throws Exception {
        assertEquals(attributes, video.getAttributes());
    }

    @Test
    public void isPresentableShouldReturnTrue() {
        Media media = new Media(id, typeVideo);
        assertThat(media.isPresentable(), is(true));
    }

    @Test
    public void toPresentableFieldsShouldEqualMediasFields() {
        Media media = new Media(id, typeVideo);
        media.setAttributes(attributes);
        Presentable presentable = media.toPresentable();

        assertThat("titles should match", presentable.getTitle(), is(equalTo(media.getTitle())));
        assertThat("descriptions should match", presentable.getDescription(), is(equalTo(media.getDescription())));
        assertThat("MediaImages should match", presentable.getMediaImage(), is(equalTo(media.getMediaImage())));
    }
}
