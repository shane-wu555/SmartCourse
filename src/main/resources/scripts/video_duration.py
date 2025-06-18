import subprocess
import json
import sys
import os

def get_video_duration(file_path, ffprobe_path="ffprobe"):
    """使用ffprobe获取视频时长(秒) - 支持自定义ffprobe路径"""
    try:
        # 构建命令
        cmd = [
            ffprobe_path,
            '-v', 'error',
            '-show_entries', 'format=duration',
            '-of', 'json',
            file_path  # 注意：不要加引号，交给subprocess处理
        ]

        # Windows专用设置
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            universal_newlines=True,
            check=True,
            creationflags=subprocess.CREATE_NO_WINDOW,
            shell=False  # 关闭 shell=True，避免路径解析问题
        )

        # 解析JSON输出
        data = json.loads(result.stdout)
        return float(data['format']['duration'])

    except FileNotFoundError:
        print(f"错误: 找不到 ffprobe。请确认路径是否正确: {ffprobe_path}", file=sys.stderr)
        return None
    except subprocess.CalledProcessError as e:
        error_output = e.stderr if e.stderr else e.stdout
        print(f"ffprobe执行错误 (code {e.returncode}): {error_output}", file=sys.stderr)
        return None
    except json.JSONDecodeError as e:
        print(f"JSON解析错误: {e}\n输出内容: {result.stdout}", file=sys.stderr)
        return None
    except Exception as e:
        print(f"未知错误: {str(e)}", file=sys.stderr)
        return None

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("用法: python video_duration.py <文件路径> [ffprobe路径]", file=sys.stderr)
        sys.exit(1)

    file_path = sys.argv[1]

    # 可选参数：第二个参数是 ffprobe 路径
    ffprobe_path = sys.argv[2] if len(sys.argv) > 2 else "ffprobe"

    # 检查文件是否存在
    if not os.path.exists(file_path):
        print(f"错误: 文件不存在: {file_path}", file=sys.stderr)
        sys.exit(2)

    duration = get_video_duration(file_path, ffprobe_path)

    # 输出结果
    if duration is not None:
        print(duration)
    else:
        print("")  # 输出空字符串表示失败
        sys.exit(3)