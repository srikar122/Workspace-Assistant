import React, { useState, useEffect, useRef } from 'react';
import { Send, Sparkles, User, Calendar, Mail, CheckSquare, Loader2, Zap } from 'lucide-react';

const ORCHESTRATION_STEPS = [
  { label: 'Inspecting live calendar for conflicts...', icon: Calendar, color: 'text-indigo-600' },
  { label: 'Executing autonomous tool chain...', icon: Zap, color: 'text-violet-600' },
  { label: 'Dispatching emails & updating tasks...', icon: Mail, color: 'text-emerald-600' },
];

const QUICK_PROMPTS = [
  'What meetings do I have coming up today?',
  'Move my Architecture Review to tomorrow at 3 PM to 4 PM and email rahul@company.com',
  'Create a task to finalize the Q3 architecture slides before tomorrow',
];

export default function ChatContainer({ messages = [], isSending, onSendPrompt, errorMessage }) {
  const [input, setInput] = useState('');
  const [stepIndex, setStepIndex] = useState(0);
  const chatEndRef = useRef(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isSending]);

  // Cycle through animated status steps while AI is orchestrating tools
  useEffect(() => {
    if (!isSending) {
      setStepIndex(0);
      return;
    }
    const interval = setInterval(() => {
      setStepIndex((prev) => (prev + 1) % ORCHESTRATION_STEPS.length);
    }, 1800);
    return () => clearInterval(interval);
  }, [isSending]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!input.trim() || isSending) return;
    onSendPrompt(input.trim());
    setInput('');
  };

  const ActiveStepIcon = ORCHESTRATION_STEPS[stepIndex].icon;

  return (
    <section className="flex-1 flex flex-col border-r border-slate-200/80 bg-gradient-to-b from-slate-50/70 via-white to-slate-50/50">
      <div className="flex-1 overflow-y-auto p-6 space-y-5">
        {messages.length === 0 && !isSending && (
          <div className="max-w-xl mx-auto mt-12 text-center space-y-6 animate-fade-in-up">
            <div className="w-14 h-14 rounded-2xl bg-indigo-50 border border-indigo-100 flex items-center justify-center mx-auto shadow-sm">
              <Sparkles className="w-7 h-7 text-indigo-600" />
            </div>
            <div className="space-y-2">
              <h2 className="text-lg font-bold text-slate-800">
                How can I manage your schedule today?
              </h2>
              <p className="text-xs text-slate-500 max-w-md mx-auto leading-relaxed">
                Give a single natural language instruction—I can inspect conflicts, reschedule meetings, send emails, and create follow-up tasks in one go.
              </p>
            </div>
            <div className="grid gap-2 text-left">
              {QUICK_PROMPTS.map((prompt, i) => (
                <button
                  key={i}
                  type="button"
                  onClick={() => onSendPrompt(prompt)}
                  className="p-3 rounded-xl bg-white hover:bg-indigo-50/60 border border-slate-200 hover:border-indigo-300 text-xs text-slate-700 hover:text-indigo-900 shadow-sm transition-all flex items-center justify-between group"
                >
                  <span>"{prompt}"</span>
                  <Send className="w-3.5 h-3.5 text-slate-400 group-hover:text-indigo-600 group-hover:translate-x-0.5 transition-all shrink-0 ml-2" />
                </button>
              ))}
            </div>
          </div>
        )}

        {messages.map((m, idx) => {
          const isUser = m.type === 'USER';
          return (
            <div
              key={m.id || idx}
              className={`flex gap-3.5 max-w-2xl animate-fade-in-up ${
                isUser ? 'ml-auto flex-row-reverse' : ''
              }`}
            >
              <div
                className={`w-8 h-8 rounded-xl flex items-center justify-center shrink-0 shadow-sm ${
                  isUser
                    ? 'bg-gradient-to-tr from-indigo-600 to-violet-600 text-white'
                    : 'bg-white border border-slate-200 text-indigo-600'
                }`}
              >
                {isUser ? <User size={15} /> : <Sparkles size={15} />}
              </div>

              <div
                className={`p-4 rounded-2xl text-sm leading-relaxed whitespace-pre-wrap shadow-sm ${
                  isUser
                    ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white rounded-tr-none'
                    : 'bg-white border border-slate-200/90 text-slate-800 rounded-tl-none'
                }`}
              >
                {m.content}
              </div>
            </div>
          );
        })}

        {/* Animated Multi-Tool Orchestration Loader */}
        {isSending && (
          <div
            role="status"
            className="flex gap-3.5 max-w-lg animate-fade-in-up"
          >
            <div className="w-8 h-8 rounded-xl bg-indigo-50 border border-indigo-200 flex items-center justify-center shrink-0">
              <Loader2 className="w-4 h-4 text-indigo-600 animate-spin" />
            </div>
            <div className="flex-1 bg-white border border-indigo-100 rounded-2xl rounded-tl-none p-4 shadow-md shadow-indigo-500/5 space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-xs font-semibold text-slate-800">
                  <ActiveStepIcon className={`w-4 h-4 ${ORCHESTRATION_STEPS[stepIndex].color} animate-bounce`} />
                  <span>{ORCHESTRATION_STEPS[stepIndex].label}</span>
                </div>
                <span className="flex gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-indigo-600 animate-bounce [animation-delay:-0.3s]" />
                  <span className="w-1.5 h-1.5 rounded-full bg-indigo-600 animate-bounce [animation-delay:-0.15s]" />
                  <span className="w-1.5 h-1.5 rounded-full bg-indigo-600 animate-bounce" />
                </span>
              </div>
              <div className="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden">
                <div className="h-full bg-gradient-to-r from-indigo-500 via-violet-500 to-emerald-500 w-2/3 animate-pulse rounded-full" />
              </div>
              <div className="flex items-center gap-3 text-[11px] text-slate-400">
                <span className="flex items-center gap-1"><Calendar size={11} /> Calendar</span>
                <span>•</span>
                <span className="flex items-center gap-1"><CheckSquare size={11} /> Tasks</span>
                <span>•</span>
                <span className="flex items-center gap-1"><Mail size={11} /> Gmail</span>
              </div>
            </div>
          </div>
        )}

        {errorMessage && (
          <div className="max-w-xl mx-auto p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-xs text-rose-700">
            <strong>Orchestration Error:</strong> {errorMessage}
          </div>
        )}

        <div ref={chatEndRef} />
      </div>

      {/* Floating Prompt Input Bar */}
      <form
        onSubmit={handleSubmit}
        className="p-4 border-t border-slate-200/80 bg-white/80 backdrop-blur-md"
      >
        <div className="max-w-4xl mx-auto flex gap-2.5 items-center bg-slate-50 border border-slate-200 focus-within:border-indigo-500 focus-within:bg-white focus-within:ring-4 focus-within:ring-indigo-500/10 rounded-xl p-1.5 transition-all shadow-sm">
          <input
            aria-label="Agent prompt input"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask your agent to schedule, reschedule, email, or add tasks..."
            className="flex-1 bg-transparent px-3 py-2 text-sm text-slate-800 placeholder:text-slate-400 outline-none"
          />
          <button
            type="submit"
            aria-label="Send prompt"
            disabled={isSending || !input.trim()}
            className="bg-indigo-600 hover:bg-indigo-500 disabled:opacity-40 text-white px-4 py-2.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 shadow-sm transition-all active:scale-95"
          >
            <span>Send</span>
            <Send size={14} />
          </button>
        </div>
      </form>
    </section>
  );
}