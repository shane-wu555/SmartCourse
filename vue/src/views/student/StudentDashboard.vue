<template>
  <div class="student-dashboard">
    <!-- 导航栏 -->
    <header class="dashboard-header">
      <div class="header-content">
        <div class="logo-group">
          <img src="@/assets/logo.png" alt="智慧学习平台" class="logo">
          <h1>学生学习中心</h1>
        </div>
        <nav class="dashboard-nav">
          <router-link 
            v-for="route in studentRoutes" 
            :key="route.path"
            :to="{ name: route.name }"
          >
            {{ route.meta.title }}
          </router-link>
        </nav>
        <div class="user-actions">
          <span class="username">{{ userName }}</span>
          <button @click="logout" class="logout-btn">退出登录</button>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="dashboard-main">
      <router-view></router-view>
    </main>
  </div>
</template>

<script>
export default {
  name: 'StudentDashboard',
  data() {
    return {
      userName: `${localStorage.getItem('username')} `,
      studentRoutes: [
        { name: 'StudentCourses', meta: { title: '课程管理' } },
        { name: 'StudentTask', meta: { title: '我的任务' } },
        { name: 'StudentGrades', meta: { title: '成绩查看' } },
        { name: 'StudentResources', meta: { title: '我的资源' } }
      ]
    }
  },
  methods: {
    async logout() {
    try {
      // 调用后端登出接口
      await this.$api.post('/auth/logout', null, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`
        }
      });
    } catch (error) {
      console.error('登出失败:', error);
      // 即使API失败也继续执行本地清理
    } finally {
      // 清理本地存储
      localStorage.removeItem('token');
      localStorage.removeItem('role');
      // 跳转到登录页
      this.$router.push('/login');
    }
  }
  }
}
</script>

<style scoped>
/* 保持原有样式不变 */
.student-dashboard {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.dashboard-header {
  background: linear-gradient(135deg, #f0f8ff 0%, #c7e0ff 100%);
  color: #2c3e50;
  padding: 15px 0;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.logo-group {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logo-group h1 {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.logo {
  height: 40px;
}

.dashboard-nav {
  display: flex;
  gap: 30px;
}

.dashboard-nav a {
  color: #2c3e50;
  text-decoration: none;
  font-weight: 500;
  padding: 5px 0;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.dashboard-nav a:hover {
  color: #4a90e2;
  border-bottom-color: rgba(74, 144, 226, 0.7);
}

.dashboard-nav a.router-link-exact-active {
  border-bottom-color: #4a90e2;
  font-weight: 600;
  color: #4a90e2;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  font-weight: 500;
  color: #2c3e50;
}

.logout-btn {
  background: rgba(74, 144, 226, 0.1);
  border: 1px solid #4a90e2;
  color: #4a90e2;
  padding: 5px 15px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.logout-btn:hover {
  background: rgba(74, 144, 226, 0.2);
}

.dashboard-main {
  flex: 1;
  background: white;
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.welcome-banner {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.welcome-banner h2 {
  color: #3a7bd5;
  margin-bottom: 10px;
}

.welcome-banner p {
  color: #666;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 40px;
}

.action-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.12);
}

.action-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #f0f8ff 0%, #c7e0ff 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  color: #4a90e2;
  font-size: 24px;
}

.action-card h3 {
  color: #333;
  margin-bottom: 8px;
}

.action-card p {
  color: #666;
  font-size: 14px;
}
</style>