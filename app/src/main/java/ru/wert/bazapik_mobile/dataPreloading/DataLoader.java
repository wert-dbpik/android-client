package ru.wert.bazapik_mobile.dataPreloading;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.MainActivity;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.FolderApiInterface;
import ru.wert.bazapik_mobile.data.api_interfaces.ProductGroupApiInterface;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.ProductGroup;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.servicesREST.FileService;
import ru.wert.bazapik_mobile.main.BaseActivity;
import ru.wert.bazapik_mobile.data.serviceQUICK.DraftQuickService;
import ru.wert.bazapik_mobile.data.serviceQUICK.PassportQuickService;
import ru.wert.bazapik_mobile.data.servicesREST.DraftService;
import ru.wert.bazapik_mobile.data.servicesREST.PassportService;
import ru.wert.bazapik_mobile.organizer.FoldersFragment;
import ru.wert.bazapik_mobile.organizer.FoldersRecViewAdapter;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import static ru.wert.bazapik_mobile.ThisApplication.ALL_PRODUCT_GROUPS;

public class DataLoader {


    public void load(Activity activity) throws Exception{

        FileService.getInstance();

        //Создается PassportService, и затем PassportQuickService
        new PassportService(activity);
        new PassportQuickService(activity);

        //Создается DraftService, и затем DraftQuickService
        new DraftService();
        new DraftQuickService();

        ProductGroupApiInterface pgApi = RetrofitClient.getInstance().getRetrofit().create(ProductGroupApiInterface.class);
        Call<List<ProductGroup>> pgCall = pgApi.getAll();
        pgCall.enqueue(new Callback<List<ProductGroup>>() {
            @Override
            public void onResponse(Call<List<ProductGroup>> call, Response<List<ProductGroup>> response) {
                if (response.isSuccessful()) {
                    ThisApplication.ALL_PRODUCT_GROUPS = response.body();
                    //Отсортируем по алфавиту
                    ALL_PRODUCT_GROUPS.sort(ThisApplication.usefulStringComparator());
                }
            }

            @Override
            public void onFailure(Call<List<ProductGroup>> call, Throwable t) {
                activity.runOnUiThread(() -> {
                    new WarningDialog1().show(activity, "Внимание!",
                            "Не удалось загрузить данные, возможно сервер не доступен. Приложение будет закрыто!");
                });
            }
        });

        //КОМПЛЕКТЫ
        FolderApiInterface folderApi = RetrofitClient.getInstance().getRetrofit().create(FolderApiInterface.class);
        Call<List<Folder>> folderCall = folderApi.getAll();
        folderCall.enqueue(new Callback<List<Folder>>() {
            @Override
            public void onResponse(Call<List<Folder>> call, Response<List<Folder>> response) {
                if (response.isSuccessful()) {
                    ThisApplication.ALL_FOLDERS = response.body();
                    //Отсортируем по алфавиту
                    ThisApplication.ALL_FOLDERS.sort(ThisApplication.usefulStringComparator());
                }
            }

            @Override
            public void onFailure(Call<List<Folder>> call, Throwable t) {
                activity.runOnUiThread(() -> {
                    new WarningDialog1().show(activity, "Внимание!",
                            "Не удалось загрузить данные, возможно сервер не доступен. Приложение будет закрыто!");

                });
            }
        });


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
