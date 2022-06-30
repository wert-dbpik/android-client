package ru.wert.bazapik_mobile.organizer;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.organizer.folders.FoldersFragment;

public interface OrganizerFragment<T extends Item> {

    RecyclerView getRv();

    List<T> getFoundItems();

    void setFoundItems(List<T> foundItems);

    List<T> getAllItems();

    void setAllItems(List<T> allItems);

    List<T> findProperItems(String text);

    OrganizerRecViewAdapter<T> getAdapter();

    void fillRecViewWithItems(List<Item> items);

}
