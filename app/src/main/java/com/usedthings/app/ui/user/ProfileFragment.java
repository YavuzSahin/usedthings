package com.panaceasoft.psbuyandsell.ui.user;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.panaceasoft.psbuyandsell.Config;
import com.panaceasoft.psbuyandsell.MainActivity;
import com.panaceasoft.psbuyandsell.R;
import com.panaceasoft.psbuyandsell.binding.FragmentDataBindingComponent;
import com.panaceasoft.psbuyandsell.databinding.FragmentProfileBinding;
import com.panaceasoft.psbuyandsell.ui.common.DataBoundListAdapter;
import com.panaceasoft.psbuyandsell.ui.common.PSFragment;
import com.panaceasoft.psbuyandsell.ui.item.adapter.ItemHorizontalListAdapter;
import com.panaceasoft.psbuyandsell.ui.item.promote.adapter.ItemPromoteHorizontalListAdapter;
import com.panaceasoft.psbuyandsell.utils.AutoClearedValue;
import com.panaceasoft.psbuyandsell.utils.Constants;
import com.panaceasoft.psbuyandsell.utils.PSDialogMsg;
import com.panaceasoft.psbuyandsell.utils.Utils;
import com.panaceasoft.psbuyandsell.viewmodel.ItemPaidHistoryViewModel.ItemPaidHistoryViewModel;
import com.panaceasoft.psbuyandsell.viewmodel.item.ItemViewModel;
import com.panaceasoft.psbuyandsell.viewmodel.user.UserViewModel;
import com.panaceasoft.psbuyandsell.viewobject.Item;
import com.panaceasoft.psbuyandsell.viewobject.ItemPaidHistory;
import com.panaceasoft.psbuyandsell.viewobject.User;
import com.panaceasoft.psbuyandsell.viewobject.common.Resource;
import com.panaceasoft.psbuyandsell.viewobject.holder.UserParameterHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ProfileFragment
 */
public class ProfileFragment extends PSFragment implements DataBoundListAdapter.DiffUtilDispatchedInterface {


    //region Variables

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private ItemViewModel itemViewModel;
    private UserViewModel userViewModel;
    private ItemPaidHistoryViewModel itemPaidHistoryViewModel;
    public PSDialogMsg psDialogMsg;

    @VisibleForTesting
    private AutoClearedValue<FragmentProfileBinding> binding;
    private AutoClearedValue<ItemHorizontalListAdapter> adapter;
    private AutoClearedValue<ItemPromoteHorizontalListAdapter> itemPromoteHorizontalListAdapter;


    //endregion


    //region Override Methods

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        FragmentProfileBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        return binding.get().getRoot();
    }


    @Override
    protected void initUIAndActions() {

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarText(((MainActivity) getActivity()).binding.toolbar, getString(R.string.menu__profile));
            ((MainActivity) this.getActivity()).binding.toolbar.setBackgroundColor(getResources().getColor(R.color.global__primary));
            ((MainActivity) getActivity()).updateMenuIconWhite();
            ((MainActivity) getActivity()).updateToolbarIconColor(Color.WHITE);
            ((MainActivity) getActivity()).refreshPSCount();
        }

        psDialogMsg = new PSDialogMsg(getActivity(), false);

        binding.get().userOwnItemList.setNestedScrollingEnabled(false);
        binding.get().editTextView.setOnClickListener(view -> navigationController.navigateToProfileEditActivity(getActivity()));
        binding.get().seeAllTextView.setOnClickListener(view -> navigationController.navigateToItemListActivity(getActivity(), loginUserId, Constants.FLAGNOPAID));
        binding.get().favouriteTextView.setOnClickListener(view -> navigationController.navigateToFavouriteActivity(getActivity()));
        binding.get().heartImageView.setOnClickListener(view -> navigationController.navigateToFavouriteActivity(getActivity()));
        binding.get().settingTextView.setOnClickListener(view -> navigationController.navigateToSettingActivity(getActivity()));
        binding.get().followingUserTextView.setOnClickListener(view -> navigationController.navigateToUserListActivity(ProfileFragment.this.getActivity(), new UserParameterHolder().getFollowingUsers()));
        binding.get().followUserTextView.setOnClickListener(view -> navigationController.navigateToUserListActivity(ProfileFragment.this.getActivity(), new UserParameterHolder().getFollowerUsers()));
        binding.get().deactivateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                psDialogMsg.showConfirmDialog(getString(R.string.profile__confirm_deactivate), getString(R.string.app__ok), getString(R.string.message__cancel_close));
                psDialogMsg.show();

                psDialogMsg.okButton.setOnClickListener(v12 -> {
                    userViewModel.setDeleteUserObj(loginUserId);

                    psDialogMsg.cancel();
                });

                psDialogMsg.cancelButton.setOnClickListener(v1 -> psDialogMsg.cancel());

            }
        });

        binding.get().paidAdViewAllTextView.setOnClickListener(view -> navigationController.navigateToItemListActivity(getActivity(), loginUserId, Constants.FLAGPAID));

    }

    @Override
    protected void initViewModels() {
        itemViewModel = ViewModelProviders.of(this, viewModelFactory).get(ItemViewModel.class);
        userViewModel = ViewModelProviders.of(this, viewModelFactory).get(UserViewModel.class);
        itemPaidHistoryViewModel = ViewModelProviders.of(this, viewModelFactory).get(ItemPaidHistoryViewModel.class);
    }

    @Override
    protected void initAdapters() {

        ItemHorizontalListAdapter nvAdapter = new ItemHorizontalListAdapter(dataBindingComponent, new ItemHorizontalListAdapter.NewsClickCallback() {
            @Override
            public void onClick(Item item) {
                navigationController.navigateToItemDetailActivity(ProfileFragment.this.getActivity(), item.id, item.title);
            }
        }, this);
        this.adapter = new AutoClearedValue<>(this, nvAdapter);
        binding.get().userOwnItemList.setAdapter(nvAdapter);


        ItemPromoteHorizontalListAdapter itemPromoteAdapter = new ItemPromoteHorizontalListAdapter(dataBindingComponent, new ItemPromoteHorizontalListAdapter.NewsClickCallback() {
            @Override
            public void onClick(ItemPaidHistory itemPaidHistory) {
                navigationController.navigateToItemDetailActivity(ProfileFragment.this.getActivity(), itemPaidHistory.item.id, itemPaidHistory.item.title);
            }
        }, this);
        this.itemPromoteHorizontalListAdapter = new AutoClearedValue<>(this, itemPromoteAdapter);
        binding.get().userPaidItemRecyclerView.setAdapter(itemPromoteAdapter);

    }

    @Override
    protected void initData() {

        userViewModel.getLoginUser().observe(this, data -> {

            if (data != null) {

                if (data.size() > 0) {
                    userViewModel.user = data.get(0).user;
                }
            }

        });

        //User
        userViewModel.setUserObj(loginUserId);
        userViewModel.getUserData().observe(this, new Observer<Resource<User>>() {
            @Override
            public void onChanged(Resource<User> listResource) {

                if (listResource != null) {

                    Utils.psLog("Got Data" + listResource.message + listResource.toString());

                    switch (listResource.status) {
                        case LOADING:
                            // Loading State
                            // Data are from Local DB

                            if (listResource.data != null) {
                                //fadeIn Animation
                                ProfileFragment.this.fadeIn(binding.get().getRoot());

                                binding.get().setUser(listResource.data);
                                Utils.psLog("Photo : " + listResource.data.userProfilePhoto);

                                ProfileFragment.this.replaceUserData(listResource.data);


                            }

                            break;
                        case SUCCESS:
                            // Success State
                            // Data are from Server

                            if (listResource.data != null) {

                                //fadeIn Animation
                                //fadeIn(binding.get().getRoot());

                                binding.get().setUser(listResource.data);
                                Utils.psLog("Photo : " + listResource.data.userProfilePhoto);

                                ProfileFragment.this.replaceUserData(listResource.data);
                            }

                            break;
                        case ERROR:
                            // Error State

                            psDialogMsg.showErrorDialog(listResource.message, ProfileFragment.this.getString(R.string.app__ok));
                            psDialogMsg.show();

                            userViewModel.isLoading = false;

                            break;
                        default:
                            // Default
                            userViewModel.isLoading = false;

                            break;
                    }

                } else {

                    // Init Object or Empty Data
                    Utils.psLog("Empty Data");

                }

                // we don't need any null checks here for the SubCategoryAdapter since LiveData guarantees that
                // it won't call us if fragment is stopped or not started.
                if (listResource != null && listResource.data != null) {
                    Utils.psLog("Got Data");


                } else {
                    //noinspection Constant Conditions
                    Utils.psLog("Empty Data");

                }
            }
        });

        //delete user
        userViewModel.getDeleteUserStatus().observe(this, result -> {

            if (result != null) {
                switch (result.status) {
                    case SUCCESS:

                        //add offer text
                        Toast.makeText(getContext(), "Success Delete user", Toast.LENGTH_SHORT).show();

                        logout();

                        break;

                    case ERROR:
                        Toast.makeText(getContext(), "Fail Delete this user", Toast.LENGTH_SHORT).show();

                        break;
                }
            }
        });

        //Item
        itemViewModel.holder.userId = loginUserId;

        itemViewModel.setItemListByKeyObj(Utils.checkUserId(loginUserId), String.valueOf(Config.LOGIN_USER_ITEM_COUNT), Constants.ZERO, itemViewModel.holder);

        itemViewModel.getItemListByKeyData().observe(this, listResource -> {

            if (listResource != null) {
                switch (listResource.status) {
                    case SUCCESS:

                        if (listResource.data != null) {
                            if (listResource.data.size() > 0) {
                                itemReplaceData(listResource.data);
                            }
                            itemViewModel.setLoadingState(false);
                        }

                        break;

                    case LOADING:

                        if (listResource.data != null) {
                            if (listResource.data.size() > 0) {
                                itemReplaceData(listResource.data);
                            }

                        }

                        break;

                    case ERROR:
                        itemViewModel.setItemListFromDbByKeyObj(Utils.checkUserId(loginUserId), String.valueOf(Config.LOGIN_USER_ITEM_COUNT), Constants.ZERO, itemViewModel.holder);

                        itemViewModel.setLoadingState(false);
                        break;
                }
            }
        });

        itemViewModel.getItemListFromDbByKeyData().observe(this, listResource -> {

            if (listResource != null) {

                itemReplaceData(listResource);

            }
        });

        // Get Paid Item History List
        itemPaidHistoryViewModel.setPaidItemHistory(Utils.checkUserId(loginUserId), String.valueOf(Config.PAID_ITEM_COUNT), String.valueOf(itemPaidHistoryViewModel.offset));

        itemPaidHistoryViewModel.getPaidItemHistory().observe(this, result -> {

            if (result != null) {
                switch (result.status) {
                    case SUCCESS:

                        replacePaidItemHistoryList(result.data);
                        itemPaidHistoryViewModel.setLoadingState(false);
                        break;

                    case LOADING:
                        replacePaidItemHistoryList(result.data);

                        break;
                    case ERROR:
                        itemPaidHistoryViewModel.setLoadingState(false);
                        break;

                        default:
                            break;
                }
            }

        });
    }

    private void logout() {

        if ((MainActivity) getActivity() != null) {
            ((MainActivity) getActivity()).hideBottomNavigation();

            userViewModel.deleteUserLogin(userViewModel.user).observe(this, status -> {
                if (status != null) {
//                    this.menuId = 0;

                    ((MainActivity) getActivity()).setToolbarText(((MainActivity) getActivity()).binding.toolbar, getString(R.string.app__app_name));

                    ((MainActivity) getActivity()).isLogout = true;

                    FacebookSdk.sdkInitialize(((MainActivity) getActivity()).getApplicationContext());
                    LoginManager.getInstance().logOut();

                    if (getFragmentManager() != null) {
                        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                }
            });
        }
    }

    @Override
    public void onDispatched() {

    }


    private void replaceUserData(User user) {

        binding.get().editTextView.setText(binding.get().editTextView.getText().toString());
//        binding.get().userNotificatinTextView.setText(binding.get().userNotificatinTextView.getText().toString());
//        binding.get().userHistoryTextView.setText(binding.get().userHistoryTextView.getText().toString());
        binding.get().favouriteTextView.setText(binding.get().favouriteTextView.getText().toString());
        binding.get().settingTextView.setText(binding.get().settingTextView.getText().toString());
        binding.get().historyTextView.setText(binding.get().historyTextView.getText().toString());
        binding.get().seeAllTextView.setText(binding.get().seeAllTextView.getText().toString());
        binding.get().joinedDateTitleTextView.setText(binding.get().joinedDateTitleTextView.getText().toString());

        String strCurrentDate = user.addedDate;
        java.text.SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date inputDate ;
        try {
            inputDate = inputFormat.parse(strCurrentDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MM yyyy HH:mm:ss", Locale.US);
            if(inputDate != null) {
                String outputDateString = outputFormat.format(inputDate.getTime());
                binding.get().joinedDateTextView.setText(outputDateString);// user.addedDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        binding.get().nameTextView.setText(user.userName);
        binding.get().overAllRatingTextView.setText(user.overallRating);
        binding.get().ratingBarInformation.setRating(user.ratingDetails.totalRatingValue);

        String ratingCount = "( " + user.ratingCount + " )";
        String followerCount = getString(R.string.profile__followers) + " ( " + user.followerCount + " )";
        String followingCount = getString(R.string.profile__following) + " ( " + user.followingCount + " )";

        binding.get().ratingCountTextView.setText(ratingCount);
        binding.get().followUserTextView.setText(followerCount);
        binding.get().followingUserTextView.setText(followingCount);

        if (user.emailVerify.equals("1")) {
            binding.get().emailImage.setVisibility(View.VISIBLE);
        } else {
            binding.get().emailImage.setVisibility(View.GONE);
        }

        if (user.facebookVerify.equals("1")) {
            binding.get().facebookImage.setVisibility(View.VISIBLE);
        } else {
            binding.get().facebookImage.setVisibility(View.GONE);
        }

        if (user.phoneVerify.equals("1")) {
            binding.get().phoneImage.setVisibility(View.VISIBLE);
        } else {
            binding.get().phoneImage.setVisibility(View.GONE);
        }

        if (user.googleVerify.equals("1")) {
            binding.get().googleImage.setVisibility(View.VISIBLE);
        } else {
            binding.get().googleImage.setVisibility(View.GONE);
        }


    }

    private void itemReplaceData(List<Item> itemList) {
        adapter.get().replace(itemList);
        binding.get().executePendingBindings();
    }

    private void replacePaidItemHistoryList(List<ItemPaidHistory> itemPaidHistories) {
        this.itemPromoteHorizontalListAdapter.get().replace(itemPaidHistories);
        binding.get().executePendingBindings();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE__PROFILE_FRAGMENT
                && resultCode == Constants.RESULT_CODE__LOGOUT_ACTIVATED) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setToolbarText(((MainActivity) getActivity()).binding.toolbar, getString(R.string.profile__title));
                //navigationController.navigateToUserFBRegister((MainActivity) getActivity());
                navigationController.navigateToUserLogin((MainActivity) getActivity());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        userViewModel.setUserObj(loginUserId);
        itemViewModel.setItemListByKeyObj(Utils.checkUserId(loginUserId), String.valueOf(Config.LOGIN_USER_ITEM_COUNT), Constants.ZERO, itemViewModel.holder);

    }
}
