<!-- components/CourseForm.vue -->
<template>
  <div class="form-dialog" v-if="visible">
    <div class="form-container">
      <h3>{{ course?.courseId ? '编辑课程' : '新增课程' }}</h3>
      <form @submit.prevent="submitForm">
        <input v-model="form.courseCode" placeholder="课程编号" required />
        <input v-model="form.name" placeholder="课程名称" required />
        <input v-model="form.credit" type="number" placeholder="学分" step="0.5" min="0.5" max="10" required />
        <input v-model="form.hours" type="number" placeholder="学时" min="1" max="200" required />
        <input v-model="form.semester" placeholder="学期 (例：2025年秋季学期)" />
        <textarea v-model="form.description" placeholder="课程描述 (可选)"></textarea>

        <div class="btn-group">
          <button class="btn primary-btn" type="submit">提交</button>
          <button class="btn" type="button" @click="cancel">取消</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  props: ['visible', 'course'],
  emits: ['submit', 'cancel'],
  data() {
    return {
      form: {
        courseCode: '',
        name: '',
        credit: 3.0,
        hours: 48,
        semester: '',
        description: ''
      }
    }
  },
  watch: {
    course: {
      immediate: true,
      handler(val) {
        this.form = val ? { ...val } : {
          courseCode: '',
          name: '',
          credit: 3.0,
          hours: 48,
          semester: '',
          description: ''
        }
      }
    }
  },
  methods: {
    submitForm() {
      this.$emit('submit', { ...this.form })
    },
    cancel() {
      this.$emit('cancel')
    }
  }
}
</script>

<style scoped>
.form-dialog {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex; justify-content: center; align-items: center;
}
.form-container {
  background: #fff; padding: 20px; border-radius: 8px; width: 400px;
}
.form-container input, .form-container textarea {
  width: 100%; margin-bottom: 10px; padding: 8px;
}
.btn-group {
  display: flex; justify-content: flex-end; gap: 10px;
}
</style>
