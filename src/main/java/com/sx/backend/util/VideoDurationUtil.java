package com.sx.backend.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class VideoDurationUtil {
    private static final String PYTHON_SCRIPT_PATH = "path/to/video_duration.py"; // 实际脚本路径

    public static int getVideoDuration(String videoFilePath) {
        // 构建命令: python脚本路径 + 视频文件路径
        ProcessBuilder processBuilder = new ProcessBuilder(
                "python", PYTHON_SCRIPT_PATH, videoFilePath
        );

        try {
            Process process = processBuilder.start();

            // 设置超时防止卡死 (30秒)
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return 0;
            }

            // 读取Python脚本输出
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String output = reader.readLine();

                if (output != null && !output.isEmpty()) {
                    // 将浮点秒数转为整数（四舍五入）
                    return (int) Math.round(Double.parseDouble(output));
                }
            }
        } catch (IOException | InterruptedException | NumberFormatException e) {
            // 异常处理（记录日志等）
            e.printStackTrace();
        }
        return 0; // 任何错误情况返回0
    }
}