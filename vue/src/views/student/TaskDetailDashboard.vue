<template>
  <div class="task-dashboard">
    <!-- 顶部导航栏 -->
    <header class="dashboard-header">
      <div class="header-content">
        <div class="logo-group">
          <img src="@/assets/logo.png" alt="智慧学习平台" class="logo" />
          <h1>我的任务</h1>
        </div>
      </div>
    </header>

    <!-- 主内容区域 -->
    <main class="dashboard-main">
      <router-view />
    </main>
  </div>
</template>

<script>
export default {
  name: 'TaskDetailDashboard',
  methods: {
    goBack() {
      this.$router.back()
    }
  }
}
</script>

<style scoped>
.task-dashboard {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
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
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-group {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logo {
  height: 40px;
}

.logo-group h1 {
  font-size: 20px;
  font-weight: 600;
}

.back-button {
  padding: 8px 16px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
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