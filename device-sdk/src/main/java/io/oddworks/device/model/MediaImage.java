package io.oddworks.device.model;

import android.util.Log;

import java.net.URI;
import java.net.URISyntaxException;

public class MediaImage {
    public static final String TAG = MediaImage.class.getSimpleName();

    private String aspect16x9;
    private String aspect3x4;
    private String aspect4x3;
    private String aspect1x1;
    private String aspect2x3;

    private String aspect16x9_w152;
    private String aspect16x9_w390;
    private String aspect16x9_w548;
    private String aspect16x9_w768;
    private String aspect16x9_w1152;
    private String aspect16x9_w1536;
    private String aspect16x9_w1920;
    private String aspect16x9_w2304;
    private String aspect16x9_w3072;

    public MediaImage(final String aspect16x9, final String aspect3x4, final String aspect4x3,
                      final String aspect1x1, final String aspect2x3,
                      final String aspect16x9_w152,
                      final String aspect16x9_w390,
                      final String aspect16x9_w548,
                      final String aspect16x9_w768,
                      final String aspect16x9_w1152,
                      final String aspect16x9_w1536,
                      final String aspect16x9_w1920,
                      final String aspect16x9_w2304,
                      final String aspect16x9_w3072) {
        this.aspect16x9 = aspect16x9;
        this.aspect3x4 = aspect3x4;
        this.aspect4x3 = aspect4x3;
        this.aspect1x1 = aspect1x1;
        this.aspect2x3 = aspect2x3;

        this.aspect16x9_w152 = aspect16x9_w152;
        this.aspect16x9_w390 = aspect16x9_w390;
        this.aspect16x9_w548 = aspect16x9_w548;
        this.aspect16x9_w768 = aspect16x9_w768;
        this.aspect16x9_w1152 = aspect16x9_w1152;
        this.aspect16x9_w1536 = aspect16x9_w1536;
        this.aspect16x9_w1920 = aspect16x9_w1920;
        this.aspect16x9_w2304 = aspect16x9_w2304;
        this.aspect16x9_w3072 = aspect16x9_w3072;
    }

    public URI getBackgroundImageURI() {
        try {
            return new URI(getAspect16x9());
        } catch (URISyntaxException e) {
            Log.d("URI exception: ", aspect16x9);
            return null;
        }
    }

    public URI getCardImageURI() {
        try {
            return new URI(getAspect16x9());
        } catch (URISyntaxException e) {
            Log.d("URI exception: ", aspect16x9);
            return null;
        }
    }

    public String getAspect16x9() {
        return aspect16x9;
    }

    public String getAspect3x4() {
        return aspect3x4;
    }

    public String getAspect4x3() {
        return aspect4x3;
    }

    public String getAspect1x1() {
        return aspect1x1;
    }

    public String getAspect2x3() {
        return aspect2x3;
    }

    public String getAspect16x9_w152() {
        return aspect16x9_w152;
    }

    public String getAspect16x9_w390() {
        return aspect16x9_w390;
    }

    public String getAspect16x9_w548() {
        return aspect16x9_w548;
    }

    public String getAspect16x9_w768() {
        return aspect16x9_w768;
    }

    public String getAspect16x9_w1152() {
        return aspect16x9_w1152;
    }

    public String getAspect16x9_w1536() {
        return aspect16x9_w1536;
    }

    public String getAspect16x9_w1920() {
        return aspect16x9_w1920;
    }

    public String getAspect16x9_w2304() {
        return aspect16x9_w2304;
    }

    public String getAspect16x9_w3072() {
        return aspect16x9_w3072;
    }

    @Override
    public String toString() {
        return "MediaImage{" +
                "aspect16x9='" + aspect16x9 + '\'' +
                ", aspect3x4='" + aspect3x4 + '\'' +
                ", aspect4x3='" + aspect4x3 + '\'' +
                ", aspect1x1='" + aspect1x1 + '\'' +
                ", aspect2x3='" + aspect2x3 + '\'' +
                ", aspect16x9_w152='" + aspect16x9_w152 + '\'' +
                ", aspect16x9_w390='" + aspect16x9_w390 + '\'' +
                ", aspect16x9_w548='" + aspect16x9_w548 + '\'' +
                ", aspect16x9_w768='" + aspect16x9_w768 + '\'' +
                ", aspect16x9_w1152='" + aspect16x9_w1152 + '\'' +
                ", aspect16x9_w1536='" + aspect16x9_w1536 + '\'' +
                ", aspect16x9_w1920='" + aspect16x9_w1920 + '\'' +
                ", aspect16x9_w2304='" + aspect16x9_w2304 + '\'' +
                ", aspect16x9_w3072='" + aspect16x9_w3072 + '\'' +
                '}';
    }

}
