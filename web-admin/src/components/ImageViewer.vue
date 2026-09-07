<template>
  <teleport to="body">
    <div v-if="visible" class="img-viewer-overlay" @click="close">
      <img :src="src" class="img-viewer-content" @click.stop />
      <span class="img-viewer-close" @click="close">✕</span>
    </div>
  </teleport>
</template>

<script setup lang="ts">
defineProps<{ visible: boolean; src: string }>()
const emit = defineEmits<{ (e: 'close'): void }>()
const close = () => emit('close')
</script>

<style scoped>
.img-viewer-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.85); z-index: 99999;
  display: flex; align-items: center; justify-content: center;
}
.img-viewer-content { max-width: 90vw; max-height: 90vh; object-fit: contain; }
.img-viewer-close {
  position: fixed; top: 20px; right: 30px; color: #fff;
  font-size: 36px; cursor: pointer; z-index: 100000;
}
</style>
