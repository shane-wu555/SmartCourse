@echo off
REM 前端项目启动脚本 (Windows)
echo 正在启动前端项目...

REM 进入前端目录
cd vue

REM 检查是否已安装依赖
if not exist "node_modules" (
    echo 正在安装依赖...
    npm install
)

REM 启动开发服务器
echo 启动开发服务器...
npm run serve

pause
