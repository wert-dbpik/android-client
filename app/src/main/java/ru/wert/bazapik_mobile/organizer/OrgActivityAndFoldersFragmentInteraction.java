package ru.wert.bazapik_mobile.organizer;

import androidx.fragment.app.FragmentManager;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.organizer.folders.FoldersFragment;
import ru.wert.bazapik_mobile.organizer.passports.PassportsFragment;

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
