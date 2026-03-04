import { reactive } from 'vue';

export function useConfirm() {
  const confirmModal = reactive({
    show: false,
    title: '确认操作',
    message: '',
    onConfirm: null,
    onCancel: null,
  });

  const showConfirm = (title, message, onConfirm, onCancel = null) => {
    confirmModal.title = title;
    confirmModal.message = message;
    confirmModal.onConfirm = onConfirm;
    confirmModal.onCancel = onCancel;
    confirmModal.show = true;
  };

  const closeConfirmModal = () => {
    confirmModal.show = false;
    confirmModal.title = '确认操作';
    confirmModal.message = '';
    confirmModal.onConfirm = null;
    confirmModal.onCancel = null;
  };

  const handleConfirm = () => {
    if (confirmModal.onConfirm) {
      confirmModal.onConfirm();
    }
    closeConfirmModal();
  };

  const handleCancel = () => {
    if (confirmModal.onCancel) {
      confirmModal.onCancel();
    }
    closeConfirmModal();
  };

  return {
    confirmModal,
    showConfirm,
    closeConfirmModal,
    handleConfirm,
    handleCancel,
  };
}
