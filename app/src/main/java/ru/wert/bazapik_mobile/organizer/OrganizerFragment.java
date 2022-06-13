package ru.wert.bazapik_mobile.organizer;

import java.util.List;

import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.search.DraftsRecViewAdapter;

public interface OrganizerFragment<T extends Item> {

    List<T> getFoundItems();
    void setFoundItems(List<T> foundItems);

    List<T> getAllItems();
    void setAllItems(List<T> allItems);

    List<T> findProperItems(String text);

    OrganizerRecViewAdapter<T> getAdapter();
}
