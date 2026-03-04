<template>
  <div class="h-full bg-white rounded-lg border border-gray-200 flex flex-col">
    <!-- 顶部概览信息 -->
    <div class="border-b border-gray-200 p-4">
      <div class="flex items-center justify-between mb-3">
        <h3 class="text-lg font-semibold text-gray-900">决策过程概览</h3>
        <div class="flex items-center gap-2">
          <span class="px-2 py-1 text-xs bg-green-100 text-green-800 rounded-full">
            事件 #{{ decisionData?.event?.eventId || '-' }}
          </span>
          <span class="px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded-full">
            {{ decisionData?.targetCount || 0 }} 个目标
          </span>
          <span class="px-2 py-1 text-xs bg-purple-100 text-purple-800 rounded-full">
            成功率
            {{ Math.round((decisionData?.successCount / decisionData?.targetCount) * 100 || 0) }}%
          </span>
        </div>
      </div>

      <!-- 当前选中策略信息 -->
      <div class="bg-blue-50 rounded-lg p-3">
        <div class="flex items-center justify-between">
          <div>
            <h4 class="font-medium text-blue-900">
              {{ decisionData?.strategyDto?.strategyName || '请在右侧选择样本数据' }}
            </h4>
            <p class="text-sm text-blue-700 mt-1">{{ decisionData?.strategyDto?.description }}</p>
          </div>
          <div class="text-right">
            <div class="text-lg font-bold text-blue-900">
              {{ decisionData?.strategyDto?.ucbExploitation?.toFixed(4) || '0' }}
            </div>
            <div class="text-xs text-blue-600">UCB开发值</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="flex-1 overflow-auto">
      <!-- 当前选中方案详情 -->
      <div class="p-4 border-b border-gray-100">
        <h4 class="font-medium text-gray-900 mb-3">当前方案详情</h4>

        <!-- 目标-资源匹配表格 -->
        <div class="bg-gray-50 rounded-lg p-3 mb-4">
          <div class="overflow-x-auto">
            <table class="min-w-full">
              <thead>
                <tr class="text-xs text-gray-500 border-b border-gray-200">
                  <th class="text-left py-2">目标ID</th>
                  <th class="text-left py-2">资源ID</th>
                  <th class="text-right py-2">距离(m)</th>
                  <th class="text-right py-2">区域距离(m)</th>
                </tr>
              </thead>
              <tbody class="text-sm">
                <tr
                  v-for="result in decisionData?.planDto?.result"
                  :key="`${result.targetId}-${result.resourceId}`"
                  class="border-b border-gray-100 last:border-b-0"
                >
                  <td class="py-2 font-medium text-blue-600">{{ result.targetId }}</td>
                  <td class="py-2 font-medium text-green-600">{{ result.resourceId }}</td>
                  <td class="py-2 text-right">{{ Math.round(result.distanceForTR) }}</td>
                  <td class="py-2 text-right">{{ Math.round(result.distanceForTZ) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 方案评估指标 -->
        <div class="grid grid-cols-3 gap-4 mb-4">
          <div class="bg-green-50 rounded-lg p-3 text-center">
            <div class="text-lg font-bold text-green-600">
              {{ decisionData?.planDto?.totalBenefit?.toFixed(2) || '0' }}
            </div>
            <div class="text-sm text-green-700">总收益</div>
          </div>
          <div class="bg-red-50 rounded-lg p-3 text-center">
            <div class="text-lg font-bold text-red-600">
              {{ decisionData?.planDto?.totalCost || '0' }}
            </div>
            <div class="text-sm text-red-700">总成本</div>
          </div>
          <div class="bg-blue-50 rounded-lg p-3 text-center">
            <div class="text-lg font-bold text-blue-600">
              {{ decisionData?.planDto?.actualGenerationTime / 1000 || '0' }}ms
            </div>
            <div class="text-sm text-blue-700">生成时间</div>
          </div>
        </div>
      </div>

      <!-- 可折叠的策略对比区域 -->
      <div class="p-4">
        <button
          @click="showStrategyComparison = !showStrategyComparison"
          class="flex items-center justify-between w-full text-sm text-gray-600 hover:text-gray-900"
        >
          <span
            >策略对比 ({{ decisionData?.decisionData?.strategyDtoList?.length || 0 }} 个策略)</span
          >
          <svg
            class="w-4 h-4 transition-transform duration-200"
            :class="{ 'rotate-180': showStrategyComparison }"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M19 9l-7 7-7-7"
            ></path>
          </svg>
        </button>

        <div v-if="showStrategyComparison">
          <div
            v-if="(decisionData?.decisionData?.strategyDtoList?.length || 0) > 2"
            class="flex items-center justify-between my-3"
          >
            <button
              @click="showAllStrategies = !showAllStrategies"
              class="text-xs text-blue-600 hover:text-blue-800"
            >
              {{ showAllStrategies ? '收起' : '展开全部策略' }}
            </button>
          </div>

          <div class="space-y-2">
            <template v-for="(strategy, index) in displayedStrategies" :key="strategy.strategyId">
              <div
                class="border rounded-lg p-3 transition-all duration-200"
                :class="{
                  'border-blue-300 bg-blue-50':
                    strategy.strategyId === decisionData?.strategyDto?.strategyId,
                  'border-gray-200 bg-gray-50':
                    strategy.strategyId !== decisionData?.strategyDto?.strategyId,
                }"
              >
                <div class="flex items-center justify-between">
                  <div class="flex-1">
                    <div class="flex items-center gap-2">
                      <span class="font-medium">{{ strategy.strategyName }}</span>
                      <span
                        class="px-2 py-0.5 text-xs rounded"
                        :class="{
                          'bg-green-100 text-green-800': strategy.strategyType === '规则',
                          'bg-orange-100 text-orange-800': strategy.strategyType === '算法',
                        }"
                      >
                        {{ strategy.strategyType }}
                      </span>
                      <span
                        v-if="strategy.strategyId === decisionData?.strategyDto?.strategyId"
                        class="px-2 py-0.5 text-xs bg-blue-100 text-blue-800 rounded"
                      >
                        已选中
                      </span>
                    </div>
                    <p class="text-sm text-gray-600 mt-1 line-clamp-2">
                      {{ strategy.description }}
                    </p>
                  </div>
                  <div class="flex flex-col items-end gap-1 ml-4">
                    <div class="flex items-center gap-3 text-sm">
                      <div class="text-center">
                        <div class="font-semibold text-green-600">
                          {{ strategy.ucbExploitation?.toFixed(3) || '0' }}
                        </div>
                        <div class="text-xs text-gray-500">开发值</div>
                      </div>
                      <div class="text-center">
                        <div class="font-semibold text-orange-600">
                          {{ strategy.ucbExploration?.toFixed(3) || '999.999' }}
                        </div>
                        <div class="text-xs text-gray-500">探索值</div>
                      </div>
                      <div class="text-center">
                        <div class="font-semibold text-blue-600">
                          {{ strategy.selectNum }}/{{ strategy.totalNum }}
                        </div>
                        <div class="text-xs text-gray-500">选中次数</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>

      <!-- 方案和详细信息区域 -->
      <div class="px-4 pb-4">
        <!-- 可折叠的方案细节对比 -->
        <div class="border-t border-gray-200 pt-4">
          <button
            @click="showPlanDetails = !showPlanDetails"
            class="flex items-center justify-between w-full text-sm text-gray-600 hover:text-gray-900"
          >
            <span
              >方案细节对比 ({{
                decisionData?.decisionData?.planDtoList?.length || 0
              }}
              个方案)</span
            >
            <svg
              class="w-4 h-4 transition-transform duration-200"
              :class="{ 'rotate-180': showPlanDetails }"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M19 9l-7 7-7-7"
              ></path>
            </svg>
          </button>

          <div v-if="showPlanDetails" class="mt-3">
            <div
              v-if="(decisionData?.decisionData?.planDtoList?.length || 0) > 2"
              class="flex items-center justify-between mb-3"
            >
              <button
                @click="showAllPlans = !showAllPlans"
                class="text-xs text-blue-600 hover:text-blue-800"
              >
                {{ showAllPlans ? '收起' : '展开全部方案' }}
              </button>
            </div>

            <div class="space-y-3">
              <template v-for="plan in displayedPlans" :key="plan.strategyId">
                <div
                  class="border rounded-lg p-3 transition-all duration-200"
                  :class="{
                    'border-blue-300 bg-blue-50':
                      plan.strategyId === decisionData?.strategyDto?.strategyId,
                    'border-gray-200 bg-white':
                      plan.strategyId !== decisionData?.strategyDto?.strategyId,
                  }"
                >
                  <!-- 方案头部信息 -->
                  <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center gap-2">
                      <span class="text-sm font-medium text-gray-900"
                        >策略 {{ plan.strategyId }}</span
                      >
                      <span
                        v-if="plan.strategyId === decisionData?.strategyDto?.strategyId"
                        class="px-2 py-0.5 text-xs bg-blue-100 text-blue-800 rounded"
                      >
                        当前选中
                      </span>
                    </div>
                    <div class="text-xs text-gray-500">
                      {{ plan.actualGenerationTime / 1000 }}ms
                    </div>
                  </div>

                  <div class="text-xs text-gray-600 mb-2">
                    {{ getStrategyName(plan.strategyId) }}
                  </div>

                  <!-- 方案指标 -->
                  <div class="grid grid-cols-4 gap-2 mb-2">
                    <div class="bg-green-50 rounded p-1.5 text-center">
                      <div class="text-xs font-bold text-green-600">
                        {{ plan.totalBenefit?.toFixed(2) || '0' }}
                      </div>
                      <div class="text-xs text-green-700">总收益</div>
                    </div>
                    <div class="bg-red-50 rounded p-1.5 text-center">
                      <div class="text-xs font-bold text-red-600">{{ plan.totalCost || '0' }}</div>
                      <div class="text-xs text-red-700">总成本</div>
                    </div>
                    <div class="bg-purple-50 rounded p-1.5 text-center">
                      <div class="text-xs font-bold text-purple-600">
                        {{ plan.auctionPrice?.toFixed(4) || '0' }}
                      </div>
                      <div class="text-xs text-purple-700">拍卖价格</div>
                    </div>
                    <div class="bg-orange-50 rounded p-1.5 text-center">
                      <div class="text-xs font-bold text-orange-600">
                        {{
                          plan.totalCost && plan.totalCost > 0
                            ? (plan.totalBenefit / plan.totalCost).toFixed(3)
                            : '0'
                        }}
                      </div>
                      <div class="text-xs text-orange-700">收益比</div>
                    </div>
                  </div>

                  <!-- 目标-资源分配 -->
                  <div class="bg-gray-50 rounded p-2">
                    <div class="text-xs font-medium text-gray-700 mb-1">目标-资源分配</div>
                    <div class="space-y-1">
                      <div
                        v-for="result in plan.result"
                        :key="`${result.targetId}-${result.resourceId}`"
                        class="flex items-center justify-between text-xs"
                      >
                        <div class="flex items-center gap-1">
                          <span class="font-medium text-blue-600">目标{{ result.targetId }}</span>
                          <span>→</span>
                          <span class="font-medium text-green-600"
                            >资源{{ result.resourceId }}</span
                          >
                        </div>
                        <div class="text-gray-500">
                          {{ Math.round(result.distanceForTR) }}m |
                          {{ Math.round(result.distanceForTZ) }}m
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>

        <!-- 可折叠的详细信息 -->
        <div class="border-t border-gray-200 pt-4 mt-4">
          <button
            @click="showDetails = !showDetails"
            class="flex items-center justify-between w-full text-sm text-gray-600 hover:text-gray-900"
          >
            <span>详细信息</span>
            <svg
              class="w-4 h-4 transition-transform duration-200"
              :class="{ 'rotate-180': showDetails }"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M19 9l-7 7-7-7"
              ></path>
            </svg>
          </button>

          <div v-if="showDetails" class="mt-3 space-y-3">
            <!-- 事件基本信息 -->
            <div class="bg-blue-50 rounded-lg p-3">
              <h5 class="font-medium text-blue-800 mb-2">事件信息</h5>
              <div class="grid grid-cols-2 gap-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-600">事件ID:</span>
                  <span class="font-medium">{{ decisionData?.event?.eventId || '-' }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600">场景ID:</span>
                  <span class="font-medium">{{ decisionData?.event?.scenarioId || '-' }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600">样本ID:</span>
                  <span class="font-medium">{{ decisionData?.sampleId || '-' }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600">树版本:</span>
                  <span class="font-medium">{{ decisionData?.treeVersion || '-' }}</span>
                </div>
              </div>
              <div class="mt-2 text-xs text-gray-500">
                创建时间:
                {{
                  decisionData?.createdAt
                    ? new Date(decisionData.createdAt).toLocaleString('zh-CN')
                    : '-'
                }}
              </div>
            </div>

            <!-- 区域信息 -->
            <div class="bg-green-50 rounded-lg p-3">
              <h5 class="font-medium text-green-800 mb-2">
                区域信息 ({{ decisionData?.event?.zoneInfo?.zone_number || 0 }}个区域)
              </h5>
              <div class="mb-3 grid grid-cols-2 gap-4 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-600">可见度:</span>
                  <span class="font-medium">{{
                    decisionData?.event?.zoneInfo?.visibility || '-'
                  }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600">坐标:</span>
                  <span class="font-medium">
                    ({{ decisionData?.event?.zoneInfo?.latitude?.toFixed(4) || '-' }},
                    {{ decisionData?.event?.zoneInfo?.longitude?.toFixed(4) || '-' }})
                  </span>
                </div>
              </div>
              <div class="space-y-2">
                <div
                  v-for="zone in decisionData?.event?.zoneInfo?.zones"
                  :key="zone.id_zone"
                  class="flex justify-between items-center text-sm border-l-2 border-green-200 pl-3"
                >
                  <span class="font-medium text-green-700">区域{{ zone.id_zone }}</span>
                  <div class="text-gray-600">
                    位置: ({{ Math.round(zone.x_zone) }}, {{ Math.round(zone.y_zone) }}) | 半径:
                    {{ zone.radius }}m
                  </div>
                </div>
              </div>
            </div>

            <!-- 目标信息 -->
            <div class="bg-yellow-50 rounded-lg p-3">
              <h5 class="font-medium text-yellow-800 mb-2">
                目标信息 ({{ decisionData?.event?.targetInfo?.target_number || 0 }}个)
              </h5>
              <div class="grid grid-cols-1 gap-2">
                <div
                  v-for="target in decisionData?.event?.targetInfo?.targets"
                  :key="target.id_target"
                  class="flex justify-between items-center text-sm"
                >
                  <span class="font-medium">目标{{ target.id_target }}</span>
                  <div class="text-gray-600">
                    位置: ({{ Math.round(target.x_target) }}, {{ Math.round(target.y_target) }}) |
                    速度: {{ target.speed }}m/s | 高度: {{ target.height }}m
                  </div>
                </div>
              </div>
            </div>

            <!-- 资源信息 -->
            <div class="bg-purple-50 rounded-lg p-3">
              <h5 class="font-medium text-purple-800 mb-2">
                可用资源 ({{
                  decisionData?.event?.resourceInfo?.resources?.filter((r) => r.available).length ||
                  0
                }}个)
              </h5>
              <div class="grid grid-cols-1 gap-2">
                <div
                  v-for="resource in decisionData?.event?.resourceInfo?.resources?.filter(
                    (r) => r.available,
                  )"
                  :key="resource.id_resource"
                  class="flex justify-between items-center text-sm"
                >
                  <div class="flex items-center gap-2">
                    <span class="font-medium">资源{{ resource.id_resource }}</span>
                    <span class="px-1.5 py-0.5 text-xs bg-purple-100 text-purple-700 rounded">{{
                      resource.type
                    }}</span>
                  </div>
                  <div class="text-gray-600">
                    位置: ({{ Math.round(resource.x_resource) }},
                    {{ Math.round(resource.y_resource) }}) | 半径: {{ resource.radius }}m
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  decisionData: {
    type: Object,
    default: () => ({}),
  },
});

const showAllStrategies = ref(false);
const showAllPlans = ref(false);
const showStrategyComparison = ref(false); // 默认折叠
const showPlanDetails = ref(false);
const showDetails = ref(false);

// 策略显示逻辑
const displayedStrategies = computed(() => {
  const strategies = props.decisionData?.decisionData?.strategyDtoList || [];
  if (showAllStrategies.value) {
    return strategies;
  }
  // 默认只显示前2个策略，优先显示已选中的
  const selected = strategies.find(
    (s) => s.strategyId === props.decisionData?.strategyDto?.strategyId,
  );
  const others = strategies
    .filter((s) => s.strategyId !== props.decisionData?.strategyDto?.strategyId)
    .slice(0, 1);
  return selected ? [selected, ...others] : strategies.slice(0, 3);
});

// 方案显示逻辑
const displayedPlans = computed(() => {
  const plans = props.decisionData?.decisionData?.planDtoList || [];
  if (showAllPlans.value) {
    return plans;
  }
  // 默认只显示前2个方案，优先显示当前选中的
  const selected = plans.find((p) => p.strategyId === props.decisionData?.strategyDto?.strategyId);
  const others = plans
    .filter((p) => p.strategyId !== props.decisionData?.strategyDto?.strategyId)
    .slice(0, 1);
  return selected ? [selected, ...others] : plans.slice(0, 2);
});

// 获取策略名称
const getStrategyName = (strategyId) => {
  const strategy = props.decisionData?.decisionData?.strategyDtoList?.find(
    (s) => s.strategyId === strategyId,
  );
  return strategy?.strategyName || `策略${strategyId}`;
};
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
  overflow: hidden;
}
</style>
