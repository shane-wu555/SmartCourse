<template>
  <header class="app-header">
    <div class="container">
      <div class="logo-group">
        <img src="@/assets/logo.png" alt="AI智慧课程平台" class="logo">
        <h1 class="title">AI智慧课程平台</h1>
      </div>
      <nav class="main-nav">
        <router-link to="/">首页</router-link>
        <router-link to="/features">产品功能</router-link>
        <router-link to="/demo">在线演示</router-link>
        <router-link to="/about">关于我们</router-link>
      </nav>
      <div class="auth-buttons">
        <button class="btn login-btn" @click="navigateToLogin">登录</button>
        <button class="btn register-btn" @click="navigateToRegister">注册</button>
      </div>
    </div>
  </header>
</template>

<script>
export default {
  methods: {
    navigateToLogin() {
      this.$router.push('/login');
    },
    navigateToRegister() {
      this.$router.push('/register');
    }
  }
}
</script>

<style scoped>
.app-header {
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 15px 0;
  position: fixed;
  width: 100%;
  top: 0;
  z-index: 1000;
}

.container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.logo-group {
  display: flex;
  align-items: center;
}

.logo {
  height: 40px;
  margin-right: 15px;
}

.title {
  font-size: 20px;
  color: #2c3e50;
  margin: 0;
  font-weight: 600;
}

.main-nav {
  display: flex;
  gap: 30px;
}

.main-nav a {
  color: #34495e;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.main-nav a:hover {
  color: #3a7bd5;
}

.main-nav a.router-link-exact-active {
  color: #3a7bd5;
  border-bottom: 2px solid #3a7bd5;
  padding-bottom: 5px;
}

.auth-buttons .btn {
  padding: 8px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  margin-left: 10px;
}

.login-btn {
  background-color: transparent;
  border: 1px solid #3a7bd5;
  color: #3a7bd5;
}

.login-btn:hover {
  background-color: #f0f7ff;
}

.register-btn {
  background-color: #3a7bd5;
  border: 1px solid #3a7bd5;
  color: white;
}

.register-btn:hover {
  background-color: #2c6bc7;
}
</style>