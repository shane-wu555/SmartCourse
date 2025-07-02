package com.sx.backend.entity;

public enum ResourceType {
    PPT("PPT", "演示文稿"),
    PDF("PDF", "便携式文档"),
    VIDEO("VIDEO", "视频文件"),
    DOCUMENT("DOCUMENT", "通用文档"),
    LINK("LINK", "网页链接"),
    IMAGE("IMAGE", "图像文件"),
    AUDIO("AUDIO", "声音文件");

    private final String value;
    private final String desc;

    ResourceType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    // 根据 value 获取枚举对象
    public static ResourceType fromValue(String value) {
        for (ResourceType type : ResourceType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid resource type: " + value);
    }

    // 判断值是否合法
    public static boolean isValidType(String value) {
        for (ResourceType type : ResourceType.values()) {
            if (type.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    /*@Override
    public String toString() {
        return "\"" + value + "\""; // 用于 JSON 输出时保持字符串格式
    }*/
}
