package ru.wert.bazapik_mobile;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import ru.wert.bazapik_mobile.data.enums.EDraftStatus;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.ProductGroup;
import ru.wert.bazapik_mobile.data.serviceQUICK.DraftQuickService;
import ru.wert.bazapik_mobile.data.serviceQUICK.FolderQuickService;
import ru.wert.bazapik_mobile.data.serviceQUICK.PassportQuickService;
import ru.wert.bazapik_mobile.data.servicesREST.DraftService;
import ru.wert.bazapik_mobile.data.servicesREST.FileService;
import ru.wert.bazapik_mobile.data.servicesREST.FolderService;
import ru.wert.bazapik_mobile.data.servicesREST.PassportService;
import ru.wert.bazapik_mobile.search.DraftsRecViewAdapter;

import static ru.wert.bazapik_mobile.constants.Consts.HIDE_PREFIXES;
import static ru.wert.bazapik_mobile.constants.Consts.SHOW_SOLID_FILES;

public class ThisApplication extends Application {

    public static final String APPLICATION_VERSION = "1.1";
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

    public static FolderService FOLDER_SERVICE;
    public static FolderQuickService FOLDER_QUICK_SERVICE;

    public static PassportService PASSPORT_SERVICE;
    public static PassportQuickService PASSPORT_QUICK_SERVICE;

    public static DraftService DRAFT_SERVICE;
    public static DraftQuickService DRAFT_QUICK_SERVICE;

    public static String SEARCH_TEXT = "";
    public static DraftsRecViewAdapter<Passport> ADAPTER;
    public static String DATA_BASE_URL;

    static SharedPreferences settings;
    static SharedPreferences.Editor editor;
    private static Context appContext;

    public static List<ProductGroup> ALL_PRODUCT_GROUPS;
    public static List<Folder> ALL_FOLDERS;


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

    /**
     * Метод фильтрует переданный список чертежей по статусу
     * @param items List<Draft>
     */
    public static void filterList(List<Draft> items) {
        if(items.isEmpty()) return;
        Iterator<Draft> i = items.iterator();
        while (i.hasNext()) {
            Draft d = i.next();
            EDraftStatus status = EDraftStatus.getStatusById(d.getStatus());
            if (status != null) {
                if ((status.equals(EDraftStatus.VALID) && !ThisApplication.showValid) ||
                        (status.equals(EDraftStatus.CHANGED) && !ThisApplication.showChanged) ||
                        (status.equals(EDraftStatus.ANNULLED) && !ThisApplication.showAnnulled))
                    i.remove();
                if(SOLID_EXTENSIONS.contains(d.getExtension()) && !SHOW_SOLID_FILES)
                    i.remove();
            }

        }
    }

    /**
     * Универсальный метод преобразования списка ArrayList<Item>  лист ArrayList<String>
     * @param itemList
     * @param <T>
     * @return
     */
    public static <T extends Item> ArrayList<String> convertToStringArray(ArrayList<T> itemList) {
        ArrayList<String> stringList = new ArrayList<>();
        for(T d : itemList){
            stringList.add(String.valueOf(d.getId()));
        }
        return stringList;
    }

    /**
     * Компаратор сравнивает usefulString объекта
     */
    public static Comparator<Item> usefulStringComparator() {
        return (o1, o2) -> {
            String str1 = o1.toUsefulString();
            String str2 = o2.toUsefulString();
            return str1.compareTo(str2);
        };
    }
}
