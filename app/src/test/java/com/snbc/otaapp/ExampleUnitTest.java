package com.snbc.otaapp;

import org.junit.Test;

import java.util.Calendar;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private static final String TAG = ExampleUnitTest.class.getName();

    @Test
    public void addition_isCorrect() {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2018,0,1);
        long millis = calendar.getTimeInMillis();

//        System.out.println("_ _" + seconds);
//        Log.i(TAG, "addition_isCorrect: " + seconds);
    }
}