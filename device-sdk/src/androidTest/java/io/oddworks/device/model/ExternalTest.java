package io.oddworks.device.model;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ExternalTest {
    private String id = "123";
    private String typeExternal = "external";
    private External external = new External(id, typeExternal);
    private String title = "Odd the Great and Powerful";
    private String description = "Odd is good! Odd is great!";
    private String url = "http://oddnetworks.com/foo.html";
    private MediaImage mediaImage = new MediaImage("a", "b", "c", "d", "e", "f", "e", "g", "h", "i", "j", "k", "l", "m");
    private HashMap<String, Object> attributes = new HashMap<>();

    @Before
    public void beforeEach() {
        attributes.put("title", title);
        attributes.put("description", description);
        attributes.put("mediaImage", mediaImage);
        attributes.put("url", url);
        attributes.put("mediaImage", mediaImage);
        external.setAttributes(attributes);
    }

    @Test
    public void testGetIdentifier() throws Exception {
        assertThat(external.getIdentifier(), is(instanceOf(Identifier.class)));
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals(id, external.getId());
    }

    @Test
    public void testGetType() throws Exception {
        assertEquals(typeExternal, external.getType());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals(title, external.getTitle());
    }

    @Test
    public void testGetDescription() throws Exception {
        assertEquals(description, external.getDescription());
    }

    @Test
    public void testGetMediaImage() throws Exception {
        assertEquals(mediaImage, external.getMediaImage());
    }

    @Test
    public void testGetUrl() throws Exception {
        assertEquals(url, external.getUrl());
    }

    @Test
    public void testSetAttributesWhenNotSet() throws Exception {
        Article article = new Article(id, typeExternal);

        assertEquals(null, article.getTitle());
        assertEquals(null, article.getDescription());
        assertEquals(null, article.getMediaImage());
        assertEquals(null, article.getUrl());
    }

    @Test
    public void testSetAttributesWhenSet() throws Exception {
        Article article = new Article(id, typeExternal);
        article.setAttributes(attributes);

        assertEquals(title, article.getTitle());
        assertEquals(description, article.getDescription());
        assertEquals(mediaImage, article.getMediaImage());
        assertEquals(url, article.getUrl());
    }

    @Test
    public void testGetAttributes() throws Exception {
        assertEquals(attributes, external.getAttributes());
    }

    @Test
    public void isPresentableShouldReturnTrue() {
        External external = new External(id, typeExternal);
        assertThat(external.isPresentable(), is(true));
    }

    @Test
    public void toPresentableFieldsShouldEqualMediasFields() {
        External external = new External(id, typeExternal);
        external.setAttributes(attributes);
        Presentable presentable = external.toPresentable();

        assertThat("titles should match", presentable.getTitle(), is(equalTo(external.getTitle())));
        assertThat("descriptions should match", presentable.getDescription(), is(equalTo(external.getDescription())));
        assertThat("MediaImages should match", presentable.getMediaImage(), is(equalTo(external.getMediaImage())));
    }
}
