package ru.wert.tubus_mobile.organizer;

import java.util.List;

import ru.wert.tubus_mobile.data.interfaces.Item;

public interface OrganizerRecViewAdapter<T extends Item> {

    void changeListOfItems(List foundItems);

}
