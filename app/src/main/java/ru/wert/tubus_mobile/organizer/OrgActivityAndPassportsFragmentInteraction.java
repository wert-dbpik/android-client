package ru.wert.tubus_mobile.organizer;

import android.widget.EditText;

import ru.wert.tubus_mobile.data.interfaces.Item;
import ru.wert.tubus_mobile.data.models.Folder;

public interface OrgActivityAndPassportsFragmentInteraction {

    void fragmentChanged(OrganizerFragment<Item> fragment);

    void setCurrentTypeFragment(FragmentTag tag);

    Folder getSelectedFolder();

    EditText getEditTextSearch();
}
