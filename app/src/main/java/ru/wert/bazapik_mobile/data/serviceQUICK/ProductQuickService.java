package ru.wert.bazapik_mobile.data.serviceQUICK;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ru.wert.bazapik_mobile.data.service_interfaces.IProductService;
import ru.wert.bazapik_mobile.data.servicesREST.ProductService;
import ru.wert.bazapik_mobile.data.exceptions.ItemIsBusyException;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Product;

public class ProductQuickService implements IProductService {

    private static ProductQuickService instance;
    private static List<Product> products;
    private static ProductService service = ProductService.getInstance();
//    public static Product DEFAULT_PRODUCT;

    private ProductQuickService() {
        reload();

//        DEFAULT_PRODUCT = new Product;

    }

    public static ProductQuickService getInstance() {
        if (instance == null)
            return new ProductQuickService();
        return instance;
    }

    public static void reload(){
        while(true) {
            if(service != null) {
                products = new ArrayList<>(service.findAll());
                break;
            }
        }
    }


    public Product findByPassportId(Long id) {
        Product foundProduct = null;
        for(Product product : products){
            if(product.getPassport().getId().equals(id)) {
                foundProduct = product;
                break;
            }
        }
        return foundProduct;
    }

    public Product findByName(String name){
        Product foundProduct = null;
        for(Product product : products){
            if(product.getPassport().getName().equals(name)) {
                foundProduct = product;
                break;
            }
        }
        return foundProduct;
    }

    public List<Product> findAllByFolderId(Long id) {
        List<Product> foundProducts = new ArrayList<>();
        for(Product product : products){
            if(product.getFolder().getId().equals(id)) {
                foundProducts.add(product);
            }
        }
        return foundProducts;
    }

    public List<Product> findAllByProductGroupId(Long id) {
        List<Product> foundProducts = new ArrayList<>();
        for(Product product : products){
            if(product.getProductGroup().getId().equals(id)) {
                foundProducts.add(product);
            }
        }
        return foundProducts;
    }

    @Override
    public Set<Draft> findDrafts(Product product) {
        return service.findDrafts(product);
    }

    @Override
    public Set<Draft> addDrafts(Product product, Draft draft) {
        Set<Draft> res = service.addDrafts(product, draft);
        DraftQuickService.reload();
        reload();
        return res;

    }

    @Override
    public Set<Draft> removeDrafts(Product product, Draft draft) {
        Set<Draft> res = service.removeDrafts(product, draft);
        DraftQuickService.reload();
        reload();
        return res;
    }

    public Product findById(Long id) {
        Product foundProduct = null;
        for(Product product : products){
            if(product.getId().equals(id)) {
                foundProduct = product;
                break;
            }
        }
        return foundProduct;
    }

    @Override
    public Product save(Product product) {
        Product res = service.save(product);
        reload();
        return res;
    }

    @Override
    public boolean update(Product product) {
        boolean res = service.update(product);
        reload();
        return res;
    }

    @Override
    public boolean delete(Product product) throws Exception {
        boolean res = service.delete(product);
        reload();
        AnyPartQuickService.reload();
        PassportQuickService.reload();
        return res;
    }

    public List<Product> findAll() {
        return products;
    }

    public List<Product> findAllByText(String text) {
        List<Product> foundProducts = new ArrayList<>();
        for(Product product : products){
            String name = product.getPassport().getName();
            String decNumber = product.getPassport().getNumber();
            if((name != null && name.contains(text)) || (decNumber != null && decNumber.contains(text))) {
                foundProducts.add(product);
            }
        }
        return foundProducts;
    }
}
