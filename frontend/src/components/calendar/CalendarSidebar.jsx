import React from 'react';
import { Calendar, Clock, Users } from 'lucide-react';

export default function CalendarSidebar({ events = [], isLoading }) {
  return (
    <div className="flex-1 flex flex-col border-b border-slate-200/80 overflow-hidden bg-slate-50/50">
      <div className="px-5 py-3.5 border-b border-slate-200/80 bg-white flex items-center justify-between">
        <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-slate-700">
          <div className="p-1.5 rounded-lg bg-indigo-50 text-indigo-600">
            <Calendar className="w-3.5 h-3.5" />
          </div>
          <span>Live Calendar</span>
        </div>
        <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-slate-100 text-slate-600">
          {events.length} {events.length === 1 ? 'Event' : 'Events'}
        </span>
      </div>

      <div className="flex-1 overflow-y-auto p-4 space-y-3">
        {isLoading ? (
          <div className="space-y-3" aria-label="Loading events">
            {[1, 2].map((n) => (
              <div
                key={n}
                className="p-3.5 rounded-xl bg-white border border-slate-200/70 space-y-2.5"
              >
                <div className="h-4 w-3/4 rounded shimmer-bg animate-shimmer" />
                <div className="h-3 w-1/2 rounded shimmer-bg animate-shimmer" />
              </div>
            ))}
          </div>
        ) : events.length === 0 ? (
          <div className="text-xs text-slate-400 text-center py-8 bg-white rounded-xl border border-dashed border-slate-200">
            No events scheduled.
          </div>
        ) : (
          events.map((e) => (
            <div
              key={e.id}
              className="group bg-white p-3.5 rounded-xl border border-slate-200/90 hover:border-indigo-300 shadow-sm hover:shadow-md transition-all animate-fade-in-up"
            >
              <div className="flex items-start justify-between gap-2">
                <div className="text-sm font-semibold text-slate-800 group-hover:text-indigo-600 transition-colors">
                  {e.summary}
                </div>
                <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-slate-100 text-slate-500 shrink-0">
                  #{e.id}
                </span>
              </div>

              <div className="text-xs text-indigo-600 font-medium flex items-center gap-1.5 mt-2 bg-indigo-50/70 px-2.5 py-1 rounded-md w-fit">
                <Clock size={12} />
                <span>{new Date(e.startTime).toLocaleString()}</span>
              </div>

              {e.attendees && (
                <div className="text-[11px] text-slate-500 flex items-center gap-1.5 mt-2 pt-2 border-t border-slate-100 truncate">
                  <Users size={12} className="text-slate-400 shrink-0" />
                  <span className="truncate">{e.attendees}</span>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}