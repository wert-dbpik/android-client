package ru.wert.bazapik_mobile.organizer.folders;

import static ru.wert.bazapik_mobile.ThisApplication.ALL_DRAFTS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import ru.wert.bazapik_mobile.R;
import ru.wert.bazapik_mobile.ThisApplication;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.models.ProductGroup;
import ru.wert.bazapik_mobile.organizer.OrganizerActivity;
import ru.wert.bazapik_mobile.organizer.OrganizerRecViewAdapter;
import ru.wert.bazapik_mobile.viewer.ViewerActivity;
import ru.wert.bazapik_mobile.warnings.WarningDialog1;

public class FoldersRecViewAdapter extends RecyclerView.Adapter<FoldersRecViewAdapter.ViewHolder> implements OrganizerRecViewAdapter {

    private List<Item> mData;
    private final LayoutInflater mInflater;
    private ItemFolderClickListener mClickListener;
    private final Context context;
    @Setter private int selectedPosition = RecyclerView.NO_POSITION;
    private FoldersFragment fragment;


    public FoldersRecViewAdapter(FoldersFragment fragment, Context context, List<Item> items) {
        this.fragment = fragment;
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = items;
    }

    /**
     * Создает новый view из имеющегося xml файла с помощью метода inflate класса LayoutInflater
     *
     * @param parent   ViewGroup
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
     *
     * @param holder   ViewHolder
     * @param position int
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        View selectedLinearLayout = holder.itemView.findViewById(R.id.selectedLinearLayout);
        selectedLinearLayout.setBackgroundColor((position == selectedPosition) ?
                context.getColor(R.color.colorPrimary) : //Цвет выделения
                context.getColor(R.color.colorPrimaryDark)); //Цвет фона

        Item item = mData.get(position);

        if (item instanceof ProductGroup) {

            holder.showFolderMenu.setVisibility(View.INVISIBLE);
            holder.showFolderMenu.setClickable(false);

            holder.folder.setClickable(false); //Не обрабатывает нажатие на себя
            if (!fragment.getUpperProductGroupId().equals(1L) && position == 0) {
                String str = "< . . . . .>";
                holder.numberAndName.setText(str);
                holder.folder.setImageDrawable(context.getDrawable(R.drawable.backward256));

            } else {
                String str = ((ProductGroup) item).getName();
                holder.numberAndName.setText(str);
                holder.folder.setImageDrawable(context.getDrawable(R.drawable.folder256));
            }

        }

        if (item instanceof Folder) {
            holder.showFolderMenu.setVisibility(View.VISIBLE);
            holder.showFolderMenu.setClickable(true);
            holder.showFolderMenu.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                ((View)v.getParent().getParent()).setBackgroundColor(context.getColor(R.color.colorPrimary)); //Выделяем строку
                notifyDataSetChanged();

                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenuInflater().inflate(R.menu.folder_context_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item1) {
                        Folder folder = (Folder) mData.get(position);
                        switch (item1.getItemId()) {
                            case R.id.showAllFolderDraftsFirst:
                                openDraftsInFolder(folder, false);
                                break;
                            case R.id.showAllFolderAssemblesFirst:
                                openDraftsInFolder(folder, true);
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            });

            String str = ((Folder) item).getName();
            holder.numberAndName.setText(str);
            holder.folder.setImageDrawable(context.getDrawable(R.drawable.folders256));
            holder.folder.setClickable(false);//Не обрабатывает нажатие на себя

        }

    }


    private void openDraftsInFolder(Folder folder, boolean assemblesFirst) {
        ArrayList<Draft> foundDrafts = new ArrayList<>();
        for (Draft d : ALL_DRAFTS) {
            if (d.getFolder().equals(folder))
                foundDrafts.add(d);
        }
        if(foundDrafts.isEmpty()) {
            new WarningDialog1().show(context, "Упс!", "Чертежей в комплекте не найдено");
            return;
        };

        ThisApplication.filterList(foundDrafts); //Фильтруем
        if (assemblesFirst)
            foundDrafts.sort(ThisApplication.draftsComparatorAssemblesFirst());
        else
            foundDrafts.sort(ThisApplication.draftsComparatorDetailFirst());

        Intent intent = new Intent(context, ViewerActivity.class);
        ArrayList<String> stringList = ThisApplication.convertToStringArray(foundDrafts);
        intent.putStringArrayListExtra("DRAFTS", stringList);
        intent.putExtra("DRAFT_ID", String.valueOf(foundDrafts.get(0).getId()));
        context.startActivity(intent);
    }


    /**
     * Возвращает общее количество элементов в списке List<P>
     *
     * @return int
     */
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Обновляет отображаемые данные
     *
     * @param items List<P>
     */
    public void changeListOfItems(List items) {
        mData = new ArrayList<Item>(items);
        selectedPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    /**
     * Вложенный класс, описывающий и создающий ограниченной количество ViewHolder
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView numberAndName;
        ImageButton folder; //кнопка в виде чертежика
        ImageButton showFolderMenu; //три точки

        ViewHolder(View itemView) {
            super(itemView);
            numberAndName = itemView.findViewById(R.id.number_and_name);
            folder = itemView.findViewById(R.id.btnFolder);
            showFolderMenu = itemView.findViewById(R.id.btnShowFoldersMenu);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            ((OrganizerActivity)fragment.getActivity()).getEditTextSearch().clearFocus();

            if (getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;

            selectedPosition = getBindingAdapterPosition();
            //Чтобы выделить строку СТРАЗУ при нажатии
            view.findViewById(R.id.selectedLinearLayout)
                    .setBackgroundColor(context.getColor(R.color.colorPrimary));

            if (mClickListener != null)
                mClickListener.onItemClick(view, getBindingAdapterPosition());

            notifyDataSetChanged();

        }





    }

    /**
     * Возвращает Item в позиции клика int
     *
     * @param index int
     * @return P extends Item
     */
    public Item getItem(int index) {
        return mData.get(index);
    }

    public void setClickListener(ItemFolderClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemFolderClickListener {
        void onItemClick(View view, int position);
    }

}

