package ru.wert.bazapik_mobile.data.serviceQUICK;

import java.util.ArrayList;
import java.util.List;

import ru.wert.bazapik_mobile.data.service_interfaces.IPrefixService;
import ru.wert.bazapik_mobile.data.servicesREST.PrefixService;
import ru.wert.bazapik_mobile.data.exceptions.ItemIsBusyException;
import ru.wert.bazapik_mobile.data.models.Prefix;

public class PrefixQuickService implements IPrefixService {

    private static PrefixQuickService instance;
    private static List<Prefix> prefixes;
    private static PrefixService service = PrefixService.getInstance();

    private PrefixQuickService() {
        reload();
    }

    public static PrefixQuickService getInstance() {
        if (instance == null)
            return new PrefixQuickService();
        return instance;
    }

    public static void reload(){
        while(true) {
            if(service != null) {
                prefixes = new ArrayList<>(service.findAll());
                break;
            }
        }
    }

    public Prefix findByName(String name) {
        Prefix foundPrefix = null;
        for(Prefix prefix : prefixes){
            if(prefix.getName() != null && prefix.getName().equals(name)) {
                foundPrefix = prefix;
                break;
            }
        }
        return foundPrefix;
    }


    public Prefix findById(Long id) {
        Prefix foundPrefix = null;
        for(Prefix prefix : prefixes){
            if(prefix.getId().equals(id)) {
                foundPrefix = prefix;
                break;
            }
        }
        return foundPrefix;
    }

    @Override
    public boolean save(Prefix prefix) {
        boolean res = service.save(prefix);
        reload();
        return res;
    }

    @Override
    public boolean update(Prefix prefix) {
        boolean res = service.update(prefix);
        reload();
        return res;
    }

    @Override
    public boolean delete(Prefix prefix) throws ItemIsBusyException {
        boolean res = service.delete(prefix);
        reload();
        return res;
    }

    public List<Prefix> findAll() {
        return (prefixes);
    }

    public List<Prefix> findAllByText(String text) {
        List<Prefix> foundPrefixes = new ArrayList<>();
        for(Prefix prefix : prefixes){
            String name = prefix.getName();
            if((name != null && name.contains(text))) {
                foundPrefixes.add(prefix);
            }
        }
        return foundPrefixes;
    }
}
