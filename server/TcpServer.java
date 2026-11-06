import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 实验三 程序1：TCP 流套接字通信 —— Java 服务器端。
 *
 * 运行方法（在 server 目录下）：
 *   javac -encoding UTF-8 TcpServer.java
 *   java TcpServer
 *
 * 服务器在 8888 端口监听，收到 Android 客户端发来的 "I am client!" 后，
 * 回复 "hello, I am Server!"。
 */
public class TcpServer {

    public static void main(String[] args) {
        int port = 8888;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        try {
            // 为服务器指定一个大于 1024 的端口号
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("[" + sdf.format(new Date()) + "] [TCP服务器] 启动成功，本机 IP："
                    + java.net.InetAddress.getLocalHost().getHostAddress()
                    + "，监听端口：" + port);
            System.out.println("[" + sdf.format(new Date()) + "] [TCP服务器] 等待接收客户端的连接...");

            while (true) {
                // 等待客户端连接
                Socket socket = serverSocket.accept();
                System.out.println("[" + sdf.format(new Date()) + "] [TCP服务器] 建立连接成功，客户端地址："
                        + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

                // 读取客户端发来的信息
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), "UTF-8"));
                String str = in.readLine();
                System.out.println("[" + sdf.format(new Date()) + "] [TCP服务器] 收到客户端发来的信息：" + str);

                // 向客户端发送消息
                PrintWriter out = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")), true);
                out.println("hello, I am Server!");
                System.out.println("[" + sdf.format(new Date()) + "] [TCP服务器] 已向客户端发送信息：hello, I am Server!");

                in.close();
                out.close();
                socket.close();
                System.out.println("[" + sdf.format(new Date()) + "] [TCP服务器] 本次连接已关闭，继续等待下一个客户端...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
