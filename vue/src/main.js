import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // 确保router/index.js已配置
import api from './axios'

const app = createApp(App)
app.config.globalProperties.$api = api
app.use(router)
app.mount('#app')