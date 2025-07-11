import axios from 'axios';

// 根据环境变量设置基础 URL
const baseURL = process.env.NODE_ENV === 'production'
  ? 'https://your-production-api.com/api' // 生产环境
  : 'http://localhost:8082/api';         // 开发环境

const api = axios.create({
  baseURL: baseURL,
  timeout: 30000 // 增加到30秒，因为AI推荐可能需要更长时间
});


// 请求拦截器
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, error => {
  return Promise.reject(error);
});

// 响应拦截器 - 修复版本
api.interceptors.response.use(
  response => {
    // 如果是blob响应，返回完整的response对象
    if (response.config.responseType === 'blob') {
      return response; // 返回完整response对象，保留data、headers等
    }
    
    return response.data; // 普通响应只返回data
  },
  error => {
    if (error.response?.status === 401) {  // 401 Unauthorized
      localStorage.removeItem('token');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    
    // 为错误对象添加更友好的错误信息
    const errorMessage = error.response?.data?.message || error.message || '请求失败';
    error.friendlyMessage = errorMessage;
    
    return Promise.reject(error);
  }
);

export default api;
