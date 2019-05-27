package de.tuberlin.mcc.simra.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.nfc.cardemulation.HostNfcFService;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import static android.content.Context.MODE_APPEND;

public class Utils {

    private static final String TAG = "Utils_LOG";

    public static String readContentFromFile(String fileName, Context context) {
        File file;
        if (fileName.contains(File.separator)) {
            file = new File(fileName);
        } else {
            file = context.getFileStreamPath(fileName);
        }
        if (file.isDirectory()) {
            return "FILE IS DIRECTORY";
        }
        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;

            while ((line = br.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return content.toString();
    }

    public static String readContentFromFileAndIncreaseFileVersion(String fileName, Context context) {
        StringBuilder content = new StringBuilder();
        int appVersion = getAppVersionNumber(context);
        String fileInfoLine = appVersion + "#-1";

        try (BufferedReader br = new BufferedReader(new FileReader(context.getFileStreamPath(fileName)))) {
            String line;
            line = br.readLine();
            String fileVersion = "" + ((Integer.valueOf(line.split("#")[1])) + 1);
            fileInfoLine = appVersion + "#" + fileVersion + System.lineSeparator();
            while ((line = br.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
            overWriteFile((fileInfoLine + content.toString()), fileName, context);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return (fileInfoLine + content.toString());
    }


    public static void appendToFile(String content, String fileName, Context context) {

        try {
            FileOutputStream writer = context.openFileOutput(fileName, MODE_APPEND);
            writer.write(content.getBytes());
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    // appends content from file <fileNameBottom> to the content of file <fileNameTop>
    // and increases both their file version number
    public static String appendFromFileToFile(String fileNameTop, String fileNameBottom, Context context){

        StringBuilder content = new StringBuilder();
        StringBuilder contentTop = new StringBuilder();
        int appVersion = getAppVersionNumber(context);
        String topFileInfoLine = appVersion + "#-1";

        try (BufferedReader br = new BufferedReader(new FileReader(context.getFileStreamPath(fileNameTop)))) {
            String line;
            line = br.readLine();
            String topFileVersion = "" + ((Integer.valueOf(line.split("#")[1])) + 1);
            topFileInfoLine = appVersion + "#" + topFileVersion + System.lineSeparator();
            while ((line = br.readLine()) != null) {
                contentTop.append(line).append(System.lineSeparator());
            }
            overWriteFile((topFileInfoLine + contentTop.toString()), fileNameTop, context);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        StringBuilder contentBottom = new StringBuilder();
        String bottomFileInfoLine = appVersion + "#-1";
        try (BufferedReader br = new BufferedReader(new FileReader(context.getFileStreamPath(fileNameBottom)))) {
            String line;
            line = br.readLine();
            String bottomFileVersion = "" + ((Integer.valueOf(line.split("#")[1])) + 1);
            bottomFileInfoLine = appVersion + "#" + bottomFileVersion + System.lineSeparator();
            while ((line = br.readLine()) != null) {
                contentBottom.append(line).append(System.lineSeparator());
            }
            overWriteFile((bottomFileInfoLine + contentBottom.toString()), fileNameBottom, context);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        content.append(topFileInfoLine).append(contentTop);
        content.append(System.lineSeparator()).append("=========================").append(System.lineSeparator());
        content.append(bottomFileInfoLine).append(contentBottom);
        return content.toString();
    }

    public static void overWriteFile(String content, String fileName, Context context) {
        try {
            FileOutputStream writer = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            writer.write(content.getBytes());
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static boolean fileExists(String fileName, Context context) {
        return context.getFileStreamPath(fileName).exists();
    }

    public static String lookUpSharedPrefs(String key, String defValue, String sharedPrefName, Context context) {
        SharedPreferences sharedPrefs = context.getApplicationContext()
                .getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        return sharedPrefs.getString(key, defValue);
    }

    public static int lookUpIntSharedPrefs(String key, int defValue, String sharedPrefName, Context context) {
        SharedPreferences sharedPrefs = context.getApplicationContext()
                .getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        return sharedPrefs.getInt(key, defValue);
    }

    public static long lookUpLongSharedPrefs(String key, long defValue, String sharedPrefName, Context context) {
        SharedPreferences sharedPrefs = context.getApplicationContext()
                .getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        return sharedPrefs.getLong(key, defValue);
    }
    public static boolean lookUpBooleanSharedPrefs(String key, boolean defValue, String sharedPrefName, Context context) {
        SharedPreferences sharedPrefs = context.getApplicationContext()
                .getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        return sharedPrefs.getBoolean(key, defValue);
    }

    public static void writeToSharedPrefs(String key, String value, String sharedPrefName, Context context) {
        SharedPreferences sharedPrefs = context.getApplicationContext()
                .getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void writeIntToSharedPrefs(String key, int value, String sharedPrefName, Context context) {
        SharedPreferences sharedPrefs = context.getApplicationContext()
                .getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void writeLongToSharedPrefs(String key, long value, String sharedPrefName, Context context) {
        SharedPreferences sharedPrefs = context.getApplicationContext()
                .getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static void writeBooleanToSharedPrefs(String key, boolean value, String sharedPrefName, Context context) {
        SharedPreferences sharedPrefs = context.getApplicationContext()
                .getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }


    // Check if an accEvent has already been annotated based on one line of the accEvents csv file.
    // Returns true, if accEvent was already annotated.

    public static boolean checkForAnnotation(String[] incidentProps) {

        // Only checking for empty strings, which means we are retaining
        // events that were labeled as 'nothing happened'
        return !incidentProps[10].equals("") || !incidentProps[11].equals("") ||
                !incidentProps[12].equals("") || !incidentProps[13].equals("") ||
                !incidentProps[14].equals("") || !incidentProps[15].equals("") ||
                !incidentProps[16].equals("") || !incidentProps[17].equals("") ||
                !incidentProps[18].equals("") || !incidentProps[19].equals("");

    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // Create an AlertDialog with an OK Button displaying a message
    public static void showMessageOK(String message, DialogInterface.OnClickListener okListener, Context context) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

    public static int getAppVersionNumber(Context context) {
        PackageInfo pinfo = null;
        try {
            pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int installedVersionNumber = -1;
        if (pinfo != null) {
            installedVersionNumber = pinfo.versionCode;
        }
        return installedVersionNumber;
    }

}