package ru.wert.bazapik_mobile;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

import ru.wert.bazapik_mobile.constants.Consts;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.serviceQUICK.DraftQuickService;
import ru.wert.bazapik_mobile.data.serviceQUICK.PassportQuickService;
import ru.wert.bazapik_mobile.data.servicesREST.DraftService;
import ru.wert.bazapik_mobile.data.servicesREST.PassportService;
import ru.wert.bazapik_mobile.dataPreloading.DataLoader;
import ru.wert.bazapik_mobile.search.ItemRecViewAdapter;

import static ru.wert.bazapik_mobile.constants.Consts.HIDE_PREFIXES;
import static ru.wert.bazapik_mobile.constants.Consts.SHOW_FOLDERS;

public class ThisApplication extends Application {

    public static final String APPLICATION_VERSION = "1.0 beta";

    public static PassportService PASSPORT_SERVICE;
    public static PassportQuickService PASSPORT_QUICK_SERVICE;

    public static DraftService DRAFT_SERVICE;
    public static DraftQuickService DRAFT_QUICK_SERVICE;

    public static String SEARCH_TEXT = "";
    public static ItemRecViewAdapter<Passport> ADAPTER;
    public static String DATA_BASE_URL;

    static SharedPreferences settings;
    static SharedPreferences.Editor editor;
    private static Context appContext;

    public static Context getAppContext(){
        return ThisApplication.appContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        settings = getSharedPreferences("DBPIKSettings", MODE_PRIVATE);
        editor = settings.edit();
        ThisApplication.appContext = this.getApplicationContext();
    }

    public static String getProp(String name){

        switch(name){
            case "IP":
                return settings.getString(name, "10.0.2.2");
            case "PORT":
                return settings.getString(name, "8080");
            case "USER_NAME":
                return settings.getString(name, "");
            case "SHOW_FOLDERS":
                return settings.getString(name, "false");
            case "HIDE_PREFIXES":
                return settings.getString(name, "false");
            default:
                return "NotFoundProperty";
        }
    }

    @SuppressLint("CommitPrefEdits")
    public static void setProp(String name, String val){
        editor.putString(name, val);
        editor.apply();
    }

    public static void loadSettings() {

        SHOW_FOLDERS = Boolean.parseBoolean(getProp("SHOW_FOLDERS"));
        HIDE_PREFIXES = Boolean.parseBoolean(getProp("HIDE_PREFIXES"));

    }
}
