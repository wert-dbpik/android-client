package ru.wert.bazapik_mobile.dataPreloading;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.data.serviceQUICK.DraftQuickService;
import ru.wert.bazapik_mobile.data.serviceQUICK.PassportQuickService;
import ru.wert.bazapik_mobile.data.servicesREST.DraftService;
import ru.wert.bazapik_mobile.data.servicesREST.PassportService;

public class DataLoader {


    public void load(Context context) throws Exception{


        //Создается PassportService, и затем PassportQuickService
        new PassportService(context);
        new PassportQuickService(context);

        //Создается DraftService, и затем DraftQuickService
        new DraftService();
        new DraftQuickService();


    }

    /**
     * Задача по загрузке данных из БД.
     * Загружаются все пасспорта(Passport), чертежи(Draft)
     *Ппосле загрузки данных открывается главная страница приложения
     */
    public static class LoadDataTask extends AsyncTask<Void, Void, Void> {
        private final WeakReference<Context> contextRef;

        public LoadDataTask(Context contextRef) {
            this.contextRef = new WeakReference<>(contextRef);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(contextRef.get(),
                    "Загружаем данные ...", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
//            PassportQuickService.reload();
//            DraftQuickService.reload();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(contextRef.get(),
                    "Данные загружены", Toast.LENGTH_LONG).show();
        }

    }
}
