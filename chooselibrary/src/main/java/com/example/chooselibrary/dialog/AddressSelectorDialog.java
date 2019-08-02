package com.example.chooselibrary.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.chooselibrary.R;
import com.example.chooselibrary.db.manager.AddressDictManager;
import com.example.chooselibrary.module.AddressEntity;
import com.example.chooselibrary.utils.SizeUtils;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

/**
 * 地址选择器
 */
public class AddressSelectorDialog extends DialogFragment {

    private static final String DEFAULT_TAB_TEXT = "请选择";

    private View mRootView;
    private TabLayout mTabLay;
    private RecyclerView mAddressRecycler;
    private AddressAdapter mProvinceAdapter, mCityAdapter, mCountyAdapter, mTownAdapter;
    private AddressDictManager mAddressManager;
    private OnSelectorCompleteListener mOnSelectorCompleteListener;
    private TabLayout.Tab mProvinceTab, mCityTab, mCountyTab, mTownTab;
    private AddressEntity mProvince, mCity, mCounty, mTown;
    private BaseQuickAdapter.OnItemClickListener mProvinceClickListener = (adapter, view, position) -> {
        mProvinceAdapter.mSelectorPosistion = position;
        mProvinceAdapter.notifyItemChanged(position);
        mAddressRecycler.scrollToPosition(position);
        mProvince = mProvinceAdapter.getItem(position);
        mProvinceTab.setText(mProvince.name);
        mCity = mCounty = mTown = null;
        mCityAdapter.mSelectorPosistion = mCountyAdapter.mSelectorPosistion = mTownAdapter.mSelectorPosistion = -1;
        List<AddressEntity> cityList = mAddressManager.getCityList(mProvince.id);
        if (hasNextLayer(cityList)) {
            addNextLayer(mCityTab, mCityAdapter, cityList, position);
        }
    };

    private BaseQuickAdapter.OnItemClickListener mCityClickListener = (adapter, view, position) -> {
        mCityAdapter.mSelectorPosistion = position;
        mCityAdapter.notifyItemChanged(position);
        mAddressRecycler.scrollToPosition(position);
        mCity = mCityAdapter.getItem(position);
        mCityTab.setText(mCity.name);
        mCounty = mTown = null;
        mCountyAdapter.mSelectorPosistion = mTownAdapter.mSelectorPosistion = -1;
        List<AddressEntity> countyList = mAddressManager.getCountyList(mCity.id);
        if (hasNextLayer(countyList)) {
            addNextLayer(mCountyTab, mCountyAdapter, countyList, position);
        }
    };

    private BaseQuickAdapter.OnItemClickListener mCountyClickListener = (adapter, view, position) -> {
        mCountyAdapter.mSelectorPosistion = position;
        mCountyAdapter.notifyItemChanged(position);
        mAddressRecycler.scrollToPosition(position);
        mCounty = mCountyAdapter.getItem(position);
        mCountyTab.setText(mCounty.name);
        mTown = null;
        mTownAdapter.mSelectorPosistion = -1;
        List<AddressEntity> streetList = mAddressManager.getStreetList(mCounty.id);
        addNextLayer(mTownTab, mTownAdapter, streetList, position);

    };

    private BaseQuickAdapter.OnItemClickListener mTownClickListener = (adapter, view, position) -> {
        mTownAdapter.mSelectorPosistion = position;
        mTownAdapter.notifyItemChanged(position);
        mAddressRecycler.scrollToPosition(position);
        mTown = mTownAdapter.getItem(position);
        mTownTab.setText(mTown.name);
        notifySelectorComplete();
    };

    public static AddressSelectorDialog newInstance() {

        Bundle args = new Bundle();

        AddressSelectorDialog fragment = new AddressSelectorDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.dialog_address_selector, container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DialogViewHolder viewHolder = DialogViewHolder.create(view);
        mTabLay = viewHolder.getView(R.id.tab_lay);
        mAddressRecycler = viewHolder.getView(R.id.address_recycler);
        viewHolder.setOnClickListener(R.id.iv_close, v -> dismiss());
        initializeTab(viewHolder);
        initializeAdapter();
        try {
            fillData();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", e.getMessage());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            //调节灰色背景透明度[0-1]，默认0.5f
            lp.dimAmount = 0.5f;
            //是否在底部显示
            lp.gravity = Gravity.BOTTOM;
            //设置dialog宽度
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            //设置dialog高度
            lp.height = SizeUtils.dp2px(getContext(), 400);
            //设置dialog进入、退出的动画
            window.setWindowAnimations(R.style.Animation_BottomSlide);
            window.setAttributes(lp);
            window.setBackgroundDrawableResource(R.drawable.shape_address_choose);
        }
        setCancelable(true);
    }

    /**
     * 下一级地址列表
     *
     * @param adapter
     * @param datas
     */
    private void addNextLayer(@NonNull TabLayout.Tab tab, @NonNull AddressAdapter adapter, @Nullable List<AddressEntity> datas, @NonNull int selectPos) {
        if (hasNextLayer(datas)) {
            addNextTab(tab);
            addNextRecycler(adapter, datas, selectPos);
        }
    }

    /**
     * 添加下一个
     * @param adapter
     * @param datas
     * @param selectPos
     */
    private void addNextRecycler(@NonNull AddressAdapter adapter, @Nullable List<AddressEntity> datas, @NonNull int selectPos) {
        mAddressRecycler.setAdapter(adapter);
        if (datas != null && !datas.isEmpty()) {
            adapter.setNewData(datas);
        }
        if (selectPos >= 0 && selectPos < adapter.getItemCount()) {
            mAddressRecycler.scrollToPosition(selectPos);
        }
    }

    /**
     * 添加下一个tab
     * @param tab
     */
    private void addNextTab(@NonNull TabLayout.Tab tab) {
        int position = tab.getPosition();
        if (mTabLay.getTabAt(position) == null) {
            if (tab.parent == null) {//由于removeTabAt方法调用了tab内部的reset将parent设置为了null
                tab = mTabLay.newTab().setText(DEFAULT_TAB_TEXT);
            }
            mTabLay.addTab(tab, true);
        } else {
            tab.setText(DEFAULT_TAB_TEXT).select();
            final int tabCount = mTabLay.getTabCount();
            for (int i = tabCount - 1; i >= position + 1; i--) {
                mTabLay.removeTabAt(i);
            }
        }
    }

    /**
     * 是否还有下一级地址
     *
     * @param address
     * @return
     */
    private boolean hasNextLayer(List<AddressEntity> address) {
        if (address == null || address.isEmpty()) {
            notifySelectorComplete();
            return false;
        } else {
            return true;
        }
    }

    /**
     * 选择完毕
     */
    private void notifySelectorComplete() {
        if (mOnSelectorCompleteListener != null) {
            StringBuilder detailAddress = new StringBuilder();
            if (mProvince != null) {
                detailAddress.append(mProvince.name);
            }
            if (mCity != null) {
                detailAddress.append(mCity.name);
            }
            if (mCounty != null) {
                detailAddress.append(mCounty.name);
            }
            if (mTown != null) {
                detailAddress.append(mTown.name);
            }
            mOnSelectorCompleteListener.onComplete(mProvince, mCity, mCounty, mTown, detailAddress.toString());
        }
        dismiss();
    }

    /**
     * 填充recycler数据
     */
    private void fillData() {
        if (mProvince == null) {
            //默认选中省级
            mTabLay.addTab(mProvinceTab);
            mAddressRecycler.setAdapter(mProvinceAdapter);
            List<AddressEntity> provinceList = mAddressManager.getProvinceList();
            mProvinceAdapter.setNewData(provinceList);
            return;
        }
        List<AddressEntity> provinceList = mAddressManager.getProvinceList();
        if (provinceList == null || provinceList.isEmpty()) return;
        int selectPos = findPositionByList(provinceList, mProvince);
        mProvinceAdapter.mSelectorPosistion = selectPos;
        mProvinceAdapter.notifyItemChanged(selectPos);
        mAddressRecycler.scrollToPosition(selectPos);
        mProvinceTab.setText(mProvince.name);
        addNextLayer(mProvinceTab, mProvinceAdapter, provinceList, selectPos);
        if (mCity == null) return;
        List<AddressEntity> cityList = mAddressManager.getCityList(mProvince.id);
        if (cityList == null || cityList.isEmpty()) return;
        selectPos = findPositionByList(cityList, mCity);
        mCityAdapter.mSelectorPosistion = selectPos;
        mCityAdapter.notifyItemChanged(selectPos);
        mAddressRecycler.scrollToPosition(selectPos);
        mCityTab.setText(mCity.name);
        addNextLayer(mCityTab, mCityAdapter, cityList, selectPos);
        if (mCounty == null) return;
        List<AddressEntity> countyList = mAddressManager.getCountyList(mCity.id);
        if (countyList == null || countyList.isEmpty()) return;
        selectPos = findPositionByList(countyList, mCounty);
        mCountyAdapter.mSelectorPosistion = selectPos;
        mCountyAdapter.notifyItemChanged(selectPos);
        mAddressRecycler.scrollToPosition(selectPos);
        mCountyTab.setText(mCounty.name);
        addNextLayer(mCountyTab, mCountyAdapter, countyList, selectPos);
        if (mTown == null) return;
        List<AddressEntity> streetList = mAddressManager.getStreetList(mCounty.id);
        if (streetList == null || streetList.isEmpty()) return;
        selectPos = findPositionByList(streetList, mTown);
        mTownAdapter.mSelectorPosistion = selectPos;
        mTownAdapter.notifyItemChanged(selectPos);
        mAddressRecycler.scrollToPosition(selectPos);
        mTownTab.setText(mTown.name);
        addNextLayer(mTownTab, mTownAdapter, streetList, selectPos);
    }

    /**
     * 查找地址所在的position
     *
     * @param datas
     * @param address
     * @return
     */
    private int findPositionByList(List<AddressEntity> datas, AddressEntity address) {
        for (int i = 0; i < datas.size(); i++) {
            AddressEntity equal = datas.get(i);
            if (equal.id == address.id) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 初始化adapter
     */
    private void initializeAdapter() {
        mProvinceAdapter = new AddressAdapter();
        mProvinceAdapter.setOnItemClickListener(mProvinceClickListener);
        mAddressManager = new AddressDictManager(getContext());
        mCityAdapter = new AddressAdapter();
        mCityAdapter.setOnItemClickListener(mCityClickListener);
        mCountyAdapter = new AddressAdapter();
        mCountyAdapter.setOnItemClickListener(mCountyClickListener);
        mTownAdapter = new AddressAdapter();
        mTownAdapter.setOnItemClickListener(mTownClickListener);
    }

    /**
     * 初始化tab
     *
     * @param holder
     */
    private void initializeTab(DialogViewHolder holder) {
        //省
        mProvinceTab = mTabLay.newTab().setText(DEFAULT_TAB_TEXT);
        //市
        mCityTab = mTabLay.newTab().setText(DEFAULT_TAB_TEXT);
        //县
        mCountyTab = mTabLay.newTab().setText(DEFAULT_TAB_TEXT);
        //镇
        mTownTab = mTabLay.newTab().setText(DEFAULT_TAB_TEXT);

        mTabLay.addOnTabSelectedListener(new SimpleSelectedListener());
    }

    /**
     * 设置完成监听
     *
     * @param listener
     */
    public AddressSelectorDialog setSelectorCompleteListener(OnSelectorCompleteListener listener) {
        this.mOnSelectorCompleteListener = listener;
        return this;
    }

    /**
     * 设置默认选中的地址
     * @return
     */
    public AddressSelectorDialog setSelected(AddressEntity province, AddressEntity city, AddressEntity county, AddressEntity town) {
        this.mProvince = province;
        this.mCity = city;
        this.mCounty = county;
        this.mTown = town;
        return this;
    }

    /**
     * 显示，需要传入一个FragmentManager，因为是继承自DialogFragment
     *
     * @param manager
     * @return
     */
    public AddressSelectorDialog show(FragmentManager manager) {
        FragmentTransaction ft = manager.beginTransaction();
        if (this.isAdded()) {
            ft.remove(this).commit();
        }
        ft.add(this, String.valueOf(System.currentTimeMillis()));
        ft.commitAllowingStateLoss();
        return this;
    }

    class SimpleSelectedListener implements TabLayout.OnTabSelectedListener {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            if (tab.getPosition() == mProvinceTab.getPosition()) {
                addNextRecycler(mProvinceAdapter, null, mProvinceAdapter.mSelectorPosistion);
            } else if (tab.getPosition() == mCityTab.getPosition()) {
                addNextRecycler(mCityAdapter, null, mCityAdapter.mSelectorPosistion);
            } else if (tab.getPosition() == mCountyTab.getPosition()) {
                addNextRecycler(mCountyAdapter, null, mCountyAdapter.mSelectorPosistion);
            } else if (tab.getPosition() == mTownTab.getPosition()) {
                addNextRecycler(mTownAdapter, null, mTownAdapter.mSelectorPosistion);
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }

    class AddressAdapter extends BaseQuickAdapter<AddressEntity, BaseViewHolder> {

        int mSelectorPosistion = -1;

        AddressAdapter() {
            super(R.layout.item_address);
        }

        @Override
        protected void convert(BaseViewHolder helper, AddressEntity item) {
            helper.setText(R.id.tv_name, item.name);
            helper.setGone(R.id.iv_selected, helper.getAdapterPosition() == mSelectorPosistion);
        }
    }

    public interface OnSelectorCompleteListener {
        void onComplete(AddressEntity province, AddressEntity city, AddressEntity county, AddressEntity town, String detailStr);
    }
}
