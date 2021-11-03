package ru.wert.bazapik_mobile.data.servicesREST;

import retrofit2.Call;
import ru.wert.bazapik_mobile.data.api_interfaces.ProductApiInterface;
import ru.wert.bazapik_mobile.data.service_interfaces.IProductService;
import ru.wert.bazapik_mobile.data.interfaces.Item;
import ru.wert.bazapik_mobile.data.interfaces.PartItem;
import ru.wert.bazapik_mobile.data.models.Draft;
import ru.wert.bazapik_mobile.data.models.Passport;
import ru.wert.bazapik_mobile.data.models.Product;
import ru.wert.bazapik_mobile.data.garbage.RetrofitClient;
import ru.wert.bazapik_mobile.data.util.BLlinks;


import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ProductService implements IProductService, PartItem {

    private static ProductService instance;
    private ProductApiInterface api;

    private ProductService() {
        BLlinks.productService = this;
        api = RetrofitClient.getInstance().getRetrofit().create(ProductApiInterface.class);
    }

    public ProductApiInterface getApi() {
        return api;
    }

    public static ProductService getInstance() {
        if (instance == null)
            return new ProductService();
        return instance;
    }

    @Override
    public Product findById(Long id) {
        try {
            Call<Product> call = api.getById(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Product findByPassportId(Long id) {
        try {
            Call<Product> call = api.getByPassportId(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Product> findAllByFolderId(Long id) {
        try {
            Call<List<Product>> call = api.getAllByFolderId(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Product> findAllByProductGroupId(Long id) {
        try {
            Call<List<Product>> call = api.getAllByProductGroupId(id);
            return call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<Draft> findDrafts(Product product) {
        try {
            Call<Set<Draft>> call = api.getDrafts(product.getId());
            Set<Draft> res = call.execute().body();
            if(res != null)
                return (res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<Draft> addDrafts(Product product, Draft draft) {
        try {
            Call<Set<Draft>> call = api.addDraft(product.getId(), draft.getId());
            Set<Draft> res = call.execute().body();
            if(res != null)
                return (res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<Draft> removeDrafts(Product product, Draft draft) {
        try {
            Call<Set<Draft>> call = api.removeDraft(product.getId(), draft.getId());
            Set<Draft> res = call.execute().body();
            if(res != null)
                return (res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Product> findAll() {
        try {
            Call<List<Product>> call = api.getAll();
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Product> findAllByText(String text) {
        try {
            Call<List<Product>> call = api.getAllByText(text);
            return (call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean save(Product entity) {
        try {
            Call<Product> call = api.create(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Product entity) {
        try {
            Call<Void> call = api.update(entity);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(Product entity) {
        Long id = entity.getId();
        try {
            Call<Void> call = api.deleteById(id);
            return call.execute().isSuccessful();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getPartName(Item product) {
        Passport p = ((Product)product).getPassport();
        return p.getPrefix().getName() + "." + p.getNumber() + "-" + ((Product)product).getVariant(); //ПИК.745222.123-02
    }


}
