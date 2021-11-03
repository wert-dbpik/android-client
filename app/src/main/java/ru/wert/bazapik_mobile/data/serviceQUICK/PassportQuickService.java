package ru.wert.bazapik_mobile.data.serviceQUICK;

import java.util.ArrayList;
import java.util.List;

import ru.wert.bazapik_mobile.data.models.Prefix;
import ru.wert.bazapik_mobile.data.service_interfaces.IPassportService;
import ru.wert.bazapik_mobile.data.servicesREST.PassportService;
import ru.wert.bazapik_mobile.data.models.Passport;


public class PassportQuickService implements IPassportService {

    private static PassportQuickService instance;
    private static List<Passport> passports;
    private static PassportService service = PassportService.getInstance();
//    public static Passport DEFAULT_FOLDER = "Разложено";

    private PassportQuickService() {
        reload();
    }

    public static PassportQuickService getInstance() {
        if (instance == null)
            return new PassportQuickService();
        return instance;
    }

    public static void reload(){
        while(true) {
            if(service != null) {
                passports = new ArrayList<>(service.findAll());
                break;
            }
        }
    }

  //   ОСНОВНЫЕ

    @Override
    public boolean save(Passport passport) {
        boolean res = service.save(passport);
        reload();
        return res;
    }

    @Override
    public boolean update(Passport passport) {
        boolean res = service.update(passport);
        reload();
        return res;
    }

    @Override
    public boolean delete(Passport passport){
        boolean res = service.delete(passport);
        reload();
        return res;
    }

    //   ПОИСКИ

    public Passport findByName(String name) {
        Passport foundPassport = null;
        for(Passport passport : passports){
            if(passport.getName() != null && passport.getName().equals(name)) {
                foundPassport = passport;
                break;
            }
        }
        return foundPassport;
    }

    public Passport findByPrefixIdAndNumber(Prefix prefix, String number) {

        Passport foundPassport = null;
        for(Passport passport : passports){
            if(passport.getNumber() != null && passport.getNumber().equals(number) && passport.getPrefix().getId().equals(prefix.getId())) {
                foundPassport = passport;
                break;
            }
        }
        return foundPassport;
    }

    @Override
    public List<Passport> findAllByName(String name) {
        List<Passport> foundPassports = new ArrayList<>();
        for(Passport passport : passports){
            if(passport.getName().equals(name))
                foundPassports.add(passport);
        }
        return foundPassports;
    }

    public Passport findById(Long id) {
        Passport foundPassport = null;
        for(Passport passport : passports){
            if(passport.getId().equals(id)) {
                foundPassport = passport;
                break;
            }
        }
        return foundPassport;
    }



    public List<Passport> findAll() {
        return passports;
    }

    public List<Passport> findAllByText(String text) {
        List<Passport> foundPassports = new ArrayList<>();
        for(Passport passport : passports){
            String name = passport.getName();
            String decNumber = passport.getPrefix().getName() + "." + passport.getNumber();
            if(name != null && name.contains(text) || decNumber.contains(text)) {
                foundPassports.add(passport);
            }
        }
        return foundPassports;
    }
}
