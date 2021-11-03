package ru.wert.bazapik_mobile.data.service_interfaces;

import java.util.List;

import ru.wert.bazapik_mobile.data.interfaces.ItemService;
import ru.wert.bazapik_mobile.data.models.Detail;

public interface IDetailService extends ItemService<Detail> {

    Detail findByPassportId(Long id);

    List<Detail> findAllByDraftId(Long id);

    List<Detail> findAllByFolderId(Long id);

}
