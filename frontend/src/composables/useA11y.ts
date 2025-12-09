import { ref, onMounted, onUnmounted, type Ref } from 'vue'

/**
 * 键盘导航 Hook，支持方向键、Tab 等
 */
export function useKeyboardNavigation(
  containerRef: Ref<HTMLElement | null>,
  options: {
    selector?: string
    loop?: boolean
    horizontal?: boolean
    vertical?: boolean
    onSelect?: (element: HTMLElement) => void
    onEscape?: () => void
  } = {}
) {
  const {
    selector = '[data-navigatable]',
    loop = true,
    horizontal = true,
    vertical = true,
    onSelect,
    onEscape
  } = options

  const currentIndex = ref(-1)
  const navigatableElements = ref<HTMLElement[]>([])

  const updateNavigatableElements = () => {
    if (!containerRef.value) return

    navigatableElements.value = Array.from(
      containerRef.value.querySelectorAll(selector)
    ) as HTMLElement[]
  }

  const focusElement = (index: number) => {
    if (index < 0 || index >= navigatableElements.value.length) return

    currentIndex.value = index
    navigatableElements.value[index].focus()
    navigatableElements.value[index].setAttribute('data-focused', 'true')

    // 移除其他元素的焦点状态
    navigatableElements.value.forEach((el, i) => {
      if (i !== index) {
        el.removeAttribute('data-focused')
      }
    })
  }

  const handleKeyDown = (event: KeyboardEvent) => {
    if (!navigatableElements.value.length) return

    const key = event.key
    let newIndex = currentIndex.value

    switch (key) {
      case 'ArrowUp':
        if (!vertical) return
        event.preventDefault()
        newIndex = currentIndex.value > 0 ? currentIndex.value - 1 : (loop ? navigatableElements.value.length - 1 : 0)
        break

      case 'ArrowDown':
        if (!vertical) return
        event.preventDefault()
        newIndex = currentIndex.value < navigatableElements.value.length - 1 
          ? currentIndex.value + 1 
          : (loop ? 0 : navigatableElements.value.length - 1)
        break

      case 'ArrowLeft':
        if (!horizontal) return
        event.preventDefault()
        newIndex = currentIndex.value > 0 ? currentIndex.value - 1 : (loop ? navigatableElements.value.length - 1 : 0)
        break

      case 'ArrowRight':
        if (!horizontal) return
        event.preventDefault()
        newIndex = currentIndex.value < navigatableElements.value.length - 1 
          ? currentIndex.value + 1 
          : (loop ? 0 : navigatableElements.value.length - 1)
        break

      case 'Enter':
      case ' ':
        event.preventDefault()
        if (currentIndex.value >= 0 && onSelect) {
          onSelect(navigatableElements.value[currentIndex.value])
        }
        break

      case 'Escape':
        event.preventDefault()
        if (onEscape) {
          onEscape()
        }
        break

      case 'Tab':
        // 让浏览器处理Tab键
        return

      default:
        return
    }

    if (newIndex !== currentIndex.value) {
      focusElement(newIndex)
    }
  }

  onMounted(() => {
    updateNavigatableElements()
    
    if (containerRef.value) {
      containerRef.value.addEventListener('keydown', handleKeyDown)
      containerRef.value.setAttribute('role', 'grid')
      containerRef.value.setAttribute('tabindex', '0')
    }

    // 监听DOM变化，更新可导航元素
    const observer = new MutationObserver(updateNavigatableElements)
    if (containerRef.value) {
      observer.observe(containerRef.value, {
        childList: true,
        subtree: true
      })
    }

    onUnmounted(() => {
      if (containerRef.value) {
        containerRef.value.removeEventListener('keydown', handleKeyDown)
      }
      observer.disconnect()
    })
  })

  return {
    currentIndex,
    navigatableElements,
    updateNavigatableElements,
    focusElement
  }
}

/**
 * 焦点管理 Hook
 */
export function useFocusManager() {
  const focusedElement = ref<HTMLElement | null>(null)
  const previousFocus = ref<HTMLElement | null>(null)

  const trapFocus = (container: HTMLElement) => {
    // 保存当前焦点
    previousFocus.value = document.activeElement as HTMLElement

    // 获取容器内的可聚焦元素
    const focusableElements = getFocusableElements(container)
    
    if (focusableElements.length > 0) {
      focusableElements[0].focus()
      focusedElement.value = focusableElements[0]
    }

    // 监听键盘事件，实现焦点循环
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key !== 'Tab') return

      const focusableElements = getFocusableElements(container)
      const currentIndex = focusableElements.indexOf(document.activeElement as HTMLElement)

      if (event.shiftKey) {
        // Shift+Tab
        if (currentIndex === 0) {
          event.preventDefault()
          focusableElements[focusableElements.length - 1].focus()
        }
      } else {
        // Tab
        if (currentIndex === focusableElements.length - 1) {
          event.preventDefault()
          focusableElements[0].focus()
        }
      }
    }

    container.addEventListener('keydown', handleKeyDown)

    return () => {
      container.removeEventListener('keydown', handleKeyDown)
      // 恢复之前的焦点
      if (previousFocus.value) {
        previousFocus.value.focus()
      }
    }
  }

  const getFocusableElements = (container: HTMLElement): HTMLElement[] => {
    const selector = [
      'a[href]',
      'button:not([disabled])',
      'input:not([disabled])',
      'select:not([disabled])',
      'textarea:not([disabled])',
      '[tabindex]:not([tabindex="-1"])',
      '[contenteditable="true"]'
    ].join(', ')

    return Array.from(container.querySelectorAll(selector)) as HTMLElement[]
  }

  return {
    focusedElement,
    trapFocus,
    getFocusableElements
  }
}

/**
 * 屏幕阅读器 Hook
 */
export function useScreenReader() {
  const announce = (message: string, priority: 'polite' | 'assertive' = 'polite') => {
    const announcement = document.createElement('div')
    announcement.setAttribute('aria-live', priority)
    announcement.setAttribute('aria-atomic', 'true')
    announcement.className = 'sr-only'
    announcement.textContent = message

    document.body.appendChild(announcement)

    // 清理元素
    setTimeout(() => {
      document.body.removeChild(announcement)
    }, 1000)
  }

  const announceError = (message: string) => {
    announce(message, 'assertive')
  }

  const announceSuccess = (message: string) => {
    announce(message, 'polite')
  }

  return {
    announce,
    announceError,
    announceSuccess
  }
}

/**
 * 高对比度模式检测 Hook
 */
export function useHighContrast() {
  const isHighContrast = ref(false)

  const checkHighContrast = () => {
    // 检测系统高对比度模式
    const mediaQuery = window.matchMedia('(prefers-contrast: high)')
    isHighContrast.value = mediaQuery.matches

    // 检测强制颜色模式（Windows高对比度）
    if (window.matchMedia('(forced-colors: active)').matches) {
      isHighContrast.value = true
    }
  }

  onMounted(() => {
    checkHighContrast()

    // 监听媒体查询变化
    const mediaQuery = window.matchMedia('(prefers-contrast: high)')
    mediaQuery.addEventListener('change', checkHighContrast)

    onUnmounted(() => {
      mediaQuery.removeEventListener('change', checkHighContrast)
    })
  })

  return {
    isHighContrast
  }
}

/**
 * 减少动画偏好检测 Hook
 */
export function useReducedMotion() {
  const prefersReducedMotion = ref(false)

  onMounted(() => {
    const mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
    prefersReducedMotion.value = mediaQuery.matches

    const handleChange = (event: MediaQueryListEvent) => {
      prefersReducedMotion.value = event.matches
    }

    mediaQuery.addEventListener('change', handleChange)

    onUnmounted(() => {
      mediaQuery.removeEventListener('change', handleChange)
    })
  })

  return {
    prefersReducedMotion
  }
}

/**
 * 语义化标签 Hook
 */
export function useSemanticTags() {
  const getHeadingTag = (level: number): string => {
    if (level < 1) return 'h1'
    if (level > 6) return 'h6'
    return `h${level}`
  }

  const getListTag = (ordered: boolean): string => {
    return ordered ? 'ol' : 'ul'
  }

  const getFormFieldTag = (type: string): string => {
    switch (type) {
      case 'email':
        return 'email'
      case 'tel':
        return 'tel'
      case 'url':
        return 'url'
      case 'search':
        return 'search'
      default:
        return 'text'
    }
  }

  return {
    getHeadingTag,
    getListTag,
    getFormFieldTag
  }
}