<template>
  <div class="course-detail-wrapper">
    <!-- 返回按钮 -->
    <button class="back-btn" @click="$router.back()">返回</button>

    <div class="course-detail">
      <!-- 左侧课程信息 -->
      <div class="course-info">
        <h2>{{ course.name }}</h2>
        <p>课程编号: {{ course.courseCode }}</p>
        <p>学分: {{ course.credit }}</p>
        <p>学时: {{ course.hours }}</p>
        <p>学期: {{ course.semester }}</p>
        <p>描述: {{ course.description }}</p>
        <p>学生人数: {{ course.studentCount }}</p>
        <p>任务数量: {{ course.taskCount }}</p>
      </div>

      <!-- 右侧知识图谱 -->
      <div class="graph-container">
        <h3>课程知识图谱</h3>
        <div id="courseKnowledgeGraph" class="graph-box"></div>
      </div>
    </div>

    <!-- 下方资源区域 -->
    <div class="resource-section">
      <h3>课程资源</h3>
      <table v-if="resources.length">
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
              <button class="btn outline-btn" @click="downloadResource(res)">下载</button>
              <button class="btn primary-btn" @click="viewResource(res)">查看</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else>暂无资源</p>
    </div>
  </div>
</template>

<script>
import api from '@/axios'
import axios from 'axios'
import * as echarts from 'echarts'

export default {
  name: 'CourseDetail',
  data() {
    return {
      courseId: this.$route.params.id,
      course: {},
      resources: []
    }
  },
  methods: {
    getRole() {
      return localStorage.getItem('userRole') || 'STUDENT'
    },
    getDetailUrl() {
      return this.getRole() === 'TEACHER'
        ? `/teacher/courses/${this.courseId}`
        : `/student/courses/${this.courseId}`
    },
    getGraphUrl() {
      // 学生端和教师端都使用教师端的知识图谱接口
      return `/teacher/courses/${this.courseId}/knowledge-graph`
    },
    getResourceUrl() {
      return this.getRole() === 'TEACHER'
        ? `/teacher/courses/${this.courseId}/resources`
        : `/student/courses/${this.courseId}/resources`
    },

    async fetchCourseDetail() {
      try {
        const res = await api.get(this.getDetailUrl())
        this.course = res.data || {}
      } catch (err) {
        console.error('获取课程详情失败', err)
        alert(err.response?.data?.message || '加载课程详情失败')
      }
    },
    async renderKnowledgeGraph() {
      try {
         // 增加超时时间
        const res = await api.get(this.getGraphUrl(), {
          timeout: 30000 // 增加到30秒
        })
        
        // 确保数据结构正确
        const graphData = res.data || {}
        const nodes = graphData.nodes || []
        const edges = graphData.edges || []

        // 等待DOM渲染完成
        await this.$nextTick()
        
        const chartElement = document.getElementById("courseKnowledgeGraph")
        if (!chartElement) {
          return
        }

        // 销毁已存在的图表实例
        const existingChart = echarts.getInstanceByDom(chartElement)
        if (existingChart) {
          existingChart.dispose()
        }

        const chart = echarts.init(chartElement)
        
        // 如果没有数据，显示提示信息
        if (nodes.length === 0) {
          chartElement.innerHTML = `
            <div style="display: flex; align-items: center; justify-content: center; height: 100%; color: #666;">
              <div style="text-align: center;">
                <p>暂无知识图谱数据</p>
                <p style="font-size: 12px;">课程还没有建立知识点关系</p>
              </div>
            </div>
          `
          return
        }
        
        const option = {
          tooltip: {
            trigger: 'item',
            formatter: function(params) {
              if (params.dataType === 'node') {
                // 显示知识点名称和描述，而不是ID
                const node = params.data;
                let content = `<strong>${node.name}</strong>`;
                if (node.description) {
                  content += `<br/>${node.description}`;
                } else {
                  content += `<br/>暂无描述`;
                }
                return content;
              } else if (params.dataType === 'edge') {
                return `${params.data.source} → ${params.data.target}<br/>关系: ${params.data.type || '关联'}`
              }
            }
          },
          series: [
            {
              type: "graph",
              layout: "force",
              roam: true,
              label: { 
                show: true,
                position: 'right',
                formatter: '{b}'
              },
              force: {
                repulsion: 800,
                edgeLength: [50, 150],
                layoutAnimation: true
              },
              data: nodes.map(n => ({
                id: n.id,
                name: n.name || `节点${n.id}`,
                description: n.description || n.content || '', // 添加描述字段
                symbolSize: 50,
                itemStyle: {
                  color: '#4CAF50'
                }
              })),
              edges: edges.map(e => ({
                source: e.source,
                target: e.target,
                type: e.type || "关联",
                label: { 
                  show: true, 
                  formatter: e.type || "关联"
                },
                lineStyle: {
                  color: '#999'
                }
              }))
            }
          ]
        }
        
        chart.setOption(option)
        
        // 窗口大小改变时重新调整图表
        window.addEventListener('resize', () => {
          chart.resize()
        })
        
      } catch (err) {
        // 显示错误信息而不是弹窗
        const chartElement = document.getElementById("courseKnowledgeGraph")
        if (chartElement) {
          chartElement.innerHTML = `
            <div style="display: flex; align-items: center; justify-content: center; height: 100%; color: #666;">
              <div style="text-align: center;">
                <p>知识图谱加载失败</p>
                <p style="font-size: 12px;">${err.response?.data?.message || err.message}</p>
              </div>
            </div>
          `
        }
      }
    },
    async fetchResources() {
      try {
        const res = await api.get(this.getResourceUrl(), {
          params: { page: 1, size: 100 }
        })
        this.resources = res.data?.content || []
      } catch (err) {
        console.error('加载资源失败', err)
        alert(err.response?.message || '资源加载失败')
      }
    },
    
    // 与ResourceManagement.vue完全一致的下载方法
    async downloadResource(resource) {
      try {
        // 根据用户角色确定下载URL路径
        const role = this.getRole()      
        const downloadUrl = role === 'TEACHER'
          ? `/teacher/resources/${resource.resourceId}/download`
          : `/student/resources/${resource.resourceId}/download`
        
        // 使用原生axios避免响应拦截器影响
        const token = localStorage.getItem('token')
        const fullUrl = `http://localhost:8082/api${downloadUrl}`
        
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

    // 与ResourceManagement.vue完全一致的查看方法
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

    formatDate(dateStr) {
      return new Date(dateStr).toLocaleString()
    },
    formatSize(bytes) {
      if (bytes < 1024) return bytes + ' B'
      if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB'
      return (bytes / 1048576).toFixed(1) + ' MB'
    },
    
    // 新增：与ResourceManagement.vue一致的资源类型格式化方法
    formatResourceType(type) {
  const typeMap = {
    'PDF': 'PDF文档',
    'PPT': 'PPT演示文稿',
    'VIDEO': '视频',
    'DOCUMENT': '文档',
    'IMAGE': '图片',
    'application/msword': '文档',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '文档',
    'application/vnd.ms-powerpoint': 'PPT演示文稿',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'PPT演示文稿',
    'application/pdf': 'PDF文档',
    'image/jpeg': '图片',
    'image/png': '图片',
    'video/mp4': '视频',
    'video/avi': '视频',
    'video/x-msvideo': '视频'
  }

  return typeMap[type] || type
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
    this.fetchCourseDetail()
    this.renderKnowledgeGraph()
    this.fetchResources()
  }
}
</script>

<style scoped>
.course-detail-wrapper {
  padding: 20px;
  max-width: 1200px;
  margin: auto;
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

.course-detail {
  display: flex;
  column-gap: 0px; /* 改得更小点贴近 */
  margin-bottom: 40px;
  align-items: flex-start;
}


.course-info {
  flex: 0 0 55%; /* 左侧占55%宽度 */
}

.graph-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: 1000px;
  width: 100%;
  margin-left: -100; /* 原来是 40px，现在设为 0，让它贴近课程信息 */
}

.graph-box {
  width: 100%;
  height: 500px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.course-info h2, .graph-container h3 {
  font-size: 24px; /* 保持一致字体大小 */
  margin-bottom: 20px;
  height: 30px;
}


.resource-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #eee;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.resource-section table {
  width: 100%;
  border-collapse: collapse;
}

.resource-section th,
.resource-section td {
  padding: 10px;
  border-bottom: 1px solid #ddd;
  text-align: left;
}

.resource-section th {
  background: #f9f9f9;
}

/* Update the font size and height for the course name and graph title */
.course-info h2, .graph-container h3 {
  font-size: 24px; /* Same font size for both */
  margin-bottom: 20px; /* Consistent spacing */
  height: 30px; /* Ensure both titles are of the same height */
}

.btn {
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.primary-btn {
  background: #4a90e2;
  color: white;
  border: none;
}

.primary-btn:hover {
  background: #357abd;
}

.outline-btn {
  background: transparent;
  border: 1px solid #4a90e2;
  color: #4a90e2;
}

.outline-btn:hover {
  background: #4a90e2;
  color: white;
}

.danger-btn {
  background: #e74c3c;
  color: white;
  border: none;
}

.danger-btn:hover {
  background: #c0392b;
}
.course-detail {
  display: flex;
  column-gap: 10px; /* 可以设 0-10px */
  margin-bottom: 40px;
  align-items: flex-start;
}

.course-info {
  flex: 0 0 30%; /* 原来是55%，改成50%或更少让右侧能靠近 */
}

.graph-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  max-width: 800px;  /* 限制图谱最大宽度 */
  width: 100%;
  margin-left: 0;  /* 删掉错误写法，或设为0 */
}


</style>