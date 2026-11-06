# 思考题

## Q1

TCP 通信中，Java 服务器端的编程步骤：
1. 创建 ServerSocket 对象并绑定监听端口（如 8888，端口号应大于 1024）；
2. 调用 accept() 阻塞等待客户端连接，连接成功后得到 Socket 对象；
3. 通过 socket.getInputStream()/getOutputStream() 获取输入输出流，用 BufferedReader/PrintWriter 读写数据；
4. 通信结束后依次关闭输入流、输出流、Socket 和 ServerSocket。

Android 客户端的编程步骤：
1. 在 AndroidManifest.xml 中声明网络权限；
2. 新建子线程，在子线程中创建 Socket(服务器IP, 端口) 与服务器建立连接（主线程不允许网络操作，否则抛出 NetworkOnMainThreadException）；
3. 用 PrintWriter 向服务器发送消息，用 BufferedReader 读取服务器返回的数据；
4. 通过 runOnUiThread() 或 Handler 回到主线程，把结果显示到界面；
5. 通信结束后关闭流和 Socket。

Android 客户端需要声明 android.permission.INTERNET 网络权限；如需判断网络状态，还可以声明 android.permission.ACCESS_NETWORK_STATE。

## Q2

发送 HTTP GET 请求的步骤：
1. 用 URL 封装请求地址，参数以 ?name=…&pwd=… 的形式拼接在地址后面，并用 URLEncoder 对参数编码；
2. 调用 openConnection() 得到 HttpURLConnection，setRequestMethod("GET") 并设置连接、读取超时；
3. 调用 getResponseCode() 获取响应状态码（200 表示成功）；
4. 通过 getInputStream() 获取输入流，用 BufferedReader 逐行读取响应内容；
5. 关闭输入流并 disconnect() 释放连接，把结果显示到界面。

发送 HTTP POST 请求的步骤：
1. 创建 HttpURLConnection 后 setRequestMethod("POST")，并设置 setDoOutput(true)；
2. 设置请求头 Content-Type: application/x-www-form-urlencoded（UTF-8 编码）；
3. 把参数拼成 name=…&pwd=… 的字符串，写入 conn.getOutputStream() 请求体；
4. 其余步骤与 GET 相同：获取状态码、读取响应流、关闭连接。

所有网络请求都要放在子线程中执行，避免阻塞主线程；本实验使用的网络 API 是 HttpURLConnection。

## Q3

1. 在布局文件中添加 WebView 组件，在 Activity 中通过 findViewById() 获取 WebView 对象；
2. 通过 webView.getSettings() 获取 WebSettings，调用 setJavaScriptEnabled(true) 支持 JavaScript，按需设置 setDomStorageEnabled(true)、缩放等属性；
3. 调用 webView.setWebViewClient(new WebViewClient(){…}) 设置页面加载监听，在 onPageStarted()、onPageFinished()、onReceivedError() 中分别处理加载开始、完成和失败；
4. 调用 webView.loadUrl("http://…") 加载指定网址，网址缺少协议时可自动补全 http://；
5. 在 AndroidManifest.xml 中声明 INTERNET 权限（加载 HTTP 网页还需允许明文流量）；重写 onBackPressed() 使返回键先回退网页历史，在 onDestroy() 中调用 webView.destroy() 释放资源。

## Q4

1. 在 AndroidManifest.xml 中声明 ACCESS_FINE_LOCATION 和 ACCESS_COARSE_LOCATION 定位权限；
2. 在运行时调用 requestPermissions() 动态申请定位权限，并在 onRequestPermissionsResult() 中处理授权结果（API 23 以后必须动态申请）；
3. 通过 getSystemService(Context.LOCATION_SERVICE) 获取 LocationManager 对象，用 isProviderEnabled() 判断 GPS/网络定位是否可用；
4. 实现 LocationListener 接口，在 onLocationChanged(Location location) 中获取经纬度、海拔、精度和时间并显示到 TextView；
5. 调用 requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener) 注册定位监听（参数依次为定位方式、最小更新时间、最小更新距离、监听器），退出时 removeUpdates() 移除监听；
6. 在模拟器中可通过 Extended Controls→Location 或 adb emu geo fix 设置经纬度，程序即可收到定位回调。

## Q5

附加题为选做内容。本次实验环境为离线环境，百度地图 SDK 需要申请 AK 并通过 Gradle 下载 SDK 依赖，无法完成 SDK 集成，因此未提交百度地图 SDK 的运行截图。其实现过程为：
1. 在百度地图开放平台申请 AK，在 AndroidManifest.xml 中配置 <meta-data android:name="com.baidu.lbsapi.API_KEY" android:value="AK"/> 以及网络、定位权限；
2. 在 build.gradle 中添加百度地图 SDK 依赖（BaiduMapSDK_Map、BaiduMapSDK_Location 等）；
3. 在布局中放置 MapView，在 Activity 中调用 SDKInitializer.initialize(getApplicationContext()) 初始化 SDK；
4. 通过 mapView.getMap() 获取 BaiduMap 对象，调用 setMyLocationEnabled(true) 显示定位图层，用 LocationClient 获取当前位置并 moveCamera 到该坐标。

# 实验小结

## S1 编程遇到的问题及解决方案

1. 现象：客户端报 NetworkOnMainThreadException，程序闪退。原因：在 onCreate() 主线程中直接创建 Socket。解决：把建立连接、收发数据全部放进 new Thread(){…}.start() 子线程，并用 runOnUiThread() 回到主线程更新 EditText。
2. 现象：客户端连接失败，提示 Connection refused。原因：服务器没有先运行，或客户端 IP、端口写错。解决：先运行服务器程序，模拟器使用 10.0.2.2 访问宿主机（真机使用主机局域网 IP），端口与服务器保持一致（8888）。
3. 现象：HTTP 请求返回 404 或连接超时。原因：本机未安装 Tomcat，test.jsp 无法访问。解决：编写 HttpTestServer 模拟 test.jsp 的 GET/POST 处理逻辑并监听 8080 端口；同时把已废弃的 HttpClient 改为 HttpURLConnection。
4. 现象：WebView 打开 HTTP 网页时报 ERR_CLEARTEXT_NOT_PERMITTED。原因：targetSdk 28 以后系统默认禁止明文 HTTP 流量。解决：在 AndroidManifest.xml 的 application 中增加 android:usesCleartextTraffic="true"。
5. 现象：程序运行后定位界面一直没有回调。原因：只声明了权限却没有动态申请，或模拟器没有设置位置。解决：增加 requestPermissions() 动态申请，并在模拟器中用 adb emu geo fix 116.397 39.909 设置经纬度后重新获取。

## S2 实验收获及体会

1. 掌握了 TCP Socket 编程：服务器端用 ServerSocket 监听、accept() 等待连接，客户端用 Socket 连接服务器，双方通过 BufferedReader/PrintWriter 收发数据，理解了流套接字面向连接、可靠传输的特点。
2. 理解了 Android 的线程规范：网络请求必须在子线程执行，结果通过 runOnUiThread()/Handler 回到主线程更新 UI，避免阻塞主线程导致 ANR。
3. 掌握了 HttpURLConnection 发送 GET/POST 请求的完整流程，包括参数拼接与 URL 编码、设置请求头、读取响应状态码和响应流，以及请求异常的处理。
4. 掌握了 WebView 的使用：WebSettings 初始化、WebViewClient 的页面加载回调、loadUrl() 加载网址，并了解了明文流量限制等安全配置。
5. 掌握了 LocationManager 定位：权限声明与运行时申请、LocationListener 回调、requestLocationUpdates() 注册监听，以及模拟器定位的调试方法。

## S3 不足之处及下一步需改进的地方

1. 程序1只实现了单次收发，没有心跳、重连和多客户端会话管理；下一步可以定义消息协议，增加心跳包和断线重连机制，并用线程池管理多个客户端。
2. 程序2使用原生 HttpURLConnection，代码较繁琐，也没有缓存和重试；下一步改用 OkHttp/Retrofit，并结合 JSON 解析统一处理请求结果和错误。
3. 程序3的 WebView 没有处理 JS 交互、文件下载和页面安全策略；下一步可通过 addJavascriptInterface()、WebChromeClient 增强交互，并限制可访问的域名白名单。
4. 程序4只显示单次经纬度，没有逆地理编码和轨迹记录；下一步可接入地图 SDK 显示地图和地址，并针对定位频率、耗电做优化。
5. 界面整体比较简陋，只用了基本的 LinearLayout 和 Button；下一步可使用 RecyclerView、Fragment 或 Compose 优化界面，并补充异常提示和输入校验。
