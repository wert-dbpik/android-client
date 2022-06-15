package ru.wert.bazapik_mobile.search;

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
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.organizer.OrganizerRecViewAdapter;
import ru.wert.bazapik_mobile.viewer.ViewerActivity;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

public class DraftsRecViewAdapter<P extends Item> extends RecyclerView.Adapter<DraftsRecViewAdapter<P>.ViewHolder> implements OrganizerRecViewAdapter {

    private final List<P> mData;
    private final LayoutInflater mInflater;
    private ItemDraftsClickListener mClickListener;
    private final Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;

    /**
     * Конструктор получает на входе список элементов List<P>
     * Для отображения в RecycleView список преобразуется в List<String>
     * @param context Context
     */
    public DraftsRecViewAdapter(Context context, List<P> items) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = items;
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
        View view = mInflater.inflate(R.layout.recview_draft_row, parent, false);
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

        P item = mData.get(position);
        //Децимальный номер
        String text;
        if(item instanceof Passport){
            if(Consts.HIDE_PREFIXES)
                text = ((Passport) item).getNumber();
            else
                text = ((Passport) item).getPrefix().getName() + "." + ((Passport) item).getNumber();
        } else
            text = item.toUsefulString();

        holder.mNumber.setText(text);

        //Наименование
        holder.mName.setText(item.getName());

        //Пиктограмма чертежа
        holder.mShowDraft.setImageDrawable(
                ContextCompat.getDrawable(this.mInflater.getContext(), R.drawable.draft));
        if (((Passport) item).getDraftIds() == null || ((Passport) item).getDraftIds().isEmpty())
            holder.mShowDraft.setBackgroundColor(Color.BLACK);
        else {
            holder.mShowDraft.setBackgroundColor(Color.WHITE);
            //При нажатии на кнопку создаем активити ViewerActivity, передаем ArrayList<String>, состоящий из id чертежей пасспорта
            holder.mShowDraft.setOnClickListener(e -> {
                openViewer((Passport) item);
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
        return mData.size();
    }

    /**
     * Обновляет отображаемые данные
     * @param items List<P>
     */
    public void changeListOfItems(List items){
        mData.clear();
        mData.addAll(items);
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

            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());

            notifyDataSetChanged();

        }
    }

    /**
     * Возвращает Item в позиции клика int
     * @param index int
     * @return P extends Item
     */
    public P getItem(int index) {
        return mData.get(index);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemDraftsClickListener itemDraftsClickListener) {
        this.mClickListener = itemDraftsClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemDraftsClickListener {
        void onItemClick(View view, int position);
    }

}

