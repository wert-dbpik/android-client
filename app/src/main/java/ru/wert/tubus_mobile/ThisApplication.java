package ru.wert.tubus_mobile;

import static ru.wert.tubus_mobile.constants.Consts.HIDE_PREFIXES;
import static ru.wert.tubus_mobile.constants.Consts.SEND_ERROR_REPORTS;
import static ru.wert.tubus_mobile.constants.Consts.SHOW_SOLID_FILES;
import static ru.wert.tubus_mobile.constants.Consts.USE_APP_KEYBOARD;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.acra.ACRA;
import org.acra.BuildConfig;
import org.acra.config.Configuration;
import org.acra.config.CoreConfiguration;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.DialogConfigurationBuilder;
import org.acra.data.StringFormat;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

import ru.wert.tubus_mobile.acra.ACRA_config;
import ru.wert.tubus_mobile.data.enums.EDraftStatus;
import ru.wert.tubus_mobile.data.interfaces.Item;
import ru.wert.tubus_mobile.data.models.Draft;
import ru.wert.tubus_mobile.data.models.Folder;
import ru.wert.tubus_mobile.data.models.Passport;
import ru.wert.tubus_mobile.data.models.ProductGroup;
import ru.wert.tubus_mobile.data.models.Room;
import ru.wert.tubus_mobile.data.models.User;
import ru.wert.tubus_mobile.data.servicesREST.DraftService;
import ru.wert.tubus_mobile.data.servicesREST.FileService;
import ru.wert.tubus_mobile.data.servicesREST.FolderService;
import ru.wert.tubus_mobile.data.servicesREST.PassportService;

public class ThisApplication extends Application {

    //Версия приложения
    public static String APPLICATION_VERSION = "1.5";
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
    public static PassportService PASSPORT_SERVICE;
    public static DraftService DRAFT_SERVICE;

    public static String DATA_BASE_URL;

    static SharedPreferences settings;
    static SharedPreferences.Editor editor;
    private static Context appContext;
    public static List<User> LIST_OF_ALL_USERS;
    public static List<Room> LIST_OF_ALL_ROOMS;
    public static List<ProductGroup> LIST_OF_ALL_PRODUCT_GROUPS;
    public static List<Draft> LIST_OF_ALL_DRAFTS;
    public static List<Folder> LIST_OF_ALL_FOLDERS;
    public static List<Passport> LIST_OF_ALL_PASSPORTS;
    public static long LAST_SYNC_TIME = 0;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        settings = getSharedPreferences("DBPIKSettings", MODE_PRIVATE);

        ACRA_config.create(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        editor = settings.edit();
        ThisApplication.appContext = this.getApplicationContext();
        APPLICATION_VERSION = getResources().getString(R.string.app_version);

        String userName = ThisApplication.getProp("USER_NAME");
        ACRA.getErrorReporter().putCustomData("crash_user_name", userName);
        ACRA.getErrorReporter().putCustomData("crash_device", "ANDROID");
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
            case "SEND_ERROR_REPORTS":
                return settings.getString(name, "true");
            case "USE_APP_KEYBOARD":
                return settings.getString(name, "true");
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
        SEND_ERROR_REPORTS = Boolean.parseBoolean(getProp("SEND_ERROR_REPORTS"));
        USE_APP_KEYBOARD = Boolean.parseBoolean(getProp("USE_APP_KEYBOARD"));

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
     * Метод парсит строку формата "yyyy-MM-dd'T'HH:mm:ss" в HH:mm
     */
    public static  String parseStringToTime(String dateString){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        try {
            Date date = format.parse(dateString);
            SimpleDateFormat myFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
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

    /**
     * Метод возвращает черашнее время в формате "yyyy-MM-dd'T'HH:mm:ss"
     */
    public static String getYesterdayTime(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return df.format(yesterday);
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
