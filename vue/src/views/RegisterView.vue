<template>
  <div class="register-container">
    <h2>用户注册</h2>
    
    <!-- 身份选择 -->
    <div class="identity-selector">
      <button 
        :class="['identity-btn', { 'active': userType === 'student' }]"
        @click="userType = 'student'"
      >
        学生注册
      </button>
      <button 
        :class="['identity-btn', { 'active': userType === 'teacher' }]"
        @click="userType = 'teacher'"
      >
        教师注册
      </button>
    </div>

    <!-- 学生注册表单 -->
    <form v-if="userType === 'student'" @submit.prevent="handleRegister">
      <div class="form-group">
        <label>用户名<span class="required">*</span></label>
        <input v-model="studentForm.username" type="text" placeholder="请输入用户名" required>
      </div>

      <div class="form-group">
        <label>真实姓名<span class="required">*</span></label>
        <input v-model="studentForm.realName" type="text" placeholder="请输入真实姓名" required>
      </div>

      <div class="form-group">
        <label>邮箱<span class="required">*</span></label>
        <input v-model="studentForm.email" type="email" placeholder="请输入邮箱" required>
      </div>

      <div class="form-group">
        <label>手机号<span class="required">*</span></label>
        <input v-model="studentForm.phone" type="tel" placeholder="请输入手机号" required>
      </div>

      <div class="form-group">
        <label>年级<span class="required">*</span></label>
        <select v-model="studentForm.grade" required>
          <option value="">请选择年级</option>
          <option v-for="year in 4" :key="year" :value="'大' + ['一','二','三','四'][year-1]">{{ '大' + ['一','二','三','四'][year-1] }}</option>
        </select>
      </div>

      <div class="form-group">
        <label>学号<span class="required">*</span></label>
        <input v-model="studentForm.studentNumber" type="text" placeholder="请输入学号" required>
      </div>

      <div class="form-group">
        <label>专业<span class="required">*</span></label>
        <input v-model="studentForm.major" type="text" placeholder="请输入专业" required>
      </div>

      <div class="form-group">
        <label>密码<span class="required">*</span></label>
        <input v-model="studentForm.password" type="password" placeholder="至少8位字符" minlength="8" required>
      </div>

      <div class="form-group">
        <label>确认密码<span class="required">*</span></label>
        <input v-model="studentForm.confirmPassword" type="password" placeholder="再次输入密码" minlength="8" required>
      </div>

      <div class="terms-group">
        <input type="checkbox" id="studentTerms" v-model="studentForm.agreeTerms">
        <label for="studentTerms">我已阅读并同意《服务条款》和《隐私政策》</label>
      </div>

      <button type="submit" class="register-btn" :disabled="!studentForm.agreeTerms">注册</button>
    </form>

    <!-- 教师注册表单 -->
    <form v-else @submit.prevent="handleRegister">
      <div class="form-group">
        <label>用户名<span class="required">*</span></label>
        <input v-model="teacherForm.username" type="text" placeholder="请输入用户名" required>
      </div>

      <div class="form-group">
        <label>真实姓名<span class="required">*</span></label>
        <input v-model="teacherForm.realName" type="text" placeholder="请输入真实姓名" required>
      </div>

      <div class="form-group">
        <label>邮箱<span class="required">*</span></label>
        <input v-model="teacherForm.email" type="email" placeholder="请输入邮箱" required>
      </div>

      <div class="form-group">
        <label>手机号<span class="required">*</span></label>
        <input v-model="teacherForm.phone" type="tel" placeholder="请输入手机号" required>
      </div>

      <div class="form-group">
        <label>职称<span class="required">*</span></label>
        <select v-model="teacherForm.title" required>
          <option value="">请选择职称</option>
          <option value="教授">教授</option>
          <option value="副教授">副教授</option>
          <option value="讲师">讲师</option>
          <option value="助教">助教</option>
        </select>
      </div>

      <div class="form-group">
        <label>工号<span class="required">*</span></label>
        <input v-model="teacherForm.employeeNumber" type="text" placeholder="请输入工号" required>
      </div>

      <div class="form-group">
        <label>所属院系<span class="required">*</span></label>
        <input v-model="teacherForm.department" type="text" placeholder="请输入所属院系" required>
      </div>

      <div class="form-group">
        <label>密码<span class="required">*</span></label>
        <input v-model="teacherForm.password" type="password" placeholder="至少8位字符" minlength="8" required>
      </div>

      <div class="form-group">
        <label>确认密码<span class="required">*</span></label>
        <input v-model="teacherForm.confirmPassword" type="password" placeholder="再次输入密码" minlength="8" required>
      </div>

      <div class="terms-group">
        <input type="checkbox" id="teacherTerms" v-model="teacherForm.agreeTerms">
        <label for="teacherTerms">我已阅读并同意《服务条款》和《隐私政策》</label>
      </div>

      <button type="submit" class="register-btn" :disabled="!teacherForm.agreeTerms">注册</button>
    </form>

    <p class="login-link">
      已有账号？<router-link to="/login">立即登录</router-link>
    </p>
  </div>
</template>

<script>
import api from '@/axios';
export default {
  name: 'RegisterView',
  data() {
    return {
      userType: 'student', // 默认显示学生注册
      studentForm: {
        username: '',
        realName: '', // 新增真实姓名字段
        email: '',
        phone: '',
        grade: '',
        studentNumber: '', // 学号
        major: '',
        password: '',
        confirmPassword: '',
        agreeTerms: false
      },
      teacherForm: {
        username: '',
        realName: '', // 新增真实姓名字段
        email: '',
        phone: '',
        title: '',
        employeeNumber: '', // 新增工号字段
        department: '',
        password: '',
        confirmPassword: '',
        agreeTerms: false
      },
      loading: false,
      errorMessage: ''
    }
  },
  methods: {
    handleRegister() {
      if (this.userType === 'student') {
        this.registerStudent()
      } else {
        this.registerTeacher()
      }
    },
    async registerStudent() {
      // 验证逻辑
      if (this.studentForm.password !== this.studentForm.confirmPassword) {
        this.errorMessage = '两次输入的密码不一致！';
        return;
      }
      
      if (!this.studentForm.agreeTerms) {
        this.errorMessage = '请同意服务条款和隐私政策';
        return;
      }

      this.loading = true;
      this.errorMessage = '';

      try {
        const payload = {
          username: this.studentForm.username,
          password: this.studentForm.password,
          email: this.studentForm.email,
          phone: this.studentForm.phone,
          realName: this.studentForm.realName, // 新增真实姓名
          role: 'STUDENT',
          studentNumber: this.studentForm.studentNumber, // 学号
          grade: this.studentForm.grade,
          major: this.studentForm.major
        };

        const response = await api.post('/auth/register', payload);
        
        // 保存token和用户信息
        localStorage.setItem('token', response.token);
        localStorage.setItem('userId', response.userId);
        localStorage.setItem('username', response.username);
        localStorage.setItem('userRole', response.role);
        
        // 跳转到学生仪表盘
        this.$router.push('/student');
        
      } catch (error) {
        console.error('学生注册失败:', error);
        this.errorMessage = error.response?.data?.error || '注册失败，请稍后再试';
      } finally {
        this.loading = false;
      }
    },
    async registerTeacher() {
      // 验证逻辑
      if (this.teacherForm.password !== this.teacherForm.confirmPassword) {
        this.errorMessage = '两次输入的密码不一致！';
        return;
      }
      
      if (!this.teacherForm.agreeTerms) {
        this.errorMessage = '请同意服务条款和隐私政策';
        return;
      }

      this.loading = true;
      this.errorMessage = '';

      try {
        const payload = {
          username: this.teacherForm.username,
          password: this.teacherForm.password,
          email: this.teacherForm.email,
          phone: this.teacherForm.phone,
          realName: this.teacherForm.realName, // 新增真实姓名
          role: 'TEACHER',
          employeeNumber: this.teacherForm.employeeNumber, // 新增工号
          title: this.teacherForm.title,
          department: this.teacherForm.department,
          bio: this.teacherForm.bio || ''
        };

        const response = await api.post('/auth/register', payload);
        
        // 保存token和用户信息
        localStorage.setItem('token', response.token);
        localStorage.setItem('userId', response.userId);
        localStorage.setItem('username', response.username);
        localStorage.setItem('userRole', response.role);
        
        // 跳转到教师仪表盘
        this.$router.push('/teacher');
        
      } catch (error) {
        console.error('教师注册失败:', error);
        this.errorMessage = error.response?.data?.error || '注册失败，请稍后再试';
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
.register-container {
  max-width: 600px;
  margin: 30px auto;
  padding: 30px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  background: white;
}

.register-container h2 {
  text-align: center;
  color: #3a7bd5;
  margin-bottom: 30px;
}

.identity-selector {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.identity-btn {
  flex: 1;
  padding: 12px;
  background: none;
  border: none;
  border-bottom: 3px solid transparent;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.identity-btn.active {
  color: #3a7bd5;
  border-bottom-color: #3a7bd5;
  font-weight: bold;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
}

.form-group input,
.form-group select,
.register-btn {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 15px;
  font-size: 16px;
}

.form-group input,
.form-group select {
  border: 1px solid #ddd;
  border-radius: 4px;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus {
  border-color: #3a7bd5;
  outline: none;
}

.form-group select {
  appearance: none;
  background: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='%23333' viewBox='0 0 24 24'%3e%3cpath d='M7 10l5 5 5-5z'/%3e%3c/svg%3e") no-repeat right 10px center;
  background-size: 20px;
}

.required {
  color: #ff4d4f;
  margin-left: 4px;
}

.terms-group {
  display: flex;
  align-items: flex-start;
  margin: 25px 0;
  width: 100%;
  box-sizing: border-box;
  font-size: 14px;
  line-height: 1.6;
}

.terms-group input[type="checkbox"] {
  margin-right: 10px;
  margin-top: 2px;
  flex-shrink: 0;
}

.terms-group label {
  flex: 1;
  color: #333;
  word-break: break-word;
}

.terms-group label a {
  color: #3a7bd5;
  text-decoration: none;
}

.terms-group label a:hover {
  text-decoration: underline;
}

.register-btn {
  background-color: #3a7bd5;
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.register-btn:hover:not(:disabled) {
  background-color: #2c6bc7;
}

.register-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.login-link a {
  color: #3a7bd5;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>