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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.format.Formatter.formatFileSize;

public class NetworkUsageActivity extends Activity {

    private static final String TAG = "liuyixi0107";
    private HashMap<Integer, Long> mDataCostMapForAllUids;
    private NetworkStatsManager mNetworkStatsManager;

    private RecyclerView list;
    private NetworkUsageAdapter adapter;

//    private RetrieveDataAsyncTask retrieveDataTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNetworkStatsManager = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);

        setContentView(R.layout.activity_network_usage);
        TextView showDataUssageStatsTitle = findViewById(R.id.data_usage_stats_title);
        showDataUssageStatsTitle.setVisibility(View.VISIBLE);
        ///////////////////////////////////////////////////////////////////////////////////////
        //专门用于盛放app信息的list
        list = findViewById(R.id.dataUsageList);
        //用于显示各个应用的list
        list.setVerticalScrollBarEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));

        adapter = new NetworkUsageAdapter(this);
        list.setAdapter(adapter);
        ///////////////////////////////////////////////////////////////////////////////////////
        //获取总流量
        computeTotalUsageDataForDevice(mNetworkStatsManager,getDurationTime());
        //获取应用流量
        mDataCostMapForAllUids = computeDataUsageforAllUids(mNetworkStatsManager, getDurationTime());

        //获取一个应用的uid
//        getApplicationUids();
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
        String total = String.format(getResources().getString(R.string.data_usage_total_summay_text), formatFileSize(getApplicationContext(),result));
        TextView showTotalDataUsage = ((TextView)findViewById(R.id.data_usage_total_summay));
        showTotalDataUsage.setText(total);
        showTotalDataUsage.setVisibility(View.VISIBLE);
        Log.d(TAG, "Total Usage Data For Device : " + total);
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

///************************************************************************************************************////
    //获取application 的 packageName/processName
//    public void getApplicationUids(){
//        //内核分配给application 的内核用户标识
//        Map<Integer, ApplicationInfo> uids = new HashMap<Integer, ApplicationInfo>();
//        //获取设备上安装应用的列表
//        List<ApplicationInfo> applications = getPackageManager().getInstalledApplications(0);
//        for (int i = 0; i < applications.size(); i++) {
//            int uid = applications.get(i).uid;
//            uids.put(uid, applications.get(i));
//            Log.d(TAG,"processName = " + applications.get(i).processName + "  / uid = " + uid);
//        }
//
//        //获取一个应用的data usage
//        computeDataUsageforOneUid(10036);
//    }

//
//    @TargetApi(Build.VERSION_CODES.O)
//    //根据UID 查询网络使用情况
//    private void computeDataUsageforOneUid(Integer uid) {
//        Log.d(TAG,"进入查找一个应用使用流量统计方法");
//        for (Integer key : mDataCostMapForAllUids.keySet()) {
//            if(key.equals(uid)){
//                Log.d(TAG, "查找一个：buckt-map(" + key + ") = " + mDataCostMapForAllUids.get(key));
//                Log.d(TAG,"查找小米Tv播放器 uid = " + uid);
//                TextView showTotalDataUsage = (findViewById(R.id.showOneAppDataUsage));
//                showTotalDataUsage.setText(Formatter.formatFileSize(getApplicationContext(), mDataCostMapForAllUids.get(key)));
//            }
//        }
//    }
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
