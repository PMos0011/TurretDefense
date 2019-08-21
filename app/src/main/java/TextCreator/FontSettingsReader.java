package TextCreator;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import pmos0011.TowerShooter.R;

public class FontSettingsReader {

    public static void reader(Context context, FontRenderer renderer) {

        Map<String, String> values = new HashMap<>();

        final InputStream inputStream = context.getResources().openRawResource(R.raw.calibri_settings);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String nextLine;

        while (true) {
            try {
                if (((nextLine = bufferedReader.readLine()) == null)) break;

                values.clear();
                for (String part : nextLine.split(" ")) {
                    String[] valuePairs = part.split("=");
                    if(valuePairs.length==2)
                        values.put(valuePairs[0],valuePairs[1]);
                }

                int id = Integer.parseInt(values.get("id"));
                float x = Float.parseFloat(values.get("x"));
                float y = Float.parseFloat(values.get("y"));
                float w= Float.parseFloat(values.get("width"));
                float h = Float.parseFloat(values.get("height"));
                float xoffset = Float.parseFloat(values.get("xoffset"));
                float yoffset = Float.parseFloat(values.get("yoffset"));

                renderer.addCharacterData(new Characters(id,x,y,w,h,xoffset,yoffset));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
