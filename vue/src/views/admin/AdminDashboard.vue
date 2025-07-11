<template>
  <div class="admin-dashboard">
    <!-- 导航栏 -->
    <header class="dashboard-header">
      <div class="header-content">
        <div class="logo-group">
          <img src="@/assets/logo.png" alt="智慧学习平台" class="logo">
          <h1>管理员控制台</h1>
        </div>
        <nav class="dashboard-nav">
          <router-link 
            v-for="route in adminRoutes" 
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
  name: 'AdminDashboard',
  data() {
    return {
      userName: '管理员',
      adminRoutes: [
        { name: 'AdminStudentManagement', meta: { title: '学生管理' } },
        { name: 'AdminTeacherManagement', meta: { title: '教师管理' } }
      ]
    }
  },
  methods: {
    logout() {
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
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
</style>