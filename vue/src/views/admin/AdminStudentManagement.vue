<template>
  <div class="student-mgmt">
    <div class="decor-lower-left"></div>
    <div class="decor-lower-right"></div>

    <h2>学生信息管理</h2>

    <div class="action-bar">
      <button class="btn primary-btn" @click="showAddModal = true">新增学生</button>
      <button class="btn primary-btn" @click="$refs.fileInput.click()">批量导入</button>
      <input type="file" accept=".csv,.xlsx" @change="importStudents" hidden ref="fileInput" />
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>学号</th>
          <th>姓名</th>
          <th>专业</th>
          <th>年级</th>
          <th>邮箱</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="student in students" :key="student.studentNumber">
          <td>{{ student.studentNumber }}</td>
          <td>{{ student.realName }}</td>
          <td>{{ student.major }}</td>
          <td>{{ student.grade }}</td>
          <td>{{ student.email }}</td>
          <td>
            <button class="btn edit-btn" @click="openEditModal(student)">编辑</button>
            <button class="btn delete-btn" @click="deleteStudent(student.studentNumber)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="pagination">
      <button class="page" :disabled="page === 1" @click="page-- && fetchStudents()">上一页</button>
      <span>第 {{ page }} 页</span>
      <button class="page" :disabled="students.length < size" @click="page++ && fetchStudents()">下一页</button>
    </div>

    <!-- 新增学生弹窗 -->
    <div v-if="showAddModal" class="modal-overlay">
      <div class="modal">
        <h3>新增学生</h3>
        <label>姓名</label>
        <input v-model="newStudent.realName" placeholder="请输入姓名" />
        <label>学号</label>
        <input v-model="newStudent.studentNumber" placeholder="请输入学号" />
        <label>年级</label>
        <input v-model="newStudent.grade" placeholder="请输入年级" />
        <label>专业</label>
        <input v-model="newStudent.major" placeholder="请输入专业" />

        <div class="modal-actions">
          <button class="btn primary-btn" @click="createStudent">提交</button>
          <button class="btn cancel-btn" @click="showAddModal = false">取消</button>
        </div>
      </div>
    </div>

    <!-- 编辑学生弹窗 -->
    <div v-if="editStudentModal" class="modal-overlay">
      <div class="modal">
        <h3>编辑学生</h3>
        <label>姓名</label>
        <input v-model="editStudent.realName" placeholder="请输入姓名" />
        <label>年级</label>
        <input v-model="editStudent.grade" placeholder="请输入年级" />
        <label>专业</label>
        <input v-model="editStudent.major" placeholder="请输入专业" />

        <div class="modal-actions">
          <button class="btn primary-btn" @click="updateStudent">更新</button>
          <button class="btn cancel-btn" @click="editStudentModal = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/axios'

export default {
  name: 'AdminStudentManagement',
  data() {
    return {
      students: [],
      showAddModal: false,
      editStudentModal: false,
      page: 1,
      size: 10,
      newStudent: { realName: '', studentNumber: '', grade: '', major: '' },
      editStudent: {}
    }
  },
  methods: {
    async fetchStudents() {
      try {
        const res = await api.get('/admin/student/students', {
          params: { page: this.page, size: this.size }
        })
        this.students = res.students || []
      } catch (err) {
        console.error('获取学生失败', err)
      }
    },
    async importStudents(e) {
      const file = e.target.files[0]
      if (!file) return
      const formData = new FormData()
      formData.append('file', file)
      try {
        await api.post('/admin/student/import', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        alert('导入成功')
        this.fetchStudents()
      } catch (err) {
        console.error('导入失败', err)
        alert('导入失败')
      }
    },
    async deleteStudent(studentNumber) {
      try {
        await api.delete(`/admin/student/${studentNumber}`)
        this.fetchStudents()
      } catch (err) {
        console.error('删除失败', err)
      }
    },
    async createStudent() {
      try {
        await api.post('/admin/student', this.newStudent)
        alert('创建成功')
        this.showAddModal = false
        this.fetchStudents()
      } catch (err) {
        console.error('创建失败', err)
        alert(err.response?.data?.message || '创建失败')
      }
    },
    openEditModal(student) {
      this.editStudent = { ...student }
      this.editStudentModal = true
    },
    async updateStudent() {
      try {
        await api.put(`/admin/student/${this.editStudent.studentNumber}`, this.editStudent)
        alert('更新成功')
        this.editStudentModal = false
        this.fetchStudents()
      } catch (err) {
        console.error('更新失败', err)
        alert(err.response?.data?.message || '更新失败')
      }
    }
  },
  mounted() {
    this.fetchStudents()
  }
}
</script>

<style scoped>
.student-mgmt {
  position: relative;
  padding: 30px;
  background-color: white;
  overflow: hidden;
  z-index: 1;
}

h2 {
  font-size: 26px;
  color: #333;
  margin-bottom: 20px;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
}

.btn {
  background: #4a90e2;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn:hover {
  background: #357abd;
}

.edit-btn {
  background: #4a90e2;
  margin-right: 6px;
}

.delete-btn {
  background: #e74c3c;
}

.cancel-btn {
  background: #e74c3c;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.data-table th, .data-table td {
  border: 1px solid #ddd;
  padding: 12px;
  text-align: center;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
}

.page {
  padding: 8px 16px;
  border-radius: 8px; /* Increase border radius */
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  background: transparent;
  border: 1px solid #4a90e2;
  color: #4a90e2;
}
.pagination-btn:hover {
  background-color: #2980b9;
}

.pagination-btn:disabled {
  background-color: #d1d1d1;
  cursor: not-allowed;
}
.page:disabled {
  color: #ccc;
  border-color: #ccc;
  cursor: not-allowed;
}

/* 弹窗背景遮罩 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99;
}

/* 弹窗样式 */
.modal {
  background: #fff;
  padding: 30px;
  width: 400px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.modal input {
  width: 100%;
  margin-bottom: 15px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.modal label {
  font-weight: bold;
  margin-bottom: 4px;
  display: inline-block;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 背景装饰 */
.student-mgmt::before {
  content: "";
  position: absolute;
  top: -80px;
  left: -80px;
  width: 220px;
  height: 220px;
  background: radial-gradient(circle, rgba(170, 205, 245, 0.25), rgba(74, 144, 226, 0.4));
  border-radius: 50%;
  filter: blur(2px);
  z-index: 0;
}

.student-mgmt::after {
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

.decor-lower-left {
  position: absolute;
  bottom: -60px;
  left: -60px;
  width: 160px;
  height: 160px;
  background: radial-gradient(circle, rgba(170, 205, 245, 0.25), rgba(74, 144, 226, 0.4));
  border-radius: 50%;
  z-index: 0;
  filter: blur(2px);
}

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
