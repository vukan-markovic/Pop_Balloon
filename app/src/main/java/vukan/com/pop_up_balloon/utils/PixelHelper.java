package vukan.com.pop_up_balloon.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * <h1>PixelHelper</h1>
 *
 * <p><b>PixelHelper class is responsible to convert pixels into dp.</b></p>
 */
public class PixelHelper {

    /**
     * This method convert screen pixels into dp.
     *
     * @param px      Number of screen pixels.
     * @param context Context of activity which call this method.
     * @return int Number of screen's dp.
     * @see TypedValue
     */
    public static int pixelsToDp(int px, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
    }
}