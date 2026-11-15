import React from 'react';
import { CheckSquare } from 'lucide-react';

export default function TaskSidebar({ tasks = [], onCompleteTask }) {
  const completedCount = tasks.filter((t) => t.status === 'COMPLETED').length;

  return (
    <div className="h-2/5 flex flex-col overflow-hidden bg-slate-50/50">
      <div className="px-5 py-3.5 border-b border-slate-200/80 bg-white flex items-center justify-between">
        <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-slate-700">
          <div className="p-1.5 rounded-lg bg-emerald-50 text-emerald-600">
            <CheckSquare className="w-3.5 h-3.5" />
          </div>
          <span>Action Items</span>
        </div>
        {tasks.length > 0 && (
          <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200/60">
            {completedCount}/{tasks.length} Done
          </span>
        )}
      </div>

      <div className="flex-1 overflow-y-auto p-4 space-y-2">
        {tasks.length === 0 ? (
          <div className="text-xs text-slate-400 text-center py-6 bg-white rounded-xl border border-dashed border-slate-200">
            No pending tasks.
          </div>
        ) : (
          tasks.map((t) => {
            const isCompleted = t.status === 'COMPLETED';
            return (
              <div
                key={t.id}
                onClick={() => !isCompleted && onCompleteTask?.(t.id)}
                className={`flex items-center gap-3 p-3 rounded-xl border text-sm transition-all animate-fade-in-up ${
                  isCompleted
                    ? 'bg-slate-100/70 border-slate-200/60 opacity-75'
                    : 'bg-white border-slate-200/90 hover:border-emerald-300 shadow-sm cursor-pointer'
                }`}
              >
                <input
                  type="checkbox"
                  checked={isCompleted}
                  readOnly
                  aria-label={`Mark ${t.title} completed`}
                  className="h-4 w-4 rounded border-slate-300 text-emerald-600 focus:ring-0 cursor-pointer accent-emerald-600"
                />
                <span
                  className={`flex-1 text-xs font-medium ${
                    isCompleted ? 'line-through text-slate-400' : 'text-slate-700'
                  }`}
                >
                  {t.title}
                </span>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}