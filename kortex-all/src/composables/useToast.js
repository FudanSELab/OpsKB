import { ref } from 'vue';

let toastIdCounter = 0;

export function useToast() {
  const toasts = ref([]);

  const showToast = (message, type = 'success') => {
    const toast = {
      id: ++toastIdCounter,
      message,
      type,
    };

    toasts.value.push(toast);

    // 自动移除 toast (3秒后)
    setTimeout(() => {
      removeToast(toast.id);
    }, 3000);
  };

  const removeToast = (id) => {
    const index = toasts.value.findIndex((toast) => toast.id === id);
    if (index > -1) {
      toasts.value.splice(index, 1);
    }
  };

  return {
    toasts,
    showToast,
    removeToast,
  };
}
