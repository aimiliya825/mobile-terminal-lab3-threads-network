package com.example.threadsnetwork;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 程序1：TCP 流套接字通信（Android 客户端）。
 *
 * 客户端在子线程中与 Java 服务器建立 Socket 连接：
 * 1) 用 PrintWriter 向服务器发送 "I am client!"；
 * 2) 用 BufferedReader 读取服务器返回的 "hello, I am Server!"；
 * 3) 通过 runOnUiThread 回到主线程把结果显示到 EditText 中。
 *
 * 注意：网络操作不能在主线程执行，否则会抛出 NetworkOnMainThreadException。
 */
public class TcpClientActivity extends Activity {

    /** 服务器 IP：模拟器用 10.0.2.2 访问宿主机，真机改为主机局域网 IP（如 192.168.1.108） */
    private static final String SERVER_IP = "10.0.2.2";
    /** 服务器端口：必须大于 1024 */
    private static final int SERVER_PORT = 8888;

    private EditText clientText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp);

        clientText = (EditText) findViewById(R.id.ClientText);
        Button connect = (Button) findViewById(R.id.btnConnect);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientText.setText("正在连接服务器 " + SERVER_IP + ":" + SERVER_PORT + " …\n");
                // 网络操作放在子线程中，避免阻塞 UI 线程
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectServer();
                    }
                }).start();
            }
        });
    }

    /** 子线程：建立连接、发送消息、接收消息 */
    private void connectServer() {
        Socket socket = null;
        try {
            // 1. 指定服务器的 IP 地址和端口，与服务器端建立连接
            socket = new Socket(SERVER_IP, SERVER_PORT);

            // 2. 通过 PrintWriter 向服务器发送信息
            final String cMessage = "I am client!";
            PrintWriter out = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
            out.println(cMessage);

            // 3. 通过 BufferedReader 读取服务器发送过来的信息
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
            final String sMessage = br.readLine();

            out.close();
            br.close();
            socket.close();

            // 4. 回到主线程，把双方收发的信息显示在输入框中
            final String result = "服务器地址：" + SERVER_IP + ":" + SERVER_PORT + "\n\n"
                    + ">> 客户端发送：" + cMessage + "\n"
                    + "<< 服务器返回：" + sMessage + "\n\n"
                    + "TCP 通信成功！";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clientText.setText(result);
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ignored) {
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clientText.setText("连接服务器失败：" + e.getMessage()
                            + "\n请先运行服务器端程序 TcpServer，并确认 IP 与端口是否正确。");
                }
            });
        }
    }
}
