package in.srain.demos.fuckbaidu;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.*;

public final class MainActivity extends AppCompatActivity {

    private final View.OnClickListener sClickHandler = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (!(v instanceof TextView) || v.getTag() == null) {
                return;
            }
            Object object = v.getTag();
            if (object == null || !(object instanceof String)) {
                return;
            }
            String url = (String) object;
            openUrl(url);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        final String formattedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView textView1 = (TextView) findViewById(R.id.text_view_1);
        String url1 = "http://" + formattedIpAddress + ":40310/daemon";
        showUrl(url1, textView1);

        TextView textView2 = (TextView) findViewById(R.id.text_view_2);
        String url2 = "http://" + formattedIpAddress + ":6259/daemon";
        showUrl(url2, textView2);
    }

    private List<String> getPackageNameList() {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> infoList = getPackageManager().queryIntentActivities(mainIntent, 0);

        HashMap<String, Boolean> packageNameList = new HashMap<String, Boolean>();
        for (int i = 0; i < infoList.size(); i++) {
            ResolveInfo info = infoList.get(i);
            packageNameList.put(info.activityInfo.packageName, true);
        }
        List<String> list = new ArrayList<>();
        Iterator<String> it = packageNameList.keySet().iterator();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    private void showUrl(String url, TextView textView) {
        final String text = String.format("<a href='%s'>%s</a>", url, url);
        textView.setText(Html.fromHtml(text));
        textView.setTag(url);
        textView.setOnClickListener(sClickHandler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            String url = "https://github.com/liaohuqiu/android-ILoveBaidu";
            openUrl(url);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void uninstall(String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void openUrl(String url) {
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(myIntent);
    }
}