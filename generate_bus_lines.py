#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成 bus_lines.json 文件
由于API接口需要特殊调用方式，此脚本提供模板和手动添加功能
"""

import json
import os

# 城市列表
CITIES = {
    "beijing": "北京",
    "shanghai": "上海",
    "guangzhou": "广州",
    "shenzhen": "深圳",
    "hangzhou": "杭州",
    "nanjing": "南京",
    "chengdu": "成都",
    "wuhan": "武汉",
    "xian": "西安",
    "chongqing": "重庆",
    "tianjin": "天津",
    "suzhou": "苏州",
    "zhengzhou": "郑州",
    "changsha": "长沙",
    "qingdao": "青岛",
    "dalian": "大连",
    "xiamen": "厦门",
    "ningbo": "宁波",
    "wuxi": "无锡",
    "foshan": "佛山",
}

# 示例数据结构 - 你可以手动添加从API获取的数据
# 格式: [{"cityUuid": "城市UUID", "lineNumber": "线路名", "lineUuid": "线路UUID"}, ...]
BUS_LINES_TEMPLATE = [
    # 北京示例
    {"cityUuid": "beijing", "lineNumber": "1路", "lineUuid": "110000_1"},
    {"cityUuid": "beijing", "lineNumber": "2路", "lineUuid": "110000_2"},
    {"cityUuid": "beijing", "lineNumber": "52路", "lineUuid": "110000_52"},
    
    # 上海示例
    {"cityUuid": "shanghai", "lineNumber": "20路", "lineUuid": "310000_20"},
    {"cityUuid": "shanghai", "lineNumber": "37路", "lineUuid": "310000_37"},
    {"cityUuid": "shanghai", "lineNumber": "49路", "lineUuid": "310000_49"},
    
    # 广州示例
    {"cityUuid": "guangzhou", "lineNumber": "1路", "lineUuid": "440100_1"},
    {"cityUuid": "guangzhou", "lineNumber": "2路", "lineUuid": "440100_2"},
    {"cityUuid": "guangzhou", "lineNumber": "3路", "lineUuid": "440100_3"},
    
    # 深圳示例
    {"cityUuid": "shenzhen", "lineNumber": "M200路", "lineUuid": "440300_M200"},
    {"cityUuid": "shenzhen", "lineNumber": "M390路", "lineUuid": "440300_M390"},
    
    # 杭州示例
    {"cityUuid": "hangzhou", "lineNumber": "7路", "lineUuid": "330100_7"},
    {"cityUuid": "hangzhou", "lineNumber": "27路", "lineUuid": "330100_27"},
    
    # 其他城市可以手动添加...
]


def generate_json():
    """生成 bus_lines.json 文件"""
    output_file = "bus_lines.json"
    
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(BUS_LINES_TEMPLATE, f, ensure_ascii=False, indent=4)
    
    print(f"已生成 {output_file}")
    print(f"文件位置: {os.path.abspath(output_file)}")
    print(f"\n共 {len(BUS_LINES_TEMPLATE)} 条线路")
    print("\n提示: 你可以手动编辑此文件添加更多线路数据")
    print("格式: [{\"cityUuid\": \"城市UUID\", \"lineNumber\": \"线路名\", \"lineUuid\": \"线路UUID\"}, ...]")


def add_bus_line(city_uuid, line_number, line_uuid):
    """添加单条线路到模板"""
    BUS_LINES_TEMPLATE.append({
        "cityUuid": city_uuid,
        "lineNumber": line_number,
        "lineUuid": line_uuid
    })
    print(f"已添加: {city_uuid} - {line_number}")


if __name__ == "__main__":
    print("=" * 50)
    print("公交线路 UUID 数据生成器")
    print("=" * 50)
    print()
    
    generate_json()
    
    print("\n" + "=" * 50)
    print("城市列表:")
    for uuid, name in CITIES.items():
        print(f"  {uuid}: {name}")
    print("=" * 50)
