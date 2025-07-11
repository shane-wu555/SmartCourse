<template>
  <footer class="app-footer">
    <div class="container">
      <div class="footer-content">
        <div class="brand-info">
          <div class="logo-group">
            <img src="@/assets/logo.png" alt="AI智慧课程平台" class="logo">
            <div class="platform-info">
              <div class="platform-name">AI智慧课程平台</div>
              <div class="platform-desc">人工智能赋能教育变革 · 知识图谱驱动个性化学习</div>
            </div>
          </div>
        </div>
        <div class="footer-links">
          <div class="link-group">
            <h5>产品服务</h5>
            <router-link to="/features">功能特性</router-link>
            <router-link to="/pricing">价格方案</router-link>
            <router-link to="/updates">更新日志</router-link>
          </div>
          <div class="link-group">
            <h5>资源中心</h5>
            <router-link to="/docs">帮助文档</router-link>
            <router-link to="/api">开发者API</router-link>
            <router-link to="/community">用户社区</router-link>
          </div>
          <div class="link-group">
            <h5>关于我们</h5>
            <router-link to="/about">公司介绍</router-link>
            <router-link to="/contact">联系我们</router-link>
            <router-link to="/careers">加入团队</router-link>
          </div>
        </div>
      </div>
      <div class="copyright">
        <p>© 2023 AI智慧课程平台 版权所有 | 京ICP备XXXXXXXX号</p>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'AppFooter'
}
</script>

<style scoped>
.app-footer {
  background-color: #f8fafc;
  color: #4a5568;
  padding: 50px 0 20px;
  border-top: 1px solid #e2e8f0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-content {
  display: flex;
  flex-wrap: wrap;
  gap: 60px;
  margin-bottom: 40px;
}

/* 修改部分：品牌信息区下移 */
.brand-info {
  flex: 1;
  min-width: 380px;
  padding-right: 80px;
  padding-top: 20px; /* 新增：整体下移20px */
  margin-bottom: 10px; /* 新增：增加下边距 */
}

.logo-group {
  display: flex;
  align-items: center;
  gap: 20px;
}

.logo {
  height: 60px;
  width: auto;
}

.platform-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.platform-name {
  font-size: 24px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}

.platform-desc {
  font-size: 16px;
  color: #718096;
  line-height: 1.5;
}

.footer-links {
  display: flex;
  flex-wrap: wrap;
  gap: 60px;
  padding-top: 30px; /* 调整：保持与品牌区对齐 */
}

.link-group {
  min-width: 160px;
}

.link-group h5 {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 18px;
  color: #2d3748;
}

.link-group a {
  display: block;
  color: #4a5568;
  margin-bottom: 12px;
  text-decoration: none;
  font-size: 15px;
  transition: color 0.2s;
}

.link-group a:hover {
  color: #3a7bd5;
}

.copyright {
  padding-top: 25px;
  border-top: 1px solid #e2e8f0;
  text-align: center;
  font-size: 14px;
  color: #718096;
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    gap: 40px;
  }
  
  .brand-info {
    min-width: 100%;
    padding-right: 0;
    padding-top: 15px; /* 移动端适当减少下移量 */
    text-align: center;
  }
  
  .logo-group {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }
  
  .footer-links {
    justify-content: space-between;
    gap: 30px;
    padding-top: 20px; /* 移动端调整对齐 */
  }
}
</style>