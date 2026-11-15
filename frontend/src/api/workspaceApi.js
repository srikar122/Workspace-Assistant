import axiosClient from './axiosClient';

export const workspaceApi = {
  fetchEvents: () => axiosClient.get('/events'),
  fetchTasks: () => axiosClient.get('/tasks'),
  fetchHistory: (userId = 'default-exec') => axiosClient.get(`/history?userId=${userId}`),
  sendChatPrompt: (payload) => axiosClient.post('/chat', payload),
  completeTask: (taskId) => axiosClient.patch(`/tasks/${taskId}/complete`),
  fetchAuthStatus: () => axiosClient.get('/auth/status'),
  logoutSession: () => axiosClient.post('/auth/logout'),
};