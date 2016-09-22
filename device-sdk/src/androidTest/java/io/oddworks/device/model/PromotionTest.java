package io.oddworks.device.model;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class PromotionTest {
    public Promotion mPromotion;

    @Before
    public void beforeEach() {
        mPromotion = new Promotion(new Identifier("123", OddObject.TYPE_PROMOTION));
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("title", "title1");
        attributes.put("description", "description1");
        attributes.put("mediaImage", new MediaImage("a", "b", "c", "d", "e", "f", "e", "g", "h", "i", "j", "k", "l", "m"));
        mPromotion.setAttributes(attributes);
    }

    @Test
    public void isPresentableShouldReturnTrue() {
        assertThat(mPromotion.isPresentable(), is(true));
    }

    @Test
    public void toPresentableFieldsShouldMatchThisFields() {
        Presentable presentable = mPromotion.toPresentable();
        assertThat("titles should match", presentable.getTitle(),
                is(equalTo(mPromotion.getTitle())));
        assertThat("descriptions should match", presentable.getDescription(),
                is(equalTo(mPromotion.getDescription())));
        assertThat("MediaImage should match", presentable.getMediaImage(),
                is(equalTo(mPromotion.getMediaImage())));
    }
}
