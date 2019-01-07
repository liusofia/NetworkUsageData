package data.usge.network.wifi.com.networkusagedata;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class NetworkDataAppItem implements Comparable<NetworkDataAppItem> {

    public long position = 0;
    public int uid = -1;
    public String id;

    public String pkgName;
    public String appName;
    public Drawable appIcon;

    public long consumeSize = 0;

    public NetworkDataAppItem() {
    }

    public void release() {
        if (appIcon != null) {
        }
    }

    @Override
    public String toString() {
        return "uid = " + uid + ", id = " + id + ", pkgName = " + pkgName + ", appName = " + appName + ", consumeSize = " + consumeSize;
    }

    public Drawable getAppIcon(Context context) {
        return appIcon;
    }

    public String getAppName(Context context) {
        return appName;
    }

    public long getConsumeSize(Context context) {
        return consumeSize;
    }

    @Override
    public int compareTo(@NonNull NetworkDataAppItem o) {
        if (o == null) {
            return -1;
        }
        return Long.valueOf(o.consumeSize).compareTo(consumeSize);
    }
}
