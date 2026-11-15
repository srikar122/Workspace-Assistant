import React from 'react';
import { Sparkles, CheckCircle2, RefreshCw, ShieldCheck, LogOut, User } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function Navbar({ onManualSync, isSyncing }) {
  const { user, logout } = useAuth();

  return (
    <header className="h-16 border-b border-slate-200/80 bg-white/90 backdrop-blur-md flex items-center justify-between px-6 sticky top-0 z-20 shadow-sm">
      <div className="flex items-center gap-3">
        <div className="relative h-10 w-10 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-500 flex items-center justify-center shadow-md shadow-indigo-500/20">
          <Sparkles className="w-5 h-5 text-white animate-pulse" />
          <span className="absolute -bottom-0.5 -right-0.5 h-3 w-3 rounded-full bg-emerald-500 ring-2 ring-white" />
        </div>
        <div>
          <div className="flex items-center gap-2">
            <h1 className="font-bold text-slate-900 tracking-tight text-base">
              Executive AI Workspace
            </h1>
            <span className="text-[11px] font-semibold px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-600 border border-indigo-200/60">
              LangChain4j + Gemini
            </span>
          </div>
          <p className="text-xs text-slate-500">
            Autonomous Calendar, Task & Email Multi-Tool Orchestrator
          </p>
        </div>
      </div>

      <div className="flex items-center gap-3">
        {user && (
          <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-lg bg-slate-100 border border-slate-200 text-xs text-slate-700">
            <User className="w-3.5 h-3.5 text-indigo-600" />
            <span className="font-semibold">{user.name}</span>
            <span className="text-slate-400">({user.userId})</span>
          </div>
        )}

        {onManualSync && (
          <button
            onClick={onManualSync}
            title="Refresh workspace state"
            className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-medium text-slate-600 hover:text-indigo-600 hover:bg-slate-100 border border-slate-200 transition-all"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isSyncing ? 'animate-spin text-indigo-600' : ''}`} />
            <span>Sync</span>
          </button>
        )}

        {user?.googleConnected ? (
          <div className="flex items-center gap-1.5 bg-emerald-50 border border-emerald-200 text-emerald-700 px-3.5 py-2 rounded-lg text-xs font-semibold">
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
            <span>Google Synced</span>
          </div>
        ) : (
          <a
            href="http://localhost:8080/oauth2/authorization/google"
            className="flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-violet-600 hover:from-indigo-500 hover:to-violet-500 text-white px-3.5 py-2 rounded-lg text-xs font-semibold shadow-sm shadow-indigo-500/20 transition-all active:scale-95"
          >
            <ShieldCheck className="w-4 h-4" />
            <span>Connect Google</span>
          </a>
        )}

        <button
          onClick={logout}
          title="Sign Out"
          className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-medium text-rose-600 hover:bg-rose-50 border border-rose-200/70 transition-all"
        >
          <LogOut className="w-3.5 h-3.5" />
          <span>Sign Out</span>
        </button>
      </div>
    </header>
  );
}