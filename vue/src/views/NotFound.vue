<!-- src/views/NotFound.vue -->
<template>
  <div style="text-align: center; padding: 60px;">
    <h1>404 - 页面不存在</h1>
    <p>你访问的页面不存在或已被删除。</p>
    <router-link to="/">返回首页</router-link>
  </div>
</template>

<script>
export default {
  name: 'NotFound'
}
</script>
