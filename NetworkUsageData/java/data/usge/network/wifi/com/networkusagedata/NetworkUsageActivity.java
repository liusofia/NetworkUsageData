package data.usge.network.wifi.com.networkusagedata;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
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

        NetworkStatsManager networkStatsManager = (NetworkStatsManager) getSystemService(Context.NETWORK_STATS_SERVICE);
        //获取总流量
        getSummaryForDevice(networkStatsManager);

        //获取应用流量
        HashMap<Integer, Long> dcMap = getDcforAllUids(networkStatsManager, getDurationTime());
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

    @TargetApi(Build.VERSION_CODES.O)
    private static HashMap<Integer, Long> getDcforAllUids(NetworkStatsManager networkStatsManager, Duration duration) {
        NetworkStats stats = null;
        HashMap<Integer, Long> map = new HashMap<Integer, Long>();
        int bucketUid = -1;
        long rt = 0L;
        int count = 0;
        try {
            int[] types = new int[]{ ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_ETHERNET };
            for (int i = 0; i < types.length; i++) {
                stats = networkStatsManager.querySummary(types[i], "", duration.start, duration.end);
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
