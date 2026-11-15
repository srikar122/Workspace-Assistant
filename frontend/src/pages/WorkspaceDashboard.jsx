import React from 'react';
import { useQueryClient } from '@tanstack/react-query';
import Navbar from '../components/layout/Navbar';
import ChatContainer from '../components/chat/ChatContainer';
import CalendarSidebar from '../components/calendar/CalendarSidebar';
import TaskSidebar from '../components/tasks/TaskSidebar';
import { useAuth } from '../context/AuthContext';
import {
  useEventsQuery,
  useTasksQuery,
  useChatHistoryQuery,
  useSendPromptMutation,
  useCompleteTaskMutation,
} from '../hooks/useWorkspaceQueries';

export default function WorkspaceDashboard() {
  const { user } = useAuth();
  const userId = user?.userId || 'default-exec';

  const queryClient = useQueryClient();
  const { data: events = [], isLoading: eventsLoading, isFetching: eventsFetching } = useEventsQuery();
  const { data: tasks = [], isFetching: tasksFetching } = useTasksQuery();
  const { data: history = [] } = useChatHistoryQuery(userId);

  const sendPromptMutation = useSendPromptMutation(userId);
  const completeTaskMutation = useCompleteTaskMutation();

  const handleManualSync = () => {
    queryClient.invalidateQueries({ queryKey: ['workspace'] });
  };

  return (
    <div className="flex flex-col h-screen bg-slate-50 text-slate-900 overflow-hidden">
      <Navbar
        onManualSync={handleManualSync}
        isSyncing={eventsFetching || tasksFetching}
      />
      <main className="flex-1 flex overflow-hidden">
        <ChatContainer
          messages={history}
          isSending={sendPromptMutation.isPending}
          errorMessage={sendPromptMutation.error?.message}
          onSendPrompt={(msg) => sendPromptMutation.mutate(msg)}
        />
        <aside className="w-96 flex flex-col bg-white shadow-sm z-10">
          <CalendarSidebar events={events} isLoading={eventsLoading} />
          <TaskSidebar
            tasks={tasks}
            onCompleteTask={(id) => completeTaskMutation.mutate(id)}
          />
        </aside>
      </main>
    </div>
  );
}