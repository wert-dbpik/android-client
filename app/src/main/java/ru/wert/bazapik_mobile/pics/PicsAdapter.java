package ru.wert.bazapik_mobile.pics;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.data.api_interfaces.FileApiInterface;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Pic;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;

public class PicsAdapter extends RecyclerView.Adapter<PicsAdapter.ViewHolder>{

    private final String TAG = "PicsAdapter";
    private final LayoutInflater inflater;
    private List<Pic> data;
    private final Context context;
    private final Activity activity;

    public PicsAdapter(Context context, List<Pic> data) {
        this.context = context;
        this.data = new ArrayList<>(data);
        this.inflater = LayoutInflater.from(context);
        this.activity = (Activity) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recview_pic_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pic pic = data.get(position);
        FileApiInterface api = RetrofitClient.getInstance().getRetrofit().create(FileApiInterface.class);
        String picName = pic.getId() + "." + pic.getExtension();
        Call<ResponseBody> call = api.download("pics", picName);
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    assert response.body() != null;
                    new Thread(()->{
                        try {
                            byte [] bt = response.body().bytes();
                            activity.runOnUiThread(()->{
                                Bitmap bmp = BitmapFactory.decodeByteArray(bt, 0 , bt.length);
                                holder.ivPicture.setAdjustViewBounds(true);
                                holder.ivPicture.setImageBitmap(bmp);
                            });
                        } catch (IOException e) {
                            Log.e(TAG, "Ошибка декодирования файла: " + e.getMessage());
                        }
                    }).start();

                } else {
                    Log.e(TAG, "Couldn't download picture");
                    Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.noimage);
                    holder.ivPicture.setAdjustViewBounds(true);
                    holder.ivPicture.setImageBitmap(bmp);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Couldn't download picture");
                Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.noimage);
                holder.ivPicture.setImageBitmap(bmp);
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView ivPicture;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPicture = itemView.findViewById(R.id.ivPicture);
        }
    }

    /**
     * Обновляет отображаемые данные
     *
     * @param items List<P>
     */
    public void changeListOfItems(List items) {
        data = new ArrayList<Pic>(items);
        notifyDataSetChanged();
    }
}
