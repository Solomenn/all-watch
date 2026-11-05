package com.solomenn.allwatch.relayprobe;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

public final class MainActivity extends Activity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextView output;
    private Button runButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = dp(16);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(pad, pad, pad, pad);
        content.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Bluetooth Relay Probe");
        title.setTextColor(Color.rgb(25, 35, 45));
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        content.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView note = new TextView(this);
        note.setText("前台网络测试。只请求两个公开 HTTPS 页面，不读取健康数据或设备标识。");
        note.setTextColor(Color.DKGRAY);
        note.setTextSize(12);
        note.setPadding(0, dp(10), 0, dp(12));
        content.addView(note);

        runButton = new Button(this);
        runButton.setText("检查网络");
        content.addView(runButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        output = new TextView(this);
        output.setTextColor(Color.rgb(30, 30, 30));
        output.setTextSize(12);
        output.setText("请在 iPhone 蓝牙连接保持、手表 Wi-Fi 未连接时测试。");
        output.setPadding(0, dp(12), 0, 0);
        content.addView(output);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(content);
        setContentView(scroll);

        runButton.setOnClickListener(v -> runProbe());
    }

    private void runProbe() {
        runButton.setEnabled(false);
        output.setText("正在检查网络并发起 HTTPS 请求…");

        executor.execute(() -> {
            StringBuilder report = new StringBuilder();
            report.append("网络状态\n");
            report.append(describeDefaultNetwork());
            report.append("\n\nHTTPS 测试\n");
            report.append(testUrl("https://example.com/"));
            report.append("\n");
            report.append(testUrl("https://www.baidu.com/"));

            runOnUiThread(() -> {
                output.setText(report.toString());
                runButton.setEnabled(true);
            });
        });
    }

    private String describeDefaultNetwork() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return "ConnectivityManager unavailable";
        }

        Network network = manager.getActiveNetwork();
        if (network == null) {
            return "activeNetwork: null";
        }

        NetworkCapabilities caps = manager.getNetworkCapabilities(network);
        if (caps == null) {
            return "activeNetwork: present; capabilities unavailable";
        }

        StringBuilder result = new StringBuilder();
        result.append("activeNetwork: present\n");
        result.append("INTERNET: ")
                .append(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
                .append("\n");
        result.append("VALIDATED: ")
                .append(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
                .append("\n");
        result.append("transports: ");
        boolean any = false;
        any |= appendTransport(result, caps, NetworkCapabilities.TRANSPORT_WIFI, "WIFI");
        any |= appendTransport(result, caps, NetworkCapabilities.TRANSPORT_CELLULAR, "CELLULAR");
        any |= appendTransport(result, caps, NetworkCapabilities.TRANSPORT_BLUETOOTH, "BLUETOOTH");
        any |= appendTransport(result, caps, NetworkCapabilities.TRANSPORT_ETHERNET, "ETHERNET");
        any |= appendTransport(result, caps, NetworkCapabilities.TRANSPORT_VPN, "VPN");
        if (!any) {
            result.append("other/unknown");
        }
        return result.toString();
    }

    private boolean appendTransport(
            StringBuilder out, NetworkCapabilities caps, int transport, String label) {
        if (!caps.hasTransport(transport)) {
            return false;
        }
        if (out.charAt(out.length() - 1) != ' ') {
            out.append(", ");
        }
        out.append(label);
        return true;
    }

    private String testUrl(String address) {
        long start = SystemClock.elapsedRealtime();
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) new URL(address).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", "AllWatchRelayProbe/0.1");

            int status = connection.getResponseCode();
            long elapsed = SystemClock.elapsedRealtime() - start;
            return address + "\nHTTP " + status + " · " + elapsed + " ms";
        } catch (UnknownHostException e) {
            return address + "\nDNS failure";
        } catch (SocketTimeoutException e) {
            return address + "\nTimeout";
        } catch (SSLException e) {
            return address + "\nTLS failure: " + e.getClass().getSimpleName();
        } catch (IOException e) {
            return address + "\nI/O failure: " + e.getClass().getSimpleName();
        } catch (Exception e) {
            return address + "\nFailure: " + e.getClass().getSimpleName();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
