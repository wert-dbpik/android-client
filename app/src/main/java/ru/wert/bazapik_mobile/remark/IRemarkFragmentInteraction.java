package ru.wert.bazapik_mobile.remark;

import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.Remark;

public interface IRemarkFragmentInteraction {

    void closeRemarkFragment();
    Passport getPassport();
    void updateRemarkAdapter();
    Passport findPassportById(Long id);
//    void changeRemark(Remark remark);
//    void deleteRemark(Remark remark);
}
