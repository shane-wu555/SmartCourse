<template>
  <div class="resource-mgmt">
    <!-- 返回按钮 -->
    <button class="back-btn" @click="$router.back()">返回</button>

    <h2>资源管理 - {{ courseName }}</h2>

    <!-- 上传资源区域 -->
    <div class="upload-section">
      <div class="form-group">
        <label>资源名称</label>
        <input v-model="uploadData.name" placeholder="输入资源名称" />
      </div>
      <div class="form-group">
        <label>资源类型</label>
        <select v-model="uploadData.type">
          <option value="PDF">PDF</option>
          <option value="PPT">PPT</option>
          <option value="VIDEO">视频</option>
          <option value="DOCUMENT">文档</option>
        </select>
      </div>
      <div class="form-group">
        <label>关联知识点</label>
        <select v-model="uploadData.knowledgePointId">
          <option disabled value="">请选择知识点</option>
          <option v-for="point in knowledgePoints" :key="point.pointId" :value="point.pointId">
            {{ point.name }}
          </option>
        </select>
      </div>
      <div class="form-group">
        <label>资源描述</label>
        <textarea v-model="uploadData.description" placeholder="输入资源描述"></textarea>
      </div>
      <div class="form-group">
        <label>选择文件</label>
        <input type="file" ref="fileInput" @change="handleFileChange" />
      </div>
      <button class="btn primary-btn" @click="uploadResource">上传资源</button>
    </div>

    <!-- 资源列表 -->
    <div class="resource-list">
      <table>
        <thead>
          <tr>
            <th>名称</th>
            <th>类型</th>
            <th>大小</th>
            <th>上传时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="res in resources" :key="res.resourceId">
            <td>{{ res.name }}</td>
            <td>{{ formatResourceType(res.type) }}</td>
            <td>{{ formatSize(res.size) }}</td>
            <td>{{ formatDate(res.uploadTime) }}</td>
            <td>
              <button @click="downloadResource(res)" class="btn outline-btn">下载</button>
              <button @click="updateResource(res)" class="btn outline-btn">编辑</button>
              <button @click="viewResource(res)" class="btn primary-btn">查看</button>
              <button @click="deleteResource(res)" class="btn danger-btn">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页控制 -->
<div class="pagination">
  <button @click="prevPage" :disabled="page === 1" class="pagination-btn">上一页</button>
  <span>第 {{ page }} / {{ totalPages }} 页</span>
  <button @click="nextPage" :disabled="page >= totalPages" class="pagination-btn">下一页</button>
</div>

  </div>
</template>

<script>
import api from '@/axios'
import axios from 'axios'

export default {
  name: 'ResourceManagement',
  data() {
    return {
      page: 1,
      courseId: this.$route.params.courseId,
      courseName: '',
      resources: [],
      knowledgePoints: [],
      uploadData: {
        name: '',
        type: 'PDF',
        description: '',
        knowledgePointId: '',
        file: null
      },
      pageSize: 10,
      totalPages: 0,
      totalElements: 0
    };
  },
  methods: {
    nextPage() {
  if (this.page < this.totalPages) {
    this.page++;
    this.fetchResources();
  }
},
prevPage() {
  if (this.page > 1) {
    this.page--;
    this.fetchResources();
  }
},
    async fetchCourseInfo() {
      try {
        const res = await api.get(`/teacher/courses/${this.courseId}`);
        this.courseName = res.data.name;
      } catch (err) {
        console.error('获取课程信息失败', err);
      }
    },

async fetchResources() {
  try {
    const res = await api.get(`/teacher/courses/${this.courseId}/resources`, {
      params: { page: this.page, size: this.pageSize }
    });

    const data = res.data || {};

    // 先更新页数，无论是否非法
    this.totalPages = data.totalPages || 1;
    this.totalElements = data.totalElements || 0;

    // 如果当前页比最大页还大，回退一页再请求
    if (this.page > this.totalPages) {
      this.page = this.totalPages;
      return this.fetchResources(); // 🔁 再次加载新页
    }

    this.resources = data.content || [];

  } catch (err) {
    console.error('获取资源失败', err);
  }
},


    async fetchKnowledgePoints() {
      try {
        const res = await api.get(`/teacher/courses/${this.courseId}/knowledge-points`);
        this.knowledgePoints = res.data || [];
      } catch (err) {
        console.error('获取知识点失败', err);
      }
    },
    handleFileChange(e) {
      this.uploadData.file = e.target.files[0];
    },
    async uploadResource() {
      if (!this.uploadData.file) {
        alert('请选择文件');
        return;
      }
      const formData = new FormData();
      formData.append('file', this.uploadData.file);
      formData.append('name', this.uploadData.name);
      formData.append('type', this.uploadData.type);
      formData.append('description', this.uploadData.description);
      formData.append('knowledgePointId', this.uploadData.knowledgePointId);

      try {
        await api.post(
          `/teacher/courses/${this.courseId}/resources`, 
          formData,
          { headers: { 'Content-Type': 'multipart/form-data' } }
        );
        alert('上传成功');
        this.resetUploadForm();
        this.fetchResources();
      } catch (err) {
        console.error('上传失败', err);
        alert(`上传失败: ${err.response?.data?.message || err.message}`);
      }
    },
    resetUploadForm() {
      this.uploadData = {
        name: '',
        type: 'PDF',
        description: '',
        knowledgePointId: '',
        file: null
      };
      this.$refs.fileInput.value = '';
    },
    async downloadResource(resource) {
      try {
        // 使用原生axios避免响应拦截器影响
        const token = localStorage.getItem('token')
        const fullUrl = `http://localhost:8082/api/teacher/resources/${resource.resourceId}/download`
        
        const response = await axios.get(fullUrl, { 
          responseType: 'blob',
          timeout: 30000,
          headers: {
            'Authorization': token ? `Bearer ${token}` : undefined
          }
        })

        // 检查响应是否成功
        if (!response || !response.data) {
          throw new Error('服务器响应异常，未获取到文件数据')
        }
        
        if (response.status && response.status !== 200) {
          throw new Error(`下载失败，状态码: ${response.status}`)
        }

        // 检查响应数据是否为有效的blob
        if (response.data.size === 0) {
          throw new Error('下载的文件大小为0，可能文件不存在或已损坏')
        }

        // 创建下载链接
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        
        // 处理文件名 - 优先使用响应头中的文件名
        let fileName = `resource_${resource.resourceId}`
        
        // 首先尝试从响应头获取文件名
        const contentDisposition = response.headers && response.headers['content-disposition']
        if (contentDisposition) {
          // 处理 UTF-8 编码的文件名
          const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/)
          if (utf8Match) {
            fileName = decodeURIComponent(utf8Match[1])
          } else {
            // 处理普通文件名
            const normalMatch = contentDisposition.match(/filename="?([^";\n]+)"?/)
            if (normalMatch) {
              fileName = normalMatch[1]
            }
          }
        }
        
        // 如果响应头没有文件名，使用资源名称
        if (!fileName || fileName === `resource_${resource.resourceId}`) {
          fileName = resource.name || `resource_${resource.resourceId}`
          
          // 根据资源类型添加合适的扩展名
          if (!fileName.includes('.')) {
            let extension = 'pdf' // 默认扩展名
            
            switch (resource.type?.toUpperCase()) {
              case 'VIDEO':
                extension = 'mp4'
                break
              case 'DOCUMENT':
                extension = 'doc'
                break
              case 'PDF':
                extension = 'pdf'
                break
              case 'PPT':
                extension = 'ppt'
                break
              case 'IMAGE':
                extension = 'jpg'
                break
              default:
                // 尝试从原始URL或其他字段获取扩展名
                if (resource.url) {
                  const urlExt = resource.url.split('.').pop()
                  if (urlExt && urlExt.length <= 4) {
                    extension = urlExt
                  }
                }
            }
            fileName = `${fileName}.${extension}`
          }
        }
        
        link.setAttribute('download', fileName)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        
        // 释放内存
        window.URL.revokeObjectURL(url)
        
      } catch (err) {
        console.error('下载失败:', err)
        
        // 提供错误信息
        let errorMessage = '下载失败'
        
        if (err.response) {
          const status = err.response.status
          
          switch (status) {
            case 404:
              errorMessage = '资源文件不存在'
              break
            case 410:
              errorMessage = '资源文件已被删除'
              break
            case 403:
              errorMessage = '没有权限下载此资源'
              break
            case 401:
              errorMessage = '登录已过期，请重新登录'
              break
            case 500:
              errorMessage = '服务器内部错误'
              break
            default:
              errorMessage = `下载失败 (错误码: ${status})`
          }
        } else if (err.code === 'ECONNABORTED') {
          errorMessage = '下载超时，请重试'
        } else if (err.message) {
          errorMessage = `下载失败: ${err.message}`
        }
        
        alert(errorMessage)
        
        // 如果是权限问题，跳转到登录页
        if (err.response && err.response.status === 401) {
          setTimeout(() => {
            this.$router.push('/login')
          }, 2000)
        }
      }
    },

    async deleteResource(resource) {
  if (!confirm(`确定删除资源 "${resource.name}" 吗？`)) return;

  try {
    await api.delete(`/teacher/resources/${resource.resourceId}`);
    alert('删除成功');

    // 先请求当前页的数据
    await this.fetchResources();

    // 如果删除后这页没内容，且不是第一页，则回退一页再重新请求
    if (this.resources.length === 0 && this.page > 1) {
      this.page--;
      await this.fetchResources();  // 🔁 关键：再次请求，更新 totalPages
    }

  } catch (err) {
    console.error('删除失败', err);
    alert('删除失败');
  }
},
    async updateResource(resource) {
      const newName = prompt('请输入新的资源名称', resource.name);
      if (!newName) return;
      try {
        await api.put(`/teacher/resources/${resource.resourceId}`, { name: newName });
        alert('更新成功');
        this.fetchResources();
      } catch (err) {
        console.error('更新失败', err);
        alert('更新失败');
      }
    },
    viewResource(resource) {
  // 在控制台输出详细的资源调试信息
  console.group('👁️ 查看资源 - 调试信息');
  console.log('📄 资源对象:', resource);
  console.log('🆔 资源ID:', resource.resourceId);
  console.log('📝 资源名称:', resource.name);
  console.log('🏷️ 原始资源类型:', resource.type);
  console.log('� MIME类型 (如果有):', resource.mimeType || resource.contentType || '未设置');
  console.log('�📊 资源大小:', resource.size, `(${this.formatSize(resource.size)})`);
  console.log('⏰ 上传时间:', resource.uploadTime);
  console.log('🔤 类型字符串长度:', resource.type?.length);
  console.log('� 文件扩展名:', this.getFileExtension(resource.name));
  console.log('�🔍 类型检测结果:');
  
  // 详细的类型判断过程
  const isVideoType1 = resource.type === 'VIDEO';
  const isVideoType2 = resource.type === 'video';
  const isVideoType3 = resource.type?.toLowerCase().startsWith('video/');
  const isVideo = isVideoType1 || isVideoType2 || isVideoType3;
  
  console.log('  📹 resource.type === "VIDEO":', isVideoType1);
  console.log('  📹 resource.type === "video":', isVideoType2);
  console.log('  📹 以"video/"开头:', isVideoType3);
  console.log('  📹 综合判断为视频:', isVideo);
  
  // 增强的文档类型检测
  const isStandardDocType = ['PDF', 'DOCUMENT', 'PPT', 'IMAGE'].includes(resource.type);
  const isPdfMime = resource.type?.toLowerCase().startsWith('application/pdf');
  const isImageMime = resource.type?.toLowerCase().startsWith('image/');
  const isWordMime = resource.type?.toLowerCase().includes('wordprocessingml.document'); // Word文档MIME类型
  const isPptMime = resource.type?.toLowerCase().includes('presentationml.presentation'); // PPT文档MIME类型
  const isExcelMime = resource.type?.toLowerCase().includes('spreadsheetml.sheet'); // Excel文档MIME类型
  const isOfficeDoc = isWordMime || isPptMime || isExcelMime;
  const isPreviewable = isStandardDocType || isPdfMime || isImageMime || isOfficeDoc;
  
  console.log('  📄 在预设类型列表中:', isStandardDocType, ['PDF', 'DOCUMENT', 'PPT', 'IMAGE']);
  console.log('  📄 以"application/pdf"开头:', isPdfMime);
  console.log('  🖼️ 以"image/"开头:', isImageMime);
  console.log('  📝 Word文档MIME类型:', isWordMime);
  console.log('  📊 PPT文档MIME类型:', isPptMime);
  console.log('  � Excel文档MIME类型:', isExcelMime);
  console.log('  🏢 Office文档类型:', isOfficeDoc);
  console.log('  �📄 综合判断可预览:', isPreviewable);
  
  // 显示可能的MIME类型映射
  if (resource.type) {
    const mimeTypeMapping = this.getMimeTypeInfo(resource.type);
    console.log('  🔄 MIME类型映射信息:', mimeTypeMapping);
  }
  
  let routeAction = '';
  
  // 判断是否为视频类型 - 支持多种视频格式标识
  if (isVideo) {
    routeAction = '跳转到视频播放器';
    console.log('🎬 动作:', routeAction);
    console.log('🛤️ 路由:', 'VideoPlayer');
    console.log('📡 预期后端处理: 视频流处理');
    console.groupEnd();
    this.$router.push({ name: 'VideoPlayer', params: { resourceId: resource.resourceId } });
  } else if (isPreviewable) {
    routeAction = '跳转到资源预览';
    console.log('📖 动作:', routeAction);
    console.log('🛤️ 路由:', 'ResourcePreview');
    
    // 预期的后端处理逻辑
    if (isWordMime || isPptMime || isExcelMime) {
      console.log('📡 预期后端处理: Office文档 -> PDF转换预览');
      console.log('🔧 MIME类型将被PreviewService正确识别并转换');
    } else if (isPdfMime || resource.type === 'PDF') {
      console.log('📡 预期后端处理: PDF直接预览');
    } else if (isImageMime || resource.type === 'IMAGE') {
      console.log('📡 预期后端处理: 图片直接预览');
    } else {
      console.log('📡 预期后端处理: 标准资源类型预览');
    }
    
    console.groupEnd();
    this.$router.push({ name: 'ResourcePreview', params: { resourceId: resource.resourceId } });
  } else {
    routeAction = '显示不支持预览提示';
    console.log('❌ 动作:', routeAction);
    console.log('⚠️ 原因: 资源类型不在支持列表中');
    console.log('💡 建议: 检查resource.type是否为有效的MIME类型或标准类型');
    console.groupEnd();
    alert(`暂不支持预览该资源类型: ${resource.type}`);
  }
},


    changePage(page) {
  this.page = page;
  this.fetchResources();
},
    formatDate(dateStr) {
      return new Date(dateStr).toLocaleString();
    },
    formatSize(bytes) {
      if (bytes < 1024) return bytes + ' B';
      if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
      return (bytes / 1048576).toFixed(1) + ' MB';
    },
    formatResourceType(type) {
  const typeMap = {
    // 显示名称映射
    'PDF': 'PDF文档',
    'PPT': 'PPT演示文稿',
    'VIDEO': '视频',
    'DOCUMENT': '文档',
    'IMAGE': '图片',

    // MIME 类型映射
    'application/pdf': 'PDF文档',
    'application/msword': '文档',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '文档',
    'application/vnd.ms-powerpoint': 'PPT演示文稿',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'PPT演示文稿',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': '文档',
    'image/jpeg': '图片',
    'image/png': '图片',
    'video/mp4': '视频',
    'video/avi': '视频',
    'video/x-msvideo': '视频',
    'video/webm': '视频'
  };

  return typeMap[type] || type;
},
    
    // 新增：获取文件扩展名的辅助方法
    getFileExtension(fileName) {
      if (!fileName) return '';
      const lastDot = fileName.lastIndexOf('.');
      return lastDot !== -1 ? fileName.slice(lastDot + 1).toLowerCase() : '';
    },
    
    // 新增：获取MIME类型信息的辅助方法
    getMimeTypeInfo(type) {
      const info = {
        originalType: type,
        isStandardType: ['PDF', 'PPT', 'VIDEO', 'DOCUMENT', 'IMAGE'].includes(type),
        isMimeType: type && type.includes('/'),
        category: 'unknown',
        expectedBackendMapping: 'unknown'
      };
      
      if (type) {
        const lowerType = type.toLowerCase();
        
        // 视频类型
        if (lowerType.startsWith('video/') || lowerType === 'video') {
          info.category = 'video';
          info.expectedBackendMapping = 'ResourceType.VIDEO';
        }
        // 图片类型
        else if (lowerType.startsWith('image/')) {
          info.category = 'image';
          info.expectedBackendMapping = 'ResourceType.IMAGE';
        }
        // PDF类型
        else if (lowerType.startsWith('application/pdf')) {
          info.category = 'pdf';
          info.expectedBackendMapping = 'ResourceType.PDF';
        }
        // Word文档
        else if (lowerType.includes('wordprocessingml.document')) {
          info.category = 'word-document';
          info.expectedBackendMapping = 'ResourceType.DOCUMENT (通过mimeTypeToResourceType映射)';
        }
        // PowerPoint
        else if (lowerType.includes('presentationml.presentation')) {
          info.category = 'powerpoint';
          info.expectedBackendMapping = 'ResourceType.PPT (通过mimeTypeToResourceType映射)';
        }
        // Excel
        else if (lowerType.includes('spreadsheetml.sheet')) {
          info.category = 'excel';
          info.expectedBackendMapping = 'ResourceType.DOCUMENT (通过mimeTypeToResourceType映射)';
        }
        // 标准类型
        else if (['PDF', 'PPT', 'VIDEO', 'DOCUMENT', 'IMAGE'].includes(type)) {
          info.category = 'standard-type';
          info.expectedBackendMapping = `ResourceType.${type}`;
        }
      }
      
      return info;
    }
  },
  mounted() {
    this.fetchCourseInfo();
    this.fetchResources();
    this.fetchKnowledgePoints();
  }
};
</script>

<style scoped>
.resource-mgmt {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.upload-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.form-group textarea {
  height: 80px;
}

.primary-btn {
  background: #4a90e2;
  color: white;
}

.outline-btn {
  background: transparent;
  border: 1px solid #4a90e2;
  color: #4a90e2;
}

.danger-btn {
  background: #e74c3c;
  color: white;
}

.btn {
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
}

.resource-list {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

th, td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

thead tr {
  background-color: #f8f9fa;
}

tbody tr:hover {
  background-color: #f1f1f1;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-bottom: 40px;
}

.pagination-btn {
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  cursor: pointer;
}


.pagination button.active {
  background: #4a90e2;
  color: white;
  border-color: #4a90e2;
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
</style>