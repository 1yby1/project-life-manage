// 响应式断点 composable — 监听 window media query,组件销毁时自动清理。
// 用法:const { isMobile } = useBreakpoint() — isMobile 是 ref<boolean>
import { onBeforeUnmount, onMounted, ref } from 'vue'

const MOBILE_MAX = 768  // 桌面/移动分界,与 Element Plus 的 sm 断点一致

export function useBreakpoint() {
  const isMobile = ref(false)
  let mql: MediaQueryList | null = null

  const onChange = (e: MediaQueryListEvent) => {
    isMobile.value = e.matches
  }

  onMounted(() => {
    if (typeof window === 'undefined') return
    mql = window.matchMedia(`(max-width: ${MOBILE_MAX}px)`)
    isMobile.value = mql.matches
    mql.addEventListener('change', onChange)
  })

  onBeforeUnmount(() => {
    mql?.removeEventListener('change', onChange)
    mql = null
  })

  return { isMobile }
}
