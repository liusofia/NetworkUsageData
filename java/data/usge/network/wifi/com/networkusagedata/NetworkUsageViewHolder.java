package data.usge.network.wifi.com.networkusagedata;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * 装数据的容器
 */
public class NetworkUsageViewHolder extends RecyclerView.ViewHolder implements View.OnFocusChangeListener{
    //这里extends RecyclerView 因为 RecyclerView 控件需要将数据放在这个list中

    public int position;
    public String id;

    public ImageView icon;
    public TextView name;
    public TextView consumeSize;
    public View background;

    public NetworkUsageViewHolder(View itemView){
        super(itemView);
        id = null;
        icon = itemView.findViewById(R.id.item_icon);
        name = itemView.findViewById(R.id.item_name);
        consumeSize = itemView.findViewById(R.id.item_size);
        itemView.setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (b == true) {
            view.setBackgroundResource(R.color.blue_text);
        } else {
            view.setBackgroundResource(R.color.trans);
        }
    }
}
