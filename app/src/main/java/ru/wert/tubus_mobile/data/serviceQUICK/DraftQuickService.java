package ru.wert.tubus_mobile.data.serviceQUICK;

import android.app.Application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ru.wert.tubus_mobile.data.exceptions.ItemIsBusyException;
import ru.wert.tubus_mobile.data.models.Draft;
import ru.wert.tubus_mobile.data.models.Folder;
import ru.wert.tubus_mobile.data.models.Product;
import ru.wert.tubus_mobile.data.service_interfaces.IDraftService;
import ru.wert.tubus_mobile.data.servicesREST.DraftService;

import static ru.wert.tubus_mobile.ThisApplication.DRAFT_SERVICE;

public class DraftQuickService extends Application implements IDraftService {

    private static List<Draft> drafts;
    private static final DraftService service = DRAFT_SERVICE;

    public DraftQuickService() {
//        DRAFT_QUICK_SERVICE = this;
        new Thread(DraftQuickService::reload).start();

    }

    public static void reload(){
        while(true) {
            if (service != null) {
                drafts = new ArrayList<>(service.findAll());
                break;
            }
        }
    }


    @Override
    public List<String> findDraftsByMask(String folder, String mask) {
        return service.findDraftsByMask(folder, mask);
    }

    @Override
    public boolean download(String path, String fileName, String extension, String tempDir) {
        return service.download(path, fileName, extension,tempDir);
    }

    @Override
    public boolean upload(String fileName, String path, File draft) throws IOException {
        boolean res =  service.upload(fileName, path, draft);
        reload();
        return res;
    }

    @Override
    public Set<Product> findProducts(Draft draft) {
        return service.findProducts(draft);
    }

    @Override
    public Set<Product> addProducts(Draft draft, Product product) {
        Set<Product> res = service.addProducts(draft, product);
        ProductQuickService.reload();
        reload();
        return res;
    }

    @Override
    public Set<Product> removeProducts(Draft draft, Product product) {
        Set<Product> res = service.removeProducts(draft, product);
        ProductQuickService.reload();
        reload();
        return res;
    }


    public List<Draft> findByPassportId(Long id) {
        List<Draft> foundDrafts = new ArrayList<>();
        for(Draft draft : drafts){
            if(draft.getPassport().getId().equals(id)) {
                foundDrafts.add(draft);
            }
        }
        return foundDrafts;
    }

    public List<Draft> findAllByFolder(Folder folder) {
       List<Draft> foundDrafts = new ArrayList<>();
        Long folderId = folder.getId();
        for(Draft draft : drafts){
            if(draft.getFolder() != null && draft.getFolder().getId().equals(folderId)) {
                foundDrafts.add(draft);
            }
        }
        return foundDrafts;
    }

    public Draft findById(Long id) {
        Draft foundDraft = null;
        for(Draft draft : drafts){
            if(draft.getId().equals(id)) {
                foundDraft = draft;
                break;
            }
        }
        return foundDraft;
    }

    @Override
    public Draft save(Draft draft) {
        Draft res = service.save(draft);
        reload();
        return res;
    }

    @Override
    public boolean update(Draft draft) {
        boolean res = service.update(draft);
        reload();
        return res;
    }

    @Override
    public boolean delete(Draft draft) throws ItemIsBusyException {
        boolean res = service.delete(draft);
        reload();
        return res;
    }

    public List<Draft> findAll() {
        return (drafts);
    }
    
    public List<Draft> findAllByText(String text) {
        List<Draft> foundDrafts = new ArrayList<>();
        for(Draft draft : drafts){
            String name = draft.getPassport().getName();
            String decNumber = draft.getPassport().getNumber();
            if((name != null && name.contains(text)) || (decNumber != null && decNumber.contains(text))) {
                foundDrafts.add(draft);
            }
        }
        return foundDrafts;
    }
}
