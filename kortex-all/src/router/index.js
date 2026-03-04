import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/knowledge',
    },
    {
      path: '/knowledge',
      component: () => import('../views/Knowledge.vue'),
    },
    {
      path: '/event-a',
      component: () => import('../views/EventA.vue'),
    },
    {
      path: '/decision',
      component: () => import('../views/Decision.vue'),
    },
    {
      path: '/tree-editor',
      component: () => import('../views/TreeEditor.vue'),
    },
  ],
});

export default router;
