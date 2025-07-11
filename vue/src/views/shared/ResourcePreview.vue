<template>
  <div class="resource-preview">
    <!-- 返回按钮 -->
    <button class="back-btn" @click="$router.back()">返回</button>
    <h2>资源预览</h2>

    <div v-if="loading">加载中...</div>

    <div v-else-if="errorMessage" class="error-container">
      <div class="error-icon">⚠️</div>
      <div class="error-message">{{ errorMessage }}</div>
      <button @click="retry" class="retry-btn">重试</button>
    </div>

    <div v-else>
      <div v-if="previewUrl" class="preview-container">
        <iframe
          :src="`https://mozilla.github.io/pdf.js/web/viewer.html?file=${encodeURIComponent(previewUrl)}`"
          width="100%"
          height="800px"
          style="min-height:600px;min-width:100%;border:none;"
          @load="onIframeLoad"
          @error="onIframeError"
        ></iframe>
        <div style="margin-top:10px;">
          <a :href="previewUrl" target="_blank" rel="noopener" style="color:#3182ce;">无法预览？点击新窗口打开</a>
        </div>
      </div>
      <div v-else class="no-preview">
        <p>无法生成预览链接</p>
        <p style="font-size: 12px; color: #999;">调试信息: previewUrl = "{{ previewUrl }}"</p>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/axios';

export default {
  name: 'ResourcePreview',
  props: ['resourceId'],
  data() {
    return {
      previewUrl: '',
      errorMessage: '',
      loading: true
    };
  },
  computed: {
    currentResourceId() {
      return this.resourceId || this.$route.params.resourceId;
    }
  },
  async mounted() {
    await this.loadPreview();
  },
  methods: {
    async loadPreview() {
      this.loading = true;
      this.errorMessage = '';

      try {
        if (!this.currentResourceId) {
          throw new Error('资源ID不能为空');
        }

        // 调用预览API
        const previewRes = await api.get(`/preview/${this.currentResourceId}`);
        
        // 处理预览API的响应
        const responseData = previewRes.data || previewRes;
        let previewUrl = '';
        
        if (typeof responseData === 'string') {
          previewUrl = responseData;
        } else if (responseData && typeof responseData === 'object') {
          previewUrl = responseData.previewUrl || responseData.url || responseData.data || '';
        }

        if (!previewUrl) {
          throw new Error('后端未返回有效的预览链接');
        }

        // 清理URL中的异常字符
        previewUrl = previewUrl.replace(/\.+pdf$/, '.pdf');

        // 确保URL是完整的
        if (previewUrl.startsWith('/')) {
          this.previewUrl = `http://localhost:8082${previewUrl}`;
        } else if (!previewUrl.startsWith('http')) {
          this.previewUrl = `http://localhost:8082/${previewUrl}`;
        } else {
          this.previewUrl = previewUrl;
        }

      } catch (err) {
        console.error('资源预览加载失败:', err);
        
        if (err.response) {
          const status = err.response.status;
          const errorData = err.response.data;
          
          if (status === 400) {
            this.errorMessage = `请求参数错误: ${errorData?.message || errorData || '资源ID格式可能不正确'}`;
          } else if (status === 404) {
            this.errorMessage = '资源不存在或已被删除';
          } else if (status === 500) {
            this.errorMessage = `服务器内部错误: ${errorData?.message || errorData || '请联系管理员'}`;
          } else if (status === 403) {
            this.errorMessage = '没有权限访问此资源';
          } else {
            this.errorMessage = `请求失败 (${status}): ${errorData?.message || errorData || err.message}`;
          }
        } else if (err.request) {
          this.errorMessage = '网络连接失败，请检查网络连接';
        } else {
          this.errorMessage = err.message || '预览加载失败';
        }
      } finally {
        this.loading = false;
      }
    },
    
    retry() {
      this.loadPreview();
    },
    
    onIframeLoad() {
      // PDF.js加载完成
    },
    
    onIframeError(event) {
      console.error('PDF预览加载失败', event);
      this.errorMessage = '资源文件加载失败，可能文件不存在或格式不支持预览';
    }
  }
};
</script>

<style scoped>
.resource-preview {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}
.back-btn {
  background: #4a90e2;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 4px;
  margin-bottom: 20px;
  cursor: pointer;
}
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  background-color: #fff5f5;
  border: 1px solid #fed7d7;
  border-radius: 8px;
  margin: 20px 0;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-message {
  color: #e53e3e;
  font-weight: bold;
  margin-bottom: 16px;
  text-align: center;
  line-height: 1.5;
}

.retry-btn {
  background-color: #3182ce;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.retry-btn:hover {
  background-color: #2c5aa0;
}

.preview-container {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.no-preview {
  text-align: center;
  padding: 40px;
  color: #718096;
}

embed {
  display: block;
}
</style>
