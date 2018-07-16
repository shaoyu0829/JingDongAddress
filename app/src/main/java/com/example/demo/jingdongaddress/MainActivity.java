package com.example.demo.jingdongaddress;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemClickListener{


    //省份的数据集合
    private List<DistrictItem> provinces;
    //城市的数据集合
    private List<DistrictItem> cities;
    //县和区域的数据集合
    private List<DistrictItem> areas;
    //村和街道的数据集合
    private List<DistrictItem> streets;
    //被选中的省份
    private String selectProvince="";
    //被选中的城市
    private String selectCity="";
    //被选中的县或区域
    private String selectArea="";
    //被选中的村或街道
    private String selectStreet="";
    //顶部的tab
    private TabLayout mTabLayoot;
    //地址列表的控件
    private ListView lvAddress;
    //地址弹窗
    private PopupWindow addressPop;
    //第一个tab（省份的tab）
    private TabLayout.Tab tabProvince;
    //第二个tab（城市的tab）
    private TabLayout.Tab tabCity;
    //第三个tab（县或区的tab）
    private TabLayout.Tab tabArea;
    //第四个tab（街道的tab）
    private TabLayout.Tab tabStreet;
    //被选中的tab的角标，默认进去是0
    private int tabIndex;
    //被选中的省份在列表中的位置，默认没有选中的为-1
    private int provinceIndex =-1;
    //被选中的城市在列表中的位置，默认没有选中的为-1
    private int cityIndex = -1;
    //被选中的县或区在列表中的位置，默认没有选中的为-1
    private int areaIndex = -1;
    //被选中的街道在列表中的位置，默认没有选中的为-1
    private int streetIndex = -1;
    //列表的适配器
    private AddressAdapter adapter;
    private TextView tvAddress;
    private String adcode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initview();
        initPopupWindow();
    }

    //初始化控件
    private void initview() {
        findViewById(R.id.rl_address).setOnClickListener(this);
        tvAddress = ((TextView) findViewById(R.id.tv_address));
    }

    //初始化地址选择弹窗
    private void initPopupWindow() {
        //加载弹窗布局
        View popView = View.inflate(this, R.layout.address_choose_pw, null);
        //弹窗布局中的顶部tab
        mTabLayoot = ((TabLayout) popView.findViewById(R.id.tablayout));
        //地址列表控件
        lvAddress = ((ListView) popView.findViewById(R.id.lv_address));
        addressPop = new PopupWindow(popView, LinearLayout.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(this,300));
        //给popupwindow设置进出动画
        addressPop.setAnimationStyle(R.style.addressAnimation);
        addressPop.setFocusable(true);
        addressPop.setBackgroundDrawable(new ColorDrawable());
        //popwindow隐藏时背景恢复
        addressPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 1f;
                getWindow().setAttributes(lp);
                //弹窗消失的时候会认为地址选择完毕，选中的地址就是选中的省市区街道拼接起来的
                String address=selectProvince+selectCity+selectArea+selectStreet;
                if (!TextUtils.isEmpty(address)){
                    tvAddress.setText(address);
                    Toast.makeText(MainActivity.this,address,Toast.LENGTH_SHORT).show();
                }else {
                    tvAddress.setText("请选择地址");
                }
            }
        });
        //初始化的时候只有一个tab，显示省份的列表
        adapter = new AddressAdapter(provinces);
        lvAddress.setAdapter(adapter);
        lvAddress.setOnItemClickListener(this);
        tabProvince = mTabLayoot.newTab().setText("请选择");
//        tabCity = mTabLayoot.newTab().setText("请选择");
//        tabArea = mTabLayoot.newTab().setText("请选择");
//        tabStreet = mTabLayoot.newTab().setText("请选择");
//        mTabLayoot.addTab(tabProvince);
        if (!TextUtils.isEmpty(selectProvince)){
            tabProvince.setText(selectProvince);
        }
        //进页面默认开始搜索中国的省份
        searchDistrict(0,"中国");
        //tab选择监听
        mTabLayoot.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabIndex=tab.getPosition();
                if (tabIndex==0){
                    //第一个tab选中的时候，给列表设置省份的数据
                    adapter.setData(provinces);
                    //设置被选中的省份
                    adapter.setSelect(provinceIndex);
                    //列表要移动到选中的地方
                    lvAddress.setSelection(provinceIndex==-1?0:provinceIndex);
                }else if (tabIndex==1){
                    adapter.setData(cities);
                    adapter.setSelect(cityIndex);
                    lvAddress.setSelection(cityIndex==-1?0:cityIndex);
                }else if (tabIndex==2){
                    adapter.setData(areas);
                    adapter.setSelect(areaIndex);
                    lvAddress.setSelection(areaIndex==-1?0:areaIndex);
                }else {
                    adapter.setData(streets);
                    adapter.setSelect(streetIndex);
                    lvAddress.setSelection(streetIndex==-1?0:streetIndex);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    /**
     * 搜索子行政区域的方法
     * @param type      对应的搜索类型（0：国家 1：省份 2：城市 3：县或区）
     * @param keyword   搜索的关键字
     */
    private void searchDistrict(final int type, String keyword){
        AmapLocationManager.getInstance().getDistrict(this,type,keyword, new DistrictSearch.OnDistrictSearchListener() {
            @Override
            public void onDistrictSearched(DistrictResult districtResult) {
                ArrayList<DistrictItem> district = districtResult.getDistrict();
                DistrictItem districtItem = district.get(districtResult.getPageCount()-1);
                //搜索到数据后，清除之前的数据
                clearData(type);
                if (type==0){
                    //列表设置省份的数据，tab清空，然后重新添加省份的tab
                    provinces=districtItem.getSubDistrict();
                    adapter.setData(provinces);
                }else if (type==1){
                    cities=districtItem.getSubDistrict();
                    adcode = districtItem.getAdcode();
                    if (cities!=null&&cities.size()>0){
                        //有城市数据则进行后面操作，清除所有tab，然后添加省份的tab和

                        mTabLayoot.removeAllTabs();
                        tabProvince = mTabLayoot.newTab().setText(selectProvince);
                        mTabLayoot.addTab(tabProvince);
                        tabCity = mTabLayoot.newTab().setText("请选择");
                        mTabLayoot.addTab(tabCity);
                        tabIndex=1;
                        tabCity.select();
                        adapter.setData(cities);
                        cityIndex=-1;
                        areaIndex=-1;
                        streetIndex=-1;
                        //取消被选中的
                        adapter.setSelect(-1);
                    }else {
                        //没有数据就让地址选择弹窗消失，地址选择完毕
                        addressPop.dismiss();
                    }
                }else if (type==2){
                    areas=districtItem.getSubDistrict();
                    adcode = districtItem.getAdcode();
                    if (areas!=null&&areas.size()>0){
                        mTabLayoot.removeAllTabs();
                        tabProvince = mTabLayoot.newTab().setText(selectProvince);
                        mTabLayoot.addTab(tabProvince);
                        tabCity = mTabLayoot.newTab().setText(selectCity);
                        mTabLayoot.addTab(tabCity);
                        tabArea = mTabLayoot.newTab().setText("请选择");
                        mTabLayoot.addTab(tabArea);
                        tabArea.select();
                        adapter.setData(areas);
                        tabIndex=2;
                        areaIndex=-1;
                        streetIndex=-1;
                        adapter.setSelect(-1);
                    }else {
                        addressPop.dismiss();
                    }
                }else if (type==3){
                    streets=districtItem.getSubDistrict();
                    if (streets==null||streets.size()==0){
                        addressPop.dismiss();
                    }else {
                        mTabLayoot.removeAllTabs();
                        tabProvince = mTabLayoot.newTab().setText(selectProvince);
                        mTabLayoot.addTab(tabProvince);
                        tabCity = mTabLayoot.newTab().setText(selectCity);
                        mTabLayoot.addTab(tabCity);
                        tabArea = mTabLayoot.newTab().setText(selectArea);
                        mTabLayoot.addTab(tabArea);
                        tabStreet = mTabLayoot.newTab().setText("请选择");
                        mTabLayoot.addTab(tabStreet);
                        adapter.setData(streets);
                        tabIndex=3;
                        tabStreet.select();
                        streetIndex=-1;
                        adapter.setSelect(-1);
                    }
                }
                lvAddress.setSelection(0);
            }
        });
    }

    /**
     * 清除数据的方法
     * @param position  当前搜索type
     */
    private void clearData(int position) {
        if (position==0){
            if (provinces!=null){
                provinces.clear();
            }
            if (cities!=null){
                cities.clear();
            }
            if (areas!=null){
                areas.clear();
            }
            if (streets!=null){
                streets.clear();
            }
            mTabLayoot.removeAllTabs();
            mTabLayoot.addTab(tabProvince);
            tabProvince.select();
            provinceIndex=-1;
            cityIndex=-1;
            areaIndex=-1;
            streetIndex=-1;
            adapter.setSelect(-1);
        }else if (position==1){
            if (cities!=null){
                cities.clear();
            }
            if (areas!=null){
                areas.clear();
            }
            if (streets!=null){
                streets.clear();
            }
            if (tabCity!=null){
                tabCity.setText("请选择");
            }else {
                tabCity= mTabLayoot.newTab().setText("请选择");
            }
        }else if (position==2){
            if (areas!=null){
                areas.clear();
            }
            if (streets!=null){
                streets.clear();
            }
            if (tabArea!=null){
                tabArea.setText("请选择");
            }else {
                tabArea=mTabLayoot.newTab().setText("请选择");
            }
        }else {
            if (streets!=null){
                streets.clear();
            }
            if (tabStreet!=null){
                tabStreet.setText("请选择");
            }else {
                tabStreet=mTabLayoot.newTab().setText("请选择");
            }
        }
    }

    //listview的条目点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (tabIndex==0){
            //当tabindex的时候，条目点击选中的是省份数据，选中之后，搜索省份对应的子行政区域
            tabProvince.setText(provinces.get(position).getName());
            searchDistrict(1,provinces.get(position).getName());
            provinceIndex=position;
            //设置选中的省份
            adapter.setSelect(provinceIndex);
            selectProvince=provinces.get(position).getName();
            //每次点击省份之后，后面所有的子行政区域全部置空，重新点击了之后才赋值，后面同理
            selectCity="";
            selectArea="";
            selectStreet="";
        }else if (tabIndex==1){
            tabCity.setText(cities.get(position).getName());
            searchDistrict(2,cities.get(position).getName());
            cityIndex=position;
            adapter.setSelect(cityIndex);
            selectCity=cities.get(position).getName();
            selectArea="";
            selectStreet="";
        }else if (tabIndex==2){
            tabArea.setText(areas.get(position).getName());
            searchDistrict(3,areas.get(position).getName());
            areaIndex=position;
            adapter.setSelect(areaIndex);
            selectArea=areas.get(position).getName();
            selectStreet="";
        }else {
            tabStreet.setText(streets.get(position).getName());
            streetIndex=position;
            adapter.setSelect(streetIndex);
            selectStreet=streets.get(position).getName();
            addressPop.dismiss();
        }
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rl_address:
                showPopupwindow(selectProvince,selectCity,selectArea,selectStreet);
                break;
        }
    }

    //弹出popupwindow
    private void showPopupwindow(String selectProvince, String selectCity, String selectArea, String selectStreet) {
        addressPop.showAsDropDown(findViewById(R.id.divier));
        //popwindow弹出时背景变灰
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (addressPop.isShowing()) {
            lp.alpha = 0.6f;
            getWindow().setAttributes(lp);
        }
    }

    //列表的适配器
    class AddressAdapter extends BaseAdapter {
        //列表数据
        private List<DistrictItem> districtItems;
        //被选中的条目，默认都不选中，为-1
        private int mSelect=-1;

        public AddressAdapter(List<DistrictItem> districtItems){
            this.districtItems=districtItems;
        }
        @Override
        public int getCount() {
            return districtItems==null?0:districtItems.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;
            if (convertView==null){
                holder=new ViewHolder();
                convertView=View.inflate(MainActivity.this,R.layout.item_area,null);
                holder.tvAddress= (TextView) convertView.findViewById(R.id.textView);
                holder.isSelect= (ImageView) convertView.findViewById(R.id.imageViewCheckMark);
                convertView.setTag(holder);
            }
            holder= (ViewHolder) convertView.getTag();
            DistrictItem districtItem = districtItems.get(position);
            holder.tvAddress.setText(districtItem.getName());
            //被选中的字体变红，后面带选中标志
            if (mSelect==-1){
                holder.tvAddress.setTextColor(Color.parseColor("#353535"));
                holder.isSelect.setVisibility(View.GONE);
            }else if (position==mSelect){
                holder.tvAddress.setTextColor(Color.parseColor("#E94715"));
                holder.isSelect.setVisibility(View.VISIBLE);
            }else {
                holder.tvAddress.setTextColor(Color.parseColor("#353535"));
                holder.isSelect.setVisibility(View.GONE);
            }

            return convertView;
        }

        /**
         * 设置选中的条目的方法
         * @param selectPosition    选中的位置
         */
        public void setSelect(int selectPosition){
            if (mSelect!=selectPosition){
                mSelect=selectPosition;
                notifyDataSetChanged();
            }
        }

        /**
         * 设置数据的方法
         * @param districtItems 新数据
         */
        public void setData(List<DistrictItem> districtItems){
            if (districtItems!=null){
                this.districtItems=districtItems;
                notifyDataSetChanged();
            }
        }

        class ViewHolder {
            ImageView isSelect;
            TextView tvAddress;
        }
    }
}
