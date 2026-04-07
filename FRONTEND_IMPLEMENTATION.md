# 前端实现示例

## 纯 HTML/JavaScript 实现

### 1. 用户管理页面（用户列表 + 踢人）

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户管理 - Sa-Token Demo</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: Arial, sans-serif;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
        }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding: 20px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
        }
        .btn-primary {
            background-color: #409EFF;
            color: white;
        }
        .btn-danger {
            background-color: #F56C6C;
            color: white;
        }
        .table-container {
            background: white;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        table {
            width: 100%;
            border-collapse: collapse;
        }
        th, td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        th {
            background-color: #f5f7fa;
            font-weight: bold;
        }
        tr:hover {
            background-color: #f5f5f5;
        }
        .dialog {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.5);
            z-index: 1000;
            align-items: center;
            justify-content: center;
        }
        .dialog-content {
            background: white;
            padding: 30px;
            border-radius: 8px;
            width: 400px;
            max-width: 90%;
        }
        .dialog-title {
            font-size: 18px;
            font-weight: bold;
            margin-bottom: 20px;
        }
        textarea {
            width: 100%;
            height: 80px;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            resize: vertical;
            font-family: inherit;
        }
        .dialog-buttons {
            display: flex;
            justify-content: flex-end;
            gap: 10px;
            margin-top: 20px;
        }
        .token-display {
            font-family: monospace;
            background: #f5f5f5;
            padding: 10px;
            border-radius: 4px;
            margin-top: 10px;
            word-break: break-all;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- 顶部栏 -->
        <div class="header">
            <h1>用户管理系统</h1>
            <div>
                <span id="currentUsername">未登录</span>
                <button class="btn btn-primary" onclick="showLoginDialog()">登录</button>
                <button class="btn" onclick="logout()">退出</button>
            </div>
        </div>

        <!-- 用户列表 -->
        <div class="table-container">
            <h2>用户列表</h2>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>用户名</th>
                        <th>昵称</th>
                        <th>角色</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody id="userTableBody">
                    <!-- 数据通过JS加载 -->
                </tbody>
            </table>
        </div>
    </div>

    <!-- 登录对话框 -->
    <div id="loginDialog" class="dialog">
        <div class="dialog-content">
            <div class="dialog-title">登录</div>
            <div>
                <label>用户名：</label>
                <input type="text" id="loginUsername" value="admin" style="width: 100%; padding: 10px; margin-bottom: 10px;">
                <label>密码：</label>
                <input type="password" id="loginPassword" value="123456" style="width: 100%; padding: 10px; margin-bottom: 20px;">
                <button class="btn btn-primary" style="width: 100%;" onclick="login()">登录</button>
                <button class="btn" style="width: 100%; margin-top: 10px;" onclick="hideLoginDialog()">取消</button>
            </div>
        </div>
    </div>

    <!-- 踢人对话框 -->
    <div id="kickoutDialog" class="dialog">
        <div class="dialog-content">
            <div class="dialog-title">踢人下线</div>
            <div>
                <p>确定要踢掉用户：<strong id="kickoutUsername"></strong> ？</p>
                <label style="display: block; margin-bottom: 10px;">踢人原因（可选）：</label>
                <textarea id="kickoutReason" placeholder="请输入踢人原因..."></textarea>
                <div class="dialog-buttons">
                    <button class="btn" onclick="hideKickoutDialog()">取消</button>
                    <button class="btn btn-danger" onclick="confirmKickout()">确认踢人</button>
                </div>
            </div>
        </div>
    </div>

    <script>
        // 配置
        const API_BASE = 'http://localhost:8081';
        let adminToken = '';
        let selectedUserId = null;

        // 初始化
        document.addEventListener('DOMContentLoaded', () => {
            checkLoginStatus();
            if (adminToken) {
                loadUsers();
            }
        });

        // 检查登录状态
        function checkLoginStatus() {
            const token = localStorage.getItem('token');
            if (token) {
                adminToken = token;
                document.getElementById('currentUsername').textContent = '管理员已登录';
                loadUsers();
            }
        }

        // 显示登录对话框
        function showLoginDialog() {
            document.getElementById('loginDialog').style.display = 'flex';
        }

        // 隐藏登录对话框
        function hideLoginDialog() {
            document.getElementById('loginDialog').style.display = 'none';
        }

        // 显示踢人对话框
        function showKickoutDialog(userId, username) {
            selectedUserId = userId;
            document.getElementById('kickoutUsername').textContent = username;
            document.getElementById('kickoutReason').value = '';
            document.getElementById('kickoutDialog').style.display = 'flex';
        }

        // 隐藏踢人对话框
        function hideKickoutDialog() {
            document.getElementById('kickoutDialog').style.display = 'none';
        }

        // 登录
        async function login() {
            const username = document.getElementById('loginUsername').value;
            const password = document.getElementById('loginPassword').value;

            try {
                const response = await fetch(`${API_BASE}/auth/login`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username, password })
                });

                const data = await response.json();

                if (data.code === 200) {
                    adminToken = data.data.token;
                    localStorage.setItem('token', adminToken);
                    document.getElementById('currentUsername').textContent = '管理员已登录';
                    hideLoginDialog();
                    loadUsers();
                } else {
                    alert('登录失败：' + data.message);
                }
            } catch (error) {
                alert('登录失败：' + error.message);
            }
        }

        // 退出登录
        function logout() {
            fetch(`${API_BASE}/auth/logout`, {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + adminToken
                }
            }).then(() => {
                localStorage.removeItem('token');
                adminToken = '';
                document.getElementById('currentUsername').textContent = '未登录';
                document.getElementById('userTableBody').innerHTML = '';
            }).catch(error => {
                alert('退出失败：' + error.message);
            });
        }

        // 加载用户列表
        async function loadUsers() {
            try {
                // 模拟用户列表数据（实际应该从用户管理接口获取）
                const users = [
                    { id: 1, username: 'admin', nickname: '管理员', roles: 'admin' },
                    { id: 2, username: 'user', nickname: '普通用户', roles: 'user' },
                    { id: 3, username: 'editor', nickname: '编辑', roles: 'user' },
                    { id: 4, username: 'viewer', nickname: '访客', roles: 'user' }
                ];

                const tbody = document.getElementById('userTableBody');
                tbody.innerHTML = '';

                users.forEach(user => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${user.id}</td>
                        <td>${user.username}</td>
                        <td>${user.nickname}</td>
                        <td>${user.roles}</td>
                        <td>
                            ${user.id !== 1 ? `<button class="btn btn-danger" onclick="showKickoutDialog(${user.id}, '${user.username}')">踢人</button>` : ''}
                        </td>
                    `;
                    tbody.appendChild(row);
                });

            } catch (error) {
                console.error('加载用户列表失败：', error);
            }
        }

        // 确认踢人
        async function confirmKickout() {
            const reason = document.getElementById('kickoutReason').value;

            try {
                const response = await fetch(`${API_BASE}/admin/kickout/${selectedUserId}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + adminToken
                    },
                    body: JSON.stringify({ reason })
                });

                const data = await response.json();

                if (data.code === 200) {
                    alert('踢人成功');
                    hideKickoutDialog();
                    loadUsers();
                } else {
                    alert('踢人失败：' + data.message);
                }
            } catch (error) {
                alert('踢人失败：' + error.message);
            }
        }

        // 关闭对话框（点击外部）
        document.getElementById('loginDialog').addEventListener('click', function(e) {
            if (e.target.id === 'loginDialog') {
                hideLoginDialog();
            }
        });

        document.getElementById('kickoutDialog').addEventListener('click', function(e) {
            if (e.target.id === 'kickoutDialog') {
                hideKickoutDialog();
            }
        });
    </script>
</body>
</html>
```

### 2. 在线会话管理页面

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>在线会话管理 - Sa-Token Demo</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: Arial, sans-serif;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            max-width: 1400px;
            margin: 0 auto;
        }
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding: 20px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
        }
        .btn-primary {
            background-color: #409EFF;
            color: white;
        }
        .btn-warning {
            background-color: #E6A23C;
            color: white;
        }
        .table-container {
            background: white;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        table {
            width: 100%;
            border-collapse: collapse;
        }
        th, td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        th {
            background-color: #f5f7fa;
            font-weight: bold;
        }
        tr:hover {
            background-color: #f5f5f5;
        }
        .badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: bold;
        }
        .badge-admin {
            background-color: #F56C6C;
            color: white;
        }
        .badge-user {
            background-color: #409EFF;
            color: white;
        }
        .device-tag {
            background-color: #909399;
            color: white;
            padding: 2px 6px;
            border-radius: 3px;
            font-size: 11px;
        }
        .status-online {
            color: #67C23A;
            font-weight: bold;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- 顶部栏 -->
        <div class="header">
            <h1>在线会话管理</h1>
            <div>
                <span id="currentUsername">未登录</span>
                <button class="btn btn-primary" onclick="showLoginDialog()">管理员登录</button>
                <button class="btn btn-primary" onclick="refreshSessions()">刷新</button>
            </div>
        </div>

        <!-- 会话列表 -->
        <div class="table-container">
            <h2>在线会话列表</h2>
            <p style="color: #666; margin-bottom: 10px;">
                当前在线会话数：<strong id="sessionCount">0</strong> 个
            </p>
            <table>
                <thead>
                    <tr>
                        <th>用户名</th>
                        <th>角色</th>
                        <th>设备</th>
                        <th>登录时间</th>
                        <th>最后活跃</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody id="sessionTableBody">
                    <!-- 数据通过JS加载 -->
                </tbody>
            </table>
        </div>
    </div>

    <!-- 登录对话框 -->
    <div id="loginDialog" class="dialog">
        <div class="dialog-content">
            <div class="dialog-title">管理员登录</div>
            <div>
                <label>用户名：</label>
                <input type="text" id="loginUsername" value="admin" style="width: 100%; padding: 10px; margin-bottom: 10px;">
                <label>密码：</label>
                <input type="password" id="loginPassword" value="123456" style="width: 100%; padding: 10px; margin-bottom: 20px;">
                <button class="btn btn-primary" style="width: 100%;" onclick="login()">登录</button>
                <button class="btn" style="width: 100%; margin-top: 10px;" onclick="hideLoginDialog()">取消</button>
            </div>
        </div>
    </div>

    <script>
        // 配置
        const API_BASE = 'http://localhost:8081';
        let adminToken = '';

        // 初始化
        document.addEventListener('DOMContentLoaded', () => {
            checkLoginStatus();
            if (adminToken) {
                loadSessions();
            }
        });

        // 检查登录状态
        function checkLoginStatus() {
            const token = localStorage.getItem('token');
            if (token) {
                adminToken = token;
                document.getElementById('currentUsername').textContent = '管理员已登录';
            }
        }

        // 显示登录对话框
        function showLoginDialog() {
            document.getElementById('loginDialog').style.display = 'flex';
        }

        // 隐藏登录对话框
        function hideLoginDialog() {
            document.getElementById('loginDialog').style.display = 'none';
        }

        // 登录
        async function login() {
            const username = document.getElementById('loginUsername').value;
            const password = document.getElementById('loginPassword').value;

            try {
                const response = await fetch(`${API_BASE}/auth/login`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username, password })
                });

                const data = await response.json();

                if (data.code === 200) {
                    adminToken = data.data.token;
                    localStorage.setItem('token', adminToken);
                    document.getElementById('currentUsername').textContent = '管理员已登录';
                    hideLoginDialog();
                    loadSessions();
                } else {
                    alert('登录失败：' + data.message);
                }
            } catch (error) {
                alert('登录失败：' + error.message);
            }
        }

        // 刷新会话列表
        async function refreshSessions() {
            if (!adminToken) {
                alert('请先登录');
                return;
            }
            loadSessions();
        }

        // 加载会话列表
        async function loadSessions() {
            try {
                const response = await fetch(`${API_BASE}/admin/online-sessions`, {
                    headers: {
                        'Authorization': 'Bearer ' + adminToken
                    }
                });

                const data = await response.json();

                if (data.code === 200) {
                    const sessions = data.data;
                    document.getElementById('sessionCount').textContent = sessions.length;

                    const tbody = document.getElementById('sessionTableBody');
                    tbody.innerHTML = '';

                    sessions.forEach(session => {
                        const row = document.createElement('tr');

                        // 判断是否为管理员
                        const isAdmin = session.roles && session.roles.includes('admin');
                        const badgeClass = isAdmin ? 'badge-admin' : 'badge-user';
                        const badgeText = isAdmin ? '管理员' : '普通用户';

                        row.innerHTML = `
                            <td>
                                <span class="badge ${badgeClass}">${badgeText}</span>
                                ${session.username}
                            </td>
                            <td>${session.roles || '-'}</td>
                            <td><span class="device-tag">${session.device || '未知'}</span></td>
                            <td>${formatDateTime(session.loginTime)}</td>
                            <td>${formatDateTime(session.lastActiveTime)}</td>
                            <td>
                                <button class="btn btn-warning" onclick="kickoutSession('${session.sessionId}', '${session.username}')">
                                    踢掉此会话
                                </button>
                            </td>
                        `;
                        tbody.appendChild(row);
                    });

                } else {
                    alert('加载失败：' + data.message);
                }
            } catch (error) {
                console.error('加载会话列表失败：', error);
                alert('加载失败：' + error.message);
            }
        }

        // 踢掉会话
        async function kickoutSession(sessionId, username) {
            if (!confirm(`确认要踢掉用户 [${username}] 的会话吗？`)) {
                return;
            }

            try {
                const response = await fetch(`${API_BASE}/admin/kickout-session/${sessionId}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + adminToken
                    },
                    body: JSON.stringify({ reason: '管理员操作' })
                });

                const data = await response.json();

                if (data.code === 200) {
                    alert('踢掉会话成功');
                    loadSessions();
                } else {
                    alert('踢掉会话失败：' + data.message);
                }
            } catch (error) {
                alert('踢掉会话失败：' + error.message);
            }
        }

        // 格式化日期时间
        function formatDateTime(dateStr) {
            if (!dateStr) return '-';

            const date = new Date(dateStr);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');

            return `${year}-${month}-${day} ${hours}:${minutes}`;
        }

        // 点击对话框外部关闭
        document.getElementById('loginDialog').addEventListener('click', function(e) {
            if (e.target.id === 'loginDialog') {
                hideLoginDialog();
            }
        });
    </script>
</body>
</html>
```

## 使用说明

### 文件1：用户管理页面
保存为 `user-management.html`，直接在浏览器中打开即可使用。

**功能**：
- 用户列表展示
- 点击"踢人"按钮（admin用户除外）
- 踢人原因输入（可选）
- 登录/退出功能

### 文件2：在线会话管理页面
保存为 `online-sessions.html`，直接在浏览器中打开即可使用。

**功能**：
- 在线会话列表展示（不含敏感Token）
- 显示用户信息、角色、设备、登录时间
- 点击"踢掉此会话"按钮
- 自动刷新会话数量

### API 地址配置

在两个 HTML 文件中，找到以下配置：
```javascript
const API_BASE = 'http://localhost:8081';
```

根据实际部署修改此地址。

---

**完整的纯 HTML/JavaScript 实现，无需任何框架依赖！**