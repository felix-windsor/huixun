import { ref, computed, watch, nextTick } from 'vue'
import type { Ref } from 'vue'

/**
 * 优化的输入框 Hook，支持防抖和验证
 */
export function useOptimizedInput(
  props: {
    modelValue: string
    validateOnInput?: boolean
    validateOnBlur?: boolean
    validationRules?: any[]
    required?: boolean
    minlength?: number
    maxlength?: number
    pattern?: string | RegExp
  },
  emit: {
    (event: 'update:modelValue', value: string): void
    (event: 'validation', isValid: boolean, errors: string[]): void
  }
) {
  const localValue = ref(props.modelValue)
  const error = ref('')
  const isTouched = ref(false)
  const isValidating = ref(false)

  // 防抖处理输入
  const debouncedInput = useDebounce((value: string) => {
    emit('update:modelValue', value)
    
    if (props.validateOnInput) {
      validate(value)
    }
  }, 300)

  // 计算属性：是否有错误
  const hasError = computed(() => !!error.value)

  // 验证函数
  const validate = async (value: string = localValue.value): Promise<boolean> => {
    isValidating.value = true
    const errors: string[] = []

    // 必填验证
    if (props.required && !value.trim()) {
      errors.push('此字段为必填项')
    }

    // 长度验证
    if (props.minlength && value.length < props.minlength) {
      errors.push(`最少需要 ${props.minlength} 个字符`)
    }

    if (props.maxlength && value.length > props.maxlength) {
      errors.push(`最多允许 ${props.maxlength} 个字符`)
    }

    // 模式验证
    if (props.pattern && value) {
      const pattern = typeof props.pattern === 'string' ? new RegExp(props.pattern) : props.pattern
      if (!pattern.test(value)) {
        errors.push('输入格式不正确')
      }
    }

    // 自定义验证规则
    if (props.validationRules) {
      for (const rule of props.validationRules) {
        if (rule.type === 'custom' && rule.validator) {
          const result = await Promise.resolve(rule.validator(value))
          if (result !== true) {
            errors.push(typeof result === 'string' ? result : rule.message || '验证失败')
          }
        }
      }
    }

    // 更新错误状态
    error.value = errors[0] || ''
    isValidating.value = false

    // 触发验证事件
    emit('validation', errors.length === 0, errors)

    return errors.length === 0
  }

  // 处理输入
  const handleInput = (event: Event) => {
    const target = event.target as HTMLInputElement
    const value = target.value
    
    localValue.value = value
    debouncedInput(value)
  }

  // 处理失焦
  const handleBlur = async () => {
    isTouched.value = true
    
    if (props.validateOnBlur) {
      await validate()
    }
  }

  // 处理聚焦
  const handleFocus = () => {
    // 可以在这里添加聚焦时的逻辑
  }

  // 重置验证状态
  const resetValidation = () => {
    error.value = ''
    isTouched.value = false
    isValidating.value = false
  }

  // 监听外部值变化
  watch(
    () => props.modelValue,
    (newValue) => {
      if (newValue !== localValue.value) {
        localValue.value = newValue
        // 如果值从外部改变，重新验证
        if (isTouched.value && (props.validateOnInput || props.validateOnBlur)) {
          nextTick(() => validate(newValue))
        }
      }
    }
  )

  return {
    localValue,
    error,
    hasError,
    isTouched,
    isValidating,
    handleInput,
    handleBlur,
    handleFocus,
    validate,
    resetValidation
  }
}

/**
 * 防抖 Hook
 */
export function useDebounce<T extends (...args: any[]) => any>(
  callback: T,
  delay: number
): T {
  let timeoutId: ReturnType<typeof setTimeout> | null = null

  return ((...args: Parameters<T>) => {
    if (timeoutId) {
      clearTimeout(timeoutId)
    }

    timeoutId = setTimeout(() => {
      callback(...args)
      timeoutId = null
    }, delay)
  }) as T
}

/**
 * 节流 Hook
 */
export function useThrottle<T extends (...args: any[]) => any>(
  callback: T,
  limit: number
): T {
  let inThrottle = false
  let lastArgs: Parameters<T> | null = null

  return ((...args: Parameters<T>) => {
    lastArgs = args

    if (!inThrottle) {
      callback(...lastArgs)
      inThrottle = true

      setTimeout(() => {
        inThrottle = false
        if (lastArgs) {
          callback(...lastArgs)
          lastArgs = null
        }
      }, limit)
    }
  }) as T
}

/**
 * 优化的点击处理 Hook
 */
export function useOptimizedClick(
  onClick: (event: Event) => void,
  options: {
    debounce?: number
    throttle?: number
    preventDefault?: boolean
    stopPropagation?: boolean
  } = {}
) {
  let processedClick = onClick

  // 应用防抖
  if (options.debounce) {
    processedClick = useDebounce(onClick, options.debounce)
  }

  // 应用节流
  if (options.throttle) {
    processedClick = useThrottle(onClick, options.throttle)
  }

  const handleClick = (event: Event) => {
    if (options.preventDefault) {
      event.preventDefault()
    }

    if (options.stopPropagation) {
      event.stopPropagation()
    }

    processedClick(event)
  }

  return handleClick
}

/**
 * 性能监控 Hook
 */
export function usePerformanceMonitor(name: string) {
  const startTime = ref<number>(0)
  const endTime = ref<number>(0)
  const duration = computed(() => endTime.value - startTime.value)

  const start = () => {
    startTime.value = performance.now()
  }

  const end = () => {
    endTime.value = performance.now()
    console.log(`${name} 执行时间: ${duration.value}ms`)
  }

  return {
    start,
    end,
    duration
  }
}

/**
 * 内存使用监控 Hook
 */
export function useMemoryMonitor() {
  const memoryInfo = ref<{
    usedJSHeapSize: number
    totalJSHeapSize: number
    jsHeapSizeLimit: number
  } | null>(null)

  const updateMemoryInfo = () => {
    const mem: any = (performance as any).memory
    if (mem) {
      memoryInfo.value = {
        usedJSHeapSize: mem.usedJSHeapSize,
        totalJSHeapSize: mem.totalJSHeapSize,
        jsHeapSizeLimit: mem.jsHeapSizeLimit
      }
    }
  }

  // 初始化内存信息
  updateMemoryInfo()

  return {
    memoryInfo,
    updateMemoryInfo
  }
}

/**
 * 优化的列表渲染 Hook
 */
export function useVirtualList<T>(
  items: Ref<T[]>,
  itemHeight: number,
  containerHeight: number
) {
  const scrollTop = ref(0)
  const startIndex = ref(0)
  const endIndex = ref(0)

  const visibleCount = computed(() => Math.ceil(containerHeight / itemHeight))

  const updateVisibleRange = () => {
    const start = Math.floor(scrollTop.value / itemHeight)
    const end = Math.min(start + visibleCount.value + 1, items.value.length)

    startIndex.value = Math.max(0, start - 1)
    endIndex.value = end
  }

  const visibleItems = computed(() => {
    return items.value.slice(startIndex.value, endIndex.value)
  })

  const offsetY = computed(() => startIndex.value * itemHeight)

  const handleScroll = (event: Event) => {
    const target = event.target as HTMLElement
    scrollTop.value = target.scrollTop
    updateVisibleRange()
  }

  // 初始化可见范围
  updateVisibleRange()

  return {
    visibleItems,
    offsetY,
    handleScroll,
    scrollTop
  }
}

/**
 * 图片懒加载 Hook
 */
export function useLazyLoad(
  src: string,
  placeholder: string = '',
  options?: IntersectionObserverInit
) {
  const imageSrc = ref(placeholder)
  const isLoading = ref(true)
  const hasError = ref(false)

  const loadImage = () => {
    const img = new Image()
    
    img.onload = () => {
      imageSrc.value = src
      isLoading.value = false
      hasError.value = false
    }

    img.onerror = () => {
      isLoading.value = false
      hasError.value = true
    }

    img.src = src
  }

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        loadImage()
        observer.disconnect()
      }
    })
  }, options)

  const observe = (element: HTMLElement) => {
    observer.observe(element)
  }

  return {
    imageSrc,
    isLoading,
    hasError,
    observe
  }
}
