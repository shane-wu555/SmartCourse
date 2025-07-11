<template>
  <div class="course-students">
    <h2>课程「{{ courseName }}」的学生列表</h2>

    <!-- 顶部操作栏 -->
    <div class="top-bar">
      <input
        type="text"
        v-model="searchKeyword"
        placeholder="搜索姓名"
        class="search-input"
      />
      <button @click="goBack">返回课程列表</button>
    </div>

    <!-- 学生表格 -->
    <table class="student-table">
      <thead>
        <tr>
          <th>姓名</th>
          <th>专业</th>
          <th>年级</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="student in paginatedStudents"
          :key="student.studentId"
        >
          <td>{{ student.realName }}</td>
          <td>{{ student.major }}</td>
          <td>{{ student.grade }}</td>

        </tr>
        <tr v-if="paginatedStudents.length === 0">
          <td colspan="5">暂无匹配数据</td>
        </tr>
      </tbody>
    </table>

    <!-- 分页控制 -->
    <div class="pagination" v-if="filteredStudents.length > pageSize">
      <button @click="prevPage" :disabled="page === 1">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button @click="nextPage" :disabled="page === totalPages">下一页</button>
    </div>
  </div>
</template>

<script>
import axios from '@/axios';

export default {
  name: 'CourseStudents',
  data() {
    return {
      courseId: this.$route.params.courseId,
      courseName: '',
      students: [],
      searchKeyword: '',
      page: 1,
      pageSize: 10
    };
  },
  computed: {
    filteredStudents() {
      const keyword = this.searchKeyword.toLowerCase();
      return this.students.filter(
        (s) =>
          s.realName?.toLowerCase().includes(keyword) ||
          s.email?.toLowerCase().includes(keyword)
      );
    },
    totalPages() {
      return Math.ceil(this.filteredStudents.length / this.pageSize) || 1;
    },
    paginatedStudents() {
      const start = (this.page - 1) * this.pageSize;
      const end = this.page * this.pageSize;
      return this.filteredStudents.slice(start, end);
    }
  },
  async mounted() {
    await this.fetchStudents();
    await this.fetchCourseName();
  },
  methods: {
    async fetchStudents() {
  try {
    const res = await axios.get(`/teacher/courses/${this.courseId}/students`);
    this.students = res || [];
  } catch (err) {
    console.error('加载学生失败', err);
    alert(err.response?.data?.message || '加载学生失败');
  }
},

    async fetchCourseName() {
      try {
        const res = await axios.get(`/teacher/courses/${this.courseId}`);
        this.courseName = res.data?.name || '未知课程';
      } catch (err) {
        console.warn('课程名加载失败');
      }
    },
    prevPage() {
      if (this.page > 1) this.page--;
    },
    nextPage() {
      if (this.page < this.totalPages) this.page++;
    },
    goBack() {
      this.$router.push({ name: 'StudentManagement' });
    }
  }
};
</script>

<style scoped>
.course-students {
  padding: 20px;
}
.top-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}
.search-input {
  padding: 6px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.student-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}
.student-table th,
.student-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: center;
}
button {
  background: #4a90e2;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}
button:hover {
  opacity: 0.9;
}
.pagination {
  margin-top: 20px;
  display: flex;
  gap: 15px;
  align-items: center;
}
</style>
