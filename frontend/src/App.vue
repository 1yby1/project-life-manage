<template>
  <MainLayout />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import MainLayout from './layouts/MainLayout.vue'

export default defineComponent({
  name: 'App',
  components: {
    MainLayout,
  },
})
</script>

<style lang="scss">
/* 基础样式由 MainLayout 接管 */
</style>
