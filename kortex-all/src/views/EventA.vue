<template>
  <div class="h-[calc(100vh-64px)]">
    <div class="max-w-screen-xl mx-auto">
      <div class="flex h-full">
        <!-- 左侧边栏 -->
        <div class="w-72 hidden md:block">
          <div class="max-h-[calc(100vh-64px)] py-4 px-2 overflow-auto sticky top-[64px]">
            <!-- 搜索框 -->
            <label class="input input-bordered input-sm flex items-center gap-2 mb-4">
              <input
                type="text"
                class="grow outline-none ring-0"
                placeholder="搜索事件..."
                v-model="searchQuery"
                @keyup.enter="handleSearch"
                @input="onSearchInput"
              />
              <!-- 清空按钮 -->
              <button
                v-if="searchQuery"
                @click="clearSearchInput"
                class="cursor-pointer hover:text-error"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 16 16"
                  fill="currentColor"
                  class="h-4 w-4 opacity-70"
                >
                  <path
                    d="M5.28 4.22a.75.75 0 0 0-1.06 1.06L6.94 8l-2.72 2.72a.75.75 0 1 0 1.06 1.06L8 9.06l2.72 2.72a.75.75 0 1 0 1.06-1.06L9.06 8l2.72-2.72a.75.75 0 0 0-1.06-1.06L8 6.94 5.28 4.22Z"
                  />
                </svg>
              </button>
              <!-- 搜索按钮 -->
              <button @click="handleSearch" class="cursor-pointer hover:text-primary">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  viewBox="0 0 16 16"
                  fill="currentColor"
                  class="h-4 w-4 opacity-70"
                >
                  <path
                    fill-rule="evenodd"
                    d="M9.965 11.026a5 5 0 1 1 1.06-1.06l2.755 2.754a.75.75 0 1 1-1.06 1.06l-2.755-2.754ZM10.5 7a3.5 3.5 0 1 1-7 0 3.5 3.5 0 0 1 7 0Z"
                    clip-rule="evenodd"
                  />
                </svg>
              </button>
            </label>
            <!-- 事件列表 -->
            <div>
              <ul class="menu menu-xs bg-base-200 rounded-lg w-full">
                <p class="text-center font-bold pt-1 pb-2">事件列表</p>
                <div class="h-[calc(100vh-192px)] flex flex-col justify-between">
                  <div class="h-[calc(100vh-220px)] overflow-auto">
                    <li
                      v-for="(event, index) in eventList"
                      :key="event.eventId"
                      :class="{
                        'bg-blue-100 text-blue-700 font-bold': selectedEvent === event.eventId,
                        'cursor-pointer': true,
                      }"
                      @click="handleEventSelect(event.eventId)"
                    >
                      <a>
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke-width="1.5"
                          stroke="currentColor"
                          class="h-4 w-4"
                        >
                          <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z"
                          />
                        </svg>
                        <div class="text-left">
                          <div class="font-medium">事件 {{ event.eventId }}</div>
                          <div class="text-xs text-gray-500">{{ event.timestamp }}</div>
                          <div class="text-xs text-gray-400 mt-1">
                            防区: {{ event.zoneCount }} | 目标: {{ event.targetCount }} | 资源:
                            {{ event.resourceCount }}
                          </div>
                        </div>
                      </a>
                    </li>
                  </div>
                  <!-- 分页控件 -->
                  <div
                    class="flex justify-between items-center my-2 px-2"
                    v-if="pagination.total > 0"
                  >
                    <div class="text-xs text-gray-500">共 {{ pagination.total }} 条</div>
                    <div class="flex gap-1">
                      <button
                        class="btn btn-xs"
                        :disabled="pagination.current <= 1"
                        @click="changePage(pagination.current - 1)"
                      >
                        上一页
                      </button>
                      <span class="text-xs self-center px-2">
                        {{ pagination.current }}/{{ Math.ceil(pagination.total / pagination.size) }}
                      </span>
                      <button
                        class="btn btn-xs"
                        :disabled="
                          pagination.current >= Math.ceil(pagination.total / pagination.size)
                        "
                        @click="changePage(pagination.current + 1)"
                      >
                        下一页
                      </button>
                    </div>
                  </div>
                </div>
              </ul>
            </div>
          </div>
        </div>

        <!-- 中间主内容区 -->
        <div class="flex-1 min-h-0 bg-white">
          <div class="px-2">
            <!-- 顶部留空区域 -->
            <div role="tablist" class="tabs tabs-boxed tabs-sm my-4 w-[20%] mx-auto">
              <a
                role="tab"
                class="tab"
                :class="{ 'tab-active': activeTab === 'Attact' }"
                @click="activeTab = 'Attact'"
                >Attact</a
              >
            </div>

            <!-- 主要内容区域 -->
            <div class="bg-gray-50 rounded-lg h-[calc(100vh-145px)] p-4 relative">
              <div class="h-full">
                <div id="map-container" class="w-full h-full rounded-lg"></div>

                <!-- 地图控制面板 -->
                <div
                  class="absolute top-6 right-6 bg-white rounded-lg shadow-lg p-3 min-w-[180px] z-[998]"
                >
                  <h4 class="font-medium text-sm mb-3 text-gray-700">图层控制</h4>

                  <!-- 防区辐射范围控制 -->
                  <div class="space-y-2 mb-3">
                    <div class="text-xs font-medium text-gray-600 mb-1">防区辐射范围</div>

                    <label class="flex items-center text-xs cursor-pointer">
                      <input
                        type="checkbox"
                        v-model="layerControls.showRadarRange"
                        @change="toggleLayerVisibility('radar')"
                        class="checkbox checkbox-xs mr-2"
                      />
                      <span class="w-3 h-3 rounded-full bg-blue-500 mr-2"></span>
                      雷达感应区 (5km)
                    </label>

                    <label class="flex items-center text-xs cursor-pointer">
                      <input
                        type="checkbox"
                        v-model="layerControls.showOpticalRange"
                        @change="toggleLayerVisibility('optical')"
                        class="checkbox checkbox-xs mr-2"
                      />
                      <span class="w-3 h-3 rounded-full bg-orange-500 mr-2"></span>
                      光电识别区 (3km)
                    </label>

                    <label class="flex items-center text-xs cursor-pointer">
                      <input
                        type="checkbox"
                        v-model="layerControls.showInterferenceRange"
                        @change="toggleLayerVisibility('interference')"
                        class="checkbox checkbox-xs mr-2"
                      />
                      <span class="w-3 h-3 rounded-full bg-red-500 mr-2"></span>
                      干涉区 (2km)
                    </label>
                  </div>

                  <!-- 资源辐射范围控制 -->
                  <div class="border-t pt-2">
                    <div class="text-xs font-medium text-gray-600 mb-1">资源辐射范围</div>
                    <label class="flex items-center text-xs cursor-pointer">
                      <input
                        type="checkbox"
                        v-model="layerControls.showResourceRange"
                        @change="toggleLayerVisibility('resource')"
                        class="checkbox checkbox-xs mr-2"
                      />
                      <span class="w-3 h-3 rounded-full bg-green-500 mr-2"></span>
                      资源作用范围
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧边栏 -->
        <div class="w-72 hidden lg:block">
          <h3 class="font-bold text-lg text-center my-4 h-[32px]">生成配置</h3>
          <div class="px-2 rounded-md">
            <!-- 配置区域 -->
            <div class="bg-blue-100 h-[calc(100vh-145px)] rounded-lg p-4 overflow-auto">
              <!-- 防区配置 -->
              <div class="mb-4">
                <div class="flex justify-between items-center mb-2">
                  <label class="block font-medium">防区配置</label>
                  <button class="btn btn-xs btn-outline btn-primary" @click="openZoneModal()">
                    添加
                  </button>
                </div>
                <div class="space-y-1 max-h-36 overflow-y-auto overflow-x-hidden">
                  <div
                    v-for="zone in configData.zones"
                    :key="zone.id"
                    class="flex justify-between items-center bg-white p-2 rounded text-xs"
                  >
                    <span class="truncate">{{ zone.id }} </span>
                    <div class="flex flex-col">
                      <span class="truncate block"> {{ zone.center.longitude }} </span>
                      <span class="truncate block"> {{ zone.center.latitude }} </span>
                    </div>
                    <div class="flex gap-1">
                      <button
                        class="btn btn-xs btn-ghost text-blue-600"
                        @click="openZoneModal(zone)"
                      >
                        编辑
                      </button>
                      <button
                        class="btn btn-xs btn-ghost text-red-600"
                        @click="deleteZone(zone.id)"
                      >
                        删除
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 资源配置 -->
              <div class="mb-4">
                <div class="flex justify-between items-center mb-2">
                  <label class="block font-medium">资源配置</label>
                  <div class="flex gap-1">
                    <button class="btn btn-xs btn-outline btn-primary" @click="openResourceModal()">
                      添加
                    </button>
                    <!-- <button
                      class="btn btn-xs btn-outline btn-primary"
                      @click="getResourcesFromServer()"
                    >
                      获取
                    </button> -->
                  </div>
                </div>
                <div class="space-y-1 max-h-36 overflow-y-auto overflow-x-hidden">
                  <div
                    v-for="resource in configData.resources"
                    :key="resource.id"
                    class="bg-white p-2 rounded text-xs"
                  >
                    <div class="flex justify-between items-center">
                      <div>
                        <div class="font-medium">
                          {{ resource.id }}:
                          {{ resourcePresets[resource.type]?.name || resource.type }}
                        </div>
                      </div>
                      <div class="flex gap-1 ml-2">
                        <button
                          class="btn btn-xs btn-ghost text-blue-600"
                          @click="openResourceModal(resource)"
                        >
                          编辑
                        </button>
                        <button
                          class="btn btn-xs btn-ghost text-red-600"
                          @click="deleteResource(resource.id)"
                        >
                          删除
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 目标配置 -->
              <div class="mb-4">
                <label class="block font-medium mb-1">目标配置</label>
                <select
                  class="select select-bordered select-sm w-full"
                  v-model="configData.target_config"
                >
                  <option disabled value="">请选择入侵规则</option>
                  <option value="SINGLE_RECON">单机侦察入侵</option>
                  <option value="DUAL_RECON">双机协同侦察</option>
                  <option value="SMALL_FORMATION">小型编队入侵</option>
                  <option value="MIXED_PENETRATION">混合编队突防</option>
                  <option value="HIGH_SPEED_ASSAULT">高速突击</option>
                  <option value="0">系统自动选择</option>
                </select>
              </div>

              <!-- 环境配置 -->
              <div class="mb-4">
                <label class="block font-medium mb-1">环境配置（能见度）</label>
                <div class="space-y-2">
                  <div class="flex items-center justify-between">
                    <span class="text-sm">低</span>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      max="1"
                      class="input input-bordered input-xs w-20"
                      v-model.number="configData.environment.visibility.low"
                    />
                  </div>
                  <div class="flex items-center justify-between">
                    <span class="text-sm">中</span>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      max="1"
                      class="input input-bordered input-xs w-20"
                      v-model.number="configData.environment.visibility.medium"
                    />
                  </div>
                  <div class="flex items-center justify-between">
                    <span class="text-sm">高</span>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      max="1"
                      class="input input-bordered input-xs w-20"
                      v-model.number="configData.environment.visibility.high"
                    />
                  </div>
                  <div class="text-xs text-gray-600">
                    总和:
                    {{
                      (
                        configData.environment.visibility.low +
                        configData.environment.visibility.medium +
                        configData.environment.visibility.high
                      ).toFixed(2)
                    }}
                  </div>
                </div>
              </div>

              <!-- 生成配置 -->
              <div class="mb-4">
                <label class="block font-medium mb-1">生成模式</label>
                <select
                  class="select select-bordered select-sm w-full mb-2"
                  v-model="configData.generation.mode"
                >
                  <option value="interval">按时间间隔</option>
                  <option value="by_count">按数量</option>
                </select>

                <div v-if="configData.generation.mode === 'interval'" class="space-y-2">
                  <div>
                    <label class="block text-sm mb-1">间隔时间（秒）</label>
                    <input
                      type="number"
                      class="input input-bordered input-sm w-full"
                      v-model.number="configData.generation.interval.interval_seconds"
                      min="1"
                    />
                  </div>
                  <div>
                    <label class="block text-sm mb-1">每次生成数量</label>
                    <input
                      type="number"
                      class="input input-bordered input-sm w-full"
                      v-model.number="configData.generation.interval.per_interval_count"
                      min="1"
                    />
                  </div>
                </div>

                <div v-if="configData.generation.mode === 'by_count'">
                  <label class="block text-sm mb-1">总数量</label>
                  <input
                    type="number"
                    class="input input-bordered input-sm w-full"
                    v-model.number="configData.generation.count.total"
                    min="1"
                  />
                </div>
              </div>

              <div class="flex">
                <button class="btn btn-primary btn-sm flex-1" @click="handleGenerate">生成</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 防区配置弹窗 -->
  <div class="modal" :class="{ 'modal-open': modals.zoneModal }">
    <div class="modal-box">
      <h3 class="font-bold text-lg">{{ currentEdit.zone ? '编辑防区' : '添加防区' }}</h3>

      <div class="py-4 space-y-4">
        <div>
          <label class="block font-medium mb-1">防区ID</label>
          <input
            type="text"
            class="input input-bordered w-full"
            v-model="tempEdit.zone.id"
            placeholder="请输入防区ID"
          />
        </div>

        <div>
          <label class="block font-medium mb-1">经度</label>
          <input
            type="number"
            step="0.000001"
            class="input input-bordered w-full"
            v-model.number="tempEdit.zone.center.longitude"
            placeholder="请输入经度"
          />
        </div>

        <div>
          <label class="block font-medium mb-1">纬度</label>
          <input
            type="number"
            step="0.000001"
            class="input input-bordered w-full"
            v-model.number="tempEdit.zone.center.latitude"
            placeholder="请输入纬度"
          />
        </div>
      </div>

      <div class="modal-action">
        <button class="btn btn-ghost" @click="modals.zoneModal = false">取消</button>
        <button class="btn btn-primary" @click="saveZone">保存</button>
      </div>
    </div>
  </div>

  <!-- 资源配置弹窗 -->
  <div class="modal" :class="{ 'modal-open': modals.resourceModal }">
    <div class="modal-box max-w-2xl">
      <h3 class="font-bold text-lg">{{ currentEdit.resource ? '编辑资源' : '添加资源' }}</h3>

      <div class="py-4 space-y-4">
        <div>
          <label class="block font-medium mb-1">资源ID</label>
          <input
            type="text"
            class="input input-bordered w-full"
            v-model="tempEdit.resource.id"
            placeholder="请输入资源ID"
          />
        </div>

        <div>
          <label class="block font-medium mb-1">资源类型</label>
          <select
            class="select select-bordered w-full"
            v-model="tempEdit.resource.type"
            @change="onResourceTypeChange"
          >
            <option disabled value="">请选择资源类型</option>
            <option v-for="(preset, code) in resourcePresets" :key="code" :value="code">
              {{ preset.name }} ({{ code }})
            </option>
          </select>
        </div>

        <!-- 位置配置 -->
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block font-medium mb-1">经度</label>
            <input
              type="number"
              step="0.000001"
              class="input input-bordered w-full"
              v-model.number="tempEdit.resource.position.longitude"
              placeholder="请输入经度"
            />
          </div>

          <div>
            <label class="block font-medium mb-1">纬度</label>
            <input
              type="number"
              step="0.000001"
              class="input input-bordered w-full"
              v-model.number="tempEdit.resource.position.latitude"
              placeholder="请输入纬度"
            />
          </div>
        </div>

        <!-- 动态参数配置（只读显示） -->
        <!-- <div
          v-if="tempEdit.resource.type && resourcePresets[tempEdit.resource.type]"
          class="border-t pt-4"
        >
          <h4 class="font-medium mb-3">资源参数配置（系统预设）</h4>
          <div class="space-y-3">
            <div
              v-for="paramConfig in resourcePresets[tempEdit.resource.type].paramConfig"
              :key="paramConfig.key"
              class="grid grid-cols-2 gap-4 items-center"
            >
              <label class="font-medium">{{ paramConfig.label }}</label>
              <div
                class="bg-gray-100 border border-gray-300 rounded px-3 py-2 text-sm text-gray-700"
              >
                {{ resourcePresets[tempEdit.resource.type].defaultParams[paramConfig.key] }}
              </div>
            </div>
          </div>
          <div class="text-xs text-gray-500 mt-2">* 距离参数由系统预设，不可修改</div>
        </div> -->
      </div>

      <div class="modal-action">
        <button class="btn btn-ghost" @click="modals.resourceModal = false">取消</button>
        <button class="btn btn-primary" @click="saveResource">保存</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { generateEvents, getResources, getEventList, getEventDetail } from '../api/eventAPI.js';

// 当前选中的标签
const activeTab = ref('Attact');

// 当前选中的事件
const selectedEvent = ref(null);
// 当前事件的详细数据
const currentEventData = ref(null);
// 分页相关状态
const pagination = ref({
  current: 1,
  size: 20,
  total: 0,
});

// 地图实例
const map = ref(null);
const mapLayers = ref({
  zones: [],
  targets: [],
  resources: [],
  resourceRanges: [],
  zoneInterferenceRanges: [], // 2000m 干涉区
  zoneOpticalRanges: [], // 3000m 光电识别区
  zoneRadarRanges: [], // 5000m 雷达感应区
});

// 图层控制状态
const layerControls = ref({
  showInterferenceRange: true, // 显示干涉区
  showOpticalRange: false, // 显示光电识别区
  showRadarRange: false, // 显示雷达感应区
  showResourceRange: true, // 显示资源辐射范围
});

// 搜索相关
const searchQuery = ref('');

// 事件列表数据（从API获取）
const eventList = ref([]);

// 获取事件列表数据
const loadEventList = async () => {
  try {
    const response = await getEventList(pagination.value.current, pagination.value.size);
    if (response.code === 200) {
      // 转换API数据格式以兼容现有显示逻辑
      eventList.value = response.data.records.map((event) => ({
        eventId: event.eventId,
        timestamp: event.timestamp,
        zones: Array(event.zoneCount)
          .fill(null)
          .map((_, i) => ({ id: `zone_${i}` })),
        targets: Array(event.targetCount)
          .fill(null)
          .map((_, i) => ({ id: `target_${i}` })),
        resources: Array(event.resourceCount)
          .fill(null)
          .map((_, i) => ({ id: `resource_${i}` })),
        zoneCount: event.zoneCount,
        targetCount: event.targetCount,
        resourceCount: event.resourceCount,
        scenarioId: event.scenarioId,
      }));

      // 更新分页信息（确保数据类型为数字）
      pagination.value.current = parseInt(response.data.current) || 1;
      pagination.value.total = parseInt(response.data.total) || 0;

      // 如果没有选中的事件，选中第一个
      if (!selectedEvent.value && eventList.value.length > 0) {
        selectedEvent.value = eventList.value[0].eventId;
      }
    }
  } catch (error) {
    console.error('加载事件列表失败:', error);
    showError('加载事件列表失败: ' + error.message);
  }
};

// 获取事件详情数据
const loadEventDetail = async (eventId) => {
  try {
    const response = await getEventDetail(eventId);
    if (response.code === 200) {
      currentEventData.value = response.data;
      renderEventData();
    }
  } catch (error) {
    console.error('加载事件详情失败:', error);
    showError('加载事件详情失败: ' + error.message);
  }
};

// 获取当前选中的事件数据（使用详情数据）
const getSelectedEvent = () => {
  return currentEventData.value;
};

// 搜索方法
const handleSearch = () => {
  if (!searchQuery.value.trim()) {
    clearSearchInput();
    return;
  }
  console.log('搜索事件:', searchQuery.value);
};

const onSearchInput = () => {
  // 处理搜索输入
};

const clearSearchInput = () => {
  searchQuery.value = '';
  console.log('清空搜索');
};

// 事件选择处理
const handleEventSelect = async (eventId) => {
  selectedEvent.value = eventId;
  await loadEventDetail(eventId);
};

// 分页处理
const changePage = async (page) => {
  const maxPage = Math.ceil(pagination.value.total / pagination.value.size);

  // 检查页码边界
  if (page < 1 || page > maxPage) {
    return;
  }
  pagination.value.current = page;
  await loadEventList();
};

// 预设资源类型配置（后台固定配置，不开放给用户修改）
const resourcePresets = {
  R_D: {
    name: '导航诱骗器',
    code: 'R_D',
    defaultParams: {
      actionDistance: 1000, // 作用距离(米)
    },
  },
  R_W: {
    name: '围捕无人机',
    code: 'R_W',
    defaultParams: {
      actionDistance: 2000, // 最大距离(米)
    },
  },
  R_T: {
    name: '通信干扰器',
    code: 'R_T',
    defaultParams: {
      actionDistance: 1500, // 干扰距离(米)
    },
  },
  R_J: {
    name: '激光武器',
    code: 'R_J',
    defaultParams: {
      actionDistance: 800, // 作用距离(米)
    },
  },
};

// 配置数据
const configData = ref({
  zones: [
    { id: '001', center: { longitude: 118.76409, latitude: 32.02442 } },
    { id: '002', center: { longitude: 118.79521, latitude: 32.06097 } },
    { id: '003', center: { longitude: 118.74854, latitude: 32.0944 } },
    { id: '004', center: { longitude: 118.80906, latitude: 32.14096 } },
  ],
  resources: [
    {
      id: '1',
      type: 'R_D',
      position: { longitude: 118.7541, latitude: 32.0144 },
      params: { actionDistance: 1000 },
    },
    {
      id: '2',
      type: 'R_T',
      position: { longitude: 118.744, latitude: 32.0345 },
      params: { actionDistance: 1500 },
    },
    {
      id: '3',
      type: 'R_J',
      position: { longitude: 118.8052, latitude: 32.051 },
      params: { actionDistance: 800 },
    },
    {
      id: '4',
      type: 'R_D',
      position: { longitude: 118.75951, latitude: 32.1009 },
      params: { actionDistance: 1000 },
    },
    {
      id: '5',
      type: 'R_T',
      position: { longitude: 118.7485, latitude: 32.1044 },
      params: { actionDistance: 1500 },
    },
    {
      id: '6',
      type: 'R_J',
      position: { longitude: 118.8086, latitude: 32.1343 },
      params: { actionDistance: 800 },
    },
    {
      id: '7',
      type: 'R_D',
      position: { longitude: 118.8191, latitude: 32.241 },
      params: { actionDistance: 1000 },
    },
    {
      id: '8',
      type: 'R_T',
      position: { longitude: 118.909, latitude: 32.209 },
      params: { actionDistance: 1500 },
    },
  ],
  target_config: '0',
  environment: {
    visibility: { low: 0.2, medium: 0.5, high: 0.3 },
  },
  generation: {
    mode: 'interval', // "interval" 或 "by_count"
    interval: { interval_seconds: 300, per_interval_count: 2 },
    count: { total: 50 },
  },
});

// 弹窗控制状态
const modals = ref({
  zoneModal: false,
  resourceModal: false,
});

// 当前编辑的项目
const currentEdit = ref({
  zone: null,
  resource: null,
});

// 临时编辑数据
const tempEdit = ref({
  zone: { id: '', center: { longitude: '', latitude: '' } },
  resource: { id: '', type: '', position: { longitude: '', latitude: '' } },
});

// 防区配置方法
const openZoneModal = (zone = null) => {
  if (zone) {
    currentEdit.value.zone = zone;
    tempEdit.value.zone = { ...zone, center: { ...zone.center } };
  } else {
    currentEdit.value.zone = null;
    tempEdit.value.zone = { id: '', center: { longitude: '', latitude: '' } };
  }
  modals.value.zoneModal = true;
};

const saveZone = () => {
  if (
    !tempEdit.value.zone.id ||
    !tempEdit.value.zone.center.longitude ||
    !tempEdit.value.zone.center.latitude
  ) {
    alert('请填写完整的防区信息');
    return;
  }

  if (currentEdit.value.zone) {
    // 编辑现有防区
    const index = configData.value.zones.findIndex((z) => z.id === currentEdit.value.zone.id);
    if (index !== -1) {
      configData.value.zones[index] = { ...tempEdit.value.zone };
    }
  } else {
    // 添加新防区
    if (configData.value.zones.find((z) => z.id === tempEdit.value.zone.id)) {
      alert('防区ID已存在');
      return;
    }
    configData.value.zones.push({ ...tempEdit.value.zone });
  }

  modals.value.zoneModal = false;
};

const deleteZone = (zoneId) => {
  if (confirm('确定要删除此防区吗？')) {
    configData.value.zones = configData.value.zones.filter((z) => z.id !== zoneId);
  }
};

// 资源配置方法
const openResourceModal = (resource = null) => {
  if (resource) {
    currentEdit.value.resource = resource;
    tempEdit.value.resource = {
      ...resource,
      position: { ...resource.position },
      params: { ...(resource.params || {}) },
    };
  } else {
    currentEdit.value.resource = null;
    tempEdit.value.resource = {
      id: '',
      type: '',
      position: { longitude: '', latitude: '' },
      params: {},
    };
  }
  modals.value.resourceModal = true;
};

const saveResource = () => {
  if (
    !tempEdit.value.resource.id ||
    !tempEdit.value.resource.type ||
    !tempEdit.value.resource.position.longitude ||
    !tempEdit.value.resource.position.latitude
  ) {
    alert('请填写完整的资源信息');
    return;
  }

  // 确保使用预设的默认参数
  const resourceType = tempEdit.value.resource.type;
  if (resourceType && resourcePresets[resourceType]) {
    tempEdit.value.resource.params = { ...resourcePresets[resourceType].defaultParams };
  }

  if (currentEdit.value.resource) {
    // 编辑现有资源
    const index = configData.value.resources.findIndex(
      (r) => r.id === currentEdit.value.resource.id,
    );
    if (index !== -1) {
      configData.value.resources[index] = { ...tempEdit.value.resource };
    }
  } else {
    // 添加新资源
    if (configData.value.resources.find((r) => r.id === tempEdit.value.resource.id)) {
      alert('资源ID已存在');
      return;
    }
    configData.value.resources.push({ ...tempEdit.value.resource });
  }

  modals.value.resourceModal = false;
};

const deleteResource = (resourceId) => {
  if (confirm('确定要删除此资源吗？')) {
    configData.value.resources = configData.value.resources.filter((r) => r.id !== resourceId);
  }
};

// 资源类型变化时自动使用预设参数
const onResourceTypeChange = () => {
  const resourceType = tempEdit.value.resource.type;
  if (resourceType && resourcePresets[resourceType]) {
    // 参数由系统预设，不需要用户修改，直接使用默认值
    tempEdit.value.resource.params = { ...resourcePresets[resourceType].defaultParams };
  } else {
    tempEdit.value.resource.params = {};
  }
};

// 生成配置方法
const handleGenerate = async () => {
  try {
    // 验证配置数据
    if (!validateConfig()) {
      return;
    }

    // 显示加载状态
    const loadingToast = showLoading('正在生成事件数据...');

    // 调用API生成事件数据
    const result = await generateEvents(configData.value);

    // 隐藏加载状态
    hideLoading(loadingToast);

    // 显示成功消息
    showSuccess('事件数据生成成功！');
  } catch (error) {
    // 隐藏加载状态
    hideLoading();

    // 显示错误消息
    showError('生成事件数据失败: ' + error.message);
    console.error('生成失败:', error);
  }
};

// 配置数据验证
const validateConfig = () => {
  // 验证防区配置
  if (!configData.value.zones || configData.value.zones.length === 0) {
    showError('请至少配置一个防区');
    return false;
  }

  // 验证资源配置
  if (!configData.value.resources || configData.value.resources.length === 0) {
    showError('请至少配置一个资源');
    return false;
  }

  // 验证目标配置
  if (!configData.value.target_config) {
    showError('请选择目标配置');
    return false;
  }

  // 验证环境配置总和
  const visibilitySum =
    configData.value.environment.visibility.low +
    configData.value.environment.visibility.medium +
    configData.value.environment.visibility.high;

  if (Math.abs(visibilitySum - 1) > 0.001) {
    showError('能见度配置总和必须为1');
    return false;
  }

  // 验证生成配置
  if (configData.value.generation.mode === 'interval') {
    if (
      !configData.value.generation.interval.interval_seconds ||
      configData.value.generation.interval.interval_seconds <= 0
    ) {
      showError('请设置有效的时间间隔');
      return false;
    }
    if (
      !configData.value.generation.interval.per_interval_count ||
      configData.value.generation.interval.per_interval_count <= 0
    ) {
      showError('请设置有效的每次生成数量');
      return false;
    }
  } else if (configData.value.generation.mode === 'by_count') {
    if (!configData.value.generation.count.total || configData.value.generation.count.total <= 0) {
      showError('请设置有效的总数量');
      return false;
    }
  }

  return true;
};

// 从服务器获取资源配置方法
const getResourcesFromServer = async () => {
  try {
    // 显示加载状态
    const loadingToast = showLoading('正在获取资源数据...');

    // 调用API获取资源数据
    const resources = await getResources();

    // 隐藏加载状态
    hideLoading(loadingToast);

    // 更新配置数据中的资源列表（如果API返回了资源数据）
    if (resources && Array.isArray(resources)) {
      configData.value.resources = resources.map((resource) => ({
        id: resource.id || `RES_${Date.now()}`,
        type: resource.type || 'R_D',
        position: {
          longitude: resource.position?.longitude || 118.7615,
          latitude: resource.position?.latitude || 32.0278,
        },
        params: resource.params || { actionDistance: 1000 },
      }));

      showSuccess(`成功获取 ${resources.length} 个资源配置`);
    } else {
      showSuccess('资源数据获取完成');
    }
  } catch (error) {
    // 隐藏加载状态
    hideLoading();

    // 显示错误消息
    showError('获取资源数据失败: ' + error.message);
    console.error('获取资源失败:', error);
  }
};

// 用户反馈相关方法
let currentToast = null;

const showLoading = (message) => {
  // 创建简单的加载提示
  currentToast = document.createElement('div');
  currentToast.className =
    'fixed top-4 right-4 bg-blue-500 text-white px-4 py-2 rounded-lg shadow-lg z-[9999] flex items-center';
  currentToast.innerHTML = `
    <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
    </svg>
    ${message}
  `;
  document.body.appendChild(currentToast);
  return currentToast;
};

const hideLoading = (toast = currentToast) => {
  if (toast && toast.parentNode) {
    toast.parentNode.removeChild(toast);
  }
  currentToast = null;
};

const showSuccess = (message) => {
  showToast(message, 'success', 'bg-green-500');
};

const showError = (message) => {
  showToast(message, 'error', 'bg-red-500');
};

const showToast = (message, type, bgClass) => {
  const toast = document.createElement('div');
  toast.className = `fixed top-4 right-4 ${bgClass} text-white px-4 py-2 rounded-lg shadow-lg z-[9999] flex items-center`;

  const icon =
    type === 'success'
      ? '<svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path></svg>'
      : '<svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd"></path></svg>';

  toast.innerHTML = `${icon}${message}`;
  document.body.appendChild(toast);

  // 3秒后自动消失
  setTimeout(() => {
    if (toast && toast.parentNode) {
      toast.parentNode.removeChild(toast);
    }
  }, 3000);
};

// 地图相关方法
const initMap = async () => {
  await nextTick();

  // 初始化地图
  map.value = L.map('map-container', { attributionControl: false, zoomControl: false }).setView(
    [32.02442, 118.76409],
    12,
  );

  // 添加简洁的地图瓦片层
  L.tileLayer('/gray/{z}/{x}/{y}.png', {
    minZoom: 0,
    maxZoom: 15,
  }).addTo(map.value);

  // 初始化时渲染第一个事件的数据
  renderEventData();
};

const clearMapLayers = () => {
  // 清除之前的图层
  mapLayers.value.zones.forEach((layer) => map.value.removeLayer(layer));
  mapLayers.value.targets.forEach((layer) => map.value.removeLayer(layer));
  mapLayers.value.resources.forEach((layer) => map.value.removeLayer(layer));
  mapLayers.value.resourceRanges.forEach((layer) => map.value.removeLayer(layer));
  mapLayers.value.zoneInterferenceRanges.forEach((layer) => map.value.removeLayer(layer));
  mapLayers.value.zoneOpticalRanges.forEach((layer) => map.value.removeLayer(layer));
  mapLayers.value.zoneRadarRanges.forEach((layer) => map.value.removeLayer(layer));

  mapLayers.value.zones = [];
  mapLayers.value.targets = [];
  mapLayers.value.resources = [];
  mapLayers.value.resourceRanges = [];
  mapLayers.value.zoneInterferenceRanges = [];
  mapLayers.value.zoneOpticalRanges = [];
  mapLayers.value.zoneRadarRanges = [];
};

const renderEventData = () => {
  if (!map.value) return;

  const eventData = getSelectedEvent();
  if (!eventData) return;

  // 清除之前的图层
  clearMapLayers();

  // 存储防区边界点，用于计算地图视角
  const zonesBounds = [];

  // 渲染防区
  eventData.zones.forEach((zone) => {
    const lat = parseFloat(zone.center.latitude);
    const lng = parseFloat(zone.center.longitude);

    if (isNaN(lat) || isNaN(lng)) {
      console.warn('防区坐标无效:', zone.zoneId, zone.center);
      return;
    }

    // 渲染雷达感应区 (5000m) - 蓝色
    const radarCircle = L.circle([lat, lng], {
      color: '#3b82f6',
      fillColor: '#3b82f6',
      fillOpacity: 0.05,
      weight: 2,
      dashArray: '10, 5',
      radius: 5000,
    });
    if (layerControls.value.showRadarRange) {
      radarCircle.addTo(map.value);
    }
    radarCircle.bindPopup(`
      <strong>雷达感应区</strong><br>
      防区: ${zone.zoneId}<br>
      范围: 5000米<br>
      类型: 雷达感应
    `);
    mapLayers.value.zoneRadarRanges.push(radarCircle);

    // 渲染光电识别区 (3000m) - 橙色
    const opticalCircle = L.circle([lat, lng], {
      color: '#f97316',
      fillColor: '#f97316',
      fillOpacity: 0.08,
      weight: 2,
      dashArray: '8, 4',
      radius: 3000,
    });
    if (layerControls.value.showOpticalRange) {
      opticalCircle.addTo(map.value);
    }
    opticalCircle.bindPopup(`
      <strong>光电识别区</strong><br>
      防区: ${zone.zoneId}<br>
      范围: 3000米<br>
      类型: 光电识别
    `);
    mapLayers.value.zoneOpticalRanges.push(opticalCircle);

    // 渲染干涉区 (2000m) - 红色
    const interferenceCircle = L.circle([lat, lng], {
      color: '#ef4444',
      fillColor: '#ef4444',
      fillOpacity: 0.1,
      weight: 2,
      dashArray: '5, 3',
      radius: 2000,
    });
    if (layerControls.value.showInterferenceRange) {
      interferenceCircle.addTo(map.value);
    }
    interferenceCircle.bindPopup(`
      <strong>干涉区</strong><br>
      防区: ${zone.zoneId}<br>
      范围: 2000米<br>
      类型: 干涉区域
    `);
    mapLayers.value.zoneInterferenceRanges.push(interferenceCircle);

    // 渲染防区核心区域
    const circle = L.circle([lat, lng], {
      color: '#3b82f6',
      fillColor: '#3b82f6',
      fillOpacity: 0.2,
      weight: 3,
    }).addTo(map.value);

    circle.bindPopup(`
      <strong>防区: ${zone.zoneId}</strong><br>
      经纬度: ${zone.center.longitude}, ${zone.center.latitude}<br>
      能见度: ${zone.visibility || 'N/A'}
    `);

    mapLayers.value.zones.push(circle);

    // 计算防区的边界点（包含最大的雷达感应区半径）
    const radiusInDegrees = 5000 / 111320; // 使用最大半径计算边界
    zonesBounds.push([lat + radiusInDegrees, lng + radiusInDegrees]);
    zonesBounds.push([lat - radiusInDegrees, lng - radiusInDegrees]);
    zonesBounds.push([lat + radiusInDegrees, lng - radiusInDegrees]);
    zonesBounds.push([lat - radiusInDegrees, lng + radiusInDegrees]);
  });

  // 渲染目标
  eventData.targets.forEach((target) => {
    const lat = parseFloat(target.position.latitude);
    const lng = parseFloat(target.position.longitude);

    if (isNaN(lat) || isNaN(lng)) {
      console.warn('目标坐标无效:', target.targetId, target.position);
      return;
    }

    const marker = L.marker([lat, lng], {
      icon: L.divIcon({
        className: 'custom-div-icon',
        html: `
          <div style="
            width: 0;
            height: 0;
            border-left: 12px solid transparent;
            border-right: 12px solid transparent;
            border-bottom: 20px solid #ef4444;
            filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3));
            position: relative;
            margin: 0;
            padding: 0;
            transform: translate(-50%, -100%);
          "></div>
        `,
        iconSize: [24, 20],
        iconAnchor: [12, 20],
      }),
    }).addTo(map.value);

    marker.bindPopup(`
      <strong>目标: ${target.targetId}</strong><br>
      经纬度: ${target.position.longitude}, ${target.position.latitude}<br>
      速度: ${target.speed} m/s<br>
      高度: ${target.altitude}米<br>
      类型: ${target.type}<br>
      尺寸: ${target.size.length}×${target.size.width}×${target.size.height}米
    `);

    mapLayers.value.targets.push(marker);
  });

  // 渲染资源
  eventData.resources.forEach((resource) => {
    const resourceInfo = resourcePresets[resource.resourceType] || { name: resource.resourceType };

    const lat = parseFloat(resource.position.latitude);
    const lng = parseFloat(resource.position.longitude);

    if (isNaN(lat) || isNaN(lng)) {
      console.warn('资源坐标无效:', resource.resourceId, resource.position);
      return;
    }

    // 根据资源类型确定标识（统一绿色）
    const resourceConfig = {
      R_D: { color: '#10b981', label: 'D', name: '导航诱骗器' },
      R_W: { color: '#10b981', label: 'W', name: '围捕无人机' },
      R_T: { color: '#10b981', label: 'T', name: '通信干扰器' },
      R_J: { color: '#10b981', label: 'J', name: '激光武器' },
    };

    const config = resourceConfig[resource.resourceType] || {
      color: '#10b981',
      label: 'R',
      name: resource.resourceType,
    };

    // 获取资源作用距离（从attributes中获取）
    let actionDistance = 1000; // 默认值
    if (resource.attributes) {
      if (resource.attributes.actionDistance) {
        actionDistance = resource.attributes.actionDistance;
      } else if (resource.attributes.jamDistance) {
        actionDistance = resource.attributes.jamDistance;
      }
    }

    // 添加资源作用距离圆圈
    const actionCircle = L.circle([lat, lng], {
      color: '#10b981',
      fillColor: '#10b981',
      fillOpacity: 0.05,
      weight: 2,
      dashArray: '5, 5',
      radius: actionDistance,
    });
    if (layerControls.value.showResourceRange) {
      actionCircle.addTo(map.value);
    }

    actionCircle.bindPopup(`
      <strong>资源作用范围</strong><br>
      资源: ${resource.resourceId}<br>
      类型: ${config.name}<br>
      作用距离: ${actionDistance}米
    `);

    mapLayers.value.resourceRanges.push(actionCircle);

    // 添加资源标记
    const marker = L.marker([lat, lng], {
      icon: L.divIcon({
        className: 'custom-div-icon',
        html: `
          <div style="
            background-color: ${config.color};
            width: 20px;
            height: 20px;
            border-radius: 50%;
            border: 3px solid white;
            box-shadow: 0 2px 6px rgba(0,0,0,0.3);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 12px;
            color: white;
            font-weight: bold;
            position: relative;
            margin: 0;
            padding: 0;
            transform: translate(-50%, -50%);
          ">
            ${config.label}
          </div>
        `,
        iconSize: [26, 26],
        iconAnchor: [13, 13],
      }),
    }).addTo(map.value);

    marker.bindPopup(`
      <strong>资源: ${resource.resourceId}</strong><br>
      经纬度: ${resource.position.longitude}, ${resource.position.latitude}<br>
      类型: ${config.name} (${resource.resourceType})<br>
      作用距离: ${actionDistance}米
    `);

    mapLayers.value.resources.push(marker);
  });

  // 调整地图视角以显示所有防区范围
  if (zonesBounds.length > 0) {
    const bounds = L.latLngBounds(zonesBounds);
    map.value.fitBounds(bounds, { padding: [30, 30] });
  } else if (eventData.zones.length > 0) {
    // 如果没有计算出边界点，则以第一个防区为中心
    const firstZone = eventData.zones[0];
    const lat = parseFloat(firstZone.center.latitude);
    const lng = parseFloat(firstZone.center.longitude);
    map.value.setView([lat, lng], 13);
  }

  // 强制刷新地图以确保标记位置正确
  setTimeout(() => {
    if (map.value) {
      map.value.invalidateSize();
    }
  }, 100);
};

// 生命周期钩子
onMounted(async () => {
  await initMap();
  await loadEventList();
});

// 图层切换功能
const toggleLayerVisibility = (layerType) => {
  if (!map.value) return;

  switch (layerType) {
    case 'radar':
      mapLayers.value.zoneRadarRanges.forEach((layer) => {
        if (layerControls.value.showRadarRange) {
          layer.addTo(map.value);
        } else {
          map.value.removeLayer(layer);
        }
      });
      break;

    case 'optical':
      mapLayers.value.zoneOpticalRanges.forEach((layer) => {
        if (layerControls.value.showOpticalRange) {
          layer.addTo(map.value);
        } else {
          map.value.removeLayer(layer);
        }
      });
      break;

    case 'interference':
      mapLayers.value.zoneInterferenceRanges.forEach((layer) => {
        if (layerControls.value.showInterferenceRange) {
          layer.addTo(map.value);
        } else {
          map.value.removeLayer(layer);
        }
      });
      break;

    case 'resource':
      mapLayers.value.resourceRanges.forEach((layer) => {
        if (layerControls.value.showResourceRange) {
          layer.addTo(map.value);
        } else {
          map.value.removeLayer(layer);
        }
      });
      break;
  }
};

// 监听选中事件变化
watch(selectedEvent, () => {
  if (selectedEvent.value) {
    loadEventDetail(selectedEvent.value);
  }
});
</script>

<style scoped>
/* 地图容器样式 */
#map-container {
  border: 1px solid #e5e7eb;
}

/* 自定义标记图标样式 */
:deep(.custom-div-icon) {
  background: transparent !important;
  border: none !important;
  margin: 0 !important;
  padding: 0 !important;
  overflow: visible !important;
}

/* 确保标记容器稳定 */
:deep(.custom-div-icon div) {
  margin: 0 !important;
  padding: 0 !important;
  box-sizing: border-box !important;
  position: relative !important;
}

/* 防止标记在地图缩放时产生偏移 */
:deep(.leaflet-marker-icon) {
  transition: none !important;
}

/* 确保标记位置固定 */
:deep(.leaflet-zoom-anim .leaflet-marker-icon) {
  transform-origin: center center !important;
}

/* 地图弹窗样式优化 */
:deep(.leaflet-popup-content) {
  font-size: 14px;
  line-height: 1.4;
}

:deep(.leaflet-popup-content strong) {
  color: #1f2937;
  font-weight: 600;
}

/* 确保地图在容器中正确显示 */
:deep(.leaflet-container) {
  height: 100% !important;
  width: 100% !important;
  border-radius: 0.5rem;
}
</style>
