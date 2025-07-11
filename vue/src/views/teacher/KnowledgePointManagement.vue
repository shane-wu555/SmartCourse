<template>
  <div class="knowledge-point-mgmt">
    <!-- 返回按钮 -->
    <button class="back-btn" @click="$router.back()">返回</button>
    <h2>知识点管理 - {{ courseName }}</h2>

    <!-- 添加知识点区域 -->
    <div class="add-knowledge-point-section">
      <div class="form-group">
        <label>知识点名称</label>
        <input v-model="newKnowledgePoint.name" placeholder="输入知识点名称" />
      </div>
      <div class="form-group">
        <label>知识点描述</label>
        <textarea v-model="newKnowledgePoint.description" placeholder="输入知识点描述"></textarea>
      </div>
      <div class="form-group">
        <label>难度等级</label>
        <select v-model="newKnowledgePoint.difficultylevel">
          <option value="">请选择难度等级</option>
          <option value="EASY">简单</option>
          <option value="MEDIUM">中等</option>
          <option value="HARD">困难</option>
        </select>
      </div>
      <button class="btn primary-btn" @click="addKnowledgePoint">添加知识点</button>
    </div>

    <!-- 知识点列表 -->
    <div class="knowledge-point-list">
      <table>
        <thead>
          <tr>
            <th>名称</th>
            <th>描述</th>
            <th>难度等级</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="point in knowledgePoints" :key="point.pointId">
            <td>{{ point.name }}</td>
            <td>{{ point.description }}</td>
            <td>
              <span class="difficulty-badge" :class="getDifficultyClass(point.difficultylevel)">
                {{ getDifficultyLabel(point.difficultylevel) }}
              </span>
            </td>
            <td>
  <button @click="viewKnowledgePoint(point)">查看</button>
  <button class="btn warning-btn" @click="editKnowledgePoint(point)">编辑</button>
  <button class="btn danger-btn" @click="deleteKnowledgePoint(point)">删除</button>
</td>

          </tr>
        </tbody>
      </table>
    </div>

    <!-- 编辑知识点弹窗 -->
    <div v-if="showEditForm" class="modal-overlay">
      <div class="modal">
        <h3>编辑知识点</h3>
        <label>知识点名称</label>
        <input v-model="editKnowledgePointData.name" required />
        <label>知识点描述</label>
        <textarea v-model="editKnowledgePointData.description" required></textarea>
        <label>难度等级</label>
        <select v-model="editKnowledgePointData.difficultylevel" required>
          <option value="">请选择难度等级</option>
          <option value="EASY">简单</option>
          <option value="MEDIUM">中等</option>
          <option value="HARD">困难</option>
        </select>
        <div class="modal-actions">
          <button @click="updateKnowledgePoint">保存</button>
          <button class="danger-btn" @click="showEditForm = false">取消</button>
        </div>
      </div>
    </div>
    
    <!-- 查看知识点详情弹窗 -->
    <div v-if="showViewModal" class="modal-overlay">
      <div class="modal">
        <h3>知识点详情</h3>
        <p><strong>名称：</strong>{{ viewPoint.name }}</p>
        <p><strong>描述：</strong>{{ viewPoint.description }}</p>
        <p><strong>难度等级：</strong>
          <span class="difficulty-badge" :class="getDifficultyClass(viewPoint.difficultylevel)">
            {{ getDifficultyLabel(viewPoint.difficultylevel) }}
          </span>
        </p>
        <div class="modal-actions">
          <button class="btn primary-btn" @click="showViewModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "@/axios";

export default {
  name: "KnowledgePointManagement",
  data() {
    return {
      courseId: this.$route.params.courseId,
      courseName: "",
      knowledgePoints: [],
      newKnowledgePoint: {
        name: "",
        description: "",
        difficultylevel: ""
      },
      showEditForm: false,
      editKnowledgePointData: {
        pointId: null,
        name: "",
        description: "",
        difficultylevel: ""
      },
      showViewModal: false,
      viewPoint: {
        name: '',
        description: '',
        difficultylevel: ''
      }
    };
  },
  methods: {
    // 获取难度等级对应的CSS类
    getDifficultyClass(difficulty) {
      switch(difficulty) {
        case 'EASY': return 'difficulty-easy';
        case 'MEDIUM': return 'difficulty-medium';
        case 'HARD': return 'difficulty-hard';
        default: return 'difficulty-unknown';
      }
    },
    
    // 获取难度等级的中文标签
    getDifficultyLabel(difficulty) {
      switch(difficulty) {
        case 'EASY': return '简单';
        case 'MEDIUM': return '中等';
        case 'HARD': return '困难';
        default: return '未知';
      }
    },
    
    async fetchCourseInfo() {
      try {
        const res = await axios.get(`/teacher/courses/${this.courseId}`);
        this.courseName = res.data.name || res.data.data?.name || "";
      } catch (err) {
        console.error("获取课程信息失败", err);
      }
    },
    
    async fetchKnowledgePoints() {
      try {
        const res = await axios.get(`/teacher/courses/${this.courseId}/knowledge-points`);
        this.knowledgePoints = res.data.data || res.data || [];
      } catch (err) {
        console.error("获取知识点失败", err);
      }
    },
    
    async addKnowledgePoint() {
      if (!this.newKnowledgePoint.name || !this.newKnowledgePoint.description || !this.newKnowledgePoint.difficultylevel) {
        alert("请填写知识点名称、描述和难度等级！");
        return;
      }
      try {
        await axios.post(`/teacher/courses/${this.courseId}/knowledge-points`, this.newKnowledgePoint);
        alert("添加成功");
        this.fetchKnowledgePoints();
        this.newKnowledgePoint = { name: "", description: "", difficultylevel: "" };
      } catch (err) {
        console.error("添加失败", err);
        alert("添加失败：" + (err.response?.data?.message || err.message));
      }
    },
    
    async viewKnowledgePoint(point) {
      try {
        const res = await axios.get(`/teacher/knowledge-points/${point.pointId}`);
        this.viewPoint = res.data.data || res.data || {};
        this.showViewModal = true;
      } catch (err) {
        console.error("获取知识点详情失败", err);
        alert("获取知识点详情失败");
      }
    },
    
    editKnowledgePoint(point) {
      this.editKnowledgePointData = { ...point };
      this.showEditForm = true;
    },
    
    async updateKnowledgePoint() {
      if (!this.editKnowledgePointData.name || !this.editKnowledgePointData.description || !this.editKnowledgePointData.difficultylevel) {
        alert("请填写知识点名称、描述和难度等级！");
        return;
      }
      try {
        await axios.put(
          `/teacher/knowledge-points/${this.editKnowledgePointData.pointId}`,
          this.editKnowledgePointData
        );
        alert("更新成功");
        this.showEditForm = false;
        this.fetchKnowledgePoints();
      } catch (err) {
        console.error("更新失败", err);
        alert("更新失败：" + (err.response?.data?.message || err.message));
      }
    },
    
    async deleteKnowledgePoint(point) {
      if (!confirm(`确认删除知识点 "${point.name}" 吗？`)) return;
      try {
        await axios.delete(`/teacher/knowledge-points/${point.pointId}`);
        alert("删除成功");
        this.fetchKnowledgePoints();
      } catch (err) {
        console.error("删除失败", err);
        alert("删除失败：" + (err.response?.data?.message || err.message));
      }
    }
  },
  mounted() {
    this.fetchCourseInfo();
    this.fetchKnowledgePoints();
  }
};
</script>

<style scoped>
.knowledge-point-mgmt {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.add-knowledge-point-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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
  box-sizing: border-box;
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
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

.warning-btn {
  background: #f39c12;
  color: white;
}

.danger-btn {
  background: #e74c3c;
  color: white;
}

/* 所有按钮的基础样式 */
.btn {
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;  /* 按钮之间的空隙 */
  border: none;
}

.btn:last-child {
  margin-right: 0; /* 最后一个按钮（删除按钮）不需要右边的空隙 */
}

.btn:hover {
  opacity: 0.9;
}

/* 可以根据需要调整具体按钮之间的间距 */
td button:nth-child(1) {
  margin-right: 10px; /* "查看"和"编辑"之间的空隙 */
}

td button:nth-child(2) {
  margin-right: 10px; /* "编辑"和"删除"之间的空隙 */
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

.back-btn:hover {
  opacity: 0.9;
}

.knowledge-point-list {
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

/* 难度等级样式 */
.difficulty-badge {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
}

.difficulty-easy {
  background-color: #d4edda;
  color: #155724;
}

.difficulty-medium {
  background-color: #fff3cd;
  color: #856404;
}

.difficulty-hard {
  background-color: #f8d7da;
  color: #721c24;
}

.difficulty-unknown {
  background-color: #e2e3e5;
  color: #6c757d;
}

/* modal 样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  padding: 20px;
  width: 500px;
  max-width: 90vw;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.modal h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
}

.modal input,
.modal textarea,
.modal select {
  width: 100%;
  margin-bottom: 15px;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.modal textarea {
  min-height: 80px;
  resize: vertical;
}

.modal label {
  font-weight: bold;
  margin-top: 8px;
  margin-bottom: 5px;
  display: block;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.modal-actions button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal-actions button:first-child {
  background: #4a90e2;
  color: white;
}

.modal-actions button:first-child:hover {
  background: #357abd;
}

.modal-actions .danger-btn {
  background: #e74c3c;
  color: white;
}

.modal-actions .danger-btn:hover {
  background: #c0392b;
}

/* 详情弹窗特殊样式 */
.modal p {
  margin-bottom: 15px;
  line-height: 1.5;
}
button {
  background: #4a90e2;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  cursor: pointer;
}
.modal p strong {
  color: #333;
}
</style>