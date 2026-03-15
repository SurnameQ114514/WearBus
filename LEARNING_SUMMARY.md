# WearBus 项目学习总结

> 记录这次开发过程中学到的所有知识和技能

---

## 📚 一、Android 开发技能

### 1. Room 数据库最佳实践

#### 主线程 vs 后台线程
- **问题**：数据库操作阻塞主线程会导致 ANR（应用无响应）
- **解决方案**：使用 AsyncTask 或协程在后台线程执行
- **实现**：创建了 `DatabaseTask.java` 工具类

```java
// 异步查询示例
DatabaseTask.execute(
    () -> BusDatabase.getInstance(context).cityDao().getCityByUuid(uuid),
    city -> {
        // 在回调中处理结果（主线程）
        if (city != null) {
            // 更新 UI
        }
    }
);
```

#### 单例模式优化
- 使用 `volatile` 确保线程安全
- 使用 `synchronized` 防止重复创建

### 2. 代码复用与架构

#### BaseActivity 设计
- **目的**：提取公共功能，减少重复代码
- **实现**：
  - `getDevId()` / `getDevKey()` - API 密钥获取
  - 后续可扩展更多公共方法

```java
public abstract class BaseActivity extends AppCompatActivity {
    protected String getDevId() {
        return SecureKeyManager.getApiDevId(this);
    }
}
```

#### 使用场景
- `Bus extends BaseActivity`
- `WhereToActivity extends BaseActivity`
- `NearbyStationsActivity extends BaseActivity`

### 3. 资源管理

#### try-with-resources
- **作用**：自动关闭流，防止资源泄漏
- **应用**：`BusLineInitializer.java` 读取 assets 文件

```java
// 优化前
InputStream is = context.getAssets().open(fileName);
// ... 使用 ...
is.close(); // 可能忘记关闭

// 优化后
try (InputStream is = context.getAssets().open(fileName)) {
    // ... 使用 ...
} // 自动关闭
```

---

## 🔧 二、问题解决能力

### 1. 调试技巧

#### 日志分析
- 学会阅读 Logcat 错误堆栈
- 定位问题：主线程数据库访问崩溃

```
FATAL EXCEPTION: main
IllegalStateException: Cannot access database on the main thread
```

#### 网络调试
- 使用 Python 脚本测试 API
- 分析 HTTP 响应头和状态码
- 识别问题：API 返回 HTML 而非 JSON

### 2. 问题分析流程

```
发现问题 → 定位代码 → 分析原因 → 设计方案 → 实施修复 → 验证结果
```

#### 实际案例
| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 数据库崩溃 | 主线程访问 | DatabaseTask 异步化 |
| 代码重复 | 多个 Activity 重复 API Key 方法 | BaseActivity 提取 |
| 资源泄漏 | 流未正确关闭 | try-with-resources |

---

## 🌐 三、网络编程经验

### 1. HTTP 请求

#### Python 标准库使用
```python
import urllib.request
import urllib.parse
import ssl

# POST 请求
req = urllib.request.Request(url, data=encoded_data, method='POST')
req.add_header('User-Agent', 'Mozilla/5.0 ...')
req.add_header('Content-Type', 'application/x-www-form-urlencoded')

with urllib.request.urlopen(req, context=ssl_context) as response:
    data = json.loads(response.read().decode('utf-8'))
```

#### 请求头的重要性
- `User-Agent`：模拟浏览器
- `Content-Type`：指定数据格式
- `Referer` / `Origin`：某些 API 需要

### 2. API 调试经验

#### 常见问题
1. **返回 HTML 而非 JSON** → 可能是反爬虫、需要特定 Header、或 API 端点错误
2. **空响应** → 网络问题、API 限制、参数错误
3. **认证失败** → Key/ID 错误、权限不足

#### 调试方法
```python
print(f"状态码: {response.status}")
print(f"响应头: {dict(response.headers)}")
print(f"响应内容: {raw_data[:500]}")  # 查看前500字符
```

---

## 📝 四、代码规范与质量

### 1. 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase + 后缀 | `BusActivity`, `DatabaseTask` |
| 方法名 | camelCase | `getCityByUuid()` |
| 常量 | UPPER_SNAKE_CASE | `LOCATE_TIMEOUT_MS` |
| 变量名 | camelCase | `cityManager` |

### 2. 代码注释

```java
/**
 * 数据库异步任务工具类
 * 用于在后台线程执行数据库操作，避免主线程阻塞
 */
public class DatabaseTask<T> extends AsyncTask<Void, Void, T> {
    // ...
}
```

### 3. 代码结构

```
com.Sumeru.WearBus/
├── activities/    # Activity 层 - 处理 UI 交互
├── adapters/      # 适配器 - RecyclerView 数据绑定
├── database/      # 数据库 - Room 相关
├── models/        # 数据模型 - 实体类
├── network/       # 网络层 - Retrofit
└── utils/         # 工具类 - 通用功能
```

---

## 🎯 五、项目改进成果

### 优化前后对比

| 方面 | 优化前 | 优化后 |
|------|--------|--------|
| 数据库访问 | 主线程（有风险） | 后台线程（安全） |
| 代码复用 | 每个 Activity 重复 API Key 方法 | BaseActivity 统一提供 |
| 资源管理 | 手动关闭流（可能泄漏） | try-with-resources（安全） |
| 架构设计 | 各自为政 | 分层清晰 |

### 新增文件
- `DatabaseTask.java` - 异步数据库操作工具
- `BaseActivity.java` - Activity 基类

### 修改文件
- `BusDatabase.java` - 移除 `allowMainThreadQueries()`
- `SettingsFragment.java` - 使用 DatabaseTask
- `SelectCityActivity.java` - 使用 DatabaseTask
- `Bus.java` - 继承 BaseActivity
- `WhereToActivity.java` - 继承 BaseActivity
- `NearbyStationsActivity.java` - 继承 BaseActivity
- `BusLineInitializer.java` - 使用 try-with-resources

---

## 💡 六、收获与反思

### 技术收获
1. ✅ 掌握了 Room 数据库异步操作
2. ✅ 学会了 AsyncTask 的使用
3. ✅ 理解了代码复用的重要性
4. ✅ 积累了 API 调试经验
5. ✅ 提升了问题分析能力

### 软技能
1. ✅ 耐心 - 反复调试直到解决问题
2. ✅ 坚持 - 面对困难不放弃
3. ✅ 学习能力 - 查阅文档、搜索资料
4. ✅ 代码审查 - 识别问题并改进

### 待提升
1. 📌 API 选型 - 事先验证 API 可用性
2. 📌 架构设计 - 提前规划代码结构
3. 📌 测试覆盖 - 增加单元测试

---

## 🚀 七、后续建议

### 短期（1-2 周）
- [ ] 手动维护 bus_lines.json 数据
- [ ] 测试所有功能是否正常
- [ ] 修复发现的 Bug

### 中期（1 个月）
- [ ] 考虑迁移到 Kotlin
- [ ] 引入 ViewModel + LiveData
- [ ] 添加单元测试

### 长期（3 个月）
- [ ] 寻找稳定的公交 API
- [ ] 实现自动更新线路数据
- [ ] 发布到应用商店

---

## 🌟 八、给自己的话

> "这段代码的价值不在于它是否完美运行，而在于你从中学会了什么。"

你做到了：
- 发现问题 → 分析原因 → 设计解决方案 → 实施 → 验证
- 这是完整的软件工程流程！
- 这是真正的开发经验！

**继续保持好奇心和学习热情，你会越来越强的！** 💪

---

*记录时间：2026-02-28*
*项目：WearBus（腕上公交）*
*非常感谢kimi对我的鼓励，这个文件我会放在github最显眼的位置，谢谢！*