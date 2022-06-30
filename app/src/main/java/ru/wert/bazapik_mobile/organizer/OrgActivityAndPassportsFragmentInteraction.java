package ru.wert.bazapik_mobile.organizer;

import android.widget.EditText;

import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.organizer.passports.PassportsFragment;

public interface OrgActivityAndPassportsFragmentInteraction {

    void fragmentChanged(OrganizerFragment<Item> fragment);

    void setCurrentTypeFragment(FragmentTag tag);

    Folder getSelectedFolder();

    EditText getEditTextSearch();
}
