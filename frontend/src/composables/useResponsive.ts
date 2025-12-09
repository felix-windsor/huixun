import { ref, computed, watch, type Ref } from 'vue'
import { debounce, throttle } from '@/utils'

/**
 * 响应式断点 Hook
 */
export function useBreakpoints() {
  const width = ref(window.innerWidth)
  const height = ref(window.innerHeight)

  const breakpoints = {
    xs: 0,
    sm: 640,
    md: 768,
    lg: 1024,
    xl: 1280,
    '2xl': 1536
  }

  const currentBreakpoint = computed(() => {
    if (width.value >= breakpoints['2xl']) return '2xl'
    if (width.value >= breakpoints.xl) return 'xl'
    if (width.value >= breakpoints.lg) return 'lg'
    if (width.value >= breakpoints.md) return 'md'
    if (width.value >= breakpoints.sm) return 'sm'
    return 'xs'
  })

  const isMobile = computed(() => width.value < breakpoints.md)
  const isTablet = computed(() => width.value >= breakpoints.md && width.value < breakpoints.lg)
  const isDesktop = computed(() => width.value >= breakpoints.lg)

  const updateDimensions = () => {
    width.value = window.innerWidth
    height.value = window.innerHeight
  }

  // 使用防抖优化性能
  const debouncedUpdate = debounce(updateDimensions, 100)

  window.addEventListener('resize', debouncedUpdate)

  return {
    width,
    height,
    currentBreakpoint,
    isMobile,
    isTablet,
    isDesktop,
    breakpoints
  }
}

/**
 * 设备类型检测 Hook
 */
export function useDevice() {
  const userAgent = navigator.userAgent.toLowerCase()
  const platform = navigator.platform.toLowerCase()

  const isIOS = /iphone|ipad|ipod/.test(userAgent)
  const isAndroid = /android/.test(userAgent)
  const isMobile = /mobile|android|iphone|ipad|phone/.test(userAgent)
  const isTablet = /tablet|ipad|playbook|silk/.test(userAgent)
  const isDesktop = !isMobile && !isTablet

  const isSafari = /^((?!chrome|android).)*safari/.test(userAgent)
  const isChrome = /chrome/.test(userAgent) && !/edge/.test(userAgent)
  const isFirefox = /firefox/.test(userAgent)
  const isEdge = /edge/.test(userAgent)

  const isTouchDevice = 'ontouchstart' in window || navigator.maxTouchPoints > 0

  return {
    isIOS,
    isAndroid,
    isMobile,
    isTablet,
    isDesktop,
    isSafari,
    isChrome,
    isFirefox,
    isEdge,
    isTouchDevice,
    userAgent,
    platform
  }
}

/**
 * 网络状态 Hook
 */
export function useNetwork() {
  const online = ref(navigator.onLine)
  const connection = ref<any | null>(null)

  const updateOnlineStatus = () => {
    online.value = navigator.onLine
  }

  window.addEventListener('online', updateOnlineStatus)
  window.addEventListener('offline', updateOnlineStatus)

  // 获取网络连接信息（如果支持）
  if ('connection' in navigator) {
    connection.value = (navigator as any).connection
  }

  return {
    online,
    connection
  }
}

/**
 * 滚动位置 Hook
 */
export function useScrollPosition(element?: Ref<HTMLElement | Window>) {
  const x = ref(0)
  const y = ref(0)
  const isScrolling = ref(false)

  const updateScrollPosition = () => {
    const target = element?.value || window
    
    if (target === window) {
      x.value = window.scrollX
      y.value = window.scrollY
    } else if (target instanceof HTMLElement) {
      x.value = target.scrollLeft
      y.value = target.scrollTop
    }
  }

  // 使用节流优化性能
  const throttledUpdate = throttle(updateScrollPosition, 16) // ~60fps

  const targetElement = computed(() => {
    return element?.value || window
  })

  const addScrollListener = () => {
    const target = targetElement.value
    if (target) {
      target.addEventListener('scroll', throttledUpdate, { passive: true })
      updateScrollPosition()
    }
  }

  const removeScrollListener = () => {
    const target = targetElement.value
    if (target) {
      target.removeEventListener('scroll', throttledUpdate)
    }
  }

  // 监听滚动状态
  let scrollTimeout: ReturnType<typeof setTimeout> | null = null
  const handleScrollStart = () => {
    isScrolling.value = true
    if (scrollTimeout) clearTimeout(scrollTimeout)
  }

  const handleScrollEnd = () => {
    scrollTimeout = setTimeout(() => {
      isScrolling.value = false
    }, 150)
  }

  watch(targetElement, (newTarget, oldTarget) => {
    if (oldTarget) removeScrollListener()
    if (newTarget) addScrollListener()
  }, { immediate: true })

  // 监听滚动开始和结束
  watch(y, () => {
    handleScrollStart()
    handleScrollEnd()
  })

  return {
    x,
    y,
    isScrolling,
    addScrollListener,
    removeScrollListener
  }
}

/**
 * 懒加载 Hook
 */
export function useIntersectionObserver(
  target: Ref<HTMLElement | null>,
  options: IntersectionObserverInit = {}
) {
  const isIntersecting = ref(false)
  const intersectionRatio = ref(0)

  const defaultOptions: IntersectionObserverInit = {
    threshold: 0.1,
    rootMargin: '50px'
  }

  const observerOptions = { ...defaultOptions, ...options }

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      isIntersecting.value = entry.isIntersecting
      intersectionRatio.value = entry.intersectionRatio
    })
  }, observerOptions)

  watch(target, (newTarget, oldTarget) => {
    if (oldTarget) {
      observer.unobserve(oldTarget)
    }
    if (newTarget) {
      observer.observe(newTarget)
    }
  }, { immediate: true })

  return {
    isIntersecting,
    intersectionRatio
  }
}

/**
 * 响应式字体大小 Hook
 */
export function useResponsiveFontSize(
  baseSize: number,
  scaleFactor = 0.5,
  minSize = 12,
  maxSize = 24
) {
  const { width } = useBreakpoints()

  const fontSize = computed(() => {
    const scale = Math.min(Math.max(width.value / 1440, 0.5), 2)
    const calculatedSize = baseSize * Math.pow(scale, scaleFactor)
    return Math.min(Math.max(calculatedSize, minSize), maxSize)
  })

  return {
    fontSize: computed(() => `${fontSize.value}px`)
  }
}

/**
 * 响应式间距 Hook
 */
export function useResponsiveSpacing(
  baseSpacing: number,
  scaleFactor = 0.3
) {
  const { currentBreakpoint } = useBreakpoints()

  const spacing = computed(() => {
    const multiplier = {
      xs: 0.75,
      sm: 0.875,
      md: 1,
      lg: 1.125,
      xl: 1.25,
      '2xl': 1.5
    }[currentBreakpoint.value] || 1

    return baseSpacing * multiplier * scaleFactor
  })

  return {
    spacing: computed(() => `${spacing.value}rem`)
  }
}

/**
 * 响应式图片 Hook
 */
export function useResponsiveImage(
  src: string,
  sizes: {
    xs?: string
    sm?: string
    md?: string
    lg?: string
    xl?: string
    '2xl'?: string
  } = {}
) {
  const { currentBreakpoint } = useBreakpoints()

  const imageSrc = computed(() => {
    return sizes[currentBreakpoint.value] || src
  })

  const imageSize = computed(() => {
    const sizeMap = {
      xs: '100vw',
      sm: '100vw',
      md: '100vw',
      lg: '50vw',
      xl: '33vw',
      '2xl': '25vw'
    }

    return sizeMap[currentBreakpoint.value] || '100vw'
  })

  return {
    imageSrc,
    imageSize
  }
}
