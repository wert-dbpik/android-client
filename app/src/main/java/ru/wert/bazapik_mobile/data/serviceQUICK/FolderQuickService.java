package ru.wert.bazapik_mobile.data.serviceQUICK;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import ru.wert.bazapik_mobile.data.models.Folder;
import ru.wert.bazapik_mobile.data.service_interfaces.IFolderService;

import static ru.wert.bazapik_mobile.ThisApplication.FOLDER_SERVICE;
import static ru.wert.bazapik_mobile.ThisApplication.PASSPORT_SERVICE;

public class FolderQuickService implements IFolderService {

    private Context context;
    private static FolderQuickService instance;
    private static List<Folder> folders;

    private FolderQuickService(Context context)  throws Exception{
        this.context = context;
//        ThisApplication.FOLDER_QUICK_SERVICE = this;
        reload();

    }


    public static void reload() throws Exception{
        while(true) {
            if(PASSPORT_SERVICE != null) {
                folders = new ArrayList<>(FOLDER_SERVICE.findAll());
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


    @Override
    public List<Folder> findAllByGroupId(Long id) {
        List<Folder> foundFolders = new ArrayList<>();
        for(Folder folder : folders){
            if(folder.getProductGroup().getId().equals(id)) {
                foundFolders.add(folder);
            }
        }
        return foundFolders;
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
    public Folder save(Folder folder)  throws Exception{
        Folder res = FOLDER_SERVICE.save(folder);
        reload();
        return res;
    }

    @Override
    public boolean update(Folder folder) throws Exception {
        boolean res = FOLDER_SERVICE.update(folder);
        reload();
        return res;
    }

    @Override
    public boolean delete(Folder folder) throws Exception{
        boolean res = FOLDER_SERVICE.delete(folder);
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

            if(name != null && name.contains(text)) {
                foundFolders.add(folder);
            }
        }
        return foundFolders;
    }
}
