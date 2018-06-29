package demo.eco.greaper.opencvdemo.color;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import demo.eco.greaper.opencvdemo.R;

public class ColorPalette {
    public static int[] getAccentColors(Context context){
        return new int[]{
                ContextCompat.getColor(context, R.color.md_red_500),
                ContextCompat.getColor(context, R.color.md_purple_500),
//                ContextCompat.getColor(context, R.color.md_deep_purple_500),
//                ContextCompat.getColor(context, R.color.md_blue_500),
//                ContextCompat.getColor(context, R.color.md_light_blue_500),
//                ContextCompat.getColor(context, R.color.md_cyan_500),
                ContextCompat.getColor(context, R.color.md_teal_500),
                ContextCompat.getColor(context, R.color.md_green_500),
                ContextCompat.getColor(context, R.color.md_yellow_500),
//                ContextCompat.getColor(context, R.color.md_orange_500),
//                ContextCompat.getColor(context, R.color.md_deep_orange_500),
//                ContextCompat.getColor(context, R.color.md_brown_500),
//                ContextCompat.getColor(context, R.color.md_blue_grey_500),
        };
    }
}
