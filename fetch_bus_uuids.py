#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
获取全国公交UUID并保存到bus_lines.json
API: https://www.apihz.cn/api/otherdiming.html
使用标准库，无需安装第三方依赖
注意：此API使用POST请求
"""

import json
import time
import os
import urllib.request
import urllib.parse
import ssl

# API配置
API_URL = "https://www.apihz.cn/api/otherdiming.html"
API_ID = "10008097"
API_KEY = "3a26b84060a6e08290e04410246c4f95"

# 主要城市列表（城市名称 -> 城市UUID）
CITIES = {
    "北京": "beijing",
    "上海": "shanghai",
    "广州": "guangzhou",
    "深圳": "shenzhen",
    "杭州": "hangzhou",
    "南京": "nanjing",
    "成都": "chengdu",
    "武汉": "wuhan",
    "西安": "xian",
    "重庆": "chongqing",
    "天津": "tianjin",
    "苏州": "suzhou",
    "郑州": "zhengzhou",
    "长沙": "changsha",
    "青岛": "qingdao",
    "大连": "dalian",
    "厦门": "xiamen",
    "宁波": "ningbo",
    "无锡": "wuxi",
    "佛山": "foshan",
}

# 城市中心坐标（经纬度）
CITY_COORDS = {
    "北京": "116.4074,39.9042",
    "上海": "121.4737,31.2304",
    "广州": "113.2644,23.1291",
    "深圳": "114.0579,22.5431",
    "杭州": "120.1551,30.2741",
    "南京": "118.7969,32.0603",
    "成都": "104.0668,30.5728",
    "武汉": "114.3054,30.5931",
    "西安": "108.9398,34.3416",
    "重庆": "106.5516,29.5630",
    "天津": "117.2009,39.0842",
    "苏州": "120.5853,31.2989",
    "郑州": "113.6253,34.7466",
    "长沙": "112.9388,28.2282",
    "青岛": "120.3826,36.0671",
    "大连": "121.6147,38.9140",
    "厦门": "118.0894,24.4798",
    "宁波": "121.5500,29.8750",
    "无锡": "120.3119,31.4912",
    "佛山": "113.1214,23.0215",
}

# 创建SSL上下文（忽略证书验证）
ssl_context = ssl.create_default_context()
ssl_context.check_hostname = False
ssl_context.verify_mode = ssl.CERT_NONE


def fetch_bus_lines(city_name, coords):
    """
    获取指定城市的公交线路UUID
    """
    print(f"正在获取 {city_name} 的公交线路...")
    
    # POST数据
    post_data = {
        "id": API_ID,
        "key": API_KEY,
        "type": "1",  # 公交
        "lnglat": coords,
        "radius": "5000",  # 5公里半径
        "page": "1",
        "limit": "50"
    }
    
    try:
        # 编码POST数据
        encoded_data = urllib.parse.urlencode(post_data).encode('utf-8')
        
        # 创建请求
        req = urllib.request.Request(API_URL, data=encoded_data, method='POST')
        req.add_header('User-Agent', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36')
        req.add_header('Content-Type', 'application/x-www-form-urlencoded')
        req.add_header('Accept', 'application/json')
        
        # 发送请求
        with urllib.request.urlopen(req, context=ssl_context, timeout=30) as response:
            raw_data = response.read()
            
        # 解析JSON
        data = json.loads(raw_data.decode('utf-8'))
        
        if data.get("code") != 200:
            print(f"  获取失败: {data.get('msg', '未知错误')}")
            return []
        
        bus_lines = []
        datas = data.get("datas", [])
        
        for item in datas:
            # 只处理公交站类型
            if item.get("poiType") != "102":
                continue
            
            station_data = item.get("stationData", [])
            for station in station_data:
                line_name = station.get("lineName", "")
                line_uuid = station.get("uuid", "")
                
                if line_name and line_uuid:
                    bus_lines.append({
                        "lineName": line_name,
                        "lineUuid": line_uuid
                    })
        
        # 去重
        seen = set()
        unique_lines = []
        for line in bus_lines:
            key = (line["lineName"], line["lineUuid"])
            if key not in seen:
                seen.add(key)
                unique_lines.append(line)
        
        print(f"  获取到 {len(unique_lines)} 条线路")
        return unique_lines
        
    except Exception as e:
        print(f"  请求异常: {e}")
        import traceback
        traceback.print_exc()
        return []


def save_to_json(bus_data, output_file):
    """
    保存为bus_lines.json格式
    """
    result = []
    
    for city_name, city_uuid in CITIES.items():
        lines = bus_data.get(city_name, [])
        for line in lines:
            result.append({
                "cityUuid": city_uuid,
                "lineNumber": line["lineName"],
                "lineUuid": line["lineUuid"]
            })
    
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(result, f, ensure_ascii=False, indent=4)
    
    print(f"\n已保存到 {output_file}")
    print(f"总共 {len(result)} 条线路")


def main():
    """
    主函数
    """
    print("=" * 50)
    print("全国公交UUID获取工具")
    print("=" * 50)
    
    bus_data = {}
    total_lines = 0
    
    for city_name, coords in CITY_COORDS.items():
        lines = fetch_bus_lines(city_name, coords)
        bus_data[city_name] = lines
        total_lines += len(lines)
        
        # 添加延迟，避免请求过快
        time.sleep(1)
    
    print("\n" + "=" * 50)
    print(f"获取完成，共 {total_lines} 条线路")
    print("=" * 50)
    
    # 保存到文件
    output_file = "bus_lines.json"
    save_to_json(bus_data, output_file)
    
    print("\n文件位置:", os.path.abspath(output_file))


if __name__ == "__main__":
    main()
