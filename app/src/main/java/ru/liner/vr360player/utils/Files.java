package ru.liner.vr360player.utils;

import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 21.04.2022, четверг
 **/
public class Files {

    private static String getMIMEType(@NonNull File file) {
        String type = "*/*";
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex < 0)
            return type;
        String end = fileName.substring(dotIndex);
        if (TextUtils.isEmpty(end) && end.length() < 2)
            return type;
        end = end.substring(1).toLowerCase();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        type = mimeTypeMap.getMimeTypeFromExtension(end);
        return type;
    }

    @Nullable
    public static File getFileFromUri(Uri uri) {
        if (uri == null)
            return null;
        if ((uri.getScheme().equals("file")) && (!TextUtils.isEmpty(uri.getPath())))
            return new File(uri.getPath());
        return null;
    }

    public static void copyFile(@NonNull File source, @NonNull File destination) throws IOException {
        try (InputStream inputStream = new FileInputStream(source); OutputStream outputStream = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0)
                outputStream.write(buffer, 0, length);
        }
    }

    public static void downloadFile(@NonNull File saveFile, @NonNull String downloadLink) {
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new URL(downloadLink).openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(saveFile);
            byte[] data = new byte[1024];
            int read;
            while ((read = bufferedInputStream.read(data, 0, 1024)) != -1)
                fileOutputStream.write(data, 0, read);
            fileOutputStream.close();
            bufferedInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static String readFile(@NonNull File file) {
        try {
            StringBuilder stringBuffer = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                stringBuffer.append(String.valueOf(buffer, 0, read));
            }
            reader.close();
            return stringBuffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeFile(@NonNull File file, @NonNull String content) {
        if (file.exists())
            if (!file.delete()) {
            }
        try {
            if (!file.createNewFile()) {
            }
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                fileOutputStream.write(content.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean ensureDirectory(@NonNull File directory) {
        if (!directory.exists()) {
            return directory.mkdir();
        }
        return true;
    }
}
