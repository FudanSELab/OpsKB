<script setup>
import MainRouter from './views/MainRouter.vue'
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="flex flex-col min-h-screen">
    <MainRouter />
    <RouterView class="flex-1 min-h-0" />
  </div>
</template>

<style scoped></style>
