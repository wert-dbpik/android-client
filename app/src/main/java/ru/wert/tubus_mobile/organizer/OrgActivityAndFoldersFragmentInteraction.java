package ru.wert.tubus_mobile.organizer;

import androidx.fragment.app.FragmentManager;
import ru.wert.tubus_mobile.data.interfaces.Item;
import ru.wert.tubus_mobile.data.models.Folder;
import ru.wert.tubus_mobile.organizer.folders.FoldersFragment;
import ru.wert.tubus_mobile.organizer.passports.PassportsFragment;

public interface OrgActivityAndFoldersFragmentInteraction {

    String getFoldersTextSearch();

    FragmentManager getFm();

    void fragmentChanged(OrganizerFragment<Item> fragment);

    PassportsFragment getCurrentPassportsFragment();

    void setSelectedFolder(Folder folder);

    void setCurrentFoldersFragment(FoldersFragment fragment);

    void setCurrentTypeFragment(FragmentTag tag);

    void setPassportsTextSearch(String passportsTextSearch);

    String getPassportsTextSearch();

}
