package ru.wert.tubus_mobile.data.service_interfaces;

import java.util.List;

import ru.wert.tubus_mobile.data.interfaces.ItemService;
import ru.wert.tubus_mobile.data.interfaces.PartItem;
import ru.wert.tubus_mobile.data.models.Assemble;

public interface IAssembleService extends ItemService<Assemble>, PartItem {

    Assemble findByPassportId(Long id);

    List<Assemble> findAllByDraftId(Long id);

    List<Assemble> findAllByFolderId(Long id);

}
