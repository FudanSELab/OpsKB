<template>
  <div class="w-72 hidden md:block">
    <div class="h-[calc(100vh-96px)] bg-gray-50 my-4 rounded-md flex flex-col">
      <div class="px-2 pt-3">
        <p class="font-bold text-center">决策场景</p>
      </div>
      <div class="flex-1 overflow-y-auto">
        <div v-if="loading" class="flex justify-center py-4">
          <span class="loading loading-spinner loading-md"></span>
        </div>
        <div v-else-if="error" class="text-red-500 text-center py-4">
          {{ error }}
        </div>
        <ul v-else class="menu rounded-box w-full">
          <li v-for="scenario in scenarios" :key="scenario.scenarioId">
            <a>{{ scenario.scenarioName || `场景${scenario.scenarioId}` }}</a>
            <ul>
              <li v-for="version in scenario.treeVersions" :key="version">
                <a
                  @click="$emit('select', scenario.scenarioId, version)"
                  :class="{
                    'font-bold':
                      currentScenarioId === scenario.scenarioId && currentTreeVersion === version,
                  }"
                >
                  {{ version }}
                  <span
                    v-if="
                      currentScenarioId === scenario.scenarioId && currentTreeVersion === version
                    "
                    class="badge badge-primary"
                    >当前</span
                  >
                </a>
              </li>
            </ul>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  scenarios: {
    type: Array,
    default: () => [],
  },
  currentScenarioId: {
    type: Number,
    default: null,
  },
  currentTreeVersion: {
    type: String,
    default: '',
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: null,
  },
});

defineEmits(['select']);
</script>
