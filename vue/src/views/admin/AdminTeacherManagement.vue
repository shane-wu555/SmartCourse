<template>
  <div class="teacher-mgmt">
    <div class="decor-lower-left"></div>
    <div class="decor-lower-right"></div>

    <h2>教师信息管理</h2>
    <div class="action-bar">
      <button class="btn primary-btn" @click="showAddModal = true">新增教师</button>
      <button class="btn primary-btn" @click="$refs.fileInput.click()">批量导入</button>
      <input type="file" accept=".csv,.xlsx" @change="importTeachers" hidden ref="fileInput" />
    </div>

    <table class="data-table">
      <thead>
        <tr>
          <th>工号</th>
          <th>姓名</th>
          <th>职称</th>
          <th>院系</th>
          <th>邮箱</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="teacher in teachers" :key="teacher.employeeNumber">
          <td>{{ teacher.employeeNumber }}</td>
          <td>{{ teacher.realName }}</td>
          <td>{{ teacher.title }}</td>
          <td>{{ teacher.department }}</td>
          <td>{{ teacher.email }}</td>
          <td>
            <button class="btn edit-btn" @click="openEditModal(teacher)">编辑</button>
            <button class="btn delete-btn" @click="deleteTeacher(teacher.employeeNumber)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="pagination">
      <button class="page" :disabled="page === 1" @click="page-- && fetchTeachers()">上一页</button>
      <span>第 {{ page }} 页</span>
      <button class="page" :disabled="teachers.length < size" @click="page++ && fetchTeachers()">下一页</button>
    </div>

    <!-- 新增教师弹窗 -->
    <div v-if="showAddModal" class="modal-overlay">
      <div class="modal">
        <h3>新增教师</h3>
        <label>姓名</label>
        <input v-model="newTeacher.realName" placeholder="请输入姓名" />
        <label>工号</label>
        <input v-model="newTeacher.employeeNumber" placeholder="请输入工号" />
        <label>职称</label>
        <input v-model="newTeacher.title" placeholder="请输入职称" />
        <label>院系</label>
        <input v-model="newTeacher.department" placeholder="请输入院系" />
        <label>简介</label>
        <textarea v-model="newTeacher.bio" placeholder="请输入简介"></textarea>

        <div class="modal-actions">
          <button class="btn primary-btn" @click="createTeacher">提交</button>
          <button class="btn cancel-btn" @click="showAddModal = false">取消</button>
        </div>
      </div>
    </div>

    <!-- 编辑教师弹窗 -->
    <div v-if="editTeacherModal" class="modal-overlay">
      <div class="modal">
        <h3>编辑教师</h3>
        <label>姓名</label>
        <input v-model="editTeacher.realName" placeholder="请输入姓名" />
        <label>职称</label>
        <input v-model="editTeacher.title" placeholder="请输入职称" />
        <label>院系</label>
        <input v-model="editTeacher.department" placeholder="请输入院系" />
        <label>简介</label>
        <textarea v-model="editTeacher.bio" placeholder="请输入简介"></textarea>

        <div class="modal-actions">
          <button class="btn primary-btn" @click="updateTeacher">更新</button>
          <button class="btn cancel-btn" @click="editTeacherModal = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/axios'

export default {
  name: 'AdminTeacherManagement',
  data() {
    return {
      teachers: [],
      showAddModal: false,
      editTeacherModal: false,
      page: 1,
      size: 10,
      newTeacher: {
        realName: '', employeeNumber: '', title: '', department: '', bio: ''
      },
      editTeacher: {}
    }
  },
  methods: {
    async fetchTeachers() {
      try {
        const res = await api.get('/admin/teacher/teachers', {
          params: { page: this.page, size: this.size }
        })
        this.teachers = res.teachers || []
      } catch (err) {
        console.error('获取教师失败', err)
      }
    },
    async importTeachers(e) {
      const file = e.target.files[0]
      if (!file) return
      const formData = new FormData()
      formData.append('file', file)
      try {
        await api.post('/admin/teacher/import', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        alert('导入成功')
        this.fetchTeachers()
      } catch (err) {
        console.error('导入失败', err)
        alert('导入失败')
      }
    },
    async deleteTeacher(employeeNumber) {
      try {
        await api.delete(`/admin/teacher/${employeeNumber}`)
        this.fetchTeachers()
      } catch (err) {
        console.error('删除失败', err)
      }
    },
    async createTeacher() {
      try {
        await api.post('/admin/teacher', this.newTeacher)
        alert('创建成功')
        this.showAddModal = false
        this.fetchTeachers()
      } catch (err) {
        console.error('创建失败', err)
        alert(err.response?.data?.error || '创建失败')
      }
    },
    openEditModal(teacher) {
      this.editTeacher = { ...teacher }
      this.editTeacherModal = true
    },
    async updateTeacher() {
      try {
        await api.put(`/admin/teacher/${this.editTeacher.employeeNumber}`, this.editTeacher)
        alert('更新成功')
        this.editTeacherModal = false
        this.fetchTeachers()
      } catch (err) {
        console.error('更新失败', err)
        alert(err.response?.data?.error || '更新失败')
      }
    }
  },
  mounted() {
    this.fetchTeachers()
  }
}
</script>

<style scoped>
.teacher-mgmt {
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

.data-table th,
.data-table td {
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
  border-radius: 8px;
  border: 1px solid #4a90e2;
  color: #4a90e2;
  background: transparent;
  cursor: pointer;
}

.page:disabled {
  color: #ccc;
  border-color: #ccc;
  cursor: not-allowed;
}

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

.modal {
  background: #fff;
  padding: 30px;
  width: 400px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.modal input,
.modal textarea {
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

.teacher-mgmt::before {
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

.teacher-mgmt::after {
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
