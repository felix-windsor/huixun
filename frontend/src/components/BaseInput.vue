<template>
  <div class="form-group">
    <label v-if="label" :for="inputId" class="form-label">
      {{ label }}
      <span v-if="required" class="text-error" aria-label="必填">*</span>
    </label>
    <div class="input-wrapper">
      <input
        :id="inputId"
        ref="inputRef"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :required="required"
        :autocomplete="autocomplete"
        :class="inputClasses"
        :aria-invalid="hasError"
        :aria-describedby="errorMessage ? errorId : helpText ? helpId : undefined"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
        @keydown="handleKeyDown"
      />
      <div v-if="$slots.suffix" class="input-suffix">
        <slot name="suffix" />
      </div>
    </div>
    <div v-if="errorMessage" :id="errorId" class="form-error" role="alert">
      <span class="error-icon">⚠</span>
      {{ errorMessage }}
    </div>
    <div v-else-if="helpText" :id="helpId" class="form-help">
      {{ helpText }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, nextTick } from 'vue'

export interface InputProps {
  modelValue: string
  type?: 'text' | 'password' | 'email' | 'number' | 'tel' | 'url' | 'search'
  label?: string
  placeholder?: string
  disabled?: boolean
  readonly?: boolean
  required?: boolean
  autocomplete?: string
  error?: string
  success?: boolean
  helpText?: string
  validateOnInput?: boolean
  validateOnBlur?: boolean
  validationRules?: ValidationRule[]
  maxlength?: number
  minlength?: number
  pattern?: string | RegExp
}

export interface ValidationRule {
  type: 'required' | 'minLength' | 'maxLength' | 'pattern' | 'email' | 'custom'
  message: string
  validator?: (value: string) => boolean
  value?: number | string | RegExp
}

export interface InputEmits {
  'update:modelValue': [value: string]
  blur: [event: FocusEvent]
  focus: [event: FocusEvent]
  input: [value: string, event: Event]
  validation: [isValid: boolean, errors: string[]]
}

const props = withDefaults(defineProps<InputProps>(), {
  type: 'text',
  disabled: false,
  readonly: false,
  required: false,
  validateOnInput: false,
  validateOnBlur: true,
  success: false
})

const emit = defineEmits<InputEmits>()

const inputRef = ref<HTMLInputElement>()
const inputId = computed(() => `input-${Math.random().toString(36).slice(2, 9)}`)
const errorId = computed(() => `error-${inputId.value}`)
const helpId = computed(() => `help-${inputId.value}`)

const localError = ref('')
const isTouched = ref(false)

const hasError = computed(() => !!props.error || !!localError.value)

const inputClasses = computed(() => [
  'form-input',
  {
    'form-input-error': hasError.value,
    'form-input-success': props.success && !hasError.value,
    'form-input-disabled': props.disabled
  }
])

const validate = (value: string): string[] => {
  const errors: string[] = []

  if (props.required && !value.trim()) {
    errors.push('此字段为必填项')
  }

  if (props.minlength && value.length < props.minlength) {
    errors.push(`最少需要 ${props.minlength} 个字符`)
  }

  if (props.maxlength && value.length > props.maxlength) {
    errors.push(`最多允许 ${props.maxlength} 个字符`)
  }

  if (props.type === 'email' && value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    errors.push('请输入有效的邮箱地址')
  }

  if (props.pattern && value) {
    const pattern = typeof props.pattern === 'string' ? new RegExp(props.pattern) : props.pattern
    if (!pattern.test(value)) {
      errors.push('输入格式不正确')
    }
  }

  if (props.validationRules) {
    props.validationRules.forEach(rule => {
      if (rule.type === 'custom' && rule.validator && !rule.validator(value)) {
        errors.push(rule.message)
      }
    })
  }

  return errors
}

const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  const value = target.value

  emit('update:modelValue', value)
  emit('input', value, event)

  if (props.validateOnInput) {
    const errors = validate(value)
    localError.value = errors[0] || ''
    emit('validation', errors.length === 0, errors)
  }
}

const handleBlur = (event: FocusEvent) => {
  isTouched.value = true
  emit('blur', event)

  if (props.validateOnBlur) {
    const errors = validate(props.modelValue)
    localError.value = errors[0] || ''
    emit('validation', errors.length === 0, errors)
  }
}

const handleFocus = (event: FocusEvent) => {
  emit('focus', event)
}

const handleKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && props.type === 'search') {
    // 搜索类型输入框回车触发搜索
    event.preventDefault()
  }
}

const focus = () => {
  nextTick(() => {
    inputRef.value?.focus()
  })
}

const blur = () => {
  inputRef.value?.blur()
}

const select = () => {
  inputRef.value?.select()
}

watch(() => props.error, (newError) => {
  localError.value = newError || ''
})

// 暴露方法给父组件
defineExpose({
  focus,
  blur,
  select,
  validate: () => validate(props.modelValue)
})
</script>

<style scoped>
.form-group {
  margin-bottom: var(--spacing-md);
}

.form-label {
  display: block;
  margin-bottom: var(--spacing-xs);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--gray-700);
}

.text-error {
  color: var(--error);
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-input {
  flex: 1;
  display: block;
  width: 100%;
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--gray-300);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  line-height: var(--line-height-normal);
  background-color: white;
  color: var(--gray-900);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.form-input::placeholder {
  color: var(--gray-400);
}

.form-input:hover:not(:disabled) {
  border-color: var(--gray-400);
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-500);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input:disabled {
  background-color: var(--gray-50);
  color: var(--gray-500);
  cursor: not-allowed;
}

.form-input-error {
  border-color: var(--error);
}

.form-input-error:focus {
  border-color: var(--error);
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.form-input-success {
  border-color: var(--success);
}

.form-input-success:focus {
  border-color: var(--success);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.input-suffix {
  position: absolute;
  right: var(--spacing-sm);
  display: flex;
  align-items: center;
  color: var(--gray-400);
}

.form-help {
  margin-top: var(--spacing-xs);
  font-size: var(--font-size-xs);
  color: var(--gray-500);
}

.form-error {
  margin-top: var(--spacing-xs);
  font-size: var(--font-size-xs);
  color: var(--error);
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.error-icon {
  font-size: var(--font-size-sm);
}
</style>