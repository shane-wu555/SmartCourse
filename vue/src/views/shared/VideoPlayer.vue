<template>
  <div class="video-player">
    <!-- 返回按钮 -->
    <button class="back-btn" @click="$router.back()">返回</button>
    <h2>视频播放</h2>
    
    <video
      ref="videoRef"
      controls
      @timeupdate="handleTimeUpdate"
      @ended="handleVideoEnd"
      @loadstart="handleLoadStart"
      @loadeddata="handleLoadedData"
      @error="handleVideoError"
      @canplay="handleCanPlay"
      @play="handlePlay"
      @pause="handlePause"
      width="100%"
      :src="videoUrl"
      preload="metadata"
      crossorigin="anonymous"
    ></video>
    
    <!-- 3D热力图显示区域 -->
    <div class="heatmap-section" v-if="loaded && !error">
      <div class="heatmap-header">
        <h3>🏔️ 观看热力图</h3>
        <div class="heatmap-controls">
          <button 
            v-if="threejsLoaded"
            :class="['tab-btn', { active: activeView === '3d' }]"
            @click="switchView('3d')"
          >
            🏔️ 3D山峰图
          </button>
          <button 
            :class="['tab-btn', { active: activeView === '2d' }]"
            @click="switchView('2d')"
          >
            📊 2D热力图
          </button>
          <button 
            class="refresh-btn"
            @click="refreshHeatmap"
            :disabled="loadingHeatmap"
          >
            {{ loadingHeatmap ? '🔄' : '🔄 刷新' }}
          </button>
        </div>
      </div>

      <!-- 3D视图控制面板 -->
      <div v-if="activeView === '3d' && threejsLoaded" class="control-panel">
        <div class="control-group">
          <label>视角:</label>
          <button @click="resetCamera" class="control-btn">重置视角</button>
          <button @click="toggleAutoRotate" class="control-btn">
            {{ autoRotate ? '停止' : '开始' }}旋转
          </button>
        </div>
        <div class="control-group">
          <label>精度:</label>
          <select v-model="resolution" @change="updateHeatmap3D">
            <option value="10">高精度 (10秒)</option>
            <option value="30">中精度 (30秒)</option>
            <option value="60">低精度 (60秒)</option>
          </select>
        </div>
        <div class="control-group">
          <label>高度缩放:</label>
          <input 
            type="range" 
            min="0.5" 
            max="3" 
            step="0.1" 
            v-model="heightScale"
            @input="updateHeightScale"
            class="slider"
          >
          <span>{{ heightScale }}x</span>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loadingHeatmap" class="heatmap-loading">
        <div class="loading-spinner small"></div>
        <span>生成3D热力图中...</span>
      </div>

      <!-- 3D热力图容器 -->
      <div 
        v-show="activeView === '3d'" 
        ref="heatmap3DContainer" 
        class="heatmap-3d-container"
        @wheel="handleWheel"
      >
        <!-- Three.js canvas will be inserted here -->
      </div>

      <!-- 2D热力图（保留原有的2D实现） -->
      <div v-show="activeView === '2d'" class="heatmap-content">
        <!-- 原有的2D热力图代码 -->
        <div class="heatmap-stats" v-if="heatmapData.stats">
          <div class="stat-item">
            <span class="stat-label">总观看时长:</span>
            <span class="stat-value">{{ formatTime(heatmapData.stats.totalWatchTime) }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">观看覆盖率:</span>
            <span class="stat-value">{{ (heatmapData.stats.coverageRate * 100).toFixed(1) }}%</span>
          </div>
          <div class="stat-item" v-if="heatmapData.stats.hottestSegmentStart !== null">
            <span class="stat-label">最热点:</span>
            <span class="stat-value">{{ formatTime(heatmapData.stats.hottestSegmentStart) }} - {{ formatTime(heatmapData.stats.hottestSegmentEnd) }}</span>
          </div>
        </div>

        <div class="heatmap-visualization">
          <div class="heatmap-timeline" ref="heatmapTimeline">
            <!-- 保留原有的2D时间轴代码 -->
            <div class="time-scale">
              <div
                v-for="mark in timeMarks"
                :key="mark.time"
                class="time-mark"
                :style="{ left: mark.position + '%' }"
              >
                {{ formatTime(mark.time) }}
              </div>
            </div>
            
            <div class="heatmap-bar">
              <div
                v-for="(segment, index) in heatmapData.segments"
                :key="index"
                class="heat-segment"
                :style="{
                  left: (segment.start / videoDuration * 100) + '%',
                  width: ((segment.end - segment.start) / videoDuration * 100) + '%',
                  backgroundColor: getHeatColor(segment.intensity),
                  opacity: 0.7 + segment.intensity * 0.3
                }"
                :title="`${formatTime(segment.start)} - ${formatTime(segment.end)}\n观看次数: ${segment.count}\n热度: ${(segment.intensity * 100).toFixed(1)}%`"
                @click="seekToTime(segment.start)"
              ></div>
            </div>
            
            <div 
              class="current-position"
              :style="{ left: (currentTime / videoDuration * 100) + '%' }"
            ></div>
          </div>
        </div>
      </div>

      <!-- 图例 -->
      <div class="legend" v-if="activeView === '3d'">
        <div class="legend-title">热度图例</div>
        <div class="legend-gradient">
          <div class="legend-item" v-for="(item, index) in legendItems" :key="index">
            <div class="legend-color" :style="{ backgroundColor: item.color }"></div>
            <span class="legend-text">{{ item.label }}</span>
          </div>
        </div>
        <div class="legend-note">
          💡 提示：鼠标拖拽旋转视角，滚轮缩放，点击山峰跳转到对应时间
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loadingHeatmap && (!heatmapData.segments || heatmapData.segments.length === 0)" class="heatmap-empty">
        <p>📈 暂无观看数据</p>
        <p class="empty-hint">继续观看视频以生成热力图数据</p>
      </div>
    </div>

    <!-- 原有的加载和错误显示 -->
    <div v-if="loading" class="loading">
      <div class="loading-spinner"></div>
      <p>视频加载中...</p>
      <p class="loading-details">正在从服务器获取视频流...</p>
    </div>
    <div v-if="error" class="error">
      <h3>❌ 视频加载失败</h3>
      <p>{{ error }}</p>
      <div class="error-details">
        <p><strong>资源ID:</strong> {{ resourceId }}</p>
        <p><strong>视频URL:</strong> {{ videoUrl }}</p>
        <button @click="retryLoad" class="retry-button">🔄 重试加载</button>
        <a :href="`/video-test.html?resourceId=${resourceId}`" target="_blank" class="test-button">🧪 打开测试工具</a>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/axios';
import * as THREE from 'three';

export default {
  name: 'VideoPlayer',
  data() {
    return {
      // 原有状态变量
      taskId: this.$route.query.taskId || '',
      showCompletionDialog: false,
      hasMarkedCompleted: false,
      videoDuration: 0,
      resourceId: this.$route.params.resourceId,
      videoUrl: '',
      videoName: '',
      lastPosition: 0,
      totalWatched: 0,
      segments: [],
      segmentStart: 0,
      segmentEnd: 0,
      loaded: false,
      loading: false,
      error: null,
      progressTimer: null,
      lastWatchedSecond: 0,
      currentTime: 0,
      
      // 热力图相关状态
      activeView: '2d', // 默认改为2d，避免3D问题
      heatmapData: {
        duration: 0,
        segments: [],
        stats: null,
        maxCount: 0
      },
      timelineData: {
        intervals: [],
        maxCount: 0
      },
      loadingHeatmap: false,
      heatmapTimer: null,
      resolution: 30, // 3D热力图的时间精度（秒）
      heightScale: 1.5, // 3D山峰高度缩放
      autoRotate: false, // 自动旋转
      
      // Three.js 相关 - 使用非响应式存储
      threejsLoaded: false, // 添加Three.js加载状态
      
      // 相机控制相关 - 使用非响应式存储
      isMouseDown: false,
      mouseStart: { x: 0, y: 0 },
      
      // 图例数据
      legendItems: [
        { color: '#0066cc', label: '低热度' },
        { color: '#00aa00', label: '中等热度' },
        { color: '#ffaa00', label: '高热度' },
        { color: '#ff4444', label: '极高热度' }
      ]
    };
  },
  computed: {
    timeMarks() {
      if (!this.videoDuration) return [];
      const marks = [];
      const interval = this.videoDuration / 5;
      for (let i = 0; i <= 5; i++) {
        const time = i * interval;
        marks.push({
          time: time,
          position: (time / this.videoDuration) * 100
        });
      }
      return marks;
    }
  },
  async mounted() {
    if (!this.resourceId) {
      this.error = '缺少资源ID参数';
      return;
    }

    // 初始化非响应式的Three.js对象
    this.initThreeJSObjects();

    // 检查Three.js是否正确加载
    this.checkThreeJS();

    await this.fetchProgress();
    await this.fetchVideoUrl();

    this.$nextTick(() => {
      if (this.$refs.videoRef) {
        this.$refs.videoRef.addEventListener('loadeddata', () => {
          if (this.lastPosition > 0) {
            this.$refs.videoRef.currentTime = this.lastPosition;
          }
          this.fetchHeatmapData();
        }, { once: true });
      }
    });

    this.progressTimer = setInterval(() => {
      if (this.loaded) this.saveProgress();
    }, 3000);

    this.heatmapTimer = setInterval(() => {
      if (this.loaded) this.fetchHeatmapData();
    }, 30000);

    // 初始化3D场景 - 延迟初始化以确保DOM准备就绪
    this.$nextTick(() => {
      // 再次延迟确保组件完全挂载
      setTimeout(() => {
        try {
          // 只有在activeView为3d时才初始化
          if (this.activeView === '3d') {
            this.init3DScene();
          }
        } catch (error) {
          console.error('初始化3D场景失败:', error);
        }
      }, 500); // 增加延迟时间
    });
  },
  beforeUnmount() {
    if (this.progressTimer) clearInterval(this.progressTimer);
    if (this.heatmapTimer) clearInterval(this.heatmapTimer);
    if (this._animationFrame) cancelAnimationFrame(this._animationFrame);
    this.saveProgress();
    this.cleanup3DScene();
  },
  methods: {
    // 初始化非响应式的Three.js对象
    initThreeJSObjects() {
      // 使用非响应式属性存储Three.js对象，避免Vue的响应式代理问题
      this._scene = null;
      this._camera = null;
      this._renderer = null;
      this._controls = null;
      this._heatmapMesh = null;
      this._animationFrame = null;
      this._cameraTarget = null;
      this._spherical = null;
      this._cleanupEvents = null;
    },

    // Three.js 检测方法
    checkThreeJS() {
      try {
        if (typeof THREE !== 'undefined' && THREE.Scene && THREE.Camera && THREE.WebGLRenderer) {
          this.threejsLoaded = true;
          this.activeView = '3d';
          console.log('Three.js 加载成功，启用3D视图');
        } else {
          this.threejsLoaded = false;
          this.activeView = '2d';
          console.warn('Three.js 未正确加载，使用2D视图');
        }
      } catch (error) {
        this.threejsLoaded = false;
        this.activeView = '2d';
        console.warn('Three.js 检查失败，自动降级为2D视图:', error);
      }
    },

    // 原有方法保持不变...
    async fetchProgress() {
      try {
        const res = await api.get(`/video-progress/${this.resourceId}`);
        if (res) {
          this.lastPosition = res.lastPosition || 0;
          this.totalWatched = res.totalWatched || 0;
          this.segments = res.segments || [];
        }
      } catch (e) {
        this.lastPosition = 0;
        this.totalWatched = 0;
        this.segments = [];
      }
    },

    async fetchVideoUrl() {
      try {
        if (!this.resourceId || this.resourceId === 'undefined' || this.resourceId === 'null') {
          this.error = '无效的资源ID，无法加载视频';
          return;
        }
        
        const res = await api.get(`/teacher/resources/${this.resourceId}`);
        let resourceData = res;
        if (res && res.data && typeof res.data === 'object') {
          resourceData = res.data;
        }
        
        if (resourceData) {
          this.videoName = resourceData.name || '未知视频';
        }
        
        const token = localStorage.getItem('token');
        const baseURL = api.defaults.baseURL || 'http://localhost:8082/api';
        this.videoUrl = `${baseURL}/teacher/resources/${this.resourceId}/play`;
        
        if (token) {
          this.videoUrl += `?token=${encodeURIComponent(token)}`;
        }
        
      } catch (err) {
        this.error = '视频加载失败: ' + (err.friendlyMessage || err.message || '未知错误');
      }
    },

    async saveProgress() {
      if (this.loaded && this.$refs.videoRef) {
        const current = this.$refs.videoRef.currentTime;
        if (current > this.segmentStart) {
          this.segments.push({
            start: this.segmentStart,
            end: current,
            count: 1
          });
          this.segmentStart = current;
        }
        if (this.segments.length === 0) {
          this.segments.push({
            start: 0,
            end: current,
            count: 1
          });
        }
      }
      try {
        const segmentsToSend = this.segments.slice();
        await api.post(`/video-progress/${this.resourceId}/update`, {
          lastPosition: this.$refs.videoRef ? this.$refs.videoRef.currentTime : this.lastPosition,
          totalWatched: this.totalWatched,
          segments: segmentsToSend
        });
        this.segments = [];
      } catch (e) {
        console.warn('保存观看进度失败', e);
      }
    },

    // 3D热力图相关方法
    init3DScene() {
      try {
        // 检查Three.js是否正确加载
        if (!this.threejsLoaded || typeof THREE === 'undefined') {
          console.error('THREE.js 未正确加载');
          return;
        }

        // 检查容器是否存在
        if (!this.$refs.heatmap3DContainer) {
          console.warn('3D容器未找到，延迟初始化');
          setTimeout(() => {
            if (this.$refs.heatmap3DContainer) {
              this.init3DScene();
            }
          }, 100);
          return;
        }

        // 清理之前的场景
        this.cleanup3DScene();

        const container = this.$refs.heatmap3DContainer;
        const width = container.clientWidth || 800;
        const height = 400;

        console.log('开始初始化3D场景，容器尺寸:', width, 'x', height);

        // 创建场景
        this._scene = new THREE.Scene();
        this._scene.background = new THREE.Color(0xf0f0f0);

        // 初始化相机目标和球坐标
        this._cameraTarget = new THREE.Vector3(0, 0, 0);
        this._spherical = new THREE.Spherical(25, Math.PI * 0.3, 0);

        // 创建相机
        this._camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 1000);
        this.updateCameraPosition();

        // 创建渲染器 - 使用最安全的设置
        this._renderer = new THREE.WebGLRenderer({ 
          antialias: true,
          alpha: false,
          preserveDrawingBuffer: false,
          powerPreference: "default"
        });
        this._renderer.setSize(width, height);
        this._renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
        this._renderer.setClearColor(0xf0f0f0, 1);
        
        // 安全地添加到容器
        container.appendChild(this._renderer.domElement);

        // 添加基础光源
        this.addLights();

        // 添加网格底板
        this.addGridPlane();

        // 添加增强的鼠标控制
        this.addEnhancedControls();

        // 开始渲染循环
        this.animate();

        // 监听窗口大小变化
        window.addEventListener('resize', this.onWindowResize);

        console.log('3D场景初始化成功');
      } catch (error) {
        console.error('3D场景初始化失败:', error);
        this.threejsLoaded = false;
        this.activeView = '2d';
        this.error = '3D渲染初始化失败，已切换到2D模式';
      }
    },

    // 使用球坐标系统安全更新相机位置
    updateCameraPosition() {
      if (!this._camera || !this._spherical || !this._cameraTarget) return;
      
      const position = new THREE.Vector3();
      position.setFromSpherical(this._spherical);
      position.add(this._cameraTarget);
      
      this._camera.position.copy(position);
      this._camera.lookAt(this._cameraTarget);
    },

    addEnhancedControls() {
      if (!this.$refs.heatmap3DContainer) return;

      const container = this.$refs.heatmap3DContainer;
      
      // 鼠标事件处理函数
      const onMouseDown = (event) => {
        this.isMouseDown = true;
        this.mouseStart.x = event.clientX;
        this.mouseStart.y = event.clientY;
        container.style.cursor = 'grabbing';
      };

      const onMouseMove = (event) => {
        if (!this.isMouseDown || !this._spherical) return;

        const deltaX = event.clientX - this.mouseStart.x;
        const deltaY = event.clientY - this.mouseStart.y;

        // 使用球坐标系统安全地更新相机位置
        this._spherical.theta -= deltaX * 0.01;
        this._spherical.phi += deltaY * 0.01;

        // 限制垂直角度范围
        this._spherical.phi = Math.max(0.1, Math.min(Math.PI - 0.1, this._spherical.phi));

        this.updateCameraPosition();

        this.mouseStart.x = event.clientX;
        this.mouseStart.y = event.clientY;
      };

      const onMouseUp = () => {
        this.isMouseDown = false;
        container.style.cursor = 'grab';
      };

      const onMouseLeave = () => {
        this.isMouseDown = false;
        container.style.cursor = 'grab';
      };

      const onWheel = (event) => {
        event.preventDefault();
        if (!this._spherical) return;

        const delta = event.deltaY * 0.05;
        this._spherical.radius = Math.max(5, Math.min(50, this._spherical.radius + delta));
        this.updateCameraPosition();
      };

      const onClick = (event) => {
        if (this.isMouseDown) return; // 拖拽时不触发点击

        const rect = container.getBoundingClientRect();
        const mouse = new THREE.Vector2(
          ((event.clientX - rect.left) / rect.width) * 2 - 1,
          -((event.clientY - rect.top) / rect.height) * 2 + 1
        );

        const raycaster = new THREE.Raycaster();
        raycaster.setFromCamera(mouse, this._camera);

        if (this._heatmapMesh && this._heatmapMesh.type === 'Group') {
          const intersects = raycaster.intersectObjects(this._heatmapMesh.children, true);
          if (intersects.length > 0) {
            const intersect = intersects[0];
            // 根据点击位置计算时间
            const normalizedX = (intersect.point.x + 20) / 40;
            const time = normalizedX * (this.videoDuration || 100);
            this.seekToTime(Math.max(0, time));
          }
        }
      };

      // 添加事件监听器
      container.addEventListener('mousedown', onMouseDown);
      document.addEventListener('mousemove', onMouseMove);
      document.addEventListener('mouseup', onMouseUp);
      container.addEventListener('mouseleave', onMouseLeave);
      container.addEventListener('wheel', onWheel);
      container.addEventListener('click', onClick);

      container.style.cursor = 'grab';

      // 保存清理函数
      this._cleanupEvents = () => {
        container.removeEventListener('mousedown', onMouseDown);
        document.removeEventListener('mousemove', onMouseMove);
        document.removeEventListener('mouseup', onMouseUp);
        container.removeEventListener('mouseleave', onMouseLeave);
        container.removeEventListener('wheel', onWheel);
        container.removeEventListener('click', onClick);
      };
    },

    addLights() {
      if (!this._scene) return;
      
      try {
        // 环境光 - 降低强度以增强立体感
        const ambientLight = new THREE.AmbientLight(0x404040, 0.4);
        this._scene.add(ambientLight);

        // 主方向光 - 模拟阳光
        const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8);
        directionalLight.position.set(15, 20, 10);
        directionalLight.castShadow = false; // 暂时禁用阴影以提高性能
        this._scene.add(directionalLight);

        // 辅助方向光 - 从另一个角度照亮
        const directionalLight2 = new THREE.DirectionalLight(0x6699ff, 0.3);
        directionalLight2.position.set(-10, 15, -5);
        this._scene.add(directionalLight2);

        // 添加点光源 - 增加山峰的戏剧性效果
        const pointLight = new THREE.PointLight(0xffaa00, 0.5, 50);
        pointLight.position.set(0, 15, 0);
        this._scene.add(pointLight);

        // 添加半球光 - 模拟天空光照
        const hemisphereLight = new THREE.HemisphereLight(0x87ceeb, 0x8b7355, 0.3);
        this._scene.add(hemisphereLight);
      } catch (error) {
        console.error('添加光源失败:', error);
      }
    },

    addGridPlane() {
      if (!this._scene) return;
      
      try {
        // 添加网格线
        const gridHelper = new THREE.GridHelper(40, 20, 0x000000, 0x000000);
        gridHelper.material.opacity = 0.2;
        gridHelper.material.transparent = true;
        this._scene.add(gridHelper);

        // 创建简单的底板
        const planeGeometry = new THREE.PlaneGeometry(40, 20);
        const planeMaterial = new THREE.MeshBasicMaterial({
          color: 0xffffff,
          transparent: true,
          opacity: 0.1,
          side: THREE.DoubleSide
        });
        const plane = new THREE.Mesh(planeGeometry, planeMaterial);
        plane.rotation.x = -Math.PI / 2;
        this._scene.add(plane);
      } catch (error) {
        console.error('添加网格底板失败:', error);
      }
    },

    async fetchHeatmapData() {
      try {
        this.loadingHeatmap = true;
        
        // 获取基础热力图数据
        let heatmapResponse;
        try {
          heatmapResponse = await api.get(`/video-progress/${this.resourceId}/heatmap`);
        } catch (apiError) {
          console.warn('获取热力图数据失败，使用模拟数据:', apiError);
          // 使用模拟数据
          heatmapResponse = {
            duration: this.videoDuration || 600,
            segments: [
              { start: 0, end: 30, count: 5, intensity: 0.5 },
              { start: 30, end: 60, count: 8, intensity: 0.8 },
              { start: 60, end: 90, count: 3, intensity: 0.3 }
            ],
            stats: {
              totalWatchTime: 120,
              coverageRate: 0.6,
              hottestSegmentStart: 30,
              hottestSegmentEnd: 60
            },
            maxCount: 8
          };
        }

        this.heatmapData = heatmapResponse || {
          duration: 0,
          segments: [],
          stats: null,
          maxCount: 0
        };

        // 如果是3D视图，获取时间轴数据
        if (this.activeView === '3d' && this.threejsLoaded) {
          try {
            let timelineResponse;
            try {
              timelineResponse = await api.get(`/video-progress/${this.resourceId}/heatmap/timeline?intervalSeconds=${this.resolution}`);
            } catch (timelineError) {
              console.warn('获取时间轴数据失败，生成模拟数据:', timelineError);
              // 生成模拟时间轴数据
              const duration = this.videoDuration || 600;
              const intervalCount = Math.ceil(duration / this.resolution);
              const intervals = [];
              
              for (let i = 0; i < intervalCount; i++) {
                intervals.push({
                  start: i * this.resolution,
                  end: (i + 1) * this.resolution,
                  count: Math.floor(Math.random() * 10),
                  intensity: Math.random()
                });
              }
              
              timelineResponse = {
                intervals: intervals,
                maxCount: 10
              };
            }

            this.timelineData = timelineResponse || {
              intervals: [],
              maxCount: 0
            };

            // 如果没有间隔数据，创建基础数据
            if (!this.timelineData.intervals || this.timelineData.intervals.length === 0) {
              console.warn('没有获取到时间轴数据，创建基础数据');
              this.timelineData = {
                intervals: [
                  { start: 0, end: this.resolution, count: 1, intensity: 0.1 }
                ],
                maxCount: 1
              };
            }

            // 更新3D热力图
            this.updateHeatmap3D();
          } catch (timelineError) {
            console.error('处理时间轴数据时出错:', timelineError);
          }
        }
      } catch (error) {
        console.error('获取热力图数据失败:', error);
        this.heatmapData = {
          duration: 0,
          segments: [],
          stats: null,
          maxCount: 0
        };
        this.timelineData = {
          intervals: [],
          maxCount: 0
        };
      } finally {
        this.loadingHeatmap = false;
      }
    },

    updateHeatmap3D() {
      try {
        if (!this._scene || !this.timelineData.intervals || this.timelineData.intervals.length === 0) {
          console.warn('3D场景或数据未准备好');
          return;
        }

        // 安全地移除旧的热力图
        if (this._heatmapMesh) {
          this._scene.remove(this._heatmapMesh);
          
          // 如果是组，递归清理所有子对象
          if (this._heatmapMesh.type === 'Group') {
            this._heatmapMesh.traverse((child) => {
              if (child.geometry) {
                child.geometry.dispose();
              }
              if (child.material) {
                if (Array.isArray(child.material)) {
                  child.material.forEach(mat => mat.dispose());
                } else {
                  child.material.dispose();
                }
              }
            });
          }
          this._heatmapMesh = null;
        }

        const intervals = this.timelineData.intervals;
        const maxCount = this.timelineData.maxCount || 1;

        // 创建一个组来包含所有的山峰
        const heatmapGroup = new THREE.Group();
        heatmapGroup.name = 'HeatmapGroup';

        const totalWidth = 40; // 总宽度
        const segmentWidth = totalWidth / intervals.length;

        intervals.forEach((interval, i) => {
          const x = -totalWidth / 2 + i * segmentWidth;
          const height = Math.max(0.1, (interval.count / maxCount) * 10 * this.heightScale);
          const intensity = interval.intensity || (interval.count / maxCount);

          // 获取颜色
          const color = this.getHeatColor3D(intensity);

          // 创建山峰几何体 - 使用圆锥体来模拟山峰，增加更多细节
          const radiusBottom = segmentWidth * 0.3; // 底部半径
          const heightSegments = Math.max(3, Math.floor(height * 2)); // 根据高度调整分段
          const radialSegments = 16; // 增加径向分段数，使山峰更圆滑

          // 为不同强度创建不同形状的山峰
          let geometry;
          if (intensity > 0.8) {
            // 高强度：尖锐的山峰
            geometry = new THREE.ConeGeometry(
              radiusBottom * 0.8, 
              height, 
              radialSegments, 
              heightSegments
            );
          } else if (intensity > 0.5) {
            // 中等强度：标准圆锥
            geometry = new THREE.ConeGeometry(
              radiusBottom, 
              height, 
              radialSegments, 
              heightSegments
            );
          } else if (intensity > 0.2) {
            // 低强度：较宽的山丘
            geometry = new THREE.ConeGeometry(
              radiusBottom * 1.3, 
              height, 
              radialSegments, 
              heightSegments
            );
          } else {
            // 极低强度：扁平的圆柱体
            geometry = new THREE.CylinderGeometry(
              radiusBottom * 0.8, 
              radiusBottom * 1.2, 
              height, 
              radialSegments
            );
          }
          
          // 创建渐变材质
          const material = new THREE.MeshPhongMaterial({
            color: color,
            transparent: true,
            opacity: 0.85,
            shininess: 30,
            specular: new THREE.Color(0x111111)
          });

          const mesh = new THREE.Mesh(geometry, material);
          
          // 设置位置 - 山峰底部贴地
          mesh.position.set(
            x + segmentWidth / 2, 
            height / 2, 
            0
          );

          // 为山峰添加轻微的随机旋转，增加自然感
          mesh.rotation.y = Math.random() * Math.PI * 2;

          // 添加用户数据用于点击检测
          mesh.userData = {
            timeStart: interval.start || (i * this.resolution),
            timeEnd: interval.end || ((i + 1) * this.resolution),
            count: interval.count,
            intensity: intensity
          };

          heatmapGroup.add(mesh);

          // 为高强度山峰添加粒子效果或光晕
          if (intensity > 0.7) {
            // 创建光晕效果
            const haloGeometry = new THREE.RingGeometry(
              radiusBottom * 0.5, 
              radiusBottom * 2, 
              8, 
              1
            );
            
            const haloMaterial = new THREE.MeshBasicMaterial({
              color: color,
              transparent: true,
              opacity: 0.2,
              side: THREE.DoubleSide
            });
            
            const haloMesh = new THREE.Mesh(haloGeometry, haloMaterial);
            haloMesh.position.set(
              x + segmentWidth / 2, 
              0.1, 
              0
            );
            haloMesh.rotation.x = -Math.PI / 2; // 水平放置
            
            heatmapGroup.add(haloMesh);

            // 为最高峰添加顶部发光点
            if (intensity > 0.9) {
              const glowGeometry = new THREE.SphereGeometry(0.2, 8, 6);
              const glowMaterial = new THREE.MeshBasicMaterial({
                color: new THREE.Color(1, 1, 0.8),
                transparent: true,
                opacity: 0.8
              });
              
              const glowMesh = new THREE.Mesh(glowGeometry, glowMaterial);
              glowMesh.position.set(
                x + segmentWidth / 2, 
                height + 0.2, 
                0
              );
              
              heatmapGroup.add(glowMesh);
            }
          }
        });

        // 将组添加到场景
        this._heatmapMesh = heatmapGroup;
        this._scene.add(this._heatmapMesh);
        
        console.log('3D山峰热力图更新成功，包含', intervals.length, '个数据段');
      } catch (error) {
        console.error('更新3D热力图失败:', error);
      }
    },

    getHeatColor3D(intensity) {
      // 返回THREE.Color对象，使用更丰富的山峰色彩
      if (intensity === 0) return new THREE.Color(0.9, 0.9, 0.9); // 灰白色
      
      // 创建从蓝色(低热度) -> 绿色(中等热度) -> 黄色(高热度) -> 红色(极高热度)的渐变
      if (intensity < 0.2) {
        // 深蓝到浅蓝
        const t = intensity * 5;
        return new THREE.Color(0.1 + t * 0.3, 0.3 + t * 0.5, 0.8 + t * 0.2);
      } else if (intensity < 0.4) {
        // 蓝色到青色
        const t = (intensity - 0.2) * 5;
        return new THREE.Color(0.1 + t * 0.2, 0.6 + t * 0.4, 0.8);
      } else if (intensity < 0.6) {
        // 青色到绿色
        const t = (intensity - 0.4) * 5;
        return new THREE.Color(0.1 - t * 0.1, 0.8 + t * 0.2, 0.8 - t * 0.8);
      } else if (intensity < 0.8) {
        // 绿色到黄色
        const t = (intensity - 0.6) * 5;
        return new THREE.Color(0.2 + t * 0.8, 0.9, 0.1 - t * 0.1);
      } else {
        // 黄色到红色
        const t = (intensity - 0.8) * 5;
        return new THREE.Color(1, 0.9 - t * 0.6, 0.1 - t * 0.1);
      }
    },

    animate() {
      try {
        if (!this._renderer || !this._scene || !this._camera) {
          return;
        }

        this._animationFrame = requestAnimationFrame(this.animate);

        // 自动旋转
        if (this.autoRotate && this._camera) {
          const time = Date.now() * 0.0005;
          this._camera.position.x = Math.cos(time) * 25;
          this._camera.position.z = Math.sin(time) * 25;
          this._camera.lookAt(0, 0, 0);
        }

        // 安全地渲染场景
        this._renderer.render(this._scene, this._camera);
      } catch (error) {
        console.error('渲染循环错误:', error);
        // 停止动画循环以防止无限错误
        if (this._animationFrame) {
          cancelAnimationFrame(this._animationFrame);
          this._animationFrame = null;
        }
      }
    },

    switchView(view) {
      // 如果尝试切换到3D但Three.js未加载，则拒绝
      if (view === '3d' && !this.threejsLoaded) {
        console.warn('Three.js 未加载，无法切换到3D视图');
        return;
      }

      this.activeView = view;
      if (view === '3d') {
        this.$nextTick(() => {
          try {
            // 如果场景不存在，初始化
            if (!this._scene) {
              this.init3DScene();
            }
            
            // 如果场景存在且有数据，更新热力图
            if (this._scene && this.timelineData.intervals.length > 0) {
              this.updateHeatmap3D();
            } else {
              // 重新获取数据
              this.fetchHeatmapData();
            }
          } catch (error) {
            console.error('切换到3D视图失败:', error);
            this.error = '3D视图初始化失败: ' + error.message;
            // 回退到2D视图
            this.activeView = '2d';
          }
        });
      }
    },

    refreshHeatmap() {
      this.fetchHeatmapData();
    },

    resetCamera() {
      if (this._spherical && this._camera) {
        // 重置到默认位置
        this._spherical.radius = 25;
        this._spherical.phi = Math.PI * 0.3;
        this._spherical.theta = 0;
        this.updateCameraPosition();
      }
    },

    toggleAutoRotate() {
      this.autoRotate = !this.autoRotate;
    },

    updateHeightScale() {
      this.updateHeatmap3D();
    },

    handleWheel(event) {
      event.preventDefault();
      if (this._spherical) {
        const delta = event.deltaY * 0.05;
        this._spherical.radius = Math.max(5, Math.min(50, this._spherical.radius + delta));
        this.updateCameraPosition();
      }
    },

    onWindowResize() {
      if (!this._renderer || !this._camera || !this.$refs.heatmap3DContainer) return;

      const container = this.$refs.heatmap3DContainer;
      const width = container.clientWidth;
      const height = 400;

      this._camera.aspect = width / height;
      this._camera.updateProjectionMatrix();
      this._renderer.setSize(width, height);
    },

    cleanup3DScene() {
      try {
        console.log('开始清理3D场景');

        // 停止动画循环
        if (this._animationFrame) {
          cancelAnimationFrame(this._animationFrame);
          this._animationFrame = null;
        }

        // 清理事件监听器
        if (this._cleanupEvents) {
          this._cleanupEvents();
          this._cleanupEvents = null;
        }

        // 清理热力图网格
        if (this._heatmapMesh) {
          if (this._scene) {
            this._scene.remove(this._heatmapMesh);
          }
          
          // 递归清理Three.js对象
          const cleanupObject = (obj) => {
            if (obj.geometry) {
              obj.geometry.dispose();
            }
            if (obj.material) {
              if (Array.isArray(obj.material)) {
                obj.material.forEach(mat => {
                  if (mat.map) mat.map.dispose();
                  if (mat.lightMap) mat.lightMap.dispose();
                  if (mat.bumpMap) mat.bumpMap.dispose();
                  if (mat.normalMap) mat.normalMap.dispose();
                  if (mat.specularMap) mat.specularMap.dispose();
                  mat.dispose();
                });
              } else {
                if (obj.material.map) obj.material.map.dispose();
                if (obj.material.lightMap) obj.material.lightMap.dispose();
                if (obj.material.bumpMap) obj.material.bumpMap.dispose();
                if (obj.material.normalMap) obj.material.normalMap.dispose();
                if (obj.material.specularMap) obj.material.specularMap.dispose();
                obj.material.dispose();
              }
            }
          };

          if (this._heatmapMesh.type === 'Group') {
            this._heatmapMesh.traverse(cleanupObject);
          } else {
            cleanupObject(this._heatmapMesh);
          }
          
          this._heatmapMesh = null;
        }

        // 清理场景中的所有对象
        if (this._scene) {
          // 获取所有子对象的副本，避免在遍历时修改数组
          const children = [...this._scene.children];
          children.forEach(child => {
            this._scene.remove(child);
            if (child.geometry) {
              child.geometry.dispose();
            }
            if (child.material) {
              if (Array.isArray(child.material)) {
                child.material.forEach(mat => mat.dispose());
              } else {
                child.material.dispose();
              }
            }
          });
          this._scene = null;
        }

        // 清理渲染器
        if (this._renderer) {
          if (this._renderer.domElement && this.$refs.heatmap3DContainer) {
            try {
              this.$refs.heatmap3DContainer.removeChild(this._renderer.domElement);
            } catch (e) {
              console.warn('移除渲染器DOM元素失败:', e);
            }
          }
          
          // 清理渲染器资源
          this._renderer.dispose();
          if (this._renderer.forceContextLoss) {
            this._renderer.forceContextLoss();
          }
          this._renderer = null;
        }

        // 清理相机和其他对象
        this._camera = null;
        this._cameraTarget = null;
        this._spherical = null;

        // 移除全局事件监听器
        window.removeEventListener('resize', this.onWindowResize);
        
        console.log('3D场景清理完成');
      } catch (error) {
        console.error('3D场景清理失败:', error);
      }
    },

    // 创建山峰几何体的方法
    createMountainGeometry(baseRadius, height, segments = 12) {
      const geometry = new THREE.BufferGeometry();
      const vertices = [];
      const indices = [];

      // 创建底部圆形顶点
      vertices.push(0, 0, 0); // 中心点
      for (let i = 0; i <= segments; i++) {
        const angle = (i / segments) * Math.PI * 2;
        const x = Math.cos(angle) * baseRadius;
        const z = Math.sin(angle) * baseRadius;
        vertices.push(x, 0, z);
      }

      // 创建山峰顶点
      const peakHeight = height;
      vertices.push(0, peakHeight, 0);

      // 创建底面三角形
      for (let i = 0; i < segments; i++) {
        indices.push(0, i + 1, i + 2);
      }

      // 创建侧面三角形
      const peakIndex = segments + 2;
      for (let i = 0; i < segments; i++) {
        const current = i + 1;
        const next = (i + 1) % segments + 1;
        indices.push(current, next, peakIndex);
      }

      // 计算法向量
      const positionAttribute = new THREE.Float32BufferAttribute(vertices, 3);
      geometry.setIndex(indices);
      geometry.setAttribute('position', positionAttribute);
      geometry.computeVertexNormals();

      return geometry;
    },

    // 原有方法...
    getHeatColor(intensity) {
      if (intensity === 0) return '#f0f0f0';
      const colors = ['#e3f2fd', '#81c784', '#ffb74d', '#ff8a65', '#e57373'];
      const index = Math.min(Math.floor(intensity * colors.length), colors.length - 1);
      return colors[index];
    },

    seekToTime(time) {
      if (this.$refs.videoRef) {
        this.$refs.videoRef.currentTime = time;
      }
    },

    formatTime(seconds) {
      if (!seconds || seconds < 0) return '00:00';
      const mins = Math.floor(seconds / 60);
      const secs = Math.floor(seconds % 60);
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    },

    // 原有的视频事件处理方法保持不变...
    handleLoadStart() {
      this.loading = true;
      this.error = null;
    },
    
    handleLoadedData() {
      this.loading = false;
      this.loaded = true;
      if (this.$refs.videoRef) {
        this.videoDuration = this.$refs.videoRef.duration;
        this.segmentStart = this.$refs.videoRef.currentTime;
        this.lastWatchedSecond = Math.floor(this.$refs.videoRef.currentTime);
      }
    },
    
    handleVideoError(e) {
      this.loading = false;
      let errorMessage = '视频加载失败';
      if (e.target?.error) {
        switch (e.target.error.code) {
          case 1: errorMessage = '视频加载被用户中止'; break;
          case 2: errorMessage = '网络错误或认证失败，无法加载视频'; break;
          case 3: errorMessage = '视频解码失败，文件可能损坏或格式不支持'; break;
          case 4: errorMessage = '不支持的视频格式或无法访问视频源'; break;
          default: errorMessage = `视频错误 (代码: ${e.target.error.code})`;
        }
      }
      this.error = errorMessage;
    },
    
    handleCanPlay() {
      this.loading = false;
    },
    
    handlePlay() {},
    
    handlePause() {
      if (this.loaded && this.$refs.videoRef) {
        const current = this.$refs.videoRef.currentTime;
        if (current > this.segmentStart) {
          this.segments.push({
            start: this.segmentStart,
            end: current,
            count: 1
          });
          this.segmentStart = current;
        }
      }
      this.saveProgress();
    },
    
    handleTimeUpdate(e) {
      if (!this.loaded) return;
      
      const current = e.target.currentTime;
      this.lastPosition = current;
      this.currentTime = current;
      
      const curSec = Math.floor(current);
      if (curSec > this.lastWatchedSecond) {
        this.totalWatched += (curSec - this.lastWatchedSecond);
        this.lastWatchedSecond = curSec;
        
        if (this.videoDuration > 0 && 
            this.totalWatched >= this.videoDuration * 0.9 &&
            this.taskId) {
          this.markTaskAsCompleted();
        }
      }
      
      if (current - this.segmentStart >= 1) {
        this.segments.push({
          start: this.segmentStart,
          end: current,
          count: 1
        });
        this.segmentStart = current;
      }
    },

    handleVideoEnd() {
      if (this.loaded && this.$refs.videoRef) {
        const current = this.$refs.videoRef.currentTime;
        if (current > this.segmentStart) {
          this.segments.push({
            start: this.segmentStart,
            end: current,
            count: 1
          });
          this.segmentStart = current;
        }
      }
      this.saveProgress();
    },
    
    async retryLoad() {
      this.error = null;
      this.loading = true;
      this.videoUrl = '';
      await this.fetchVideoUrl();
    },

    async markTaskAsCompleted() {
      try {
        await api.put(`/submissions/complete/${this.taskId}`);
        console.log('任务已自动标记为完成');
      } catch (err) {
        console.error('自动标记任务失败:', err);
      }
    }
  }
};
</script>

<style scoped>
.video-player {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

video {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

/* 3D热力图样式 */
.heatmap-section {
  margin-top: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.heatmap-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.heatmap-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.heatmap-controls {
  display: flex;
  gap: 10px;
  align-items: center;
}

.tab-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.tab-btn.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.tab-btn:hover:not(.active) {
  background: #f8f9fa;
}

.refresh-btn {
  padding: 8px 12px;
  border: 1px solid #28a745;
  background: white;
  color: #28a745;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.refresh-btn:hover:not(:disabled) {
  background: #28a745;
  color: white;
}

.control-panel {
  background: white;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 20px;
  border: 1px solid #e9ecef;
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  align-items: center;
}

.control-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.control-group label {
  font-weight: 500;
  color: #333;
  white-space: nowrap;
}

.control-btn {
  padding: 6px 12px;
  border: 1px solid #007bff;
  background: white;
  color: #007bff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.control-btn:hover {
  background: #007bff;
  color: white;
}

.slider {
  width: 80px;
}

.heatmap-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 40px;
  color: #666;
}

.heatmap-3d-container {
  width: 100%;
  height: 400px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 6px;
  border: 1px solid #e9ecef;
  overflow: hidden;
  cursor: grab;
  position: relative;
}

.heatmap-3d-container:active {
  cursor: grabbing;
}

.legend {
  margin-top: 15px;
  padding: 15px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.legend-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
}

.legend-gradient {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 3px;
  border: 1px solid #ddd;
}

.legend-text {
  font-size: 14px;
  color: #666;
}

.legend-note {
  font-size: 12px;
  color: #999;
  font-style: italic;
}

/* 2D热力图样式（保留原有样式）*/
.heatmap-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.stat-value {
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

.heatmap-visualization {
  background: white;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  padding: 20px;
}

.heatmap-timeline {
  position: relative;
  height: 80px;
}

.time-scale {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 20px;
}

.time-mark {
  position: absolute;
  font-size: 11px;
  color: #666;
  transform: translateX(-50%);
}

.heatmap-bar {
  position: absolute;
  top: 30px;
  left: 0;
  right: 0;
  height: 30px;
  background: #f8f9fa;
  border-radius: 4px;
  overflow: hidden;
}

.heat-segment {
  position: absolute;
  height: 100%;
  cursor: pointer;
  transition: all 0.3s;
  border-right: 1px solid rgba(255,255,255,0.5);
}

.heat-segment:hover {
  opacity: 1 !important;
  transform: scaleY(1.2);
  z-index: 2;
}

.current-position {
  position: absolute;
  top: 25px;
  width: 2px;
  height: 40px;
  background: #ff4757;
  z-index: 3;
  transform: translateX(-50%);
}

.heatmap-empty {
  text-align: center;
  padding: 40px;
  color: #666;
}

.empty-hint {
  font-size: 14px;
  color: #999;
  margin-top: 8px;
}

/* 原有样式保持不变 */
.loading {
  text-align: center;
  padding: 40px 20px;
  color: #666;
  font-size: 16px;
}

.loading-spinner {
  display: inline-block;
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

.loading-spinner.small {
  width: 20px;
  height: 20px;
  border-width: 2px;
  margin: 0;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-details {
  font-size: 14px;
  color: #999;
  margin-top: 10px;
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

.error {
  text-align: center;
  padding: 30px 20px;
  color: #c62828;
  font-size: 16px;
  background-color: #ffebee;
  border: 1px solid #ffcdd2;
  border-radius: 8px;
  margin-top: 20px;
}

.error h3 {
  margin: 0 0 15px 0;
  color: #c62828;
}

.error-details {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 4px;
  font-size: 14px;
  text-align: left;
}

.error-details p {
  margin: 8px 0;
  word-break: break-all;
  color: #666;
}

.retry-button {
  display: inline-block;
  margin-top: 15px;
  padding: 10px 20px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.retry-button:hover {
  background-color: #2980b9;
}

.test-button {
  display: inline-block;
  margin-top: 15px;
  margin-left: 10px;
  padding: 10px 20px;
  background-color: #f39c12;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  font-size: 14px;
  transition: background-color 0.3s;
}

.test-button:hover {
  background-color: #e67e22;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .heatmap-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .heatmap-controls {
    justify-content: center;
  }
  
  .control-panel {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .control-group {
    justify-content: space-between;
  }
  
  .heatmap-stats {
    flex-direction: column;
    gap: 10px;
  }
  
  .legend-gradient {
    flex-wrap: wrap;
    gap: 10px;
  }
}
</style>