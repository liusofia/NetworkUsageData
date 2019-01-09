package data.usge.network.wifi.com.networkusagedata;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class NetworkUsageAdapter extends RecyclerView.Adapter<NetworkUsageViewHolder> {
    private static Drawable YouTubeDrawable = null;
    private static String youTubePkgName = "com.xm.webcontent";

    private List<NetworkDataAppItem> itemList = new ArrayList<NetworkDataAppItem>();
    private Context context;

    public NetworkUsageAdapter(Context context) {
        this.context = context;
    }

    @Override
    public NetworkUsageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NetworkUsageViewHolder(LayoutInflater.from(context).inflate(R.layout.network_usage_item_layout, parent, false));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(NetworkUsageViewHolder holder, int position) {
        if ((itemList == null) || (itemList.size() <= 0)) {
            return;
        }
        NetworkDataAppItem item = itemList.get(position);
        if (item.pkgName.equals(youTubePkgName)) {
            if (YouTubeDrawable == null) {
                YouTubeDrawable = ContextCompat.getDrawable(context, R.drawable.dc_appicon_yt);
            }
            holder.icon.setImageDrawable(YouTubeDrawable);
        } else {
            holder.icon.setImageDrawable(item.getAppIcon(context));
        }
        holder.name.setText(item.getAppName(context));
        holder.consumeSize.setText(Formatter.formatFileSize(context, item.getConsumeSize(context)));
        holder.id = item.id;
        holder.position = position;
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    public void clean() {
        if (itemList != null) {
            itemList.clear();
        }
    }

    public void setList(List<NetworkDataAppItem> list) {
        itemList = list;
    }
}
