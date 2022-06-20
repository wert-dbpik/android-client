package ru.wert.bazapik_mobile.organizer.folders;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.api_interfaces.DraftApiInterface;
import ru.wert.bazapik_mobile.data.api_interfaces.FolderApiInterface;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.ProductGroup;
import ru.wert.bazapik_mobile.data.retrofit.RetrofitClient;
import ru.wert.bazapik_mobile.organizer.OrganizerRecViewAdapter;
import ru.wert.bazapik_mobile.viewer.ViewerActivity;

import static ru.wert.bazapik_mobile.ThisApplication.ALL_DRAFTS;

public class FoldersRecViewAdapter extends RecyclerView.Adapter<FoldersRecViewAdapter.ViewHolder> implements OrganizerRecViewAdapter {

    private final List<Item> mData;
    private final LayoutInflater mInflater;
    private ItemFolderClickListener mClickListener;
    private final Context context;
    @Getter@Setter private int selectedPosition = RecyclerView.NO_POSITION;
    private FoldersFragment fragment;

    /**
     * Конструктор получает на входе список элементов List<P>
     * Для отображения в RecycleView список преобразуется в List<String>
     * @param context Context
     */
    public FoldersRecViewAdapter(FoldersFragment fragment, Context context, List<Item> items) {
        this.fragment = fragment;
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
        View view = mInflater.inflate(R.layout.recview_folder_row, parent, false);

        return new ViewHolder(view);
    }

    /**
     * Связывает каждый ViewHolder с позицией в списке в позиции int с данными
     * @param holder ViewHolder
     * @param position int
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        View itemView = holder.itemView.findViewById(R.id.selected_position);

        if (selectedPosition != RecyclerView.NO_POSITION) //Если ничего не выделенно
        itemView.setBackgroundColor((position == selectedPosition) ?
                            context.getColor(R.color.colorPrimary) : //Цвет выделения
                            context.getColor(R.color.colorPrimaryDark)); //Цвет фона

        Item item = mData.get(position);

        if(item instanceof ProductGroup){

            holder.llFolder.removeView(holder.showFolderMenu);

            if(!fragment.getCurrentProductGroupId().equals(1L) && position == 0){
                String str = "< . . . . .>";
                holder.numberAndName.setText(str);
                holder.folder.setImageDrawable(context.getDrawable(R.drawable.backward256));

            } else {
                String str = ((ProductGroup)item).getName();
                holder.numberAndName.setText(str);
                holder.folder.setImageDrawable(context.getDrawable(R.drawable.folder256));
            }

//            holder.folder.setOnClickListener(e->{
//
//            });
        }

        if(item instanceof Folder){
            String str = ((Folder)item).getName();
            holder.numberAndName.setText(str);
            holder.folder.setImageDrawable(context.getDrawable(R.drawable.folders256));
            holder.folder.setClickable(false);
            holder.showFolderMenu.setOnClickListener(v->{
                Folder folder = (Folder) mData.get(position);
                openDraftsInFolder(folder);
            });
        }

    }

    private void openDraftsInFolder(Folder folder){
        ArrayList<Draft> foundDrafts = new ArrayList<>();
        for(Draft d : ALL_DRAFTS){
            if(d.getFolder().equals(folder))
                foundDrafts.add(d);
        }
        ThisApplication.filterList(foundDrafts); //Фильтруем
        foundDrafts.sort(ThisApplication.draftsReversComparator());

        Intent intent = new Intent(context, ViewerActivity.class);
        ArrayList<String> stringList = ThisApplication.convertToStringArray(foundDrafts);
        intent.putStringArrayListExtra("DRAFTS", stringList);
        intent.putExtra("DRAFT_ID", String.valueOf(foundDrafts.get(0).getId()));
        context.startActivity(intent);
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
        TextView numberAndName;
        ImageButton folder; //кнопка в виде чертежика
        ImageButton showFolderMenu; //три точки
        LinearLayout llFolder;

        ViewHolder(View itemView) {
            super(itemView);
            numberAndName = itemView.findViewById(R.id.number_and_name);
            folder = itemView.findViewById(R.id.btnFolder);
            showFolderMenu = itemView.findViewById(R.id.btnShowFoldersMenu);
            llFolder = itemView.findViewById(R.id.llFolder);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;

            selectedPosition = getBindingAdapterPosition();
            view.findViewById(R.id.selected_position)
                    .setBackgroundColor(context.getColor(R.color.colorPrimary));

            if (mClickListener != null)
                mClickListener.onItemClick(view, getBindingAdapterPosition());

            notifyDataSetChanged();

        }
    }

    /**
     * Возвращает Item в позиции клика int
     * @param index int
     * @return P extends Item
     */
    public Item getItem(int index) {
        return mData.get(index);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemFolderClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemFolderClickListener {
        void onItemClick(View view, int position);
    }

}

