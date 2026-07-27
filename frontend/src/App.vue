<template>
  <router-view />
</template>

<script setup lang="ts">
// 根组件 - 所有页面通过路由渲染
</script>

<style>
/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f7fa;
}

a {
  text-decoration: none;
  color: inherit;
}
</style>
