@echo off
chcp 65001 >nul
cd /d %~dp0
set JAVA_HOME=D:\Android\jdk-21\jdk-21.0.12.1+1
set PATH=%JAVA_HOME%\bin;%PATH%
echo ============================================
echo  实验三 服务端程序
echo    1) Java TCP 服务器（端口 8888）
echo    2) Java HTTP 服务器（端口 8080，模拟 test.jsp）
echo ============================================
echo 正在编译 ...
javac -encoding UTF-8 TcpServer.java HttpTestServer.java
if errorlevel 1 (
    echo 编译失败，请检查 JDK 环境。
    pause
    exit /b 1
)
start "TCP服务器 8888" java TcpServer
timeout /t 2 >nul
start "HTTP服务器 8080" java HttpTestServer
echo 两个服务器窗口已启动。
pause
