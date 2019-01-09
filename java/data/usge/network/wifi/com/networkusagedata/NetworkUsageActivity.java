package data.usge.network.wifi.com.networkusagedata;

import android.app.Activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.util.List;

public class NetworkUsageActivity extends Activity {
    private static final String TAG = "liuyixi0107";
    static final int RETRIEVE_RESULT_FAILED = -2;
    static final int RETRIEVE_RESULT_UNKNOWN = -1;
    static final int RETRIEVE_RESULT_SUCCESS = 0;

    //专门用于盛放app信息的RecyclerView
    private RecyclerView RecyclerViewList;
    private NetworkUsageAdapter adapter;

    private RetrieveDataAsyncTask retrieveDataTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_network_usage);
        TextView showDataUssageStatsTitle = findViewById(R.id.data_usage_stats_title);
        showDataUssageStatsTitle.setVisibility(View.VISIBLE);

        RecyclerViewList = findViewById(R.id.dataUsageList);
        //用于显示各个应用的list
        RecyclerViewList.setVerticalScrollBarEnabled(false);//去掉滚动条
        RecyclerViewList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));

        adapter = new NetworkUsageAdapter(this);
        RecyclerViewList.setAdapter(adapter);

        //用于进行加载
        loading(true);
        findViewById(R.id.data_usage_total_summay).setVisibility(View.INVISIBLE);

        //启动一个线程
        retrieveDataTask = new RetrieveDataAsyncTask();
        retrieveDataTask.execute(new Long(System.currentTimeMillis()));
    }

    private void loading(boolean ing) {
        findViewById(R.id.processingRelayout).setVisibility(ing ? View.VISIBLE : View.GONE);
    }

    //线程
    private class RetrieveDataAsyncTask extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            ItemListWrapper AsyncTaskWrapper = null;
            try{
                SystemClock.sleep(3000);
                AsyncTaskWrapper = NetworkUsageManager.getDataWrapper(getApplicationContext());
                Log.d(TAG,"Activity.java :: RetrieveDataAsyncTask() into return = " + AsyncTaskWrapper);
            }catch (Exception e){
                e.printStackTrace();
            }
            return AsyncTaskWrapper;
        }

        @Override
        protected void onPostExecute(Object obj) {
            loading(false);
            ItemListWrapper wrapper = (obj == null ? null : (ItemListWrapper) obj);
            int ret = (wrapper == null ? RETRIEVE_RESULT_FAILED : wrapper.result);
            if(ret == RETRIEVE_RESULT_SUCCESS){
                Log.d(TAG,"Activity.java :: onPostExecute() RETRIEVE_RESULT_SUCCESS");
                if(adapter != null){
                    adapter.setList(wrapper.wrapperList);
                    adapter.notifyDataSetChanged();

                    String text = String.format(getResources().getString(R.string.data_usage_total_summay_text), Formatter.formatFileSize(getApplicationContext(), wrapper.periodTotal));
                    ((TextView) findViewById(R.id.data_usage_total_summay)).setText(text);
                    findViewById(R.id.data_usage_total_summay).setVisibility(View.VISIBLE);

                    RecyclerViewList.post(new Runnable() {
                        @Override
                        public void run() {
                            RecyclerViewList.scrollToPosition(0);
                            RecyclerViewList.requestLayout();
                        }
                    });
                }
            }else{
                if ((ret == RETRIEVE_RESULT_FAILED) || (wrapper.wrapperList == null) || (wrapper.wrapperList.size() <= 0)) {
                    //没有网络状态时候的显示
//                    showError(true);
                    findViewById(R.id.data_usage_total_summay).setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    //用于装数据的容器
    static class ItemListWrapper {
        int result = RETRIEVE_RESULT_UNKNOWN;
        long periodTotal = 0;
        List<NetworkDataAppItem> wrapperList = null;
    }
}
