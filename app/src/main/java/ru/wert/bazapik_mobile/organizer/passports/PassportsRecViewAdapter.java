package ru.wert.bazapik_mobile.organizer.passports;

import static ru.wert.bazapik_mobile.ThisApplication.ALL_PASSPORTS;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.constants.Consts;
import ru.wert.bazapik_mobile.data.api_interfaces.DraftApiInterface;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.organizer.OrganizerRecViewAdapter;
import ru.wert.bazapik_mobile.viewer.ViewerActivity;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

public class PassportsRecViewAdapter extends RecyclerView.Adapter<PassportsRecViewAdapter.ViewHolder> implements OrganizerRecViewAdapter {

    private final List<Passport> data;
    private final LayoutInflater inflater;
    private passportsClickListener clickListener;
    private final Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;

    /**
     * Конструктор получает на входе список элементов List<P>
     * Для отображения в RecycleView список преобразуется в List<String>
     * @param context Context
     */
    public PassportsRecViewAdapter(Context context, List<Passport> items) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.data = items;
    }

    /**
     * Создает новый view из имеющегося xml файла с помощью метода inflate класса LayoutInflater
     * @param parent ViewGroup
     * @param viewType int
     * @return новый view типа ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recview_draft_row, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Связывает каждый ViewHolder с позицией в списке в позиции int с данными
     * @param holder ViewHolder
     * @param position int
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (selectedPosition != RecyclerView.NO_POSITION) //Если ничего не выделенно
            holder.itemView.findViewById(R.id.selected_position)
                    .setBackgroundColor((position == selectedPosition) ?
                            context.getColor(R.color.colorPrimary) : //Цвет выделения
                            context.getColor(R.color.colorPrimaryDark)); //Цвет фона

        Passport passport = data.get(position);
        //Децимальный номер
        String text;

        if(Consts.HIDE_PREFIXES)
            text = passport.getNumber();
        else
            text = passport.getPrefix().getName() + "." + ((Passport) passport).getNumber();


        holder.mNumber.setText(text);

        //Наименование
        holder.mName.setText(passport.getName());

        //Пиктограмма чертежа
        holder.mShowDraft.setImageDrawable(
                ContextCompat.getDrawable(this.inflater.getContext(), R.drawable.draft));
        //Через жопу, потомучто draftIds не сереализуется вместе с Passport в списке Folder
        if (ALL_PASSPORTS.get(ALL_PASSPORTS.indexOf(passport)).getDraftIds().isEmpty())
            holder.mShowDraft.setBackgroundColor(Color.BLACK);
        else {
            holder.mShowDraft.setBackgroundColor(Color.WHITE);
            //При нажатии на кнопку создаем активити ViewerActivity, передаем ArrayList<String>, состоящий из id чертежей пасспорта
            holder.mShowDraft.setOnClickListener(e -> {
                openViewer(passport);
            });
        }
    }

    /**
     * Открываем окно с доступными чертежами
     */
    private void openViewer(Passport passport){
        DraftApiInterface api = RetrofitClient.getInstance().getRetrofit().create(DraftApiInterface.class);
        Call<List<Draft>> call =  api.getByPassportId(passport.getId());
        call.enqueue(new Callback<List<Draft>>() {
            @Override
            public void onResponse(Call<List<Draft>> call, Response<List<Draft>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    ArrayList<Draft> foundDrafts = new ArrayList<>(response.body());
                    ThisApplication.filterList(foundDrafts); //Фильтруем
                    ArrayList<String> stringList = ThisApplication.convertToStringArray(foundDrafts);

                    Intent intent = new Intent(context, ViewerActivity.class);
                    intent.putStringArrayListExtra("DRAFTS", stringList);
                    intent.putExtra("DRAFT_ID", String.valueOf(foundDrafts.get(0).getId()));
                    context.startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<List<Draft>> call, Throwable t) {
                new WarningDialog1().show(context, "Внимание!","Проблемы на линии!");
            }

        });

    }

    /**
     * Возвращает общее количество элементов в списке List<P>
     * @return int
     */
    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * Обновляет отображаемые данные
     * @param items List<P>
     */
    public void changeListOfItems(List items){
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    /**
     * Вложенный класс, описывающий и создающий ограниченной количество ViewHolder
     *
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mNumber;
        TextView mName;
        ImageButton mShowDraft; //кнопка в виде чертежика


        ViewHolder(View itemView) {
            super(itemView);
            mNumber = itemView.findViewById(R.id.number);
            mName = itemView.findViewById(R.id.name);
            mShowDraft = itemView.findViewById(R.id.btnShowDraftsNow);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

            selectedPosition = getAdapterPosition();
            view.findViewById(R.id.selected_position)
                    .setBackgroundColor(context.getColor(R.color.colorPrimary));

            if (clickListener != null)
                clickListener.onItemClick(view, getAdapterPosition());

            notifyDataSetChanged();

        }
    }

    /**
     * Возвращает Item в позиции клика int
     * @param index int
     * @return P extends Item
     */
    public Passport getItem(int index) {
        return data.get(index);
    }

    // allows clicks events to be caught
    public void setClickListener(passportsClickListener passportsClickListener) {
        this.clickListener = passportsClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface passportsClickListener {
        void onItemClick(View view, int position);
    }

}

