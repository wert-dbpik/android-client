package ru.wert.bazapik_mobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import ru.wert.bazapik_mobile.constants.Consts;
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
import ru.wert.bazapik_mobile.organizer.passports.PassportsRecViewAdapter;

import static ru.wert.bazapik_mobile.constants.Consts.HIDE_PREFIXES;
import static ru.wert.bazapik_mobile.constants.Consts.SHOW_SOLID_FILES;

public class ThisApplication extends Application {

    //Версия приложения
    public static final String APPLICATION_VERSION = "1.2.1";
    public static boolean APP_VERSION_NOTIFICATION_SHOWN = false;
    public static String APPLICATION_VERSION_AVAILABLE;

    //Разрешения
    public static final int REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE = 0;
    public static final int REQUEST_CODE_PERMISSION_CAMERA = 1;

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
//    public static FolderQuickService FOLDER_QUICK_SERVICE;

    public static PassportService PASSPORT_SERVICE;
//    public static PassportQuickService PASSPORT_QUICK_SERVICE;

    public static DraftService DRAFT_SERVICE;
//    public static DraftQuickService DRAFT_QUICK_SERVICE;

    public static String SEARCH_TEXT = "";
    public static PassportsRecViewAdapter ADAPTER;
    public static String DATA_BASE_URL;

    static SharedPreferences settings;
    static SharedPreferences.Editor editor;
    private static Context appContext;

    public static List<ProductGroup> ALL_PRODUCT_GROUPS;
    public static List<Folder> ALL_FOLDERS;
    public static List<Draft> ALL_DRAFTS;
    public static List<Passport> ALL_PASSPORTS;
    private final Consts consts = new Consts();//Чтобы класс не удалялся

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
                return settings.getString(name, "192.168.2.132");
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
     * Универсальный метод преобразования списка ArrayList<Item> в лист ArrayList<String> из id
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

    /**
     * Компаратор сравнивает чертеж по НОМЕРУ -> ТИПУ -> СТРАНИЦЕ
     */
    public static Comparator<Draft> draftsComparatorAssemblesFirst() {
        return (o1, o2) -> {

            String number1 = o1.getPassport().getNumber();
            String number2 = o2.getPassport().getNumber();

            //Чем больше нулей в начале номера, тем выше в списке
            if (number1.startsWith("4") ||
                    number1.startsWith("9"))  number1 = "0000".concat(number1);
            else if (number1.startsWith("3")) number1 = "000".concat(number1);
            else if (number1.startsWith("6")) number1 = "00".concat(number1);
            else if (number1.startsWith("7")) number1 = "0".concat(number1);

            if (number2.startsWith("4") ||
                    number2.startsWith("9"))  number2 = "0000".concat(number2);
            else if (number2.startsWith("3")) number2 = "000".concat(number2);
            else if (number2.startsWith("6")) number2 = "00".concat(number2);
            else if (number2.startsWith("7")) number2 = "0".concat(number2);

            //Сборки должны быть первыми
            int result = number1.compareTo(number2);

            if (result == 0) {
                //Сравниваем тип чертежа
                result = o1.getDraftType() - o2.getDraftType();
                if (result == 0) {
                    //Сравниваем номер страницы
                    result = o1.getPageNumber() - o2.getPageNumber();
                }
            }
            return result;
        };
    }

    /**
     * Компаратор сравнивает чертеж по НОМЕРУ -> ТИПУ -> СТРАНИЦЕ
     * Реверсивность относится только к НОМЕРУ
     */
    public static Comparator<Draft> draftsComparatorDetailFirst() {
        return (o1, o2) -> {
            //Сравниваем номер чертежа, причем 745 должен быть выше, чем 469
            String number1 = o1.getPassport().getNumber();
            String number2 = o2.getPassport().getNumber();

            //Чем больше нулей в начале номера, тем выше в списке
            if (number1.startsWith("4") ||
                    number1.startsWith("9"))  number1 = "0".concat(number1);
            else if (number1.startsWith("3")) number1 = "00".concat(number1);
            else if (number1.startsWith("6")) number1 = "000".concat(number1);
            else if (number1.startsWith("7")) number1 = "0000".concat(number1);

            if (number2.startsWith("4") ||
                    number2.startsWith("9"))  number2 = "0".concat(number2);
            else if (number2.startsWith("3")) number2 = "00".concat(number2);
            else if (number2.startsWith("6")) number2 = "000".concat(number2);
            else if (number2.startsWith("7")) number2 = "0000".concat(number2);

            int result = number1.compareTo(number2);

            if (result == 0) {
                //Сравниваем тип чертежа
                result = o1.getDraftType() - o2.getDraftType();
                if (result == 0) {
                    //Сравниваем номер страницы
                    result = o1.getPageNumber() - o2.getPageNumber();
                }
            }
            return result;
        };
    }

    public static Comparator<Passport> passportsComparatorDetailFirst() {
        return (o1, o2) -> {
            //Сравниваем номер чертежа, причем 745 должен быть выше, чем 469
            String number1 = o1.getNumber();
            String number2 = o2.getNumber();

            //Чем больше нулей в начале номера, тем выше в списке
            if (number1.startsWith("4") ||
                    number1.startsWith("9"))  number1 = "0".concat(number1);
            else if (number1.startsWith("3")) number1 = "00".concat(number1);
            else if (number1.startsWith("6")) number1 = "000".concat(number1);
            else if (number1.startsWith("7")) number1 = "0000".concat(number1);

            if (number2.startsWith("4") ||
                    number2.startsWith("9"))  number2 = "0".concat(number2);
            else if (number2.startsWith("3")) number2 = "00".concat(number2);
            else if (number2.startsWith("6")) number2 = "000".concat(number2);
            else if (number2.startsWith("7")) number2 = "0000".concat(number2);

            return number1.compareTo(number2);

        };
    }

    public static Comparator<Passport> passportsComparatorAssemblesFirst() {
        return (o1, o2) -> {

            String number1 = o1.getNumber();
            String number2 = o2.getNumber();

            //Чем больше нулей в начале номера, тем выше в списке
            if (number1.startsWith("4") ||
                    number1.startsWith("9"))  number1 = "0000".concat(number1);
            else if (number1.startsWith("3")) number1 = "000".concat(number1);
            else if (number1.startsWith("6")) number1 = "00".concat(number1);
            else if (number1.startsWith("7")) number1 = "0".concat(number1);

            if (number2.startsWith("4") ||
                    number2.startsWith("9"))  number2 = "0000".concat(number2);
            else if (number2.startsWith("3")) number2 = "000".concat(number2);
            else if (number2.startsWith("6")) number2 = "00".concat(number2);
            else if (number2.startsWith("7")) number2 = "0".concat(number2);

            //Сборки должны быть первыми
            return number1.compareTo(number2);

        };
    }

    /**
     * Метод парсит строку формата "yyyy-MM-dd'T'HH:mm:ss" в необходимый фотрмат
     */
    public static  String parseStringToDate(String dateString){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        try {
            Date date = format.parse(dateString);
            SimpleDateFormat myFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            return myFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    /**
     * Метод парсит строку формата "yyyy-MM-dd'T'HH:mm:ss" в необходимый фотрмат
     */
    public static  String parseStringToDateAndTime(String dateString){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        try {
            Date date = format.parse(dateString);
            SimpleDateFormat myFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            return myFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    /**
     * Метод возвращает текущее время в формате "yyyy-MM-dd'T'HH:mm:ss"
     */
    public static String getCurrentTime(){
        Date date = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return df.format(date);
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static List<Uri> clipDataToList(ClipData clipData){
        List<Uri> list = new ArrayList<>();
        for(int i = 0 ; i < clipData.getItemCount(); i++){
            list.add(clipData.getItemAt(i).getUri());
        }
        return list;

    }

}
