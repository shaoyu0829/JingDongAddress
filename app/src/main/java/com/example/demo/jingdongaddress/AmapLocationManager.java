package com.example.demo.jingdongaddress;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;

/**
 * Created by Administrator on 2016/9/12.
 */
public class AmapLocationManager {
    private static final AmapLocationManager mapLocationManager = new AmapLocationManager();
    public AMapLocationClient mLocationClient;
    public AMapLocationClientOption mLocationOption;
    private OnMapLocation onMapLocation;
    private Context context;

    /**
     * 获取当前的地址（同时纪录最新经纬度）
     *
     * @param context
     * @param onMapLocation
     */
    public void getLocationMessage(Context context, OnMapLocation onMapLocation) {
        this.context = context;
        this.onMapLocation = onMapLocation;
        mLocationOption = new AMapLocationClientOption();
        mLocationClient = new AMapLocationClient(context);
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        mLocationOption.setOnceLocation(true);
        mLocationOption.setLocationCacheEnable(false);
//        mLocationOption.setWifiActiveScan(true);
        mLocationClient.setLocationListener(mLocationListener);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
    }
    public static AmapLocationManager getInstance() {

        return mapLocationManager;
    }

    private AMapLocationListener mLocationListener=new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            mLocationClient.stopLocation();
            if (aMapLocation.getErrorCode()==0){
                onMapLocation.onMapSuccess(aMapLocation.getLongitude(), aMapLocation.getLatitude());
            }
        }
    };

    public void getDistrict(Context context, int type, String keyword, DistrictSearch.OnDistrictSearchListener onDistrictSearchListener){
        DistrictSearch search = new DistrictSearch(context);
        DistrictSearchQuery query = new DistrictSearchQuery();
        query.setKeywords(keyword);//传入关键字
        query.setShowBoundary(false);//是否返回边界值
//        String keywordsLevel="";
//        if (type==0){
//            keywordsLevel= DistrictSearchQuery.KEYWORDS_COUNTRY;
//        }else if (type==1){
//            keywordsLevel=DistrictSearchQuery.KEYWORDS_PROVINCE;
//        }else if (type==2){
//            keywordsLevel=DistrictSearchQuery.KEYWORDS_CITY;
//        }else {
//            keywordsLevel=DistrictSearchQuery.KEYWORDS_BUSINESS;
//        }
//        query.setKeywordsLevel(keywordsLevel);
        query.setShowChild(true);
        search.setQuery(query);

        search.setOnDistrictSearchListener(onDistrictSearchListener);//绑定监听器
        search.searchDistrictAsyn();//开始搜索
    }
}

