package ru.wert.tubus_mobile.dataPreloading;

import android.app.Activity;

import ru.wert.tubus_mobile.data.servicesREST.DraftService;
import ru.wert.tubus_mobile.data.servicesREST.FileService;
import ru.wert.tubus_mobile.data.servicesREST.PassportService;

public class DataLoader {


    public void load(Activity activity) throws Exception{

        FileService.getInstance();

        //Создается PassportService, и затем PassportQuickService
        new PassportService(activity);
//        new PassportQuickService(activity);

        //Создается DraftService, и затем DraftQuickService
        new DraftService();
//        new DraftQuickService();

        DataLoadingAsyncTask task = new DataLoadingAsyncTask(activity);
        task.execute();
    }

}
