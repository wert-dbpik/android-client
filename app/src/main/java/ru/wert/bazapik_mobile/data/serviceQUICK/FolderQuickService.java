package ru.wert.bazapik_mobile.data.serviceQUICK;

import java.util.ArrayList;
import java.util.List;

import ru.wert.bazapik_mobile.data.service_interfaces.IFolderService;
import ru.wert.bazapik_mobile.data.servicesREST.FolderService;
import ru.wert.bazapik_mobile.data.models.Folder;

import static ru.wert.bazapik_mobile.data.util.BLConst.RAZLOZHENO;

public class FolderQuickService implements IFolderService {

    private static FolderQuickService instance;
    private static List<Folder> folders;
    private static FolderService service = FolderService.getInstance();
    public static Folder DEFAULT_FOLDER;

    private FolderQuickService() {
        reload();

        DEFAULT_FOLDER = service.findByName(RAZLOZHENO);

    }

    public static FolderQuickService getInstance() {
        if (instance == null)
            return new FolderQuickService();
        return instance;
    }

    public static void reload(){
        while(true) {
            if(service != null) {
                folders = new ArrayList<>(service.findAll());
                break;
            }
        }
    }

    public Folder findByName(String name) {
        Folder foundFolder = null;
        for(Folder folder : folders){
            if(folder.getName() != null && folder.getName().equals(name)) {
                foundFolder = folder;
                break;
            }
        }
        return foundFolder;
    }

    public Folder findByDecNumber(String number) {
        Folder foundFolder = null;
        for(Folder folder : folders){
            if(folder.getDecNumber() != null && folder.getDecNumber().equals(number)) {
                foundFolder = folder;
                break;
            }
        }
        return foundFolder;
    }

    public Folder findById(Long id) {
        Folder foundFolder = null;
        for(Folder folder : folders){
            if(folder.getId().equals(id)) {
                foundFolder = folder;
                break;
            }
        }
        return foundFolder;
    }

    @Override
    public boolean save(Folder folder) {
        boolean res = service.save(folder);
        reload();
        return res;
    }

    @Override
    public boolean update(Folder folder) {
        boolean res = service.update(folder);
        reload();
        return res;
    }

    @Override
    public boolean delete(Folder folder){
        boolean res = service.delete(folder);
        reload();
        return res;
    }

    public List<Folder> findAll() {
        return (folders);
    }

    public List<Folder> findAllByText(String text) {
        List<Folder> foundFolders = new ArrayList<>();
        for(Folder folder : folders){
            String name = folder.getName();
            String decNumber = folder.getDecNumber();
            if((name != null && name.contains(text)) || (decNumber != null && decNumber.contains(text))) {
                foundFolders.add(folder);
            }
        }
        return foundFolders;
    }
}
