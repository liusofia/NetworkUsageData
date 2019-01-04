package data.usge.network.wifi.com.networkusagedata;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkUsageActivity extends Activity {

    private static final String TAG = "liuyixi0103";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_usage);
        TextView showDataUssageStatsTitle = findViewById(R.id.data_usage_stats_title);
        showDataUssageStatsTitle.setVisibility(View.VISIBLE);

        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
        //获取总流量
        computeTotalUsageDataForDevice(networkStatsManager,getDurationTime());
        //获取应用流量
        HashMap<Integer, Long> dcMap = computeDataUsageforAllUids(networkStatsManager, getDurationTime());
        //获取应用的uid
        getApplicationUids();
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void computeTotalUsageDataForDevice(NetworkStatsManager networkStatsManager, Duration duration){
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
        String total = String.format(getResources().getString(R.string.data_usage_total_summay_text), Formatter.formatFileSize(getApplicationContext(),result));
        TextView showTotalDataUsage = ((TextView)findViewById(R.id.data_usage_total_summay));
        showTotalDataUsage.setText(total);
        showTotalDataUsage.setVisibility(View.VISIBLE);
        Log.d(TAG, "Total: " + total);
    }

    //获取application 的 packageName/processName
    public void getApplicationUids(){
        //内核分配给application 的内核用户标识
        Map<Integer, ApplicationInfo> uids = new HashMap<Integer, ApplicationInfo>();
        //获取设备上安装应用的列表
        List<ApplicationInfo> applications = getPackageManager().getInstalledApplications(0);
        for (int i = 0; i < applications.size(); i++) {
            int uid = applications.get(i).uid;
            uids.put(uid, applications.get(i));
            Log.d(TAG,"processName = " + applications.get(i).processName + "  / uid = " + uid);
        }
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
                        Log.d(TAG,"computeDataUsageforAllUids() into , count = " + count + ", success = " + success + ", bucketUid = " + bucketUid + ", result = " + result);
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
            Log.d(TAG, "buckt-map(" + key + ") = " + map.get(key));
        }
        return map;
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
