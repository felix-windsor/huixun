<template>
  <form
    :class="['form', className]"
    @submit.prevent="handleSubmit"
    novalidate
  >
    <slot
      :values="formState.values"
      :errors="formState.errors"
      :isValid="formState.isValid"
      :isSubmitting="formState.isSubmitting"
      :handleChange="handleChange"
      :handleBlur="handleBlur"
      :validateField="validateField"
    />
  </form>
</template>

<script setup lang="ts">
import { reactive, watch, provide } from 'vue'

export interface FormProps<T extends Record<string, any>> {
  initialValues: T
  validationSchema?: ValidationSchema<T>
  onSubmit: (values: T) => Promise<void> | void
  className?: string
  validateOnMount?: boolean
  validateOnChange?: boolean
  validateOnBlur?: boolean
}

export interface ValidationRule {
  required?: boolean
  minLength?: number
  maxLength?: number
  pattern?: RegExp | string
  custom?: (value: any, values?: any) => boolean | string
  message?: string
}

export type ValidationSchema<T> = {
  [K in keyof T]?: ValidationRule | ValidationRule[]
}

export interface FormState<T> {
  values: T
  errors: Partial<Record<keyof T, string>>
  touched: Partial<Record<keyof T, boolean>>
  isValid: boolean
  isSubmitting: boolean
  isDirty: boolean
}

const props = withDefaults(defineProps<FormProps<any>>(), {
  validateOnMount: false,
  validateOnChange: true,
  validateOnBlur: true
})

const emit = defineEmits<{
  submit: [values: any]
  validation: [isValid: boolean, errors: any]
}>()

const formState = reactive<FormState<any>>({
  values: { ...props.initialValues },
  errors: {},
  touched: {},
  isValid: true,
  isSubmitting: false,
  isDirty: false
})

// 提供表单上下文给子组件
provide('form-context', {
  formState,
  handleChange,
  handleBlur,
  validateField,
  setFieldValue,
  setFieldError
})

function validateField(name: string, value?: any): string[] {
  const fieldSchema = props.validationSchema?.[name]
  if (!fieldSchema) return []

  const fieldValue = value ?? formState.values[name]
  const rules = Array.isArray(fieldSchema) ? fieldSchema : [fieldSchema]
  const errors: string[] = []

  rules.forEach(rule => {
    if (rule.required && (fieldValue === undefined || fieldValue === null || fieldValue === '')) {
      errors.push(rule.message || '此字段为必填项')
      return
    }

    if (rule.minLength && typeof fieldValue === 'string' && fieldValue.length < rule.minLength) {
      errors.push(rule.message || `最少需要 ${rule.minLength} 个字符`)
      return
    }

    if (rule.maxLength && typeof fieldValue === 'string' && fieldValue.length > rule.maxLength) {
      errors.push(rule.message || `最多允许 ${rule.maxLength} 个字符`)
      return
    }

    if (rule.pattern && typeof fieldValue === 'string') {
      const pattern = typeof rule.pattern === 'string' ? new RegExp(rule.pattern) : rule.pattern
      if (!pattern.test(fieldValue)) {
        errors.push(rule.message || '输入格式不正确')
        return
      }
    }

    if (rule.custom) {
      const result = rule.custom(fieldValue, formState.values)
      if (result !== true) {
        errors.push(typeof result === 'string' ? result : rule.message || '验证失败')
        return
      }
    }
  })

  return errors
}

function validateForm(): boolean {
  if (!props.validationSchema) return true

  const errors: any = {}
  let isValid = true

  Object.keys(props.validationSchema).forEach(fieldName => {
    const fieldErrors = validateField(fieldName)
    if (fieldErrors.length > 0) {
      errors[fieldName] = fieldErrors[0]
      isValid = false
    }
  })

  formState.errors = errors
  formState.isValid = isValid

  emit('validation', isValid, errors)
  return isValid
}

function handleChange(name: string, value: any) {
  const oldValue = formState.values[name]
  formState.values[name] = value
  
  if (oldValue !== value) {
    formState.isDirty = true
  }

  if (props.validateOnChange && formState.touched[name]) {
    const errors = validateField(name, value)
    formState.errors[name] = errors[0] || ''
    formState.isValid = Object.values(formState.errors).every(error => !error)
  }
}

function handleBlur(name: string) {
  formState.touched[name] = true

  if (props.validateOnBlur) {
    const errors = validateField(name)
    formState.errors[name] = errors[0] || ''
    formState.isValid = Object.values(formState.errors).every(error => !error)
  }
}

function setFieldValue(name: string, value: any) {
  handleChange(name, value)
}

function setFieldError(name: string, error?: string) {
  formState.errors[name] = error || ''
  formState.isValid = Object.values(formState.errors).every(error => !error)
}

async function handleSubmit() {
  formState.isSubmitting = true

  try {
    // 提交前验证整个表单
    const isValid = validateForm()
    
    if (!isValid) {
      formState.isSubmitting = false
      return
    }

    // 执行提交逻辑
    await props.onSubmit({ ...formState.values })
    
    // 提交成功后重置状态
    formState.isDirty = false
    formState.touched = {}
    
    emit('submit', { ...formState.values })
  } catch (error) {
    console.error('表单提交失败:', error)
    // 可以在这里处理提交错误，比如显示错误消息
  } finally {
    formState.isSubmitting = false
  }
}

function resetForm() {
  formState.values = { ...props.initialValues }
  formState.errors = {}
  formState.touched = {}
  formState.isValid = true
  formState.isSubmitting = false
  formState.isDirty = false
}

// 监听初始值变化
watch(() => props.initialValues, (newValues) => {
  formState.values = { ...newValues }
  formState.isDirty = false
  formState.touched = {}
  
  if (props.validateOnMount) {
    validateForm()
  }
}, { deep: true })

// 初始化时验证
if (props.validateOnMount) {
  validateForm()
}

// 暴露方法给父组件
defineExpose({
  validate: validateForm,
  reset: resetForm,
  setFieldValue,
  setFieldError,
  formState
})
</script>

<style scoped>
.form {
  width: 100%;
}
</style>