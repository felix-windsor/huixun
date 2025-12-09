<template>
  <div class="demo-container">
    <h1>UI 组件库演示</h1>
    
    <!-- Button 演示 -->
    <section class="demo-section">
      <h2>按钮组件 (BaseButton)</h2>
      
      <div class="demo-group">
        <h3>基础变体</h3>
        <div class="button-group">
          <BaseButton variant="primary" @click="handleClick('primary')">主要按钮</BaseButton>
          <BaseButton variant="secondary" @click="handleClick('secondary')">次要按钮</BaseButton>
          <BaseButton variant="danger" @click="handleClick('danger')">危险按钮</BaseButton>
          <BaseButton variant="text" @click="handleClick('text')">文字按钮</BaseButton>
        </div>
      </div>

      <div class="demo-group">
        <h3>尺寸大小</h3>
        <div class="button-group">
          <BaseButton size="sm" variant="primary">小尺寸</BaseButton>
          <BaseButton size="md" variant="primary">中尺寸</BaseButton>
          <BaseButton size="lg" variant="primary">大尺寸</BaseButton>
        </div>
      </div>

      <div class="demo-group">
        <h3>状态演示</h3>
        <div class="button-group">
          <BaseButton variant="primary" :loading="loadingStates.primary" @click="toggleLoading('primary')">
            加载状态
          </BaseButton>
          <BaseButton variant="secondary" disabled>禁用状态</BaseButton>
          <BaseButton variant="primary" block>块级按钮</BaseButton>
        </div>
      </div>
    </section>

    <!-- Input 演示 -->
    <section class="demo-section">
      <h2>输入框组件 (BaseInput)</h2>
      
      <div class="demo-group">
        <h3>基础输入</h3>
        <BaseInput
          v-model="formData.username"
          label="用户名"
          placeholder="请输入用户名"
          help-text="用户名长度为3-20个字符"
          required
        />
      </div>

      <div class="demo-group">
        <h3>邮箱验证</h3>
        <BaseInput
          v-model="formData.email"
          type="email"
          label="邮箱地址"
          placeholder="请输入邮箱地址"
          required
          validate-on-blur
          :validation-rules="emailRules"
        />
      </div>

      <div class="demo-group">
        <h3>密码输入</h3>
        <BaseInput
          v-model="formData.password"
          type="password"
          label="密码"
          placeholder="请输入密码"
          required
          :minlength="6"
          :maxlength="20"
          validate-on-input
        />
      </div>

      <div class="demo-group">
        <h3>错误状态</h3>
        <BaseInput
          v-model="formData.errorField"
          label="错误演示"
          placeholder="输入内容触发错误"
          :error="errorMessage"
          validate-on-input
          @validation="handleValidation"
        />
      </div>

      <div class="demo-group">
        <h3>成功状态</h3>
        <BaseInput
          v-model="formData.successField"
          label="成功演示"
          placeholder="输入正确内容"
          :success="!!formData.successField && formData.successField.length >= 3"
        />
      </div>
    </section>

    <!-- Form 演示 -->
    <section class="demo-section">
      <h2>表单组件 (BaseForm)</h2>
      
      <BaseForm
        ref="formRef"
        :initial-values="formData"
        :validation-schema="formValidation"
        :validate-on-mount="false"
        :validate-on-change="true"
        :validate-on-blur="true"
        @submit="handleFormSubmit"
        @validation="handleFormValidation"
      >
        <template #default="{ values, errors, isValid, isSubmitting, handleChange, handleBlur }">
          <div class="form-demo">
            <BaseInput
              :model-value="values.name"
              label="姓名"
              placeholder="请输入姓名"
              required
              :error="errors.name"
              @update:model-value="val => handleChange('name', val)"
              @blur="() => handleBlur('name')"
            />

            <BaseInput
              :model-value="values.phone"
              type="tel"
              label="手机号"
              placeholder="请输入手机号"
              required
              :error="errors.phone"
              @update:model-value="val => handleChange('phone', val)"
              @blur="() => handleBlur('phone')"
            />

            <BaseInput
              :model-value="values.address"
              label="地址"
              placeholder="请输入地址"
              :error="errors.address"
              @update:model-value="val => handleChange('address', val)"
              @blur="() => handleBlur('address')"
            />

            <div class="form-actions">
              <BaseButton 
                type="submit" 
                variant="primary" 
                :loading="isSubmitting"
                :disabled="!isValid"
              >
                提交表单
              </BaseButton>
              <BaseButton 
                type="button" 
                variant="secondary"
                @click="resetForm"
              >
                重置
              </BaseButton>
            </div>

            <div v-if="formMessage" class="form-message" :class="formMessageType">
              {{ formMessage }}
            </div>
          </div>
        </template>
      </BaseForm>
    </section>

    <!-- 响应式演示 -->
    <section class="demo-section">
      <h2>响应式设计</h2>
      <div class="responsive-demo">
        <div class="responsive-card">
          <h3>自适应卡片</h3>
          <p>在不同屏幕尺寸下自动调整布局</p>
          <BaseButton variant="primary">操作按钮</BaseButton>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { BaseForm } from '@/components'

// 按钮演示数据
const loadingStates = reactive({
  primary: false,
  secondary: false,
  danger: false,
  text: false
})

const handleClick = (type: string) => {
  console.log(`点击了 ${type} 按钮`)
}

const toggleLoading = (type: string) => {
  loadingStates[type as keyof typeof loadingStates] = true
  setTimeout(() => {
    loadingStates[type as keyof typeof loadingStates] = false
  }, 2000)
}

// 输入框演示数据
const formData = reactive({
  username: '',
  email: '',
  password: '',
  errorField: '',
  successField: '',
  name: '',
  phone: '',
  address: ''
})

const emailRules = [
  {
    type: 'custom' as const,
    message: '请输入有效的邮箱地址',
    validator: (value: string) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
  }
]

const errorMessage = ref('')

const handleValidation = (isValid: boolean, errors: string[]) => {
  if (!isValid && formData.errorField) {
    errorMessage.value = errors[0] || '输入内容不符合要求'
  } else {
    errorMessage.value = ''
  }
}

// 表单演示
const formRef = ref<InstanceType<typeof BaseForm>>()
const formMessage = ref('')
const formMessageType = ref<'success' | 'error'>('success')

const formValidation = {
  name: {
    required: true,
    minLength: 2,
    maxLength: 10,
    message: '姓名长度为2-10个字符'
  },
  phone: {
    required: true,
    pattern: /^1[3-9]\d{9}$/,
    message: '请输入有效的手机号'
  },
  address: {
    maxLength: 100,
    message: '地址长度不能超过100个字符'
  }
}

const handleFormSubmit = async (values: any) => {
  console.log('表单提交:', values)
  
  // 模拟API调用
  await new Promise(resolve => setTimeout(resolve, 1500))
  
  formMessage.value = '表单提交成功！'
  formMessageType.value = 'success'
  
  // 3秒后清除消息
  setTimeout(() => {
    formMessage.value = ''
  }, 3000)
}

const handleFormValidation = (isValid: boolean, errors: any) => {
  console.log('表单验证:', isValid, errors)
}

const resetForm = () => {
  formRef.value?.reset()
  formMessage.value = ''
}
</script>

<style scoped>
.demo-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-xl);
  font-family: var(--font-family-base);
}

.demo-container h1 {
  margin-bottom: var(--spacing-2xl);
  color: var(--gray-900);
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
}

.demo-section {
  margin-bottom: var(--spacing-3xl);
  padding: var(--spacing-xl);
  background: white;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--gray-200);
}

.demo-section h2 {
  margin-bottom: var(--spacing-xl);
  color: var(--gray-900);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-semibold);
  border-bottom: 2px solid var(--primary-200);
  padding-bottom: var(--spacing-sm);
}

.demo-group {
  margin-bottom: var(--spacing-xl);
}

.demo-group:last-child {
  margin-bottom: 0;
}

.demo-group h3 {
  margin-bottom: var(--spacing-md);
  color: var(--gray-700);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-medium);
}

.button-group {
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
  align-items: center;
}

.form-demo {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.form-actions {
  display: flex;
  gap: var(--spacing-md);
  margin-top: var(--spacing-xl);
}

.form-message {
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
  margin-top: var(--spacing-md);
  font-size: var(--font-size-sm);
}

.form-message.success {
  background-color: var(--success-light);
  color: #065f46;
  border: 1px solid var(--success);
}

.form-message.error {
  background-color: var(--error-light);
  color: #991b1b;
  border: 1px solid var(--error);
}

.responsive-demo {
  padding: var(--spacing-lg);
  background: var(--gray-50);
  border-radius: var(--radius-lg);
  border: 1px solid var(--gray-200);
}

.responsive-card {
  background: white;
  padding: var(--spacing-xl);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  text-align: center;
}

.responsive-card h3 {
  margin-bottom: var(--spacing-sm);
  color: var(--gray-900);
}

.responsive-card p {
  margin-bottom: var(--spacing-lg);
  color: var(--gray-600);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .demo-container {
    padding: var(--spacing-md);
  }
  
  .demo-section {
    padding: var(--spacing-md);
    margin-bottom: var(--spacing-xl);
  }
  
  .button-group {
    flex-direction: column;
    align-items: stretch;
  }
  
  .form-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .demo-container {
    padding: var(--spacing-sm);
  }
  
  .demo-container h1 {
    font-size: var(--font-size-2xl);
  }
  
  .demo-section h2 {
    font-size: var(--font-size-xl);
  }
}
</style>