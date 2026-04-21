<template>
  <div v-if="open" class="modal-wrap" role="dialog" aria-modal="true">
    <div class="overlay" @click="onClose" />
    <div class="modal">
      <div class="modal-header">
        <div class="modal-title">{{ title }}</div>
        <button class="modal-close" type="button" @click="onClose">关闭</button>
      </div>
      <div class="modal-body">
        <slot />
      </div>
      <div class="modal-footer">
        <button class="btn" type="button" @click="onClose">取消</button>
        <button class="btn btn-primary" type="button" @click="onConfirm">
          {{ confirmText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'CommonModal',
  props: {
    open: { type: Boolean, required: true },
    title: { type: String, default: '' },
    confirmText: { type: String, default: '确认' },
  },
  emits: ['close', 'confirm'],
  setup(props, { emit }) {
    const onClose = () => emit('close')
    const onConfirm = () => emit('confirm')
    return { onClose, onConfirm }
  },
})
</script>

<style lang="scss" scoped>
.modal-wrap {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
}
.overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
}
.modal {
  position: relative;
  width: min(720px, calc(100vw - 24px));
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
}
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #e5e7eb;
}
.modal-title {
  font-weight: 700;
}
.modal-close {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 10px;
  padding: 8px 10px;
  cursor: pointer;
  font-size: 12px;
}
.modal-body {
  padding: 14px 16px;
}
.modal-footer {
  padding: 14px 16px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.btn {
  border: 1px solid #e5e7eb;
  background: #fff;
  border-radius: 10px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 12px;
}
.btn-primary {
  background: #0369a1;
  color: #fff;
  border-color: #0369a1;
}
</style>

