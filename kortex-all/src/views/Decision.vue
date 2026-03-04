<template>
  <div class="h-[calc(100vh-64px)]">
    <div class="max-w-screen-xl mx-auto">
      <div class="flex">
        <!-- 左侧边栏 -->
        <ScenarioSidebar
          :scenarios="scenariosData.scenarios.value"
          :current-scenario-id="scenariosData.currentScenarioId.value"
          :current-tree-version="scenariosData.currentTreeVersion.value"
          :loading="scenariosData.loading.value"
          :error="scenariosData.error.value"
          @select="handleScenarioSelect"
        />

        <!-- 中间主内容区 -->
        <div class="flex-1 min-h-0 h-[calc(100vh-96px)] m-4">
          <div class="h-full flex flex-col">
            <!-- 面包屑 -->
            <div class="breadcrumbs text-sm shrink-0">
              <ul>
                <li>策略生成</li>
                <li v-if="scenariosData.currentScenarioName.value">
                  <a>{{ scenariosData.currentScenarioName.value }}</a>
                </li>
                <li v-if="scenariosData.currentTreeVersion.value && rightTabActive !== 'events'">
                  {{ scenariosData.currentTreeVersion.value }}
                </li>
                <li v-if="rightTabActive === 'samples' && samplesData.currentSampleId.value">
                  样本{{ samplesData.currentSampleId.value }}
                </li>
                <li v-if="rightTabActive === 'events' && eventsData.currentEventId.value">
                  事件{{ eventsData.currentEventId.value }}
                </li>
              </ul>
            </div>

            <!-- 操作区 -->
            <div class="flex items-center justify-between">
              <div class="tabs tabs-boxed">
                <a
                  class="tab tab-sm"
                  :class="{ 'tab-active': samplesData.currentSampleType.value === 'training' }"
                  @click="handleSampleTypeChange('training')"
                >
                  训练
                </a>
                <a
                  class="tab tab-sm"
                  :class="{ 'tab-active': samplesData.currentSampleType.value === 'fact' }"
                  @click="handleSampleTypeChange('fact')"
                >
                  实战
                </a>
              </div>

              <div class="flex items-center gap-2">
                <template v-if="samplesData.currentSampleType.value === 'training'">
                  <button
                    class="btn btn-sm"
                    @click="handleImportExternalTree"
                    :disabled="importTreeLoading"
                  >
                    <span v-if="importTreeLoading" class="loading loading-spinner loading-xs"></span>
                    {{ importTreeLoading ? '更新中...' : '更新策略树' }}
                  </button>
                  <button
                    class="btn btn-sm"
                    :class="{
                      'btn-error': trainingData.isTraining.value,
                      'btn-primary': !trainingData.isTraining.value,
                    }"
                    @click="handleTrainingButtonClick"
                    :disabled="
                      !scenariosData.currentScenarioId.value ||
                      !scenariosData.currentTreeVersion.value
                    "
                  >
                    {{ trainingData.isTraining.value ? '停止训练' : '开始训练' }}
                  </button>
                </template>
              </div>
            </div>

            <!-- 决策树图表区域（仅在样本TAB时显示） -->
            <TreeChartSection
              v-if="rightTabActive === 'samples'"
              ref="treeChartSection"
              :tree-version="scenariosData.currentTreeVersion.value"
              :loading="treeChartData.loading.value"
              :error="treeChartData.error.value"
              @fullscreen="handleTreeFullscreen"
              @retry="handleTreeRetry"
              @update:collapsed="handleTreeCollapse"
            />

            <!-- 样本TAB：决策过程展示区 -->
            <DecisionProcess
              v-if="rightTabActive === 'samples'"
              :decision-data="samplesData.currentDecisionData.value"
              class="flex-1 mt-4 overflow-auto"
            />

            <!-- 事件TAB：事件决策流程展示区 -->
            <div v-if="rightTabActive === 'events'" class="flex-1 mt-4 overflow-auto">
              <EventDecisionFlow
                :event-detail="eventsData.currentEventDetail.value"
                :decision-flow="decisionFlowData.decisionFlow.value"
                :loading="decisionFlowData.loading.value"
                :scenario-id="scenariosData.currentScenarioId.value"
                :tree-version="scenariosData.currentTreeVersion.value"
                @strategy-search="handleStrategySearch"
                @ucb-select="handleUcbSelect"
                @plan-generate="handlePlanGenerate"
                @train-once="handleTrainOnce"
              />
            </div>
          </div>
        </div>

        <!-- 右侧主内容区 -->
        <SampleEventList
          :active-tab="rightTabActive"
          :scenario-name="scenariosData.currentScenarioName.value"
          :scenario-id="scenariosData.currentScenarioId.value"
          :tree-version="scenariosData.currentTreeVersion.value"
          :sample-list="samplesData.sampleList.value"
          :current-sample-id="samplesData.currentSampleId.value"
          :sample-loading="samplesData.loading.value"
          :sample-error="samplesData.error.value"
          :sample-pagination="samplesData.pagination.value"
          :event-list="eventsData.eventList.value"
          :current-event-id="eventsData.currentEventId.value"
          :event-loading="eventsData.loading.value"
          :event-error="eventsData.error.value"
          :event-pagination="eventsData.pagination.value"
          :current-sample-type="samplesData.currentSampleType.value"
          @tab-change="handleTabChange"
          @select-sample="handleSelectSample"
          @select-event="handleSelectEvent"
          @page-change="handlePageChange"
          @sample-type-change="handleSampleTypeChange"
        />
      </div>
    </div>

    <!-- 全屏策略树模态框 -->
    <TreeFullscreenModal
      ref="fullscreenModal"
      :visible="isTreeFullscreen"
      :tree-version="scenariosData.currentTreeVersion.value"
      :loading="treeChartData.loading.value"
      :error="treeChartData.error.value"
      @close="isTreeFullscreen = false"
      @retry="handleTreeRetry"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, onUnmounted } from 'vue';
import DecisionProcess from '../components/decision/DecisionProcess.vue';
import EventDecisionFlow from '../components/decision/EventDecisionFlow.vue';
import ScenarioSidebar from '../components/decision/ScenarioSidebar.vue';
import SampleEventList from '../components/decision/SampleEventList.vue';
import TreeChartSection from '../components/decision/TreeChartSection.vue';
import TreeFullscreenModal from '../components/decision/TreeFullscreenModal.vue';
import { importExternalTree } from '../api/decisionAPI.js';
import { useScenarios } from '../composables/decision/useScenarios.js';
import { useSamples } from '../composables/decision/useSamples.js';
import { useEvents } from '../composables/decision/useEvents.js';
import { useTraining } from '../composables/decision/useTraining.js';
import { useDecisionFlow } from '../composables/decision/useDecisionFlow.js';
import { useTreeChart } from '../composables/decision/useTreeChart.js';

// 使用组合式函数
const scenariosData = useScenarios();
const samplesData = useSamples();
const eventsData = useEvents();
const trainingData = useTraining();
const decisionFlowData = useDecisionFlow();
const treeChartData = useTreeChart();

// 组件引用
const treeChartSection = ref(null);
const fullscreenModal = ref(null);

// 页面状态
const rightTabActive = ref('samples');
const isTreeFullscreen = ref(false);
const importTreeLoading = ref(false);

// ========== 事件处理函数 ==========

// 选择场景和版本
const handleScenarioSelect = async (scenarioId, version) => {
  scenariosData.setCurrentScenarioAndVersion(scenarioId, version);

  // 重置分页
  samplesData.pagination.value.current = 1;
  eventsData.pagination.value.current = 1;

  // 停止之前的训练状态轮询
  trainingData.stopTrainingStatusPolling();

  // 同时加载决策树、样本列表、事件列表和训练状态
  await Promise.all([
    treeChartData.loadTreeData(scenarioId, version),
    samplesData.loadSampleList(scenarioId, version),
    eventsData.loadEventList(),
    trainingData.loadTrainingStatus(scenarioId),
  ]);

  // 展开树图（如果在样本TAB）
  if (treeChartSection.value && rightTabActive.value === 'samples') {
    treeChartSection.value.collapsed = false;
    nextTick(() => {
      if (treeChartSection.value?.chartContainer && treeChartData.treeData.value.length > 0) {
        treeChartData.treeChartRef.value = treeChartSection.value.chartContainer;
        treeChartData.initTreeChart();
      }
    });
  }

  // 根据当前激活的TAB，清空选中状态
  if (rightTabActive.value === 'samples') {
    samplesData.resetSamples();
  } else if (rightTabActive.value === 'events') {
    if (eventsData.eventList.value.length > 0) {
      await eventsData.loadEventDetail(eventsData.eventList.value[0].eventId);
      decisionFlowData.resetDecisionFlow('eventDetail');
    } else {
      eventsData.resetEvents();
      decisionFlowData.resetDecisionFlow();
    }
  }

  // 如果训练正在进行中，开始状态轮询
  if (trainingData.trainingStatus.value.running) {
    trainingData.startTrainingStatusPolling(scenarioId, async () => {
      await samplesData.loadSampleList(scenarioId, version);
    });
  }
};

// 选择样本
const handleSelectSample = async (sampleId) => {
  await samplesData.loadSampleDetail(sampleId);
  // 更新树图高亮
  updateTreeChart();
};

// 选择事件
const handleSelectEvent = async (eventId) => {
  await eventsData.loadEventDetail(eventId);
  decisionFlowData.resetDecisionFlow('eventDetail');
};

// TAB切换
const handleTabChange = async (newTab) => {
  rightTabActive.value = newTab;

  if (newTab === 'samples') {
    eventsData.resetEvents();
    samplesData.resetSamples();
    decisionFlowData.resetDecisionFlow();

    if (samplesData.sampleList.value.length === 0 && scenariosData.currentTreeVersion.value) {
      await samplesData.loadSampleList(
        scenariosData.currentScenarioId.value,
        scenariosData.currentTreeVersion.value,
      );
    }

    // 切换回样本tab时，重新初始化树图
    nextTick(() => {
      // 确保树图容器展开
      if (treeChartSection.value) {
        treeChartSection.value.collapsed = false;
      }

      if (treeChartSection.value?.chartContainer && treeChartData.treeData.value.length > 0) {
        // 先销毁旧的图表实例，确保重新创建
        treeChartData.disposeChart();
        // 设置新的容器引用
        treeChartData.treeChartRef.value = treeChartSection.value.chartContainer;
        // 重新初始化图表
        treeChartData.initTreeChart();
      }
    });
  } else if (newTab === 'events') {
    samplesData.resetSamples();

    if (eventsData.eventList.value.length === 0 && scenariosData.currentScenarioId.value) {
      await eventsData.loadEventList();
    }
  }
};

// 切换样本类型
const handleSampleTypeChange = async (type) => {
  samplesData.setSampleType(type);
  if (scenariosData.currentScenarioId.value && scenariosData.currentTreeVersion.value) {
    await samplesData.loadSampleList(
      scenariosData.currentScenarioId.value,
      scenariosData.currentTreeVersion.value,
    );
  }
};

// 分页处理
const handlePageChange = async (page) => {
  if (rightTabActive.value === 'samples') {
    samplesData.changePage(page);
    await samplesData.loadSampleList(
      scenariosData.currentScenarioId.value,
      scenariosData.currentTreeVersion.value,
    );
  } else if (rightTabActive.value === 'events') {
    eventsData.changePage(page);
    await eventsData.loadEventList();
  }
};

// ========== 决策流程相关函数 ==========

const handleStrategySearch = async () => {
  await decisionFlowData.handleStrategySearch(
    eventsData.currentEventId.value,
    scenariosData.currentTreeVersion.value,
  );
};

const handleUcbSelect = async () => {
  await decisionFlowData.handleUcbSelect(
    eventsData.currentEventId.value,
    scenariosData.currentTreeVersion.value,
  );
};

const handlePlanGenerate = async () => {
  await decisionFlowData.handlePlanGenerate(
    eventsData.currentEventId.value,
    scenariosData.currentTreeVersion.value,
  );
};

const handleTrainOnce = async () => {
  await decisionFlowData.handleTrainOnce(
    eventsData.currentEventId.value,
    scenariosData.currentTreeVersion.value,
    async () => {
      // 刷新样本列表
      if (rightTabActive.value === 'samples') {
        await samplesData.loadSampleList(
          scenariosData.currentScenarioId.value,
          scenariosData.currentTreeVersion.value,
        );
      }
    },
  );
};

// ========== 训练相关函数 ==========

const handleTrainingButtonClick = () => {
  if (trainingData.isTraining.value) {
    trainingData.handleStopTraining(scenariosData.currentScenarioId.value);
  } else {
    trainingData.handleStartTraining(
      scenariosData.currentScenarioId.value,
      scenariosData.currentTreeVersion.value,
      async () => {
        // 训练完成后刷新样本列表
        await samplesData.loadSampleList(
          scenariosData.currentScenarioId.value,
          scenariosData.currentTreeVersion.value,
        );
      },
    );
  }
};

// ========== 树图相关函数 ==========

const handleTreeFullscreen = () => {
  isTreeFullscreen.value = true;
  nextTick(() => {
    setTimeout(() => {
      initFullscreenTreeChart();
    }, 100);
  });
};

const handleTreeRetry = async () => {
  await treeChartData.loadTreeData(
    scenariosData.currentScenarioId.value,
    scenariosData.currentTreeVersion.value,
  );
  nextTick(() => {
    if (treeChartSection.value?.chartContainer && treeChartData.treeData.value.length > 0) {
      treeChartData.treeChartRef.value = treeChartSection.value.chartContainer;
      treeChartData.initTreeChart();
    }
  });
};

const handleTreeCollapse = (collapsed) => {
  if (!collapsed) {
    // 展开时调整图表大小
    nextTick(() => {
      setTimeout(() => {
        if (treeChartSection.value?.chartContainer) {
          treeChartData.resizeChart();
        }
      }, 300);
    });
  }
};

const updateTreeChart = () => {
  if (treeChartSection.value?.chartContainer && treeChartData.treeData.value.length > 0) {
    treeChartData.treeChartRef.value = treeChartSection.value.chartContainer;
    const targetTreeId = samplesData.currentDecisionData.value?.decisionData?.treeDto?.treeId;
    nextTick(() => {
      treeChartData.initTreeChart(targetTreeId);
    });
  }
};

const initFullscreenTreeChart = () => {
  const targetTreeId = samplesData.currentDecisionData.value?.decisionData?.treeDto?.treeId;
  treeChartData.initFullscreenTreeChart(
    fullscreenModal.value?.fullscreenChartContainer,
    targetTreeId,
  );
};

// ========== 其他函数 ==========

const handleImportExternalTree = async () => {
  importTreeLoading.value = true;
  try {
    const response = await importExternalTree();
    if (response.code === 200) {
      alert(response.data || '策略树更新成功');
      await scenariosData.loadScenarios();
    } else {
      throw new Error(response.message || '更新策略树失败');
    }
  } catch (err) {
    console.error('更新策略树失败:', err);
    alert('更新策略树失败: ' + err.message);
  } finally {
    importTreeLoading.value = false;
  }
};

// ========== 监听器 ==========

// 监听样本数据变化，更新树图
watch(
  () => samplesData.currentDecisionData.value,
  () => {
    updateTreeChart();
    if (isTreeFullscreen.value) {
      initFullscreenTreeChart();
    }
  },
  { deep: true },
);

// 监听全屏状态变化
watch(isTreeFullscreen, (isFullscreen) => {
  if (isFullscreen && treeChartData.treeData.value.length > 0) {
    nextTick(() => {
      setTimeout(() => {
        initFullscreenTreeChart();
      }, 100);
    });
  } else if (!isFullscreen) {
    treeChartData.disposeFullscreenChart();
  }
});

// ========== 生命周期 ==========

onMounted(async () => {
  await scenariosData.loadScenarios();

  // 设置树图引用
  if (treeChartSection.value?.chartContainer) {
    treeChartData.treeChartRef.value = treeChartSection.value.chartContainer;
  }
});

onUnmounted(() => {
  treeChartData.disposeChart();
  treeChartData.disposeFullscreenChart();
  trainingData.stopTrainingStatusPolling();
});
</script>

<style scoped>
/* 可以添加自定义样式 */
</style>
