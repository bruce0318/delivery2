package com.example.blindclient;

import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.SimpleNaviListener;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.enums.TravelStrategy;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.NaviPoi;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.List;
import java.util.Objects;

public class Navigation {
    Context context;
    Context applicationContext;

    private PoiSearch.Query query; // 搜索目的地
    private AMapLocationClient mLocationClient; // 定位
    private String keywords; // 定位目的地
    private PoiSearch poiSearch; // 搜索目的地
    private PoiResult poiResult; // poi返回的结果
    private NaviPoi start; // 起点，即当前位置
    private NaviPoi end; // 终点
    private AMapNavi navi; // 导航
    private AMapNaviListener naviListener = new SimpleNaviListener(){
        @Override
        public void onCalculateRouteSuccess(AMapCalcRouteResult routeResult) {
            // 开启导航
            navi.startNavi(NaviType.GPS);
            navi.startSpeak();
        }

        @Override
        public void onCalculateRouteFailure(AMapCalcRouteResult routeResult){
            Log.e("Navigation", "路线计算失败!");
        }
    };

    // 搜索目的地结果监听
    PoiSearch.OnPoiSearchListener searchListener = new PoiSearch.OnPoiSearchListener() {
        @Override
        public void onPoiSearched(PoiResult result, int i) {
            if (result != null && result.getQuery() != null && result.getQuery().equals(query)) {
                // 搜索poi的结果
                poiResult = result;
                // 取得搜索到的poiitems有多少页
                List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                end = new NaviPoi(
                        poiItems.get(0).getAdName(),
                        new LatLng(poiItems.get(0).getLatLonPoint().getLatitude(), poiItems.get(0).getLatLonPoint().getLongitude()),
                        poiItems.get(0).getPoiId());
                Log.i("Navigation", "终点确定成功");
                startNavi();
            }
        }

        @Override
        public void onPoiItemSearched(PoiItem poiItem, int i) {

        }
    };

    // 定位结果监听
    AMapLocationListener mAMapLocationListener = new AMapLocationListener(){
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if(aMapLocation.getErrorCode() == 0){
                // 设置起点
                start = new NaviPoi("", new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()), "");
                Log.i("Navigation", "起点确定成功");
                // 获取当前城市
                String city = aMapLocation.getCity();
                // 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
                query = new PoiSearch.Query(keywords, "", city);
                // 设置每页最多返回多少条poiitem
                query.setPageSize(10);
                // 设置查第一页
                query.setPageNum(1);

                try {
                    poiSearch = new PoiSearch(context, query);
                } catch (AMapException e) {
                    Log.e("Navigation", e.getErrorMessage());
                }
                poiSearch.setOnPoiSearchListener(searchListener);
                poiSearch.searchPOIAsyn();
            }
            else{
                // 定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    };

    public Navigation(String keywords, Context context, Context applicationContext) {
        // 记录目的地
        this.keywords = keywords;
        this.context = context;
        this.applicationContext = applicationContext;
        try{
            this.mLocationClient = new AMapLocationClient(applicationContext);
            this.startLoc();
        }
        catch(Exception e){
            Log.e("Navigation", Objects.requireNonNull(e.getLocalizedMessage()));
        }
    }

    public void startLoc(){
        // 定位获取城市信息
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        // 定位模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 定位次数：1
        locationOption.setOnceLocation(true);
        locationOption.setOnceLocationLatest(true);
        // 定位设置
        mLocationClient.setLocationOption(locationOption);
        // 设置监听
        mLocationClient.setLocationListener(mAMapLocationListener);
        // 开始定位
        mLocationClient.startLocation();
    }

    private void startNavi() {
        try{
            navi = AMapNavi.getInstance(applicationContext);

            // 启动组件
            navi.addAMapNaviListener(naviListener);

            // 设置语音播报
            navi.setUseInnerVoice(true, false);

            // 计算路线
            navi.calculateWalkRoute(start, end, TravelStrategy.SINGLE);
        }
        catch(com.amap.api.maps.AMapException e){
            Log.e("Navigation", e.getErrorMessage());
        }
    }

    public void stopNavi(){
        navi.stopNavi();
        navi.stopSpeak();
    }
}
