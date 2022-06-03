package ru.wert.bazapik_mobile;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.serviceQUICK.DraftQuickService;
import ru.wert.bazapik_mobile.data.serviceQUICK.PassportQuickService;
import ru.wert.bazapik_mobile.data.servicesREST.DraftService;
import ru.wert.bazapik_mobile.data.servicesREST.FileService;
import ru.wert.bazapik_mobile.data.servicesREST.PassportService;
import ru.wert.bazapik_mobile.search.ItemRecViewAdapter;

import static ru.wert.bazapik_mobile.constants.Consts.HIDE_PREFIXES;
import static ru.wert.bazapik_mobile.constants.Consts.SHOW_SOLID_FILES;

public class ThisApplication extends Application {

    public static final String APPLICATION_VERSION = "1.0";
    public static boolean APP_VERSION_NOTIFICATION_SHOWN = false;
    public static String APPLICATION_VERSION_AVAILABLE;

    //Расширение
    public static List<String> PDF_EXTENSIONS = Collections.singletonList("pdf");
    public static List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    public static List<String> SOLID_EXTENSIONS = Arrays.asList("eprt", "easm");
    public static List<String> DRAW_EXTENSIONS = Arrays.asList("prt", "sldprt", "asm", "sldasm", "drw", "sldrw", "dxf");

    //Фильтр
    public static boolean showValid = true;
    public static boolean showChanged;
    public static boolean showAnnulled;

    public static FileService FILE_SERVICE;

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
            case "SHOW_SOLID_FILES":
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

        SHOW_SOLID_FILES = Boolean.parseBoolean(getProp("SHOW_SOLID_FILES"));
        HIDE_PREFIXES = Boolean.parseBoolean(getProp("HIDE_PREFIXES"));

    }

    public static Animation createBlinkAnimation(){
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(400); //You can manage the time of the blink with this parameter
        animation.setStartOffset(20);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(Animation.INFINITE);
        return animation;
    }

    public static ProgressDialog createProgressDialog(Context context, String fileName){
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("в процессе....");
        progressDialog.setTitle("Загрузка файла " + fileName);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        return progressDialog;
    }
}
