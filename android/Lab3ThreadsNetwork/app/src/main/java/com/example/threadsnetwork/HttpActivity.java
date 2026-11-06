package com.example.threadsnetwork;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * 程序2：HTTP 网络请求（GET / POST）。
 *
 * 使用 HttpURLConnection 向 test.jsp 发送请求并读取响应：
 * GET  ：把 name、pwd 拼在 URL 的问号后面，请求参数随地址一起发送；
 * POST ：把参数按 name=..&pwd=.. 的形式写入请求体，并设置 Content-Type。
 * 所有网络请求都在子线程中执行，结果通过 runOnUiThread 显示到界面。
 */
public class HttpActivity extends Activity {

    /** test.jsp 对应的网址：模拟器用 10.0.2.2 访问宿主机 */
    private static final String HTTP_URL = "http://10.0.2.2:8080/test.jsp";

    private EditText etName, etPwd;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);

        etName = (EditText) findViewById(R.id.etName);
        etPwd = (EditText) findViewById(R.id.etPasswd);
        tvResult = (TextView) findViewById(R.id.tvResult);
        Button btnGet = (Button) findViewById(R.id.btnGet);
        Button btnPost = (Button) findViewById(R.id.btnPost);

        // 以 GET 方式提交用户名和密码信息到 Web 服务器
        btnGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRequest("GET");
            }
        });

        // 以 POST 方式提交用户名和密码信息到 Web 服务器
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRequest("POST");
            }
        });
    }

    private void doRequest(final String method) {
        final String name = etName.getText().toString().trim();
        final String pwd = etPwd.getText().toString().trim();
        tvResult.setText("正在发送 " + method + " 请求…\n");
        new Thread(new Runnable() {
            @Override
            public void run() {
                request(method, name, pwd);
            }
        }).start();
    }

    /** 在子线程中发送 GET 或 POST 请求 */
    private void request(String method, String name, String pwd) {
        HttpURLConnection conn = null;
        try {
            String params = "name=" + URLEncoder.encode(name, "UTF-8")
                    + "&pwd=" + URLEncoder.encode(pwd, "UTF-8");
            String urlStr = HTTP_URL;
            if ("GET".equals(method)) {
                // GET：请求参数直接拼接在网址后面
                urlStr = HTTP_URL + "?" + params;
            }

            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestMethod(method);
            conn.setRequestProperty("User-Agent", "Android-Lab3");

            if ("POST".equals(method)) {
                // POST：请求参数写入请求体
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(params.getBytes("UTF-8"));
                os.flush();
                os.close();
            }

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                // 读取 Web 服务器返回的数据
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                final String response = sb.toString();
                final String urlShown = urlStr;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvResult.setText("请求方式：" + method + "\n"
                                + "请求地址：" + urlShown + "\n"
                                + "响应状态码：" + code + "\n\n"
                                + "服务器返回：" + response);
                    }
                });
            } else {
                final int c = code;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvResult.setText("请求失败，响应状态码：" + c);
                    }
                });
            }
            conn.disconnect();
        } catch (final Exception e) {
            e.printStackTrace();
            if (conn != null) {
                conn.disconnect();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvResult.setText("请求失败：" + e.getMessage()
                            + "\n请先运行 HTTP 服务器程序 HttpTestServer。");
                }
            });
        }
    }
}
