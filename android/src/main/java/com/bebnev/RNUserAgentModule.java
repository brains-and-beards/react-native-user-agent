package com.bebnev;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.module.annotations.ReactModule;
import java.lang.Runtime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ReactModule(name = RNUserAgentModule.NAME)
public class RNUserAgentModule extends ReactContextBaseJavaModule {

    public static final String NAME = "RNUserAgent";

    public RNUserAgentModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return NAME;
    }

    protected String getUserAgent() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return WebSettings.getDefaultUserAgent(
                    getReactApplicationContext()
                );
            } else {
                return System.getProperty("http.agent");
            }
        } catch (RuntimeException e) {
            return System.getProperty("http.agent");
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return this.capitalize(model);
        } else {
            return this.capitalize(manufacturer) + " " + model;
        }
    }

    @ReactMethod
    protected void getWebViewUserAgent(final Promise p) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            p.resolve(
                WebSettings.getDefaultUserAgent(getReactApplicationContext())
            );
        }

        UiThreadUtil.runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    p.resolve(
                        new WebView(getReactApplicationContext())
                            .getSettings()
                            .getUserAgentString()
                    );
                }
            }
        );
    }

    private PackageInfo getPackageInfo() throws Exception {
        return getReactApplicationContext()
            .getPackageManager()
            .getPackageInfo(getReactApplicationContext().getPackageName(), 0);
    }

    @Override
    public Map<String, Object> getConstants() {
        String packageName = getReactApplicationContext().getPackageName();
        String applicationVersion = "";
        String applicationName = "";
        String buildNumber = "";
        String userAgent = "";

        try {
            applicationName = getReactApplicationContext()
                .getApplicationInfo()
                .loadLabel(getReactApplicationContext().getPackageManager())
                .toString();
            applicationVersion = getPackageInfo().versionName;
            buildNumber = Integer.toString(getPackageInfo().versionCode);
            userAgent =
                packageName +
                '/' +
                applicationVersion +
                '.' +
                buildNumber.toString() +
                " (" +
                this.getDeviceName() +
                "; Android " +
                Build.VERSION.RELEASE +
                ')';
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<String, Object> constants = new HashMap<String, Object>();

        constants.put("systemName", "Android");
        constants.put("systemVersion", Build.VERSION.RELEASE);
        constants.put("packageName", packageName);
        constants.put("applicationName", applicationName);
        constants.put("applicationVersion", applicationVersion);
        constants.put("buildNumber", buildNumber);
        constants.put("userAgent", userAgent);

        return constants;
    }
}
