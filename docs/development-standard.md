# 血压记录 App 开发规范

## 一、项目概述

### 1.1 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Kotlin | 1.9.20 |
| UI框架 | Jetpack Compose | BOM 2023.10.01 |
| 编译SDK | Android | 34 |
| 最小SDK | Android | 26 |
| 架构 | MVVM + Clean Architecture | - |
| DI框架 | Hilt | 2.48.1 |
| 数据库 | Room | 2.6.1 |
| 网络 | Retrofit + OkHttp | 2.9.0 / 4.12.0 |
| 导航 | Navigation Compose | 2.7.5 |
| 协程 | Kotlin Coroutines | 1.7.3 |
| 图表 | Vico | 1.13.1 |

### 1.2 模块结构

```
app/
├── src/main/java/com/bloodpressure/app/
│   ├── domain/           # 领域层 - 业务逻辑
│   │   ├── model/        # 领域模型
│   │   └── usecase/      # 用例
│   ├── data/            # 数据层 - 数据操作
│   │   ├── local/        # 本地数据库
│   │   ├── remote/      # 远程API
│   │   ├── repository/  # 仓储实现
│   │   ├── export/      # 导出服务
│   │   └── preferences/ # 偏好设置
│   ├── di/              # 依赖注入
│   └── ui/              # 表现层
│       ├── home/        # 首页功能
│       ├── record/      # 记录功能
│       ├── history/     # 历史功能
│       ├── settings/    # 设置功能
│       ├── navigation/  # 导航
│       └── theme/      # 主题
```

---

## 二、架构规范

### 2.1 分层架构

采用 **Clean Architecture** 三层架构 + **MVVM** 模式：

```
┌─────────────────────────────────────────┐
│           UI Layer (Compose)            │
│   Screens → ViewModels → UI State       │
├─────────────────────────────────────────┤
│            Domain Layer                 │
│   Models → UseCases → Repository Interface│
├─────────────────────────────────────────┤
│             Data Layer                  │
│   Repository Impl → Data Sources        │
│   (Room / Retrofit / DataStore)         │
└─────────────────────────────────────────┘
```

### 2.2 层间依赖规则

- **UI层** 依赖 **Domain层**，不直接依赖 Data 层
- **Domain层** 是独立的，不依赖任何其他层
- **Data层** 实现 Domain 层定义的接口

### 2.3 通信方式

| 层级间通信 | 方式 |
|-----------|------|
| UI → ViewModel | StateFlow / SharedFlow |
| ViewModel → UseCase | suspend 函数 |
| UseCase → Repository | suspend 函数 |
| Repository → DataSource | suspend 函数 |

---

## 三、命名规范

### 3.1 文件命名

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| Kotlin文件 | UpperCamelCase | `BloodPressureRecord.kt` |
| Compose文件 | UpperCamelCase | `HomeScreen.kt` |
| ViewModel | Screen + ViewModel | `HomeViewModel.kt` |
| Entity | Entity后缀 | `BloodPressureRecordEntity.kt` |
| DAO | DAO后缀 | `BloodPressureDao.kt` |
| Repository | Repository后缀 | `BloodPressureRepository.kt` |
| UseCase | 具体业务 + UseCase | `GetRecordsUseCase.kt` |
| 模块包名 | 全小写，驼峰 | `com.bloodpressure.app` |

### 3.2 类/接口命名

| 类型 | 规则 | 示例 |
|------|------|------|
| 普通类 | UpperCamelCase | `class MainActivity` |
| 数据类 | UpperCamelCase | `data class User` |
| 抽象类 | Base/Abstract前缀 | `abstract class BaseViewModel` |
| 异常类 | Exception后缀 | `class NetworkException` |
| 接口 | I或able后缀 | `interface Repository` |
| sealed class | 描述性名称 | `sealed class UiState` |
| enum | 描述性名称 | `enum class Period` |

### 3.3 函数命名

| 场景 | 规则 | 示例 |
|------|------|------|
| 业务方法 | 动词/动词短语 | `getRecords()`, `saveRecord()` |
| 事件处理 | on+事件名 | `onRecordClick()` |
| 状态变化 | update+状态名 | `updateLoadingState()` |
| 协程启动 | 动词+Async | `loadDataAsync()` |

### 3.4 变量命名

| 类型 | 规则 | 示例 |
|------|------|------|
| 成员变量 | lowerCamelCase | `private val recordList` |
| 局部变量 | lowerCamelCase | `val filteredList` |
| 常量 | UPPER_SNAKE_CASE | `const val MAX_RECORDS = 100` |
| 布尔变量 | is/has/can前缀 | `isLoading`, `hasData` |
| 集合变量 | 复数形式 | `records`, `items` |

---

## 四、代码风格规范

### 4.1 Kotlin 代码约定

遵循 [Kotlin 官方编码约定](https://kotlinlang.org/docs/coding-conventions.html)：

#### 格式化
- 使用4个空格缩进（而非Tab）
- 大括号与语句同行
- 运算符前后加空格
- 单行不超过120字符

#### 命名
```kotlin
// 类名 - UpperCamelCase
class BloodPressureRecord

// 函数/变量 - lowerCamelCase
fun getRecordById(id: Long): BloodPressureRecord?
val recordList = mutableListOf()

// 常量 - UPPER_SNAKE_CASE
companion object {
    const val DEFAULT_PAGE_SIZE = 20
    const val MAX_UPLOAD_SIZE = 10 * 1024 * 1024L
}

// 枚举 - 全大写或驼峰
enum class Period { MORNING, EVENING }
```

#### 空安全
```kotlin
// 推荐：使用安全调用和 Elvis 操作符
val name = user?.name ?: "Unknown"

// 不推荐：使用 == null 检查
if (user?.name != null) { }

// 推荐：lateinit 延迟初始化
private lateinit var binding: ActivityMainBinding

// 不推荐：可空类型使用 as? 或 as
val nullable: String? = "value"
val result = nullable as? String ?: ""
```

### 4.2 Compose 规范

#### 状态管理
```kotlin
// 推荐：使用 StateFlow
private val _uiState = MutableStateFlow(HomeUiState())
val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

// 推荐：collectAsStateWithLifecycle
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
}
```

#### 重组优化
```kotlin
// 推荐：使用 remember 避免重组时重新计算
@Composable
fun MyComponent(data: List<Item>) {
    val sortedData = remember(data) {
        data.sortedByDescending { it.timestamp }
    }
}

// 推荐：使用 derivedStateOf 减少下游重组
@Composable
fun ListScreen(items: List<Item>) {
    val sortedItems by remember {
        derivedStateOf {
            items.sortedByDescending { it.date }
        }
    }
}

// 推荐：使用 stable 注解标记稳定类型
@Stable
class UiState {
    val items: List<Item>
}
```

#### 命名约定
```kotlin
// Composable 函数 - 名词或动词现在分词
@Composable
fun HomeScreen() { }

@Composable
fun RecordCard(record: BloodPressureRecord) { }

// 状态类 - UiState 后缀
data class HomeUiState(
    val isLoading: Boolean = false,
    val records: List<BloodPressureRecord> = emptyList(),
    val error: String? = null
)

// 事件类 - UiEvent 后缀
sealed class HomeUiEvent {
    data object Refresh : HomeUiEvent()
    data class DeleteRecord(val id: Long) : HomeUiEvent()
}
```

---

## 五、目录结构规范

### 5.1 推荐结构

```
app/src/main/java/com/bloodpressure/app/
├── domain/
│   ├── model/
│   │   └── BloodPressureRecord.kt
│   └── usecase/
│       ├── GetRecordsUseCase.kt
│       ├── SaveRecordUseCase.kt
│       └── DeleteRecordUseCase.kt
├── data/
│   ├── local/
│   │   ├── BloodPressureDatabase.kt
│   │   ├── dao/
│   │   │   └── BloodPressureDao.kt
│   │   └── entity/
│   │       └── BloodPressureRecordEntity.kt
│   ├── remote/
│   │   └── FeishuService.kt
│   ├── repository/
│   │   └── BloodPressureRepository.kt
│   └── preferences/
│       └── SettingsRepository.kt
├── di/
│   └── DatabaseModule.kt
└── ui/
    ├── home/
    │   ├── HomeScreen.kt
    │   └── HomeViewModel.kt
    ├── record/
    │   ├── RecordScreen.kt
    │   └── RecordViewModel.kt
    ├── history/
    │   ├── HistoryScreen.kt
    │   └── HistoryViewModel.kt
    ├── settings/
    │   ├── SettingsScreen.kt
    │   └── SettingsViewModel.kt
    ├── navigation/
    │   └── Navigation.kt
    ├── theme/
    │   ├── Theme.kt
    │   └── Type.kt
    └── MainActivity.kt
```

### 5.2 资源文件结构

```
app/src/main/res/
├── drawable/
│   ├── ic_launcher_background.xml
│   └── ic_launcher_foreground.xml
├── mipmap-anydpi-v26/
│   └── ic_launcher.xml
├── values/
│   ├── strings.xml
│   └── themes.xml
└── xml/
    └── file_paths.xml
```

---

## 六、依赖注入规范

### 6.1 Hilt 模块组织

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        app: Application
    ): BloodPressureDatabase {
        return Room.databaseBuilder(
            app,
            BloodPressureDatabase::class.java,
            "blood_pressure.db"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideDao(database: BloodPressureDatabase): BloodPressureDao {
        return database.bloodPressureDao()
    }
}
```

### 6.2 注入方式

```kotlin
// ViewModel 注入 - 使用 hiltViewModel()
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) { }

// 协程作用域 - 使用 viewModelScope
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecordsUseCase: GetRecordsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    fun loadRecords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // ...
        }
    }
}
```

---

## 七、数据库规范

### 7.1 Room 实体定义

```kotlin
@Entity(tableName = "blood_pressure_records")
data class BloodPressureRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,           // 存储格式: yyyy-MM-dd
    val period: String,         // 存储枚举名
    val systolic: Int,
    val diastolic: Int,
    val heartRate: Int?,
    val note: String?,
    val syncStatus: String,
    val syncTime: String?,
    val createdAt: String,      // 存储格式: yyyy-MM-dd'T'HH:mm:ss
    val updatedAt: String
)
```

### 7.2 DAO 定义

```kotlin
@Dao
interface BloodPressureDao {
    
    @Query("SELECT * FROM blood_pressure_records ORDER BY date DESC, period DESC")
    fun getAllRecords(): Flow<List<BloodPressureRecordEntity>>
    
    @Query("SELECT * FROM blood_pressure_records WHERE date = :date ORDER BY period DESC")
    fun getRecordsByDate(date: String): Flow<List<BloodPressureRecordEntity>>
    
    @Query("SELECT * FROM blood_pressure_records WHERE id = :id")
    suspend fun getRecordById(id: Long): BloodPressureRecordEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BloodPressureRecordEntity): Long
    
    @Update
    suspend fun updateRecord(record: BloodPressureRecordEntity)
    
    @Delete
    suspend fun deleteRecord(record: BloodPressureRecordEntity)
    
    @Query("DELETE FROM blood_pressure_records WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

### 7.3 Domain 与 Entity 转换

```kotlin
// Entity 到 Domain
fun BloodPressureRecordEntity.toDomain(): BloodPressureRecord {
    return BloodPressureRecord(
        id = id,
        date = LocalDate.parse(date),
        period = Period.valueOf(period),
        // ...
    )
}

// Domain 到 Entity
fun BloodPressureRecord.toEntity(): BloodPressureRecordEntity {
    return BloodPressureRecordEntity(
        id = id,
        date = date.toString(),
        period = period.name,
        // ...
    )
}
```

---

## 八、网络规范

### 8.1 Retrofit 服务定义

```kotlin
interface FeishuService {
    
    @POST("app/v1/records/sync")
    suspend fun syncToTable(
        @Body request: SyncRequest
    ): FeishuResult<SyncResponse>
    
    @GET("app/v1/records")
    suspend fun getRecords(): FeishuResult<List<RecordResponse>>
    
    @GET("health")
    suspend fun testConnection(): FeishuResult<Unit>
}
```

### 8.2 OkHttp 拦截器

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(LoggingInterceptor.Builder()
                .setLevel(Level.BODY)
                .build())
            .build()
    }
}
```

---

## 九、Git 提交规范

### 9.1 提交信息格式

使用 [Conventional Commits](https://www.conventionalcommits.org/) 格式：

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 9.2 Type 类型

| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | Bug修复 |
| docs | 文档更新 |
| style | 代码格式调整 |
| refactor | 重构 |
| test | 测试相关 |
| chore | 构建/工具链更新 |

### 9.3 示例

```bash
# 新功能
git commit -m "feat: add blood pressure record export to CSV"

# Bug修复
git commit -m "fix: resolve date parsing issue in morning period"

# 文档
git commit -m "docs: update README with installation instructions"

# 重构
git commit -m "refactor: extract common repository logic"
```

---

## 十、测试规范

### 10.1 单元测试

```kotlin
class GetRecordsUseCaseTest {
    
    @Test
    fun `should return records when repository returns data`() = runTest {
        // given
        val expectedRecords = listOf(
            BloodPressureRecord(
                id = 1,
                date = LocalDate.now(),
                period = Period.MORNING,
                systolic = 120,
                diastolic = 80
            )
        )
        whenever(repository.getAllRecords()).thenReturn(expectedRecords)
        
        // when
        val result = useCase()
        
        // then
        assertEquals(expectedRecords, result)
    }
}
```

### 10.2 测试覆盖

| 模块 | 覆盖率要求 |
|------|------------|
| UseCase | ≥80% |
| Repository | ≥70% |
| ViewModel | ≥60% |

### 10.3 自动化UI测试（Espresso）

本项目采用 **Espresso** 作为行业标准的自动化UI测试框架。

#### 10.3.1 为什么选择Espresso？

| 方案 | 优点 | 缺点 |
|------|------|------|
| **Espresso** | 专为Android设计，无需特殊权限，API友好，社区成熟 | 仅支持Android |
| UI Automator | 跨应用测试，权限灵活 | 速度较慢，API较复杂 |
| Appium | 跨平台（iOS/Android/Web） | 需额外服务器，配置复杂 |
| adb shell input tap | 无需App修改 | 需要INJECT_EVENTS权限（root），不可靠 |

**结论**：Espresso 是本项目App内测试的最佳选择。

#### 10.3.2 依赖配置

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        // ...
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    // Espresso Core
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    
    // JUnit4 Runner
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    
    // Compose Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    // Activity Scenario
    androidTestImplementation("androidx.test:activity:1.8.2")
}
```

#### 10.3.3 测试文件结构

```
app/src/androidTest/java/com/bloodpressure/app/
└── ui/
    └── MainActivityTest.kt
```

#### 10.3.4 测试示例

```kotlin
package com.bloodpressure.app.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun appLaunches() {
        // 验证应用启动成功
        onView(withId(R.id.main_container))
            .check(matches(isDisplayed()))
    }
}
```

#### 10.3.5 常用Espresso API

##### 查找视图
```kotlin
// 按ID查找
onView(withId(R.id.btn_save))

// 按文本查找
onView(withText("保存"))

// 按内容描述查找
onView(withContentDescription("返回"))

 // 按ID+文本组合
onView(allOf(withId(R.id.btn_save), withText("保存")))
```

##### 视图断言
```kotlin
// 验证可见性
.check(matches(isDisplayed()))
.check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))

// 验证文本内容
.check(matches(withText("正常")))
.check(matches(withHint("请输入收缩压")))

// 验证存在性
.check(matches(isDisplayed()))
```

##### 用户交互
```kotlin
// 点击操作
onView(withId(R.id.btn_save)).perform(click())

// 长按操作
onView(withId(R.id.record_item)).perform(longClick())

// 输入文本
onView(withId(R.id.input_systolic)).perform(typeText("120"))

// 清除文本
onView(withId(R.id.input_systolic)).perform(clearText())

// 滑动操作
onView(withId(R.id.scroll_view)).perform(swipeUp())
```

##### Compose测试
```kotlin
// Compose Matcher
onNodeWithText("保存").assertIsDisplayed()

// 点击Compose元素
onNodeWithText("保存").performClick()

// 滚动到Compose元素
onNodeWithText("历史记录").performScrollTo().assertIsDisplayed()
```

#### 10.3.6 运行测试

```bash
# 运行所有Instrumented测试
./gradlew connectedAndroidTest

# 运行特定测试类
./gradlew connectedAndroidTest --tests="com.bloodpressure.app.ui.MainActivityTest"

# 运行特定测试方法
./gradlew connectedAndroidTest --tests="com.bloodpressure.app.ui.MainActivityTest.appLaunches"
```

#### 10.3.7 测试最佳实践

1. **每个测试独立**：测试之间不应有依赖关系
2. **命名规范**：测试方法名应描述测试场景
3. **AAA模式**：Arrange（准备）→ Act（执行）→ Assert（断言）
4. **幂等性**：测试可以重复运行，结果一致
5. **只测UI行为**：不测试业务逻辑（业务逻辑用单元测试）

#### 10.3.8 待扩展测试用例

```kotlin
@Test
fun testSaveBloodPressureRecord() {
    // 1. 点击添加按钮
    onView(withId(R.id.btn_add)).perform(click())
    
    // 2. 输入血压数据
    onView(withId(R.id.input_systolic)).perform(typeText("120"))
    onView(withId(R.id.input_diastolic)).perform(typeText("80"))
    
    // 3. 点击保存
    onView(withId(R.id.btn_save)).perform(click())
    
    // 4. 验证保存成功
    onView(withText("保存成功")).check(matches(isDisplayed()))
}

@Test
fun testDeleteRecord() {
    // 1. 长按记录
    onView(withId(R.id.record_item)).perform(longClick())
    
    // 2. 确认删除
    onView(withText("确认")).perform(click())
    
    // 3. 验证删除成功
    onView(withText("删除成功")).check(matches(isDisplayed()))
}

@Test
fun testHistoryFilter() {
    // 1. 点击筛选按钮
    onView(withId(R.id.btn_filter)).perform(click())
    
    // 2. 选择"昨天"
    onView(withText("昨天")).perform(click())
    
    // 3. 验证筛选结果
    onView(withId(R.id.history_list)).check(matches(isDisplayed()))
}
```

---

## 十一、安全规范

### 11.1 敏感信息

- **禁止** 在代码中硬编码密钥、Token
- 使用 `BuildConfig` 或安全存储方案
- 日志中**禁止**输出敏感信息

### 11.2 网络安全

- 使用 HTTPS
- 验证服务器证书
- 接口添加身份验证

### 11.3 数据存储

- 用户偏好使用 DataStore
- 敏感数据考虑加密存储

---

## 十二、版本规范

### 12.1 版本号格式

遵循 [语义化版本](https://semver.org/)：

```
MAJOR.MINOR.PATCH
```

| 级别 | 说明 |
|------|------|
| MAJOR | 不兼容的API变更 |
| MINOR | 向后兼容的功能新增 |
| PATCH | 向后兼容的问题修复 |

### 12.2 Android 版本

- **versionCode**: 整数递增
- **versionName**: 语义化版本号

---

## 附录

### A. 参考资料

1. [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
2. [Jetpack Compose Best Practices](https://developer.android.com/develop/ui/compose/performance/bestpractices)
3. [Android Architecture](https://developer.android.com/topic/architecture)
4. [Hilt Documentation](https://dagger.dev/hilt/)
5. [Room Database](https://developer.android.com/jetpack/androidx/releases/room)
6. [Now in Android - Sample App](https://github.com/android/nowinandroid)

### B. 工具推荐

- 静态分析: detekt
- 格式化: ktlint
- 依赖检查: Gradle dependency analysis
