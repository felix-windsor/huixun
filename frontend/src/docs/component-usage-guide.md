# 组件库使用指南

## 快速开始

### 1. 安装组件库

组件已经集成到项目中，可以直接使用。

### 2. 基础组件

#### BaseButton 按钮组件

```vue
<template>
  <!-- 基础按钮 -->
  <BaseButton variant="primary" @click="handleClick">主要按钮</BaseButton>
  <BaseButton variant="secondary">次要按钮</BaseButton>
  <BaseButton variant="danger">危险按钮</BaseButton>
  <BaseButton variant="text">文字按钮</BaseButton>

  <!-- 不同尺寸 -->
  <BaseButton size="sm">小尺寸</BaseButton>
  <BaseButton size="md">中尺寸</BaseButton>
  <BaseButton size="lg">大尺寸</BaseButton>

  <!-- 状态 -->
  <BaseButton :loading="true">加载中</BaseButton>
  <BaseButton disabled>已禁用</BaseButton>
  <BaseButton block>块级按钮</BaseButton>
</template>
```

**Props:**
- `variant`: 'primary' | 'secondary' | 'danger' | 'text' - 按钮变体
- `size`: 'sm' | 'md' | 'lg' - 按钮尺寸
- `loading`: boolean - 加载状态
- `disabled`: boolean - 禁用状态
- `block`: boolean - 块级按钮
- `type`: 'button' | 'submit' | 'reset' - 按钮类型

**Events:**
- `click`: 点击事件

#### BaseInput 输入框组件

```vue
<template>
  <!-- 基础输入 -->
  <BaseInput
    v-model="value"
    label="用户名"
    placeholder="请输入用户名"
    required
  />

  <!-- 带验证 -->
  <BaseInput
    v-model="email"
    type="email"
    label="邮箱"
    placeholder="请输入邮箱"
    validate-on-blur
    :validation-rules="emailRules"
  />

  <!-- 错误状态 -->
  <BaseInput
    v-model="input"
    label="输入框"
    :error="errorMessage"
    help-text="请输入正确的格式"
  />
</template>

<script setup>
const emailRules = [
  {
    type: 'custom',
    message: '请输入有效的邮箱地址',
    validator: (value) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
  }
]
</script>
```

**Props:**
- `modelValue`: string - 输入值（v-model）
- `type`: 'text' | 'password' | 'email' | 'number' | 'tel' | 'url' | 'search'
- `label`: string - 标签文本
- `placeholder`: string - 占位符
- `required`: boolean - 是否必填
- `disabled`: boolean - 是否禁用
- `error`: string - 错误消息
- `success`: boolean - 成功状态
- `helpText`: string - 帮助文本
- `validateOnInput`: boolean - 输入时验证
- `validateOnBlur`: boolean - 失焦时验证
- `validationRules`: ValidationRule[] - 验证规则

**Events:**
- `update:modelValue`: 值更新
- `blur`: 失焦事件
- `focus`: 聚焦事件
- `input`: 输入事件
- `validation`: 验证结果

#### BaseForm 表单组件

```vue
<template>
  <BaseForm
    ref="formRef"
    :initial-values="formData"
    :validation-schema="validationSchema"
    :validate-on-change="true"
    :validate-on-blur="true"
    @submit="handleSubmit"
  >
    <template #default="{ values, errors, isValid, isSubmitting, handleChange, handleBlur }">
      <BaseInput
        :model-value="values.username"
        label="用户名"
        required
        :error="errors.username"
        @update:model-value="val => handleChange('username', val)"
        @blur="() => handleBlur('username')"
      />

      <BaseInput
        :model-value="values.password"
        type="password"
        label="密码"
        required
        :error="errors.password"
        @update:model-value="val => handleChange('password', val)"
        @blur="() => handleBlur('password')"
      />

      <BaseButton 
        type="submit" 
        variant="primary"
        :loading="isSubmitting"
        :disabled="!isValid"
      >
        提交
      </BaseButton>
    </template>
  </BaseForm>
</template>

<script setup>
const formData = {
  username: '',
  password: ''
}

const validationSchema = {
  username: {
    required: true,
    minLength: 3,
    maxLength: 20,
    message: '用户名长度为3-20个字符'
  },
  password: {
    required: true,
    minLength: 6,
    message: '密码最少6个字符'
  }
}

const handleSubmit = async (values) => {
  console.log('表单提交:', values)
  // 处理提交逻辑
}
</script>
```

**Props:**
- `initialValues`: object - 初始值
- `validationSchema`: ValidationSchema - 验证模式
- `validateOnMount`: boolean - 挂载时验证
- `validateOnChange`: boolean - 变化时验证
- `validateOnBlur`: boolean - 失焦时验证

**Events:**
- `submit`: 表单提交
- `validation`: 验证结果

**Methods:**
- `validate()`: 手动验证表单
- `reset()`: 重置表单
- `setFieldValue(field, value)`: 设置字段值
- `setFieldError(field, error)`: 设置字段错误

### 3. 工具函数

#### 防抖和节流

```typescript
import { debounce, throttle } from '@/utils'

// 防抖 - 延迟执行
const debouncedSearch = debounce((query: string) => {
  performSearch(query)
}, 300)

// 节流 - 限制频率
const throttledScroll = throttle((event: Event) => {
  handleScroll(event)
}, 100)
```

#### 响应式 Hook

```vue
<template>
  <div>
    <p>当前断点: {{ currentBreakpoint }}</p>
    <p>是否移动端: {{ isMobile }}</p>
  </div>
</template>

<script setup>
import { useBreakpoints, useDevice } from '@/composables'

const { currentBreakpoint, isMobile, isTablet, isDesktop } = useBreakpoints()
const { isIOS, isAndroid, isTouchDevice } = useDevice()
</script>
```

#### 无障碍 Hook

```vue
<template>
  <div ref="containerRef">
    <button data-navigatable @click="handleClick">按钮1</button>
    <button data-navigatable @click="handleClick">按钮2</button>
  </div>
</template>

<script setup>
import { useKeyboardNavigation, useScreenReader } from '@/composables'

const containerRef = ref()
const { announce } = useScreenReader()

const { currentIndex } = useKeyboardNavigation(containerRef, {
  onSelect: (element) => {
    element.click()
    announce('已选择项目')
  }
})
</script>
```

### 4. 样式系统

#### CSS变量

```css
:root {
  /* 颜色系统 */
  --primary-500: #3b82f6;
  --primary-600: #2563eb;
  --gray-500: #6b7280;
  --gray-700: #374151;
  
  /* 间距系统 */
  --spacing-xs: 0.25rem;
  --spacing-sm: 0.5rem;
  --spacing-md: 1rem;
  
  /* 字体系统 */
  --font-size-sm: 0.875rem;
  --font-size-base: 1rem;
  --font-weight-medium: 500;
  
  /* 动画系统 */
  --transition-fast: 150ms ease-out;
  --transition-base: 250ms ease-out;
}
```

#### 工具类

```html
<!-- 文本 -->
<div class="text-center text-gray-700 font-medium">居中文本</div>

<!-- 背景 -->
<div class="bg-gray-50 rounded-lg shadow-md">卡片内容</div>

<!-- 动画 -->
<div class="transition animate-fade-in">动画元素</div>
```

### 5. 最佳实践

#### 组件使用

1. **始终使用v-model** 进行双向绑定
2. **合理使用验证** 避免过度验证
3. **提供清晰的错误消息**
4. **使用适当的HTML类型** (email, tel等)
5. **考虑无障碍访问**

#### 性能优化

1. **使用防抖/节流** 处理频繁事件
2. **合理使用计算属性** 避免重复计算
3. **使用虚拟滚动** 处理大量数据
4. **实现懒加载** 优化图片加载
5. **使用CSS动画** 替代JavaScript动画

#### 无障碍设计

1. **提供清晰的标签和说明**
2. **确保键盘可访问性**
3. **使用适当的ARIA标签**
4. **提供错误状态反馈**
5. **支持屏幕阅读器**

### 6. 常见问题

#### Q: 表单验证不生效？
A: 确保设置了 `validate-on-blur` 或 `validate-on-input`，并提供了验证规则。

#### Q: 按钮点击无响应？
A: 检查是否被 `disabled` 或 `loading` 状态阻止，确保事件处理器正确绑定。

#### Q: 样式不生效？
A: 确认已正确引入样式文件，检查CSS变量是否正确设置。

#### Q: 响应式失效？
A: 使用 `useBreakpoints()` Hook 获取断点信息，确保媒体查询正确设置。

### 7. 更新日志

#### v1.0.0 (2024-12-09)
- ✨ 新增 BaseButton 组件
- ✨ 新增 BaseInput 组件
- ✨ 新增 BaseForm 组件
- ✨ 新增响应式 Hook
- ✨ 新增无障碍 Hook
- ✨ 新增性能优化 Hook
- 📚 完善文档和示例
- 🐛 修复已知问题

---

如需更多帮助，请查看组件源代码或联系开发团队。