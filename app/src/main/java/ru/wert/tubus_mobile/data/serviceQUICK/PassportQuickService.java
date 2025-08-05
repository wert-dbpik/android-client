package ru.wert.tubus_mobile.data.serviceQUICK;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import ru.wert.tubus_mobile.data.models.Prefix;
import ru.wert.tubus_mobile.data.service_interfaces.IPassportService;
import ru.wert.tubus_mobile.data.models.Passport;

import static ru.wert.tubus_mobile.ThisApplication.PASSPORT_SERVICE;


public class PassportQuickService implements IPassportService {

    private Context context;
    private static List<Passport> passports;

    public PassportQuickService(Context context) throws Exception{
        this.context = context;
//        ThisApplication.PASSPORT_QUICK_SERVICE = this;
        reload();
    }

    public static void reload() throws Exception{
        while(true) {
            if(PASSPORT_SERVICE != null) {
                passports = new ArrayList<>(PASSPORT_SERVICE.findAll());
                break;
            }
        }
    }

  //   ОСНОВНЫЕ

    @Override
    public Passport save(Passport passport) throws Exception{
        Passport res = PASSPORT_SERVICE.save(passport);
        reload();
        return res;
    }

    @Override
    public boolean update(Passport passport) throws Exception {
        boolean res = PASSPORT_SERVICE.update(passport);
        reload();
        return res;
    }

    @Override
    public boolean delete(Passport passport) throws Exception{
        boolean res = PASSPORT_SERVICE.delete(passport);
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
