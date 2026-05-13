<template>
  <div ref="elRef" class="bar-chart" :style="{ height: heightPx }" />
</template>

<script lang="ts">
import { defineComponent, onBeforeUnmount, onMounted, ref, watch, PropType } from 'vue'
import type { EChartsOption } from 'echarts'
import { echarts, EChartsType } from './echartsCore'

/**
 * 通用 ECharts 图表容器:接收 EChartsOption,负责 mount/resize/dispose。
 * 业务组件只需准备 option,无需关心 echarts.init 细节。
 */
export default defineComponent({
  name: 'BarChart',
  props: {
    option: {
      type: Object as PropType<EChartsOption>,
      required: true,
    },
    /** 高度(像素);默认 320 适配两个专题页 KPI 下方区域 */
    height: {
      type: Number,
      default: 320,
    },
  },
  setup(props) {
    const elRef = ref<HTMLDivElement>()
    let chart: EChartsType | null = null

    const heightPx = `${props.height}px`

    const resize = () => chart?.resize()

    onMounted(() => {
      if (!elRef.value) return
      chart = echarts.init(elRef.value)
      chart.setOption(props.option)
      window.addEventListener('resize', resize)
    })

    watch(() => props.option, (next) => {
      chart?.setOption(next, true)
    }, { deep: true })

    onBeforeUnmount(() => {
      window.removeEventListener('resize', resize)
      chart?.dispose()
      chart = null
    })

    return { elRef, heightPx }
  },
})
</script>

<style scoped>
.bar-chart {
  width: 100%;
}
</style>
