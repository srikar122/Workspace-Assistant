import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { workspaceApi } from '../api/workspaceApi';

export const WORKSPACE_KEYS = {
  events: ['workspace', 'events'],
  tasks: ['workspace', 'tasks'],
  history: (userId) => ['workspace', 'history', userId],
};

export function useEventsQuery() {
  return useQuery({
    queryKey: WORKSPACE_KEYS.events,
    queryFn: workspaceApi.fetchEvents,
  });
}

export function useTasksQuery() {
  return useQuery({
    queryKey: WORKSPACE_KEYS.tasks,
    queryFn: workspaceApi.fetchTasks,
  });
}

export function useChatHistoryQuery(userId) {
  return useQuery({
    queryKey: WORKSPACE_KEYS.history(userId),
    queryFn: () => workspaceApi.fetchHistory(userId),
    enabled: Boolean(userId),
  });
}

export function useSendPromptMutation(userId) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (message) =>
      workspaceApi.sendChatPrompt({ userId, message }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: WORKSPACE_KEYS.history(userId) });
      queryClient.invalidateQueries({ queryKey: WORKSPACE_KEYS.events });
      queryClient.invalidateQueries({ queryKey: WORKSPACE_KEYS.tasks });
    },
  });
}

export function useCompleteTaskMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (taskId) => workspaceApi.completeTask(taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: WORKSPACE_KEYS.tasks });
    },
  });
}