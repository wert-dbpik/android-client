package ru.wert.bazapik_mobile.organizer;

import java.util.List;

import ru.wert.bazapik_mobile.data.interfaces.Item;

public interface OrganizerRecViewAdapter<T extends Item> {
    void changeListOfItems(List foundItems);
}
