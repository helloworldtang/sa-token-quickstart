# 踢人功能改进实现说明

## 改进概述

已实现改进的踢人功能，解决原始设计中"无法获取被踢用户Token"的交互问题。

## 新增功能

### 1. 在线会话管理

#### 接口
```
GET /admin/online-sessions
需要权限：登录 + admin角色
```

#### 功能
- ✅ 获取所有在线用户的会话列表
- ✅ 显示用户信息（ID、用户名、昵称、角色）
- ✅ 显示登录时间、最后活跃时间
- ✅ 显示设备信息
- ✅ **不包含敏感Token**

#### 返回数据结构
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "sessionId": "session-123",  // 会话ID（非Token）
      "userId": 1,
      "username": "admin",
      "nickname": "管理员",
      "roles": "admin",
      "loginTime": "2024-04-02T10:00:00",
      "lastActiveTime": "2024-04-02T10:00:00",
      "ip": "127.0.0.1",
      "device": "Chrome / macOS"
    }
  ]
}
```

### 2. 根据用户ID踢人

#### 接口
```
POST /admin/kickout/{userId}
需要权限：登录 + admin角色
Content-Type: application/json（可选）
```

#### 请求示例
```bash
# 踢掉用户ID为2的用户
POST /admin/kickout/2
Authorization: Bearer <管理员Token>
Content-Type: application/json

{
  "reason": "违反用户协议"  // 可选
}
```

#### 优点
- ✅ 符合后台管理习惯
- ✅ 从用户列表选择，点击踢人
- ✅ 踢掉用户的所有登录会话（多设备场景）
- ✅ 无需知道用户的Token
- ✅ 支持记录踢人原因

### 3. 根据会话ID踢人

#### 接口
```
POST /admin/kickout-session/{sessionId}
需要权限：登录 + admin角色
Content-Type: application/json（可选）
```

#### 请求示例
```bash
# 踢掉指定会话
POST /admin/kickout-session/session-123
Authorization: Bearer <管理员Token>
Content-Type: application/json

{
  "reason": "异地登录警告"  // 可选
}
```

#### 优点
- ✅ 精细化控制：可以选择踢掉单个设备
- ✅ 保留用户其他设备的登录
- ✅ 支持多设备登录场景
- ✅ 使用会话ID而非Token
- ✅ 安全合规

## 交互流程对比

### 原始设计（有问题）
```
管理员登录
  ↓
要踢人？
  ↓
需要输入"被踢用户的Token"  ❌ 问题：从哪获取？
  ↓
无法完成操作  ❌
```

### 改进设计（可用）

#### 方式A：用户列表 + 根据ID踢人
```
管理员登录 → 用户管理页面
  ↓
显示用户列表
  - 用户ID：1
  - 用户名：user
  - 角色：普通用户
  ↓
点击"踢人"按钮
  ↓
POST /admin/kickout/2
  ↓
完成 ✓
```

#### 方式B：会话列表 + 根据会话ID踢人
```
管理员登录 → 在线会话页面
  ↓
显示会话列表
  - 用户：user
  - 设备：Chrome / Windows
  - 登录时间：2024-04-02 10:00
  ↓
点击"踢掉此会话"
  ↓
POST /admin/kickout-session/session-123
  ↓
完成 ✓
```

## 新增文件

### DTO 类
- `OnlineSessionDTO.java` - 在线会话信息DTO
- `KickoutRequest.java` - 踢人请求DTO（支持原因）

### 控制器
- `SessionController.java` - 会话管理控制器
  - `GET /admin/online-sessions` - 获取在线会话列表
  - `POST /admin/kickout/{userId}` - 根据用户ID踢人
  - `POST /admin/kickout-session/{sessionId}` - 根据会话ID踢人

### 服务层扩展
- `AuthService.java` - 新增方法：
  - `void kickoutByUserId(Long userId, KickoutRequest request)`
  - `void kickoutBySessionId(String sessionId, KickoutRequest request)`
  - `List<OnlineSessionDTO> getOnlineSessions()`

- `AuthServiceImpl.java` - 实现新增方法

### 测试
- `SessionControllerTest.java` - SessionController 集成测试
- `AuthServiceTest.java` - 新增单元测试

## 使用示例

### 1. 在线会话管理页面

```vue
<template>
  <div>
    <el-button @click="loadSessions">刷新会话列表</el-button>
    <el-table :data="sessions" style="width: 100%">
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="roles" label="角色" width="100" />
      <el-table-column prop="device" label="设备" width="180" />
      <el-table-column prop="loginTime" label="登录时间" width="180" />
      <el-table-column prop="lastActiveTime" label="最后活跃" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button
            type="warning"
            size="small"
            @click="kickoutSession(row.sessionId)">
            踢掉此会话
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
const sessions = ref([]);
const loadSessions = async () => {
  const res = await axios.get('/admin/online-sessions');
  sessions.value = res.data.data;
};

const kickoutSession = (sessionId) => {
  ElMessageBox.confirm('确认踢掉此会话？')
    .then(() => {
      axios.post(`/admin/kickout-session/${sessionId}`, {
        reason: '管理员操作'
      }).then(() => {
        ElMessage.success('踢掉会话成功');
        loadSessions();
      });
    });
};
</script>
```

### 2. 用户管理 + 踢人

```vue
<template>
  <el-table :data="users" @row-click="handleRowClick">
    <el-table-column prop="id" label="用户ID" width="80" />
    <el-table-column prop="username" label="用户名" width="120" />
    <el-table-column prop="nickname" label="昵称" width="120" />
    <el-table-column prop="roles" label="角色" width="120" />
    <el-table-column label="操作" width="200">
      <template #default="{ row }">
        <el-button
          type="danger"
          size="small"
          @click="kickoutUser(row.id)">
          踢人
        </el-button>
      </template>
    </el-table-column>
  </el-table>

  <el-dialog v-model="kickoutDialogVisible" title="踢人原因" width="400px">
    <el-form :model="kickoutForm">
      <el-form-item label="踢人原因">
        <el-input
          v-model="kickoutForm.reason"
          type="textarea"
          placeholder="请输入踢人原因（可选）"
          :rows="3"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="kickoutDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="confirmKickout">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
const kickoutDialogVisible = ref(false);
const selectedUserId = ref(null);
const kickoutForm = ref({ reason: '' });

const kickoutUser = (userId) => {
  selectedUserId.value = userId;
  kickoutDialogVisible.value = true;
};

const confirmKickout = async () => {
  await axios.post(`/admin/kickout/${selectedUserId.value}`, kickoutForm.value);
  ElMessage.success('踢人成功');
  kickoutDialogVisible.value = false;
  loadUsers();
};
</script>
```

## Sa-Token API 使用说明

### 获取在线会话列表

```java
// 搜索所有Token
List<String> tokens = StpUtil.searchTokenValue("", 0, -1, false);

// 遍历Token，获取Session信息
for (String token : tokens) {
    SaSession session = StpUtil.getSessionByToken(token);
    // 从Session获取用户信息
    Object user = session.get("user");
}
```

### 根据用户ID踢人

```java
// 踢掉用户的所有会话
StpUtil.kickout(userId);
```

### 根据会话ID踢人

```java
// 获取会话ID对应的Token
String token = StpUtil.stpLogic.getTokenValueBySessionId(sessionId, "token-id");

// 根据Token踢人
StpUtil.kickoutByTokenValue(token);
```

## Knife4j 文档

启动应用后访问：http://localhost:8081/doc.html

在**会话管理**分组下可以看到：
- 获取在线会话列表
- 根据用户ID踢人
- 踢掉指定会话

## 总结

### 改进前后对比

| 特性 | 原始设计 | 改进设计 |
|------|---------|---------|
| 踢人方式 | 需要输入Token（无法获取）❌ | 根据用户ID/会话ID ✅ |
| 在线用户列表 | 无 ❌ | 完整的会话列表 ✅ |
| 交互体验 | 无法使用 ❌ | 直观易用 ✅ |
| 多设备支持 | 不支持 ❌ | 可选择性踢掉单个设备 ✅ |
| 符合习惯 | 不符合 ❌ | 符合常规后台习惯 ✅ |
| 踢人原因 | 无 ❌ | 支持记录原因 ✅ |

### 测试覆盖

- ✅ 单元测试：AuthService 新增方法测试
- ✅ 集成测试：SessionController 完整测试
- ✅ 权限测试：admin角色验证
- ✅ 登录状态测试：未登录拦截测试

### 推荐使用方式

1. **用户列表 + 根据ID踢人**：
   - 适合需要强制用户下线的场景
   - 踢掉用户的所有设备
   - 操作简单快速

2. **会话列表 + 根据会话ID踢人**：
   - 适合需要精细控制的场景
   - 可以选择踢掉单个设备
   - 保留用户其他会话

两种方式可以共存，根据实际业务需求选择使用。

---

**改进已完成，踢人功能现在可以正常使用！**