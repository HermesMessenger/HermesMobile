package org.hermesmessenger.hermes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.Serializable;

public class User implements Serializable {

    private String color;
    private String image;

    public User(String color, String image_b64) {
        this.color = color;
        this.image = image_b64;
    }

    public String getColor() {
        return color;
    }

    public String getImage_b64() {
        return this.image;
    }

    public Bitmap getImage_bitmap() {

        byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

    }

}
