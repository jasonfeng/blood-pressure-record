# Code Review 报告

## 📊 概览

| 指标 | 数值 |
|------|------|
| 总文件数 | 23 |
| 总代码行数 | 2,897 |
| 最大文件 | HomeScreen.kt (460行) |
| 平均文件 | 126行 |

---

## 🔴 严重问题 (CRITICAL)

| 文件 | 行号 | 问题 | 建议 |
|------|------|------|------|
| `FeishuService.kt` | 23-26 | OkHttpClient 在每个请求时创建新实例，浪费资源 | 改为使用 Hilt 注入单例 OkHttpClient |
| `SettingsRepository.kt` | 22-23 | App Secret 明文存储在 DataStore 中 | 考虑使用 EncryptedSharedPreferences |

---

## 🟠 高优先级 (HIGH)

| 文件 | 行号 | 问题 | 建议 |
|------|------|------|------|
| `HomeScreen.kt` | 310, 323 | 使用 Emoji (🌅, 🌙) 作为图标 | 使用 Material Icons |
| `HomeScreen.kt` | 401 | Emoji (❤️) 在代码中 | 使用 Icon 组件 |
| `SettingsScreen.kt` | 74, 233 | Emoji (📊) 在代码中 | 使用 Icon 组件 |
| `FeishuService.kt` | 68, 101, 145 | 使用 `execute()` 阻塞主线程 | 使用 `enqueue()` 或在协程中调用 |
| `ExportService.kt` | 59, 108 | 使用 `e.printStackTrace()` | 使用日志框架 |

---

## 🟡 中等优先级 (MEDIUM)

| 文件 | 行号 | 问题 | 建议 |
|------|------|------|------|
| `RecordScreen.kt` | 293 | NumberPicker 没有实现 | 使用 Material 3 TimePicker |
| `HomeScreen.kt` | 189-191 | 每次重组都计算平均值 | 使用 `remember` 缓存 |
| `HomeScreen.kt` | 46-47 | 重复计算 periodText/periodIcon | 提取到变量 |
| `RecordViewModel.kt` | 76-79 | 数字输入验证可合并 | 提取通用函数 |
| `FeishuService.kt` | 30-31 | 内存缓存无线程安全 | 使用 AtomicReference |

---

## 🟢 代码质量 (Code Quality)

### 文件大小分布

| 文件 | 行数 | 状态 |
|------|------|------|
| HomeScreen.kt | 460 | ⚠️ 偏大 |
| SettingsScreen.kt | 378 | ⚠️ 偏大 |
| RecordScreen.kt | 342 | ⚠️ 偏大 |
| 其他 | <200 | ✅ 正常 |

### 良好实践 ✅

- 使用 MVVM 架构
- 使用 StateFlow 管理状态
- 使用 Hilt 依赖注入
- 使用 Room 数据库
- 使用 collectAsStateWithLifecycle
- 代码分层层清晰

---

## 📝 改进建议

### 1. Emoji 替换 (HIGH)

```kotlin
// 替换前
Icon = "🌅"
Text(text = "❤️ $it")

// 替换后
Icon = Icons.Default.WbSunny
Icon(Icons.Default.Favorite, contentDescription = null)
```

### 2. OkHttpClient 注入 (CRITICAL)

```kotlin
// DatabaseModule.kt 添加
@Provides
@Singleton
fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}

// FeishuService.kt
@Singleton
class FeishuService @Inject constructor(
    private val client: OkHttpClient
)
```

### 3. 数值计算缓存 (MEDIUM)

```kotlin
// HomeScreen.kt
val sortedRecords = remember(records) { records.sortedBy { it.date } }
val avgSystolic = remember(sortedRecords) { 
    sortedRecords.map { it.systolic }.average() 
}
```

### 4. 日志替代 printStackTrace (HIGH)

```kotlin
// 替换前
e.printStackTrace()

// 替换后
Log.e(TAG, "Export failed", e)
```

---

## ✅ 通过项

- [x] 无硬编码凭证
- [x] 无 SQL 注入风险
- [x] 无 XSS 漏洞
- [x] 输入验证存在 (RecordViewModel)
- [x] 异常处理存在
- [x] 依赖注入正确使用
- [x] 数据库操作在 IO 线程

---

## 📈 统计

- 🔴 CRITICAL: 2
- 🟠 HIGH: 5
- 🟡 MEDIUM: 5
- ✅ 良好: 8

**总体评价**: 代码质量良好，架构清晰。主要问题是 Emoji 使用和 OkHttpClient 实例管理。
