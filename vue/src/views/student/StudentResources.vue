<template>
   
  <div class="student-resources">
    <div class="decor-lower-left"></div>
<div class="decor-lower-right"></div>
    <h2>我的学习资源</h2>
    <div v-if="courses.length === 0">暂无选课</div>

    <div v-for="course in courses" :key="course.courseId" class="course-section">
      <h3>{{ course.name }}（{{ course.courseCode }}）</h3>

      <div v-if="course.resources && course.resources.length">
        <div class="resource-list">
          <div class="resource-card" v-for="res in course.resources" :key="res.resourceId">
            <h4>{{ res.name }}</h4>
            <p>类型：{{ formatResourceType(res.type) }}</p>
            <p>上传时间：{{ formatDate(res.uploadTime) }}</p>
            <div class="resource-actions">
              <button @click="viewResource(res)" class="primary-btn">查看</button>
              <button @click="downloadResource(res)" class="primary-btn">下载</button>
            </div>
          </div>
        </div>
      </div>
      <div v-else>
        <p class="empty">暂无资源</p>
      </div>
    </div>
  </div>
</template>

<script>
import axios from '@/axios';

export default {
  name: 'StudentResources',
  data() {
    return {
      courses: []
    };
  },
  methods: {
    async fetchCoursesAndResources() {
      try {
        const courseRes = await axios.get('/student/courses', { params: { page: 1, size: 100 } });
        const courseList = courseRes.content || [];

        const courseWithResources = await Promise.all(
          courseList.map(async (course) => {
            try {
              const res = await axios.get(`/student/courses/${course.courseId}/resources`, {
                params: { page: 1, size: 100 }
              });
              course.resources = res.data?.content || [];
            } catch (err) {
              console.error(`获取课程 ${course.name} 的资源失败`, err);
              course.resources = [];
            }
            return course;
          })
        );

        this.courses = courseWithResources;
      } catch (err) {
        console.error('获取选课失败', err);
        alert('加载课程失败');
      }
    },

    viewResource(resource) {
      // 在控制台输出详细的资源调试信息
      console.group('👁️ 查看资源 - 调试信息');
      console.log('📄 资源对象:', resource);
      console.log('🆔 资源ID:', resource.resourceId);
      console.log('📝 资源名称:', resource.name);
      console.log('🏷️ 原始资源类型:', resource.type);
      console.log('🗂️ MIME类型 (如果有):', resource.mimeType || resource.contentType || '未设置');
      console.log('📊 资源大小:', resource.size, `(${this.formatSize(resource.size)})`);
      console.log('⏰ 上传时间:', resource.uploadTime);
      console.log('🔤 类型字符串长度:', resource.type?.length);
      console.log('📁 文件扩展名:', this.getFileExtension(resource.name));
      console.log('🔍 类型检测结果:');
      
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
      console.log('  📗 Excel文档MIME类型:', isExcelMime);
      console.log('  🏢 Office文档类型:', isOfficeDoc);
      console.log('  📄 综合判断可预览:', isPreviewable);
      
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

    async downloadResource(resource) {
      try {
        // 使用原生axios避免响应拦截器影响
        const rawAxios = (await import('axios')).default;
        const token = localStorage.getItem('token')
        const fullUrl = `http://localhost:8082/api/student/resources/${resource.resourceId}/download`
        
        const response = await rawAxios.get(fullUrl, { 
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

    formatDate(dateStr) {
      const d = new Date(dateStr);
      return d.toLocaleString();
    },
    
    formatResourceType(type) {
      const typeMap = {
        'PDF': 'PDF文档',
        'PPT': 'PPT课件',
        'VIDEO': '视频',
        'DOCUMENT': '文档',
        'IMAGE': '图片'
      };
      return typeMap[type] || type;
    },
    
    formatSize(bytes) {
      if (!bytes || bytes === 0) return '0 B';
      if (bytes < 1024) return bytes + ' B';
      if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
      return (bytes / 1048576).toFixed(1) + ' MB';
    },
    
    getFileExtension(fileName) {
      if (!fileName) return '';
      const lastDot = fileName.lastIndexOf('.');
      return lastDot !== -1 ? fileName.slice(lastDot + 1).toLowerCase() : '';
    },

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
    this.fetchCoursesAndResources();
  }
};
</script>

<style scoped>
.student-resources {
  padding: 20px;
}
.course-section {
  margin-bottom: 30px;
}
.resource-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.resource-card {
  background: #fff;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 260px; /* ⬅️ 增加卡片高度 */
  box-sizing: border-box;
  transition: transform 0.3s, box-shadow 0.3s;
}

.resource-card:hover {
  transform: translateY(-10px);  /* 卡片上浮 */
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1); /* 增加阴影 */
}

.resource-actions {
  margin-top: auto;
  display: flex;
  gap: 10px;
}

.primary-btn {
  background: #4a90e2;
  color: white;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  text-decoration: none;
  display: inline-block;
  margin-top: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.primary-btn:hover {
  background: #357abd;
}

.empty {
  color: #888;
  font-style: italic;
}

/* 左上角圆形装饰 */
.student-resources {
  position: relative;
  z-index: 1;
  padding: 30px;
  background-color: white;
  overflow: hidden;
}

/* 左上角圆形装饰 */
.student-resources::before {
  content: "";
  position: absolute;
  top: -80px;
  left: -80px;
  width: 220px;
  height: 220px;
  background: radial-gradient(circle at center, rgba(170, 205, 245, 0.25), rgba(74, 144, 226, 0.4));
  border-radius: 50%;
  z-index: 0;
  filter: blur(2px);
}

/* 右上角斜切装饰 */
.student-resources::after {
  content: "";
  position: absolute;
  top: 0;
  right: 0;
  width: 180px;
  height: 180px;
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.3), rgba(74, 144, 226, 0.4));
  clip-path: polygon(100% 0, 0 0, 100% 100%);
  z-index: 0;
}

/* 顶部右边圆点 */
.decor-circle-small {
  position: absolute;
  top: 30px;
  right: 60px;
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background: rgba(74, 144, 226, 0.25);
  z-index: 0;
}

/* 左下角圆弧 */
.decor-lower-left {
  position: absolute;
  bottom: -60px;
  left: -60px;
  width: 160px;
  height: 160px;
  background: radial-gradient(circle at center, rgba(170, 205, 245, 0.25), rgba(74, 144, 226, 0.4));
  border-radius: 50%;
  z-index: 0;
  filter: blur(2px);
}

/* 右下角斜切图形 */
.decor-lower-right {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, rgba(74, 144, 226, 0.3), rgba(74, 144, 226, 0.4));
  clip-path: polygon(0 100%, 100% 100%, 100% 0);
  z-index: 0;
}

</style>
