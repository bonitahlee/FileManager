package com.bonita.filemanager.constants;

import android.view.KeyEvent;

/**
 * <pre>
 *     Blaze key mapping
 * </pre>
 *
 * @author parkho79
 * @date 2021-10-21
 */
public interface BlazeKey {

    long LONG_KEY_TIME = 2000;

    int LONG_ACCUMULATION_VALUE = 9000;
    int DEEP_ACCUMULATION_VALUE = 90000;

    int POWER = KeyEvent.KEYCODE_POWER;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FUNCTION KEY

    int FUNCTION_MODE_SHORT = KeyEvent.KEYCODE_F1;
    int FUNCTION_MODE_LONG = KeyEvent.KEYCODE_F1 + LONG_ACCUMULATION_VALUE;
    int FUNCTION_MODE_DEEP = KeyEvent.KEYCODE_F1 + LONG_ACCUMULATION_VALUE + DEEP_ACCUMULATION_VALUE;

    int FUNCTION_WIFI_SHORT = KeyEvent.KEYCODE_F2;
    int FUNCTION_WIFI_LONG = KeyEvent.KEYCODE_F2 + LONG_ACCUMULATION_VALUE;
    int FUNCTION_WIFI_DEEP = KeyEvent.KEYCODE_F2 + LONG_ACCUMULATION_VALUE + DEEP_ACCUMULATION_VALUE;

    int FUNCTION_BLUETOOTH_SHORT = KeyEvent.KEYCODE_F3;
    int FUNCTION_BLUETOOTH_LONG = KeyEvent.KEYCODE_F3 + LONG_ACCUMULATION_VALUE;
    int FUNCTION_BLUETOOTH_DEEP = KeyEvent.KEYCODE_F3 + LONG_ACCUMULATION_VALUE + DEEP_ACCUMULATION_VALUE;

    int FUNCTION_CONNECT_SHORT = KeyEvent.KEYCODE_F4;
    int FUNCTION_CONNECT_LONG = KeyEvent.KEYCODE_F4 + LONG_ACCUMULATION_VALUE;
    int FUNCTION_CONNECT_DEEP = KeyEvent.KEYCODE_F4 + LONG_ACCUMULATION_VALUE + DEEP_ACCUMULATION_VALUE;

    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // NAVIGATION KEY

    int NAVIGATION_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
    int NAVIGATION_LEFT_LONG = KeyEvent.KEYCODE_DPAD_LEFT + LONG_ACCUMULATION_VALUE;
    int NAVIGATION_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
    int NAVIGATION_RIGHT_LONG = KeyEvent.KEYCODE_DPAD_RIGHT + LONG_ACCUMULATION_VALUE;
    int NAVIGATION_UP = KeyEvent.KEYCODE_DPAD_UP;
    int NAVIGATION_UP_LONG = KeyEvent.KEYCODE_DPAD_UP + LONG_ACCUMULATION_VALUE;
    int NAVIGATION_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
    int NAVIGATION_DOWN_LONG = KeyEvent.KEYCODE_DPAD_DOWN + LONG_ACCUMULATION_VALUE;
    int NAVIGATION_OK = KeyEvent.KEYCODE_ENTER;
    int NAVIGATION_OK_LONG = KeyEvent.KEYCODE_ENTER + LONG_ACCUMULATION_VALUE;

    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // CONTROL KEY

    int CONTROL_HOME = KeyEvent.KEYCODE_HOME;
    int CONTROL_HOME_LONG = KeyEvent.KEYCODE_HOME + LONG_ACCUMULATION_VALUE;
    int CONTROL_MENU = KeyEvent.KEYCODE_MENU;
    int CONTROL_MENU_LONG = KeyEvent.KEYCODE_MENU + LONG_ACCUMULATION_VALUE;
    int CONTROL_CANCEL = KeyEvent.KEYCODE_CLEAR;
    int CONTROL_CANCEL_LONG = KeyEvent.KEYCODE_CLEAR + LONG_ACCUMULATION_VALUE;
    int CONTROL_DELETE = KeyEvent.KEYCODE_DEL;
    int CONTROL_DELETE_LONG = KeyEvent.KEYCODE_DEL + LONG_ACCUMULATION_VALUE;

    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // NUMERIC KEY

    int NUMERIC_1 = KeyEvent.KEYCODE_1;
    int NUMERIC_1_LONG = KeyEvent.KEYCODE_1 + LONG_ACCUMULATION_VALUE;
    int NUMERIC_2 = KeyEvent.KEYCODE_2;
    int NUMERIC_2_LONG = KeyEvent.KEYCODE_2 + LONG_ACCUMULATION_VALUE;
    int NUMERIC_3 = KeyEvent.KEYCODE_3;
    int NUMERIC_3_LONG = KeyEvent.KEYCODE_3 + LONG_ACCUMULATION_VALUE;
    int NUMERIC_4 = KeyEvent.KEYCODE_4;
    int NUMERIC_4_LONG = KeyEvent.KEYCODE_4 + LONG_ACCUMULATION_VALUE;
    int NUMERIC_5 = KeyEvent.KEYCODE_5;
    int NUMERIC_5_LONG = KeyEvent.KEYCODE_5 + LONG_ACCUMULATION_VALUE;
    int NUMERIC_6 = KeyEvent.KEYCODE_6;
    int NUMERIC_6_LONG = KeyEvent.KEYCODE_6 + LONG_ACCUMULATION_VALUE;
    int NUMERIC_7 = KeyEvent.KEYCODE_7;
    int NUMERIC_7_LONG = KeyEvent.KEYCODE_7 + LONG_ACCUMULATION_VALUE;
    int NUMERIC_8 = KeyEvent.KEYCODE_8;
    int NUMERIC_8_LONG = KeyEvent.KEYCODE_8 + LONG_ACCUMULATION_VALUE;
    int NUMERIC_9 = KeyEvent.KEYCODE_9;
    int NUMERIC_9_LONG = KeyEvent.KEYCODE_9 + LONG_ACCUMULATION_VALUE;
    int NUMERIC_STAR = KeyEvent.KEYCODE_STAR;
    int NUMERIC_STAR_LONG = KeyEvent.KEYCODE_STAR + LONG_ACCUMULATION_VALUE;
    int NUMERIC_0 = KeyEvent.KEYCODE_0;
    int NUMERIC_0_LONG = KeyEvent.KEYCODE_0 + LONG_ACCUMULATION_VALUE;
    int NUMERIC_POUND = KeyEvent.KEYCODE_POUND;
    int NUMERIC_POUND_LONG = KeyEvent.KEYCODE_POUND + LONG_ACCUMULATION_VALUE;


    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // LEFT EDGE KEY

    int LEFT_EDGE_RECORDING = KeyEvent.KEYCODE_MEDIA_RECORD;
    int LEFT_EDGE_RECORDING_LONG = KeyEvent.KEYCODE_MEDIA_RECORD + LONG_ACCUMULATION_VALUE;
    int LEFT_EDGE_VOICE_CONTROL = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
    int LEFT_EDGE_VOICE_CONTROL_LONG = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE + LONG_ACCUMULATION_VALUE;
    int LEFT_EDGE_VOLUME_UP = KeyEvent.KEYCODE_VOLUME_UP;
    int LEFT_EDGE_VOLUME_UP_LONG = KeyEvent.KEYCODE_VOLUME_UP + LONG_ACCUMULATION_VALUE;
    int LEFT_EDGE_VOLUME_DOWN = KeyEvent.KEYCODE_VOLUME_DOWN;
    int LEFT_EDGE_VOLUME_DOWN_LONG = KeyEvent.KEYCODE_VOLUME_DOWN + LONG_ACCUMULATION_VALUE;


    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // RIGHT EDGE KEY

    int RIGHT_EDGE_LOCK = KeyEvent.KEYCODE_F12;
}