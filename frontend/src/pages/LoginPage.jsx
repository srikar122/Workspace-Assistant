import React, { useState } from 'react';
import { Sparkles, ShieldCheck, ArrowRight, Calendar, Mail, CheckSquare, UserCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const { loginWithWorkspaceId } = useAuth();
  const [userId, setUserId] = useState('');
  const [name, setName] = useState('');

  const handleLocalLogin = (e) => {
    e.preventDefault();
    if (!userId.trim()) return;
    loginWithWorkspaceId(userId, name);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-indigo-50/40 to-violet-50/50 flex items-center justify-center p-6">
      <div className="max-w-4xl w-full grid md:grid-cols-2 bg-white rounded-3xl shadow-xl shadow-indigo-500/5 border border-slate-200/80 overflow-hidden animate-fade-in-up">
        
        {/* Left Branding Panel */}
        <div className="bg-gradient-to-br from-indigo-600 via-indigo-700 to-violet-800 p-10 text-white flex flex-col justify-between">
          <div className="space-y-6">
            <div className="inline-flex items-center gap-2.5 bg-white/10 backdrop-blur-md px-3.5 py-1.5 rounded-full border border-white/15">
              <Sparkles className="w-4 h-4 text-indigo-200" />
              <span className="text-xs font-semibold tracking-wide">Enterprise AI Orchestrator</span>
            </div>

            <h1 className="text-3xl font-bold leading-tight">
              Agentic Meeting & Executive Assistant
            </h1>
            <p className="text-sm text-indigo-100/90 leading-relaxed">
              Manage your calendar conflicts, action items, and attendee emails from a single natural language conversation with persistent PostgreSQL memory.
            </p>
          </div>

          <div className="space-y-3 pt-8 border-t border-white/15 text-xs text-indigo-100">
            <div className="flex items-center gap-2.5">
              <Calendar className="w-4 h-4 text-indigo-300 shrink-0" />
              <span>Autonomous Google Calendar conflict detection</span>
            </div>
            <div className="flex items-center gap-2.5">
              <Mail className="w-4 h-4 text-indigo-300 shrink-0" />
              <span>Automated Gmail attendee notifications via OAuth2</span>
            </div>
            <div className="flex items-center gap-2.5">
              <CheckSquare className="w-4 h-4 text-indigo-300 shrink-0" />
              <span>Action-item extraction & persistent session memory</span>
            </div>
          </div>
        </div>

        {/* Right Sign-In Panel */}
        <div className="p-10 flex flex-col justify-center space-y-6">
          <div>
            <h2 className="text-xl font-bold text-slate-900">Sign in to your Workspace</h2>
            <p className="text-xs text-slate-500 mt-1">
              Connect Google for live Calendar/Gmail sync, or sign in with a Workspace ID.
            </p>
          </div>

          {/* Option 1: Google OAuth2 Login */}
          <a
            href="http://localhost:8080/oauth2/authorization/google"
            className="w-full flex items-center justify-center gap-2.5 bg-white hover:bg-slate-50 text-slate-800 font-semibold text-xs py-3.5 px-4 rounded-xl border-2 border-slate-200 hover:border-indigo-400 shadow-sm transition-all active:scale-[0.99]"
          >
            <ShieldCheck className="w-4 h-4 text-indigo-600" />
            <span>Continue with Google (Calendar & Gmail OAuth2)</span>
          </a>

          <div className="relative flex py-1 items-center">
            <div className="flex-grow border-t border-slate-200" />
            <span className="flex-shrink mx-3 text-[11px] uppercase tracking-wider text-slate-400 font-semibold">
              Or Local Workspace Session
            </span>
            <div className="flex-grow border-t border-slate-200" />
          </div>

          {/* Option 2: Workspace User ID Form */}
          <form onSubmit={handleLocalLogin} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                Full Name
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g., Mara Srikar"
                className="w-full bg-slate-50 border border-slate-200 focus:border-indigo-500 focus:bg-white rounded-xl px-3.5 py-2.5 text-sm text-slate-800 outline-none transition-all"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                Workspace ID / Email <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                required
                aria-label="Workspace ID input"
                value={userId}
                onChange={(e) => setUserId(e.target.value)}
                placeholder="e.g., srikar@workspace.com"
                className="w-full bg-slate-50 border border-slate-200 focus:border-indigo-500 focus:bg-white rounded-xl px-3.5 py-2.5 text-sm text-slate-800 outline-none transition-all"
              />
              <p className="text-[11px] text-slate-400 mt-1">
                Used as your unique <code>@MemoryId</code> key in PostgreSQL.
              </p>
            </div>

            <button
              type="submit"
              className="w-full bg-gradient-to-r from-indigo-600 to-violet-600 hover:from-indigo-500 hover:to-violet-500 text-white font-semibold text-xs py-3.5 px-4 rounded-xl shadow-md shadow-indigo-500/20 flex items-center justify-center gap-2 transition-all active:scale-[0.99]"
            >
              <UserCheck className="w-4 h-4" />
              <span>Enter Executive Workspace</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}