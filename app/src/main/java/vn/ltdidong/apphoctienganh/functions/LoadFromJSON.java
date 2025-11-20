package vn.ltdidong.apphoctienganh.functions;

import android.content.Context;
import android.util.Log;

import vn.ltdidong.apphoctienganh.models.ReadingPassage;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoadFromJSON {
    // hàm load json vào model ReadingPassage
    public ReadingPassage loadFromJSON(String json) {
        ReadingPassage RP = new ReadingPassage();

        Gson gson = new Gson();
        RP = gson.fromJson(json, ReadingPassage.class);

        return RP;
    }

    public static List<ReadingPassage> loadAllPassages(Context context) {
        List<ReadingPassage> passages = new ArrayList<>();
        Gson gson = new Gson();

        File dir = context.getFilesDir();
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    String json = readFile(file);
                    if (json != null) {
                        ReadingPassage passage = gson.fromJson(json, ReadingPassage.class);
                        passages.add(passage);
                    }
                }
            }
        }

        return passages;
    }

    private static String readFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            Log.e("!!! Load from JSON", e.getMessage());
            return null;
        }
    }
}
