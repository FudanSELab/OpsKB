<template>
  <div class="h-full flex flex-col bg-white rounded-md border border-gray-300">
    <!-- 顶部操作按钮区 -->
    <div class="p-4 border-b border-gray-200 flex items-center justify-between shrink-0">
      <h3 class="font-medium text-gray-800">决策流程</h3>
      <div class="flex gap-2">
        <!-- 策略搜索按钮 -->
        <button
          v-if="decisionFlow.step === 'eventDetail'"
          class="btn btn-sm btn-primary"
          :disabled="loading.strategySearch || !eventDetail"
          @click="$emit('strategy-search')"
        >
          <span v-if="loading.strategySearch" class="loading loading-spinner loading-xs"></span>
          策略搜索
        </button>

        <!-- UCB策略选择按钮 -->
        <button
          v-if="decisionFlow.step === 'strategies'"
          class="btn btn-sm btn-primary"
          :disabled="loading.ucbSelect"
          @click="$emit('ucb-select')"
        >
          <span v-if="loading.ucbSelect" class="loading loading-spinner loading-xs"></span>
          UCB策略选择
        </button>

        <!-- 生成调整方案按钮 -->
        <button
          v-if="decisionFlow.step === 'ucbSelected'"
          class="btn btn-sm btn-primary"
          :disabled="loading.planGenerate"
          @click="$emit('plan-generate')"
        >
          <span v-if="loading.planGenerate" class="loading loading-spinner loading-xs"></span>
          生成调整方案
        </button>

        <!-- 策略优选执行按钮 -->
        <button
          v-if="decisionFlow.step === 'plans'"
          class="btn btn-sm btn-primary"
          :disabled="loading.trainOnce"
          @click="$emit('train-once')"
        >
          <span v-if="loading.trainOnce" class="loading loading-spinner loading-xs"></span>
          策略优选执行
        </button>

        <!-- 执行完毕按钮 -->
        <button v-if="decisionFlow.step === 'completed'" class="btn btn-sm btn-success" disabled>
          ✓ 执行完毕
        </button>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="flex-1 overflow-auto p-4">
      <!-- 空状态 -->
      <div
        v-if="!eventDetail && decisionFlow.step === 'none'"
        class="flex items-center justify-center h-full"
      >
        <div class="text-center text-gray-400">
          <svg
            class="w-16 h-16 mx-auto mb-3 text-gray-300"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="1.5"
              d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
            ></path>
          </svg>
          <p class="text-base font-medium">请选择一个事件</p>
          <p class="text-sm mt-1">从右侧事件列表中选择一个事件以开始决策流程</p>
        </div>
      </div>

      <!-- 事件详情 -->
      <div v-if="eventDetail && decisionFlow.step === 'eventDetail'">
        <h4 class="font-bold text-lg mb-4">事件详情</h4>
        <div class="grid grid-cols-2 gap-4">
          <!-- 基本信息 -->
          <div class="col-span-2 bg-gray-50 p-4 rounded-md">
            <h5 class="font-semibold mb-2">基本信息</h5>
            <div class="space-y-1 text-sm">
              <p><span class="font-medium">事件ID:</span> {{ eventDetail.eventId }}</p>
              <p><span class="font-medium">场景ID:</span> {{ eventDetail.scenarioId }}</p>
            </div>
          </div>

          <!-- 目标信息 -->
          <div class="bg-blue-50 p-4 rounded-md">
            <h5 class="font-semibold mb-2">目标信息</h5>
            <div class="space-y-1 text-sm">
              <p>
                <span class="font-medium">目标数量:</span>
                {{ eventDetail.targetCount || eventDetail.targetInfo?.target_number || 0 }}
              </p>
              <div v-if="eventDetail.targets || eventDetail.targetInfo?.targets" class="mt-2">
                <p class="font-medium">目标列表:</p>
                <ul class="list-disc list-inside pl-2 space-y-1 mt-1">
                  <li
                    v-for="target in (
                      eventDetail.targets ||
                      eventDetail.targetInfo?.targets ||
                      []
                    ).slice(0, 3)"
                    :key="target.targetId || target.id_target"
                  >
                    目标{{ target.targetId || target.id_target }} - 速度:
                    {{ Number(target.speed).toFixed(2) }} - 高度:
                    {{ Number(target.altitude).toFixed(2) || target.height }}
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <!-- 资源信息 -->
          <div class="bg-green-50 p-4 rounded-md">
            <h5 class="font-semibold mb-2">资源信息</h5>
            <div class="space-y-1 text-sm">
              <p>
                <span class="font-medium">资源数量:</span>
                {{ eventDetail.resourceCount || eventDetail.resourceInfo?.resource_number || 0 }}
              </p>
              <div v-if="eventDetail.resources || eventDetail.resourceInfo?.resources" class="mt-2">
                <p class="font-medium">资源列表:</p>
                <ul class="list-disc list-inside pl-2 space-y-1 mt-1">
                  <li
                    v-for="resource in (
                      eventDetail.resources ||
                      eventDetail.resourceInfo?.resources ||
                      []
                    ).slice(0, 3)"
                    :key="resource.resourceId || resource.id_resource"
                  >
                    资源{{ resource.resourceId || resource.id_resource }} -
                    {{ getResourceTypeName(resource.resourceType || resource.type) }}
                  </li>
                </ul>
              </div>
            </div>
          </div>

          <!-- 防区信息 -->
          <div class="col-span-2 bg-yellow-50 p-4 rounded-md">
            <h5 class="font-semibold mb-2">防区信息</h5>
            <div class="space-y-1 text-sm">
              <p>
                <span class="font-medium">防区数量:</span>
                {{ eventDetail.zoneCount || eventDetail.zoneInfo?.zone_number || 0 }}
              </p>
              <p>
                <span class="font-medium">可见度:</span>
                {{
                  eventDetail.zones?.[0]?.visibility || eventDetail.zoneInfo?.visibility || '未知'
                }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- 策略集合 -->
      <div v-if="decisionFlow.step === 'strategies' || decisionFlow.step === 'ucbSelected'">
        <h4 class="font-bold text-lg mb-4">策略集合</h4>

        <!-- 策略树可视化组件 -->
        <!-- <StrategyTreeChart
          :scenario-id="scenarioId"
          :tree-version="treeVersion"
          :strategies="decisionFlow.strategies"
          :selected-strategy-ids="decisionFlow.selectedStrategyIds"
          :visible="decisionFlow.step === 'strategies' || decisionFlow.step === 'ucbSelected'"
        /> -->

        <!-- 策略列表 -->
        <div class="space-y-2">
          <div
            v-for="strategy in decisionFlow.strategies"
            :key="strategy.strategyId"
            class="p-4 rounded-md border"
            :class="{
              'bg-blue-100 border-blue-500': isStrategySelected(strategy.strategyId),
              'bg-gray-50 border-gray-200': !isStrategySelected(strategy.strategyId),
            }"
          >
            <div class="flex items-center justify-between">
              <div class="flex-1">
                <h5 class="font-semibold">{{ strategy.strategyName }}</h5>
                <p class="text-sm text-gray-600 mt-1">{{ strategy.description }}</p>
                <div class="flex gap-4 mt-2 text-xs text-gray-500">
                  <span>类型: {{ strategy.strategyType }}</span>
                  <span>UCB探索: {{ strategy.ucbExploration?.toFixed(4) || '0' }}</span>
                  <span>UCB利用: {{ strategy.ucbExploitation?.toFixed(4) || '0' }}</span>
                  <span>选择次数: {{ strategy.selectNum }}/{{ strategy.totalNum }}</span>
                </div>
              </div>
              <div v-if="isStrategySelected(strategy.strategyId)" class="ml-4">
                <span class="badge badge-primary">已选中</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 方案集合 -->
      <div v-if="decisionFlow.step === 'plans'">
        <h4 class="font-bold text-lg mb-4">方案集合</h4>
        <div class="space-y-2">
          <div
            v-for="(plan, index) in decisionFlow.plans"
            :key="index"
            class="p-4 rounded-md border bg-gray-50 border-gray-200"
          >
            <div class="flex items-center justify-between mb-2">
              <h5 class="font-semibold">方案 {{ index + 1 }} (策略 {{ plan.strategyId }})</h5>
              <div class="flex gap-2">
                <span class="badge badge-sm">生成时间: {{ plan.actualGenerationTime }}ms</span>
                <span class="badge badge-sm badge-success"
                  >收益: {{ plan.totalBenefit?.toFixed(2) }}</span
                >
                <span class="badge badge-sm badge-warning">成本: {{ plan.totalCost }}</span>
              </div>
            </div>
            <div class="text-sm">
              <p class="font-medium">拍卖价格: {{ plan.auctionPrice?.toFixed(4) }}</p>
              <div class="mt-2">
                <p class="font-medium">资源分配:</p>
                <ul class="list-disc list-inside pl-2 space-y-1 mt-1">
                  <li v-for="(item, idx) in plan.result" :key="idx">
                    目标{{ item.targetId }} → 资源{{ item.resourceId || '无' }}
                    <span v-if="item.resourceId" class="text-xs text-gray-500">
                      (距离: {{ item.distanceForTR?.toFixed(2) }}m)
                    </span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 执行完毕结果 -->
      <div v-if="decisionFlow.step === 'completed' && decisionFlow.finalResult">
        <h4 class="font-bold text-lg mb-4">策略评估结果</h4>
        <div class="grid grid-cols-2 gap-4">
          <!-- 基本信息 -->
          <div class="col-span-2 bg-green-50 p-4 rounded-md border border-green-200">
            <h5 class="font-semibold mb-2 text-green-800">✓ 执行成功</h5>
            <div class="space-y-1 text-sm">
              <p>
                <span class="font-medium">样本ID:</span> {{ decisionFlow.finalResult.sampleId }}
              </p>
              <p><span class="font-medium">事件ID:</span> {{ decisionFlow.finalResult.eventId }}</p>
              <p>
                <span class="font-medium">策略ID:</span> {{ decisionFlow.finalResult.strategyId }}
              </p>
              <p><span class="font-medium">方案ID:</span> {{ decisionFlow.finalResult.planId }}</p>
            </div>
          </div>

          <!-- UCB评分信息 -->
          <div class="bg-blue-50 p-4 rounded-md">
            <h5 class="font-semibold mb-2">UCB评分</h5>
            <div class="space-y-1 text-sm">
              <p>
                <span class="font-medium">利用项:</span>
                {{ decisionFlow.finalResult.ucbExploitationScore?.toFixed(4) || '0' }}
              </p>
              <p>
                <span class="font-medium">目标数量:</span>
                {{ decisionFlow.finalResult.targetCount }}
              </p>
              <p>
                <span class="font-medium">成功数量:</span>
                {{ decisionFlow.finalResult.successCount }}
              </p>
            </div>
          </div>

          <!-- 时间信息 -->
          <div class="bg-purple-50 p-4 rounded-md">
            <h5 class="font-semibold mb-2">时间信息</h5>
            <div class="space-y-1 text-sm">
              <p>
                <span class="font-medium">创建时间:</span>
                {{ new Date(decisionFlow.finalResult.createdAt).toLocaleString() }}
              </p>
              <p>
                <span class="font-medium">更新时间:</span>
                {{ new Date(decisionFlow.finalResult.updatedAt).toLocaleString() }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// import StrategyTreeChart from './StrategyTreeChart.vue';

const props = defineProps({
  eventDetail: {
    type: Object,
    default: null,
  },
  decisionFlow: {
    type: Object,
    required: true,
  },
  loading: {
    type: Object,
    required: true,
  },
  scenarioId: {
    type: Number,
    default: null,
  },
  treeVersion: {
    type: String,
    default: '',
  },
});

// 资源类型映射
const resourceTypeMap = {
  R_D: '导航诱骗',
  R_W: '网捕无人机',
  R_J: '激光武器',
  R_T: '通信干扰',
};

// 获取资源类型显示名称
const getResourceTypeName = (type) => {
  return resourceTypeMap[type] || type;
};

// 判断策略是否被选中
const isStrategySelected = (strategyId) => {
  return props.decisionFlow.selectedStrategyIds.includes(strategyId);
};

// 定义事件
defineEmits(['strategy-search', 'ucb-select', 'plan-generate', 'train-once']);
</script>

<style scoped>
/* 可以添加自定义样式 */
</style>
