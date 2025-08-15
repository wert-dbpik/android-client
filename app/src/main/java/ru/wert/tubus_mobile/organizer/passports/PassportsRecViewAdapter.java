package ru.wert.tubus_mobile.organizer.passports;

import static ru.wert.tubus_mobile.ThisApplication.LIST_OF_ALL_PASSPORTS;
import static ru.wert.tubus_mobile.viewer.ViewerActivity.$ALL_DRAFTS;
import static ru.wert.tubus_mobile.viewer.ViewerActivity.$CURRENT_DRAFT;
import static ru.wert.tubus_mobile.viewer.ViewerActivity.$CURRENT_PASSPORT;

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

import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.tubus_mobile.R;
import ru.wert.tubus_mobile.ThisApplication;
import ru.wert.tubus_mobile.constants.Consts;
import ru.wert.tubus_mobile.data.api_interfaces.DraftApiInterface;
import ru.wert.tubus_mobile.data.interfaces.Item;
import ru.wert.tubus_mobile.data.models.Draft;
import ru.wert.tubus_mobile.data.models.Passport;
import ru.wert.tubus_mobile.data.retrofit.RetrofitClient;
import ru.wert.tubus_mobile.organizer.OrganizerActivity;
import ru.wert.tubus_mobile.organizer.OrganizerRecViewAdapter;
import ru.wert.tubus_mobile.organizer.history.HistoryManager;
import ru.wert.tubus_mobile.viewer.ViewerActivity;
import ru.wert.tubus_mobile.warnings.AppWarnings;

public class PassportsRecViewAdapter extends RecyclerView.Adapter<PassportsRecViewAdapter.ViewHolder> implements OrganizerRecViewAdapter {

    @Getter private final List<Item> data;
    private final LayoutInflater inflater;
    private passportsClickListener clickListener;
    private final Context context;
    @Setter@Getter private int selectedPosition = RecyclerView.NO_POSITION;
    private PassportsFragment fragment;
    private final HistoryManager historyManager;

    /**
     * Конструктор получает на входе список элементов List<P>
     * Для отображения в RecycleView список преобразуется в List<String>
     * @param context Context
     */
    public PassportsRecViewAdapter(PassportsFragment fragment, Context context, List<Item> items,
                                   HistoryManager historyManager) {
        this.fragment = fragment;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.data = items;
        this.historyManager = historyManager;
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

        View itemView = holder.itemView.findViewById(R.id.selectedLinearLayout);

        itemView.setBackgroundColor((position == selectedPosition) ?
                context.getColor(R.color.colorPrimary) : //Цвет выделения
                context.getColor(R.color.colorPrimaryDark)); //Цвет фона

        Passport passport = (Passport) data.get(position);

        //Децимальный номер
        String numberText;

        if(Consts.HIDE_PREFIXES)
            numberText = passport.getNumber();
        else {
            if(passport.getPrefix().getName().equals("-"))
                numberText = passport.getNumber();
            else
                numberText = passport.getPrefix().getName() + "." + passport.getNumber();
        }

        holder.mNumber.setText(numberText);

        //Наименование
        holder.mName.setText(passport.getName());

        //Пиктограмма чертежа
        holder.mShowDraft.setImageDrawable(
                ContextCompat.getDrawable(this.inflater.getContext(), R.drawable.draft));
        //Через жопу, потомучто draftIds не сереализуется вместе с Passport в списке Folder
        if (LIST_OF_ALL_PASSPORTS.get(LIST_OF_ALL_PASSPORTS.indexOf(passport)).getDraftIds().isEmpty())
//        if(passport.getDraftIds().isEmpty())
            holder.mShowDraft.setBackgroundColor(Color.BLACK);
        else {
            holder.mShowDraft.setBackgroundColor(Color.WHITE);
            //При нажатии на кнопку создаем активити ViewerActivity, передаем ArrayList<String>, состоящий из id чертежей пасспорта
            holder.mShowDraft.setOnClickListener(e -> {
                selectedPosition = holder.getBindingAdapterPosition();
                if(selectedPosition == RecyclerView.NO_POSITION) return; //Если ткнули в путое место
                itemView.setBackgroundColor(context.getColor(R.color.colorPrimary)); //Выделяем строку
                notifyDataSetChanged(); //Сбрасываем остальные выделения

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

                    Intent intent = new Intent(context, ViewerActivity.class);
                    intent.putExtra($ALL_DRAFTS, foundDrafts);
                    intent.putExtra($CURRENT_DRAFT, foundDrafts.get(0));
                    intent.putExtra($CURRENT_PASSPORT, passport);
                    context.startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<List<Draft>> call, Throwable t) {
                AppWarnings.showAlert_NoConnection(context);
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
        selectedPosition = RecyclerView.NO_POSITION;
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
            ((OrganizerActivity)fragment.getActivity()).getEditTextSearch().clearFocus();

            if (getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;

            selectedPosition = getBindingAdapterPosition();
            view.findViewById(R.id.selectedLinearLayout)
                    .setBackgroundColor(context.getColor(R.color.colorPrimary));

            // Добавляем запись в историю при клике
            Passport passport = (Passport) data.get(getBindingAdapterPosition());
            if (historyManager != null) {
                historyManager.addToHistory(passport.toUsefulString());
            }

            if (clickListener != null)
                clickListener.onItemClick(view, getBindingAdapterPosition());

            notifyDataSetChanged();

        }
    }

    /**
     * Возвращает Item в позиции клика int
     * @param index int
     * @return P extends Item
     */
    public Passport getItem(int index) {
        Passport passport = (Passport) data.get(index);
        return passport ;
    }

    public void setClickListener(passportsClickListener passportsClickListener) {
        this.clickListener = passportsClickListener;
    }

    public interface passportsClickListener {
        void onItemClick(View view, int position);
    }

}

