package in.srain.demos.fuckbaidu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import in.srain.cube.concurrent.SimpleExecutor;
import in.srain.cube.concurrent.SimpleTask;

public final class MainActivity extends AppCompatActivity {


    private static final String BAIDU_FAMILY_APP_LIST = "https://raw.githubusercontent.com/liaohuqiu/android-ILoveBaidu/master/package-list.txt";
    private static final String QIU_BAI_WAN_I_LOVE_BAIDU = "https://github.com/liaohuqiu/android-ILoveBaidu";

    //使用包名查看器，从百度手机市场搜索"百度",可以查看到百度的大部分应用！
    private static final String[] START_BAIDU_APP = new String[]{
            "com.baidu",//百度大多应用
            "com.nuomi",//百度糯米，百度糯米商家
            "com.ting.mp3",//百度音乐,千千动听
            "com.duoku",//百度星玩家，百度多酷棋牌，百度游戏
            "com.dragon.android",//百度手机助手（原91手机助手）
            "cn.opda.a.phonoalbumshoushou",//百度手机卫士
            "com.dianxinos.optimizer",//百度卫士极客版
            "com.chuanke.ikk",//百度传课
            "cn.jingling.motu.photowonder",//百度魔图
            "com.nd.assistance",//百度连接助手
            "com.hiapk.marketpho"//安卓市场
    };

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

    List<String> mDirtyPackageList = new ArrayList<String>();

    List<String> mRemoteDirtyPackageList = new ArrayList<String>();

    private TextView mInfoTextView;

    private final SimpleTask mSimpleTask = new SimpleTask() {


        @Override
        public void doInBackground() {

            mRemoteDirtyPackageList = new ArrayList<String>();
            mRemoteDirtyPackageList.addAll(Arrays.asList(START_BAIDU_APP));

            mDirtyPackageList = new ArrayList<String>();
            try {
                URL url = new URL(BAIDU_FAMILY_APP_LIST);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String str;
                while ((str = in.readLine()) != null) {
                    if (str.endsWith(".*")) {
                        str = str.substring(0, str.length() - 2);
                    }
                    mRemoteDirtyPackageList.add(str);
                }
                in.close();
            } catch (IOException e) {
            }

            List<String> installedList = getPackageNameList();
            for (int i = 0; i < installedList.size(); i++) {
                String item = installedList.get(i);
                if (isDirty(item)) {
                    mDirtyPackageList.add(item);
                }
            }
        }

        private boolean isDirty(String packageName) {
            for (int i = 0; i < mRemoteDirtyPackageList.size(); i++) {
                String item = mRemoteDirtyPackageList.get(i);
                if (packageName.startsWith(item)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onFinish(boolean canceled) {
            int size = mDirtyPackageList.size();
            String ptn = getString(R.string.info_many);
            String text = String.format(ptn, size);
            mInfoTextView.setTextColor(Color.RED);
            if (size == 0) {
                text = getString(R.string.info_none);
                mInfoTextView.setTextColor(Color.GREEN);
            } else if (size == 1) {
                text = getString(R.string.info_one);
            }
            mInfoTextView.setText(text);
        }
    };

    private UnInstalledReceiver mUninstalledReceiver;

    private void uninstallOneDirtyAPP() {
        if (mDirtyPackageList.size() > 0) {
            String first = mDirtyPackageList.get(0);
            mDirtyPackageList.remove(0);
            uninstall(first);
        }
    }

    void checkDirtyPackage() {
        mSimpleTask.restart();
        SimpleExecutor.getInstance().execute(mSimpleTask);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        String formattedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));

        // force to set to localhost
        formattedIpAddress = "127.0.0.1";

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView textView1 = (TextView) findViewById(R.id.text_view_1);
        String url1 = "http://" + formattedIpAddress + ":40310/daemon";
        showUrl(url1, textView1);

        TextView textView2 = (TextView) findViewById(R.id.text_view_2);
        String url2 = "http://" + formattedIpAddress + ":6259/daemon";
        showUrl(url2, textView2);

        mInfoTextView = (TextView) findViewById(R.id.text_view_3);
        mInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uninstallOneDirtyAPP();
            }
        });
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
    protected void onResume() {
        super.onResume();
        checkDirtyPackage();
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
            openUrl(QIU_BAI_WAN_I_LOVE_BAIDU);
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
        try {
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(myIntent);

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.no_browser), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mUninstalledReceiver = new UnInstalledReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        this.registerReceiver(mUninstalledReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUninstalledReceiver != null) {
            this.unregisterReceiver(mUninstalledReceiver);
        }
    }

    /**
     * uninstall
     * add by malin
     */
    class UnInstalledReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (intent != null) {
                            String packageNameRaw = intent.getDataString();
                            if (packageNameRaw != null && !packageNameRaw.replaceAll(" ", "").equals("") && packageNameRaw.contains("package:")) {
                                String packageName = packageNameRaw.replaceAll("package:", "");
                                Toast.makeText(MainActivity.this, getString(R.string.uninstall_success) + " " + packageName, Toast.LENGTH_SHORT).show();
                                uninstallOneDirtyAPP();
                            }
                        }
                    }
                });
            }
        }
    }
}