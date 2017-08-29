package org.helpapaw.helpapaw.utils;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.PawApplication;

import static org.helpapaw.helpapaw.data.models.Signal.SOLVED;
import static org.helpapaw.helpapaw.data.models.Signal.SOMEBODY_ON_THE_WAY;

/**
 * Created by milen on 29/08/17.
 */

public class StatusUtils {

    public static String getStatusStringForCode(int statusCode) {
        switch (statusCode) {
            case SOLVED:
                return PawApplication.getContext().getString(R.string.txt_solved);
            case SOMEBODY_ON_THE_WAY:
                return PawApplication.getContext().getString(R.string.txt_somebody_on_the_way);
            default:
                return PawApplication.getContext().getString(R.string.txt_help_needed);
        }
    }

    public static int getPinResourceForCode(int statusCode) {
        switch (statusCode) {
            case SOLVED:
                return R.drawable.pin_green;
            case SOMEBODY_ON_THE_WAY:
                return R.drawable.pin_orange;
            default:
                return R.drawable.pin_red;
        }
    }
}
