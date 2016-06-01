package io.oddworks.device.model;

/**
 * Objects that can be directly represented in a gui (except images) should generate this object.
 */
public class Presentable {
    private final String title;
    private final String description;
    private final MediaImage mediaImage;

    public Presentable(String title, String description, MediaImage mediaImage) {
        this.title = title;
        this.description = description;
        this.mediaImage = mediaImage;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public MediaImage getMediaImage() {
        return mediaImage;
    }

    @Override
    public String toString() {
        return "Presentable{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", mediaImage=" + mediaImage +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Presentable that = (Presentable) o;

        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        return !(mediaImage != null ? !mediaImage.equals(that.mediaImage) : that.mediaImage != null);

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (mediaImage != null ? mediaImage.hashCode() : 0);
        return result;
    }
}
