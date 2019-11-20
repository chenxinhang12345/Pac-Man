package com.example.chenxinhang.pacman;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONArray;
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
    @Test
    public void maze_Parsing(){
        String walls = "{\"Rows\":[{\"X1\":0 ,\"X2\":200,\"Y\":800},{\"X1\":200 ,\"X2\":400,\"Y\":800},{\"X1\":0 ,\"X2\":200,\"Y\":1000}]," +
                                       "\"Cols\": [{\"Y1\":800 ,\"Y2\":1000,\"X\":0},{\"Y1\":800 ,\"Y2\":1000,\"X\":200}]}";
        try{
            JSONObject obj = new JSONObject(walls);
            JSONArray rowsArr = obj.getJSONArray("Rows");
            JSONArray colsArr = obj.getJSONArray("Cols");
                String json = rowsArr.getString(0);
                JSONObject objRow = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                int x1 = objRow.getInt("X1");
                int x2 = objRow.getInt("X2");
                int y = objRow.getInt("Y");
                assertTrue(y == 800 );
                assertTrue(x1 ==0);
                assertTrue(x2 == 200);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
