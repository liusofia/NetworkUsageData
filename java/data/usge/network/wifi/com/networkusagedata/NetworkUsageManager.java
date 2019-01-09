package data.usge.network.wifi.com.networkusagedata;

import android.annotation.TargetApi;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Process;
import android.text.format.Formatter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class NetworkUsageManager {

    static NetworkUsageActivity.ItemListWrapper getDataWrapper(Context context){
        final String TAG = "liuyixi0107";

        HashMap<Integer, Long> mDataCostMapForAllUidsMap;
        NetworkStatsManager mNetworkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);;

        NetworkUsageActivity.ItemListWrapper wrapper = new NetworkUsageActivity.ItemListWrapper();
        //wrap类中的成员变量
        List<NetworkDataAppItem> list = new ArrayList<NetworkDataAppItem>();

        //获取应用流量
        mDataCostMapForAllUidsMap = computeDataUsageforAllUids(mNetworkStatsManager, getDurationTime());

        //数据分两种
        NetworkDataAppItem item = null;
        NetworkDataAppItem systemItem = null;

        long grossUidTotal = 0L;
        long uidTotal = 0L;

        //这里又计算一遍total的值
        for (Integer dcUid : mDataCostMapForAllUidsMap.keySet()) {
            grossUidTotal += mDataCostMapForAllUidsMap.get(dcUid);
        }
        for(Integer dcUid : mDataCostMapForAllUidsMap.keySet()){
            if(getApplicationUids(context).containsKey(dcUid) == true){
                item = new NetworkDataAppItem();
                long one = mDataCostMapForAllUidsMap.get(dcUid);
                //系统应用
                if (beSystem(dcUid) == true) {
                    if (systemItem == null) {
                        systemItem = new NetworkDataAppItem();
                        systemItem.uid = Process.SYSTEM_UID;
                        systemItem.consumeSize = 0L;
                        systemItem.pkgName = "";
                        systemItem.appName = context.getResources().getString(R.string.network_data_consumption_apps_system);
                        systemItem.appIcon = context.getResources().getDrawable(R.drawable.dc_appicon_systemapps);
                    }
                    systemItem.consumeSize += one;
                } else {
                    item.uid = dcUid;
                    item.consumeSize = one;
                    item.pkgName = getApplicationUids(context).get(dcUid).packageName;
                    item.appName = (String) context.getPackageManager().getApplicationLabel(getApplicationUids(context).get(dcUid));
                    item.appIcon = getApplicationUids(context).get(dcUid).loadIcon(context.getPackageManager());
                    list.add(item);
                }
                uidTotal += one;
            }
        }

        long finalTotal = computeTotalUsageDataForDevice(mNetworkStatsManager, getDurationTime());
        if (finalTotal <= grossUidTotal) {
            finalTotal = grossUidTotal;
        }

        if (finalTotal > uidTotal) {
            long gap = finalTotal - uidTotal;
            if (systemItem == null) {
                systemItem = new NetworkDataAppItem();
                systemItem.uid = Process.SYSTEM_UID;
                systemItem.consumeSize = 0L;
                systemItem.pkgName = "";
                systemItem.appName = context.getResources().getString(R.string.network_data_consumption_apps_system);
                systemItem.appIcon = context.getResources().getDrawable(R.drawable.dc_appicon_systemapps);
            }
            systemItem.consumeSize += gap;
        }
        if (systemItem != null) {
            list.add(systemItem);
        }

        //排序
        Collections.sort(list);
        //为Wrapper赋值
        wrapper.result = NetworkUsageActivity.RETRIEVE_RESULT_SUCCESS;
        wrapper.wrapperList = list;
        wrapper.periodTotal = finalTotal;

        // dev
        for (int i = 0; i < wrapper.wrapperList.size(); i++) {
            Log.d(TAG, "=====================> wrapper.wrapperList-" + i + " = " + wrapper.wrapperList.get(i).toString());
        }
        String str = Formatter.formatFileSize(context.getApplicationContext(), uidTotal) + ", " + Formatter.formatFileSize(context.getApplicationContext(), grossUidTotal) + ", " + Formatter.formatFileSize(context, finalTotal);
        Log.d(TAG, "=====================> uidTotal = " + uidTotal + ", grossUidTotal = " + grossUidTotal + ", finalTotal = " + finalTotal + " (" + str + ")");

        return wrapper;
    }

    //获取总流量
    @TargetApi(Build.VERSION_CODES.O)
    public static long computeTotalUsageDataForDevice(NetworkStatsManager networkStatsManager, Duration duration){
        NetworkStats.Bucket bucket = null;
        long result = 0L;
        try{
            int[] types = new int[]{
                    ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_ETHERNET
            };

            for(int i = 0;i < types.length;i++){
                //结果是整个设备的汇总数据使用情况
                bucket = networkStatsManager.querySummaryForDevice(types[i], "", duration.start, duration.end);
                if (bucket != null) {
                    long resultTmp = bucket.getRxBytes() + bucket.getTxBytes();
                    result += resultTmp;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取应用uids
     * @return
     */
    public static Map<Integer, ApplicationInfo> getApplicationUids(Context context){
        //内核分配给application 的内核用户标识
        Map<Integer, ApplicationInfo> uids = new HashMap<Integer, ApplicationInfo>();
        //获取设备上安装应用的列表
        List<ApplicationInfo> applications = context.getPackageManager().getInstalledApplications(0);
        for (int i = 0; i < applications.size(); i++) {
            int uid = applications.get(i).uid;
            uids.put(uid, applications.get(i));
//            Log.d(TAG,"processName = " + applications.get(i).processName + "  / uid = " + uid);
        }
        return uids;
    }

    @TargetApi(Build.VERSION_CODES.O)
    //根据UID 查询网络使用情况
    private static HashMap<Integer, Long> computeDataUsageforAllUids(NetworkStatsManager networkStatsManager, Duration duration) {
        NetworkStats stats = null;
        HashMap<Integer, Long> map = new HashMap<Integer, Long>();
        int bucketUid = -1;
        long result = 0L;
        int count;
        try {
            //参数1:定义在ContivityManager中的网络接口类型
            //参数2:网络接口的用户标识
            int[] types = new int[]{
                    ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_ETHERNET
            };

            for (int i = 0; i < types.length; i++) {
                stats = networkStatsManager.querySummary(types[i], "", duration.start, duration.end);
                if (stats != null) {
                    count = 0;
                    do {
                        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                        boolean success = stats.getNextBucket(bucket);
                        if (success == true) {
                            result = bucket.getRxBytes() + bucket.getTxBytes();
                            bucketUid = bucket.getUid();
                            //app uid 或 通过网络共享使用数据的uid 或 系统uid
                            long last = (map.get(bucketUid) == null ? 0L : map.get(bucketUid));
                            map.put(bucketUid, last + result);
                        }
//                        Log.d(TAG,"computeDataUsageforAllUids() into , count = " + count + ", success = " + success + ", bucketUid = " + bucketUid + ", result = " + result);
                        count++;
                    } while (stats.hasNextBucket());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "computeDataUsageforAllUids(), map.size = " + map.size());

        // dev
        for (Integer key : map.keySet()) {
//            Log.d(TAG, "buckt-map(" + key + ") = " + map.get(key));
        }
        return map;
    }

    //获取时间
    private static class Duration {
        long start = 0L; // millis
        long end = 0L;
        Duration(long start, long end) {
            this.start = start;
            this.end = end;
        }
        @Override
        public String toString() {
            return "" + start + "/" + end;
        }
    }

    //用于获取起始时间
    public static Duration getDurationTime(){
        //用于获得日期
        Calendar calendar = Calendar.getInstance();
        //设置年,月,日 为0,0,0
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        //返回一个月中的第几天 返回一个月中的最后一天
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        //返回以毫秒为单位的日历，返回当前时间
        Duration duration = new Duration(calendar.getTimeInMillis(), System.currentTimeMillis());

        return duration;
    }

    private static boolean beSystem(int uid) {
        if ((uid == Process.SYSTEM_UID)
                || (uid == 0)       // ROOT_UID
                || (uid == 2000)) { // SHELL_UID
            return true;
        }
        return false;
    }
}
