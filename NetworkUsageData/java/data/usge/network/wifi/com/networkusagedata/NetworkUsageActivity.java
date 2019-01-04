package data.usge.network.wifi.com.networkusagedata;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;
import java.util.HashMap;


public class NetworkUsageActivity extends Activity {

    private static final String TAG = "NetworkUsageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_usage);
        //获取包名的uid
//        getUidByPackageName();

        //动态获取权限
//        hasPermissionToReadNetworkStats();

        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
        //获取总流量
        getSummaryForDevice(networkStatsManager);

        //获取应用流量
        HashMap<Integer, Long> dcMap = getDcforAllUids(networkStatsManager, getDurationTime());

//        computeTotalConsumption(networkStatsManager,getDurationTime());
    }

    public void getSummaryForDevice(NetworkStatsManager networkStatsManager){
        NetworkStats.Bucket bucket = null;
        try{
            bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, "", 0, System.currentTimeMillis());
            Log.d(TAG, "Total: " + (bucket.getRxBytes() + bucket.getTxBytes()));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean hasPermissionToReadNetworkStats() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }

        requestReadNetworkStats();
        return false;
    }
    // 打开“有权查看使用情况的应用”页面
    private void requestReadNetworkStats() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static HashMap<Integer, Long> getDcforAllUids(NetworkStatsManager networkStatsManager, Duration duration) {
        Log.d(TAG,"getDcforAllUids into");
        NetworkStats stats = null;
        HashMap<Integer, Long> map = new HashMap<Integer, Long>();
        int bucketUid = -1;
        long rt = 0L;
        int count = 0;
        try {
            int[] types = new int[]{ ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_ETHERNET };
            for (int i = 0; i < types.length; i++) {
                stats = networkStatsManager.querySummary(ConnectivityManager.TYPE_WIFI, "", getTimesMonthMorning(), System.currentTimeMillis());
                if (stats != null) {
                    count = 0;
                    do {
                        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
                        boolean bl = stats.getNextBucket(bucket);
                        if (bl == true) {
                            rt = bucket.getRxBytes() + bucket.getTxBytes();
                            bucketUid = bucket.getUid();
                            long last = (map.get(bucketUid) == null ? 0L : map.get(bucketUid));
                            map.put(bucketUid, last + rt);
                        }
                        Log.d(TAG,"getDcforAllUids() into , count = " + count + ", bl = " + bl + ", bucketUid = " + bucketUid + ", rt = " + rt);
                        count++;

                    } while (stats.hasNextBucket());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "================> getDcforAllUids, map.size = " + map.size());

        // dev
        for (Integer key : map.keySet()) {
            Log.i(TAG, "=====================> buckt-map(" + key + ") = " + map.get(key));
        }

        return map;
    }

    public static long getTimesMonthMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTimeInMillis();
    }

//    //查询设备总的流量统计信息
//    private static long computeTotalConsumption(NetworkStatsManager networkStatsManager, Duration duration) {
//        long rt = 0L;
//        try {
//            int[] types = new int[]{ ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_ETHERNET };
//            for (int i = 0; i < types.length; i++) {
//                //第二个参数为网络接口标示
//                NetworkStats.Bucket bucket = networkStatsManager.querySummaryForDevice(types[i], null, duration.start, duration.end);
//                if (bucket != null) {
//                    long rtTmp = bucket.getRxBytes() + bucket.getTxBytes();
//                    rt += rtTmp;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Log.i(TAG, "================> computeTotalConsumption, rt = " + rt);
//        return rt;
//    }

//    public static int getUidByPackageName(Context context, String packageName) {
//        int packageUid = -1;
//        PackageManager packageManager = context.getPackageManager();
//        try{
//            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
//            packageUid = packageInfo.applicationInfo.uid;
//            Log.d("liuyixi", packageInfo.packageName + " uid: " + uid);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return packageUid;
//    }

    //用于获取起始时间
    public Duration getDurationTime(){
        //用于获得日期
        Calendar calendar = Calendar.getInstance();
        //设置年,月,日 为0,0,0
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        //返回一个月中的第几天 返回一个月中的最后一天
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        //返回以毫秒为单位的日历，返回当前时间
        Duration duration = new Duration(calendar.getTimeInMillis(), System.currentTimeMillis());
        Log.d(TAG,"duration : " + duration);

        return duration;
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
}
