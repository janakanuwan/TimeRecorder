package com.nus.hci.timerrecord;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Pattern: https://developer.android.com/reference/java/util/regex/Pattern
 * DataStorage: https://developer.android.com/training/data-storage/files#java
 */
public final class FileUtil {

    private static final String TAG = FileUtil.class.getName();

    private static final String APP_FOLDER = "TimerRecord";

    private static final SimpleDateFormat DATE_FORMAT_YYYYMMDD_HHMMSS = new SimpleDateFormat("yyyyMMdd_HHmmss");

    /**
     * @param fileName
     * @return the absolute file name (w.r.t. application folder)
     */
    public static String getAbsoluteFilePath(String fileName) {
        File appFolder = getApplicationFolder();
        return appFolder.getAbsolutePath() + File.separator + fileName;
    }

    /**
     * @return application folder
     */
    public static File getApplicationFolder() {
        File appFolder = new File(Environment.getExternalStorageDirectory(), APP_FOLDER);
        if (!appFolder.exists()) {
            if (!appFolder.mkdirs()) {
                Log.e(TAG, "[FILE] Creating application folder failed");
            }
        }
        return appFolder;
    }

    public static boolean isFileExists(String absolutePath) {
        File file = new File(absolutePath);
        return file.exists();
    }

    /**
     * Write input stream to given file name in application folder
     *
     * @param in
     * @param outputFileName file name with extension
     * @throws IOException
     */
    public static void writeFile(InputStream in, String outputFileName) throws IOException {
        try (
                InputStream inputStream = in;
                OutputStream outputStream = new FileOutputStream(new File(getAbsoluteFilePath(outputFileName)));
        ) {
            byte[] dataBytes = new byte[4096];
            while (true) {
                int read = inputStream.read(dataBytes);
                if (read == -1) {
                    break;
                }
                outputStream.write(dataBytes, 0, read);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Write input stream to given file name in application folder
     *
     * @param data           byte array of data to be written
     * @param outputFileName file name with extension
     * @throws IOException
     */
    public static void writeFile(byte[] data, String outputFileName) throws IOException {
        try (
                OutputStream outputStream = new FileOutputStream(new File(getAbsoluteFilePath(outputFileName)));
        ) {
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * @return the current DateTime in "YYYYMMDD_HHMMSS" format
     */
    public static String getFormattedCurrentDateTime() {
        return DATE_FORMAT_YYYYMMDD_HHMMSS.format(new Date());
    }

    /**
     * @param fileName
     * @return the content of file as a String
     */
    public static String readFile(String fileName) throws IOException {
        String data = null;
        try (InputStream in = new FileInputStream(new File(getAbsoluteFilePath(fileName)))) {
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            data = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw e;
        }
        return data;
    }

    /**
     * @param fileName
     * @return the content of file as a {@link List}
     */
    public static List<String> readFileLines(String fileName) throws IOException {
        String line;
        List<String> lineList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(FileUtil.getAbsoluteFilePath(fileName))))) {
            while ((line = br.readLine()) != null) {
                lineList.add(line);
            }
        } catch (IOException e) {
            throw e;
        }
        return lineList;
    }

}
