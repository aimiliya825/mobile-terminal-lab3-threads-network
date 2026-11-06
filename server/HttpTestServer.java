import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 实验三 程序2：HTTP 网络请求 —— 模拟 test.jsp 的 Java HTTP 服务器。
 *
 * 运行方法（在 server 目录下）：
 *   javac -encoding UTF-8 HttpTestServer.java
 *   java HttpTestServer
 *
 * 访问地址：http://主机IP:8080/test.jsp
 *   GET  http://主机IP:8080/test.jsp?name=wwh&pwd=123456
 *   POST http://主机IP:8080/test.jsp  （请求体 name=wwh&pwd=123456）
 * 用户名 wwh、密码 123456 时返回 "GET was used. Hello, wwh"（或 POST），否则返回 "Failed to sign in!"。
 *
 * 同时提供 http://主机IP:8080/index.html 供程序3 WebView 加载。
 */
public class HttpTestServer {

    private static final String USER_NAME = "wwh";
    private static final String PASSWORD = "123456";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/test.jsp", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                handleTestJsp(exchange);
            }
        });
        server.createContext("/index.html", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                send(exchange, 200, "text/html; charset=UTF-8", INDEX_HTML);
            }
        });
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // 其它路径统一返回演示页面
                send(exchange, 200, "text/html; charset=UTF-8", INDEX_HTML);
            }
        });
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        log("[HTTP服务器] 启动成功，监听端口：" + port);
        log("[HTTP服务器] 测试地址：http://127.0.0.1:8080/test.jsp?name=wwh&pwd=123456");
        log("[HTTP服务器] WebView 页面：http://127.0.0.1:8080/index.html");
    }

    /** 模拟 test.jsp 的处理逻辑：GET 取查询串，POST 取请求体，校验用户名和密码 */
    private static void handleTestJsp(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String thisname = "";
        String thispwd = "";
        try {
            if ("GET".equals(method)) {
                String reqParam = exchange.getRequestURI().getRawQuery();
                if (reqParam != null) {
                    Map<String, String> map = parseParams(reqParam);
                    thisname = map.getOrDefault("name", "");
                    thispwd = map.getOrDefault("pwd", "");
                }
            } else if ("POST".equals(method)) {
                String body = readAll(exchange.getRequestBody());
                Map<String, String> map = parseParams(body);
                thisname = map.getOrDefault("name", "");
                thispwd = map.getOrDefault("pwd", "");
            }

            String response;
            if (USER_NAME.equals(thisname) && PASSWORD.equals(thispwd)) {
                response = method + " was used. Hello, " + thisname;
            } else {
                response = "Failed to sign in!";
            }
            log("[HTTP服务器] 收到 " + method + " 请求，name=" + thisname
                    + "，pwd=" + thispwd + "，返回：" + response);
            send(exchange, 200, "text/plain; charset=UTF-8", response);
        } catch (Exception e) {
            log("[HTTP服务器] 处理请求出错：" + e.getMessage());
            send(exchange, 500, "text/plain; charset=UTF-8", "Server Error");
        }
    }

    private static Map<String, String> parseParams(String raw) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        if (raw == null || raw.isEmpty()) {
            return map;
        }
        for (String pair : raw.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                map.put(key, value);
            }
        }
        return map;
    }

    private static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    private static void send(HttpExchange exchange, int code, String contentType, String body)
            throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(code, data.length);
        OutputStream os = exchange.getResponseBody();
        os.write(data);
        os.close();
        exchange.close();
    }

    private static void log(String msg) {
        System.out.println("[" + SDF.format(new Date()) + "] " + msg);
    }

    /** 程序3 WebView 加载的演示网页 */
    private static final String INDEX_HTML = "<!DOCTYPE html>\n"
            + "<html lang=\"zh-CN\">\n"
            + "<head>\n"
            + "<meta charset=\"UTF-8\">\n"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
            + "<title>实验三 WebView 测试页面</title>\n"
            + "<style>\n"
            + "  body { font-family: sans-serif; margin: 0; background: #f4f7fb; color: #223; }\n"
            + "  .header { background: #1565C0; color: #fff; padding: 18px 16px; }\n"
            + "  .header h1 { margin: 0; font-size: 19px; }\n"
            + "  .header p { margin: 6px 0 0; font-size: 12px; opacity: .9; }\n"
            + "  .card { background: #fff; margin: 14px 12px; padding: 14px; border-radius: 8px; }\n"
            + "  .card h2 { margin: 0 0 8px; font-size: 15px; color: #1565C0; }\n"
            + "  .card p { margin: 4px 0; font-size: 13px; line-height: 1.6; }\n"
            + "  ul { padding-left: 20px; margin: 4px 0; }\n"
            + "  li { font-size: 13px; line-height: 1.9; }\n"
            + "  .tag { display: inline-block; background: #E3F2FD; color: #1565C0;\n"
            + "         border-radius: 4px; padding: 2px 6px; font-size: 12px; margin: 2px 2px 0 0; }\n"
            + "  .footer { text-align: center; font-size: 11px; color: #999; padding: 10px 0 18px; }\n"
            + "</style>\n"
            + "</head>\n"
            + "<body>\n"
            + "  <div class=\"header\">\n"
            + "    <h1>移动终端软件开发 · 实验三</h1>\n"
            + "    <p>线程与网络通信 &nbsp;|&nbsp; 胡俊峰 &nbsp;202402323</p>\n"
            + "  </div>\n"
            + "  <div class=\"card\">\n"
            + "    <h2>WebView 加载成功</h2>\n"
            + "    <p>该页面由本地 Java HTTP 服务器（端口 8080）返回，说明 WebView 已成功通过 HTTP 加载网页。</p>\n"
            + "    <p>当前访问地址：<b>http://10.0.2.2:8080/index.html</b></p>\n"
            + "  </div>\n"
            + "  <div class=\"card\">\n"
            + "    <h2>实验内容</h2>\n"
            + "    <ul>\n"
            + "      <li>程序1：TCP 流套接字通信（ServerSocket / Socket）</li>\n"
            + "      <li>程序2：HTTP 网络请求（GET / POST）</li>\n"
            + "      <li>程序3：WebView 加载网页</li>\n"
            + "      <li>程序4：LocationManager 定位</li>\n"
            + "    </ul>\n"
            + "  </div>\n"
            + "  <div class=\"card\">\n"
            + "    <h2>涉及的知识点</h2>\n"
            + "    <span class=\"tag\">子线程</span>\n"
            + "    <span class=\"tag\">Socket 通信</span>\n"
            + "    <span class=\"tag\">HttpURLConnection</span>\n"
            + "    <span class=\"tag\">WebViewClient</span>\n"
            + "    <span class=\"tag\">LocationManager</span>\n"
            + "    <span class=\"tag\">运行时权限</span>\n"
            + "  </div>\n"
            + "  <div class=\"footer\">智能网联汽车学院 · 移动终端软件开发实验</div>\n"
            + "</body>\n"
            + "</html>\n";
}
