package com.panaceasoft.psbuyandsell.repository.itemcondition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.panaceasoft.psbuyandsell.AppExecutors;
import com.panaceasoft.psbuyandsell.Config;
import com.panaceasoft.psbuyandsell.api.ApiResponse;
import com.panaceasoft.psbuyandsell.api.PSApiService;
import com.panaceasoft.psbuyandsell.db.ItemConditionDao;
import com.panaceasoft.psbuyandsell.db.PSCoreDb;
import com.panaceasoft.psbuyandsell.repository.common.NetworkBoundResource;
import com.panaceasoft.psbuyandsell.repository.common.PSRepository;
import com.panaceasoft.psbuyandsell.utils.Utils;
import com.panaceasoft.psbuyandsell.viewobject.ItemCondition;
import com.panaceasoft.psbuyandsell.viewobject.common.Resource;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemConditionRepository extends PSRepository {
    private ItemConditionDao itemConditionDao;

    @Inject
    ItemConditionRepository(PSApiService psApiService, AppExecutors appExecutors, PSCoreDb db, ItemConditionDao itemConditionDao) {

        super(psApiService, appExecutors, db);
        this.itemConditionDao = itemConditionDao;
    }

    public LiveData<Resource<List<ItemCondition>>> getAllItemConditionList(String limit, String offset) {

        return new NetworkBoundResource<List<ItemCondition>, List<ItemCondition>>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull List<ItemCondition> itemTypeList) {
                Utils.psLog("SaveCallResult of getAllCategoriesWithUserId");

//
//                try {
//                    db.beginTransaction();
//
//                    itemConditionDao.deleteAllItemCondition();
//
//                    itemConditionDao.insertAll(itemTypeList);
//
//                    db.setTransactionSuccessful();
//
//                } catch (Exception e) {
//                    Utils.psErrorLog("Error in doing transaction of getAllCategoriesWithUserId", e);
//                } finally {
//                    db.endTransaction();
//                }

                try {
                    db.runInTransaction(() -> {
                        itemConditionDao.deleteAllItemCondition();

                        itemConditionDao.insertAll(itemTypeList);

                    });
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at ", ex);
                }
            }


            @Override
            protected boolean shouldFetch(@Nullable List<ItemCondition> data) {

                return connectivity.isConnected();
            }

            @NonNull
            @Override
            protected LiveData<List<ItemCondition>> loadFromDb() {

                Utils.psLog("Load From Db (All Categories)");

                return itemConditionDao.getAllItemCondition();

            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<ItemCondition>>> createCall() {
                Utils.psLog("Call Get All Categories webservice.");

                return psApiService.getItemConditionTypeList(Config.API_KEY,limit, offset);
            }

            @Override
            protected void onFetchFailed(String message) {
                Utils.psLog("Fetch Failed of About Us");
            }

        }.asLiveData();
    }


}