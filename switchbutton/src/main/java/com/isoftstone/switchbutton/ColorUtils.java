package com.isoftstone.switchbutton;

import ohos.agp.components.ComponentState;
import ohos.agp.utils.Color;

/**
 * Generate thumb and background color state list use tintColor
 * Created by kyle on 15/11/4.
 */
public class ColorUtils {

    private static int defaultColor = Color.CYAN.getValue();

    static int getDefaultColor(){
        return defaultColor;
    }

    static int[][] ThumbColorstates = new int[][]{
            {ComponentState.COMPONENT_STATE_CHECKED},
            {ComponentState.COMPONENT_STATE_EMPTY},
            {ComponentState.COMPONENT_STATE_DISABLED},
            {ComponentState.COMPONENT_STATE_PRESSED}
    };

    static int[] ThumbColors = new int[]{
            Color.BLUE.getValue(),
            Color.WHITE.getValue(),
            Color.LTGRAY.getValue(),
            Color.BLUE.getValue()
    };

    static int[] BackColors = new int[]{
            Color.YELLOW.getValue(),
            Color.GRAY.getValue(),
            Color.DKGRAY.getValue(),
            Color.DKGRAY.getValue()
    };


    public static int getThumbColorForStates(int states){
        switch (states){
            case ComponentState.COMPONENT_STATE_EMPTY:
                return ThumbColors[0];
            case ComponentState.COMPONENT_STATE_CHECKED:
                return ThumbColors[1];
            default:
                return defaultColor;

        }
    }

    public static int getThumbColorForNextStates(int states){
        switch (states){
            case ComponentState.COMPONENT_STATE_EMPTY:
                return ThumbColors[1];
            case ComponentState.COMPONENT_STATE_CHECKED:
                return ThumbColors[0];
            default:
                return defaultColor;

        }
    }

    public static int getBlackColorForStates(int states){
        switch (states){
            case ComponentState.COMPONENT_STATE_EMPTY:
                return BackColors[0];
            case ComponentState.COMPONENT_STATE_CHECKED:
                return BackColors[1];
            default:
                return defaultColor;

        }
    }

    static int[] generateThumbColorWithTintColor(final int tintColor) {

        ThumbColors = new int[]{
                Color.BLACK.getValue(),Color.GREEN.getValue()
        };
        return ThumbColors;
    }

    static int[][] BackColorstates = new int[][]{
            {ComponentState.COMPONENT_STATE_CHECKED},
            {ComponentState.COMPONENT_STATE_EMPTY},
            {ComponentState.COMPONENT_STATE_DISABLED},
            {ComponentState.COMPONENT_STATE_PRESSED}
    };


    static int[] generateBackColorWithTintColor(final int tintColor) {

        int[] colors = new int[]{
                tintColor - 0xE1000000,
                0x10000000,
        };
        return colors;
    }

}
