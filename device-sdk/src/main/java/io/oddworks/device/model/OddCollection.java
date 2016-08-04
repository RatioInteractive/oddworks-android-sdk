package io.oddworks.device.model;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

import io.oddworks.device.Util;

import static io.oddworks.device.Util.getString;

public class OddCollection extends OddObject {
    private static final String TAG = OddCollection.class.getSimpleName();
    public static final String ENTITIES = "entities";

    private String title;
    private String subtitle;
    private String program_info;
    private String description;
    private MediaImage mediaImage;
    private DateTime releaseDate;

    public OddCollection(final Identifier identifier) {
        super(identifier);
    }

    public OddCollection(final String id, final String type) {
        super(id, type);
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getProgramInfo() {
        return program_info;
    }

    public String getDescription() {
        return description;
    }

    public MediaImage getMediaImage() {
        return mediaImage;
    }

    public DateTime getReleaseDate() {
        return releaseDate;
    }

    @Override
    public void setAttributes(Map<String, Object> attributes) {
        this.title = getString(attributes, "title", null);
        this.subtitle = getString(attributes, "subtitle", null);
        this.program_info = getString(attributes, "program_info", null);
        this.description = getString(attributes, "description", null);
        this.mediaImage = (MediaImage) attributes.get("mediaImage");
        this.releaseDate = Util.getDateTime(attributes, "releaseDate", null);
    }

    @Override
    public Map<String, Object> getAttributes() {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("title", getTitle());
        attributes.put("subtitle", getSubtitle());
        attributes.put("program_info", getProgramInfo());
        attributes.put("description", getDescription());
        attributes.put("mediaImage", getMediaImage());
        attributes.put("releaseDate", getReleaseDate());
        return attributes;
    }

    @Override
    public boolean isPresentable() {
        return true;
    }

    @Override
    public Presentable toPresentable() {
        return new Presentable(title, description, mediaImage);
    }

    @Override
    public String toString() {
        return "OddCollection{" +
                "title='" + getTitle() + '\'' +
                ", subtitle='" + getSubtitle() + '\'' +
                ", program_info='" + getProgramInfo() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", mediaImage=" + getMediaImage() +
                ", releaseDate=" + getReleaseDate() +
                '}';
    }
}
