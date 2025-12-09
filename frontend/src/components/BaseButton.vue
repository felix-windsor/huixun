<template>
  <button
    :class="buttonClasses"
    :disabled="disabled || loading"
    :type="type"
    :aria-busy="loading"
    :aria-disabled="disabled"
    @click="handleClick"
    @keydown="handleKeyDown"
  >
    <span class="btn-content">
      <svg
        v-if="loading"
        class="btn-spinner"
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <circle
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-dasharray="32"
          stroke-dashoffset="32"
        />
      </svg>
      <slot />
    </span>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

export interface ButtonProps {
  variant?: 'primary' | 'secondary' | 'danger' | 'text'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  disabled?: boolean
  block?: boolean
  type?: 'button' | 'submit' | 'reset'
}

export interface ButtonEmits {
  click: [event: Event]
}

const props = withDefaults(defineProps<ButtonProps>(), {
  variant: 'primary',
  size: 'md',
  loading: false,
  disabled: false,
  block: false,
  type: 'button'
})

const emit = defineEmits<ButtonEmits>()

const buttonClasses = computed(() => [
  'btn',
  `btn-${props.variant}`,
  `btn-${props.size}`,
  {
    'btn-loading': props.loading,
    'btn-block': props.block
  }
])

const handleClick = (event: Event) => {
  if (props.loading || props.disabled) {
    event.preventDefault()
    return
  }
  emit('click', event)
}

const handleKeyDown = (event: KeyboardEvent) => {
  // 支持空格键激活按钮
  if (event.key === ' ' && !props.loading && !props.disabled) {
    event.preventDefault()
    emit('click', event as any)
  }
}
</script>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  line-height: var(--line-height-tight);
  text-decoration: none;
  cursor: pointer;
  transition: all var(--transition-fast);
  user-select: none;
  white-space: nowrap;
  vertical-align: middle;
  position: relative;
  overflow: hidden;
  background: none;
}

.btn:focus {
  outline: 2px solid var(--primary-500);
  outline-offset: 2px;
}

/* Primary variant */
.btn-primary {
  background-color: var(--primary-600);
  color: white;
  border-color: var(--primary-600);
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--primary-700);
  border-color: var(--primary-700);
  box-shadow: var(--shadow-md);
}

.btn-primary:active:not(:disabled) {
  background-color: var(--primary-800);
  border-color: var(--primary-800);
  transform: translateY(1px);
}

/* Secondary variant */
.btn-secondary {
  background-color: white;
  color: var(--gray-700);
  border-color: var(--gray-300);
}

.btn-secondary:hover:not(:disabled) {
  background-color: var(--gray-50);
  border-color: var(--gray-400);
  color: var(--gray-900);
}

.btn-secondary:active:not(:disabled) {
  background-color: var(--gray-100);
  transform: translateY(1px);
}

/* Danger variant */
.btn-danger {
  background-color: var(--error);
  color: white;
  border-color: var(--error);
}

.btn-danger:hover:not(:disabled) {
  background-color: #dc2626;
  border-color: #dc2626;
}

.btn-danger:active:not(:disabled) {
  background-color: #b91c1c;
  transform: translateY(1px);
}

/* Text variant */
.btn-text {
  background-color: transparent;
  color: var(--primary-600);
  border-color: transparent;
  padding: var(--spacing-xs) var(--spacing-sm);
}

.btn-text:hover:not(:disabled) {
  background-color: var(--primary-50);
  color: var(--primary-700);
}

.btn-text:active:not(:disabled) {
  background-color: var(--primary-100);
}

/* Sizes */
.btn-sm {
  padding: var(--spacing-xs) var(--spacing-sm);
  font-size: var(--font-size-xs);
  border-radius: var(--radius-sm);
}

.btn-md {
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: var(--font-size-sm);
  border-radius: var(--radius-md);
}

.btn-lg {
  padding: var(--spacing-md) var(--spacing-lg);
  font-size: var(--font-size-base);
  border-radius: var(--radius-lg);
}

/* States */
.btn:disabled,
.btn-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-loading {
  color: transparent;
  pointer-events: none;
}

.btn-block {
  display: flex;
  width: 100%;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.btn-spinner {
  width: 16px;
  height: 16px;
  animation: spin 1s linear infinite;
  color: inherit;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>