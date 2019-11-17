package com.example.chenxinhang.pacman;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.example.chenxinhang.pacman", appContext.getPackageName());
    }
    @Test
    public void data_Parsing(){
        try {
            String info = "{\"ID\":3,\"X\":5,\"Y\":6}";
            JSONObject obj = new JSONObject(info);
            int X = obj.getInt("X");
            int Y = obj.getInt("Y");
            int ID = obj.getInt("ID");
            int color = obj.getInt("Color");
            assertEquals(X,5);
            assertEquals(Y,6);
            assertEquals(ID,3);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
