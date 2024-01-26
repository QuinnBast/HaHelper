<script setup>
import { RouterLink, RouterView } from 'vue-router'
import Menubar from 'primevue/menubar';
</script>

<template>
  <main>
    <Menubar :model="items" />
    <RouterView />
  </main>
</template>
