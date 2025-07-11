<template>
  <!-- 仅保留路由视图，所有页面内容将在router-view中渲染 -->
  <router-view />
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
/* 可选的全局基础样式 */
body {
  margin: 0;
  font-family: 'Arial', sans-serif;
  background-color: #f5f7fa;
}
</style>