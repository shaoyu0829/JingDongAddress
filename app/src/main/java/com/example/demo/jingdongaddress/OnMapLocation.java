package com.example.demo.jingdongaddress;

/**
 * Created by Administrator on 2016/9/9.
 */
public interface OnMapLocation {

    /**
     * 成功返回 简单的经纬度
     *@param lg    经度
     * @param lat    纬度
     */
    void onMapSuccess(double lg, double lat);
}
