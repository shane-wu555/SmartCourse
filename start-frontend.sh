#!/bin/bash

# 前端项目启动脚本
echo "正在启动前端项目..."

# 进入前端目录
cd vue

# 检查是否已安装依赖
if [ ! -d "node_modules" ]; then
    echo "正在安装依赖..."
    npm install
fi

# 启动开发服务器
echo "启动开发服务器..."
npm run serve
