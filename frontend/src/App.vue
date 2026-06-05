<script setup lang="ts">
// 根组件：仅作为路由出口容器
</script>

<template>
  <div class="app-root">
    <router-view v-slot="{ Component }">
      <component :is="Component" />
    </router-view>
  </div>
</template>

<style scoped>
.app-root {
  min-height: 100vh;
  width: 100%;
  display: flex;
  flex-direction: column;
}
</style>
