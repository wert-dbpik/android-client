package ru.wert.bazapik_mobile.remark;

import androidx.fragment.app.FragmentContainerView;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.Remark;
import ru.wert.bazapik_mobile.pics.PicsAdapter;

public interface IRemarkFragmentInteraction {

    void closeRemarkFragment();
    Passport getPassport();
    void updateRemarkAdapter();
    Passport findPassportById(Long id);
    FragmentContainerView getRemarkContainerView();
//    void changeRemark(Remark remark);
//    void deleteRemark(Remark remark);
}
