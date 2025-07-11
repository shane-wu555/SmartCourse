<template>
  <div class="grade-mgmt">
    
    <h2>成绩分析</h2>

    <label>选择课程：
      <select v-model="selectedCourseId" @change="fetchReport">
        <option disabled value="">请选择课程</option>
        <option v-for="c in courses" :key="c.courseId" :value="c.courseId">
          {{ c.name }}
        </option>
      </select>
    </label>

    <div v-if="report && report.performers?.length">
      <table>
        <thead>
          <tr>
            <th>学号</th>
            <th>姓名</th>
            <th>得分率</th>
            <th>班级排名</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in report.performers" :key="s.studentNumber">
            <td>{{ s.studentNumber }}</td>
            <td>{{ s.studentName }}</td>
            <td>{{ s.gradeRate.toFixed(2) }}%</td>
            <td>{{ s.rank }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-else>
      <p>暂无学生成绩数据</p>
    </div>

    <button class="primary-btn" @click="exportReport" :disabled="!selectedCourseId">
      导出成绩报表
    </button>
  </div>
</template>

<script>
import axios from '@/axios'

export default {
  data() {
    return {
      courses: [],
      selectedCourseId: '',
      report: null
    }
  },
  mounted() {
    this.fetchCourses()
  },
  methods: {
    async fetchCourses() {
      try {
        const res = await axios.get('/teacher/courses')
        this.courses = res.data || []
      } catch (err) {
        // 获取课程失败
      }
    },
    async fetchReport() {
      if (!this.selectedCourseId) return
      try {
        const res = await axios.get(`/reports/course/${this.selectedCourseId}`)
        this.report = res || { performers: [] }
      } catch (err) {
        this.report = null
      }
    },
   async exportReport() {
      if (!this.selectedCourseId) return;

    try {
    const res = await axios.get(`/reports/export/${this.selectedCourseId}`, {
      responseType: 'blob',
      timeout: 30000,
      headers: {
        'Accept': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      }
    });

    // 现在res应该是完整的response对象，包含data和headers
    const blob = res.data;
    const headers = res.headers;

        if (!blob || blob.size === 0) {
          alert('导出的文件为空，请检查是否有数据');
          return;
        }

        // 创建下载链接
        const url = window.URL.createObjectURL(blob);
        
        // 从响应头中获取文件名
        let filename = '成绩报表.xlsx';
        const contentDisposition = headers['content-disposition'];
        if (contentDisposition) {
          const filenameMatch = contentDisposition.match(/filename\*?=([^;]+)/);
          if (filenameMatch) {
            filename = decodeURIComponent(filenameMatch[1].replace(/^UTF-8''/, ''));
          }
        }

        // 触发下载
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        
      } catch (err) {
        alert('导出失败: ' + (err.message || '未知错误'));
      }
    }


  }
}
</script>

<style scoped>
select {
  padding: 8px 12px;
  font-size: 16px;
  min-width: 180px; /* 控制宽度 */
  height: 40px; /* 控制高度 */
  border-radius: 4px;
  border: 1px solid #ccc;
  margin-left: 4px;
}

.grade-mgmt {
  padding: 20px;
}
.primary-btn {
  background: #4a90e2;
  color: #fff;
  border: none;
  padding: 10px 16px;
  border-radius: 4px;
  margin-top: 16px;
  cursor: pointer;
}
table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}
table th, table td {
  padding: 8px;
  border: 1px solid #ccc;
}
/* 左上角圆形装饰 */
.grade-mgmt {
  position: relative;
  z-index: 1;
  padding: 30px;
  background-color: white;
  overflow: hidden;
}

/* 左上角圆形装饰 */
.grade-mgmt::before {
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
.grade-mgmt::after {
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
