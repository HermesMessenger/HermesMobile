package org.hermesmessenger.hermes;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.InputStream;

public class Utils {
    static public Bitmap getB64Image(String base64) {
        byte[] decodedByte = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}
