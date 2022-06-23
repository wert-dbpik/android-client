package ru.wert.bazapik_mobile.dataPreloading;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.DraftApiInterface;
import ru.wert.bazapik_mobile.data.api_interfaces.FolderApiInterface;
import ru.wert.bazapik_mobile.data.api_interfaces.PassportApiInterface;
import ru.wert.bazapik_mobile.data.api_interfaces.ProductGroupApiInterface;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.ProductGroup;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.data.serviceQUICK.DraftQuickService;
import ru.wert.bazapik_mobile.data.serviceQUICK.PassportQuickService;
import ru.wert.bazapik_mobile.data.servicesREST.DraftService;
import ru.wert.bazapik_mobile.data.servicesREST.FileService;
import ru.wert.bazapik_mobile.data.servicesREST.PassportService;
import ru.wert.bazapik_mobile.organizer.OrganizerActivity;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

import static ru.wert.bazapik_mobile.ThisApplication.ALL_PRODUCT_GROUPS;

public class DataLoadingAsyncTask extends AsyncTask<Void, Void, Void> {

    private Activity activity;

    public DataLoadingAsyncTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        try {

            //ЧЕРТЕЖИ
            DraftApiInterface draftApi = RetrofitClient.getInstance().getRetrofit().create(DraftApiInterface.class);
            Call<List<Draft>> draftCall = draftApi.getAll();
            List<Draft> allDrafts = draftCall.execute().body();
            allDrafts.sort(ThisApplication.usefulStringComparator());
            ThisApplication.ALL_DRAFTS = allDrafts;

            //ПАССПОРТА
            PassportApiInterface pasApi = RetrofitClient.getInstance().getRetrofit().create(PassportApiInterface.class);
            Call<List<Passport>> passCall = pasApi.getAll();
            List<Passport> allPassports = passCall.execute().body();
            allPassports.sort(ThisApplication.usefulStringComparator());
            ThisApplication.ALL_PASSPORTS = allPassports;

            //ГРУППЫ ИЗДЕЛИЙ
            ProductGroupApiInterface pgApi = RetrofitClient.getInstance().getRetrofit().create(ProductGroupApiInterface.class);
            Call<List<ProductGroup>> pgCall = pgApi.getAll();
            List<ProductGroup> allProductGroups = pgCall.execute().body();
            allProductGroups.sort(ThisApplication.usefulStringComparator());
            ThisApplication.ALL_PRODUCT_GROUPS = allProductGroups;

            //КОМПЛЕКТЫ
            FolderApiInterface folderApi = RetrofitClient.getInstance().getRetrofit().create(FolderApiInterface.class);
            Call<List<Folder>> folderCall = folderApi.getAll();
            List<Folder> allFolders = folderCall.execute().body();
            allFolders.sort(ThisApplication.usefulStringComparator());
            ThisApplication.ALL_FOLDERS = allFolders;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);

        Intent intent = new Intent(activity, OrganizerActivity.class);
        activity.startActivity(intent);
    }
}
