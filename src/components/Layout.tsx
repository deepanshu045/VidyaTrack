import { useEffect, useState, type ReactNode } from "react";
import { clearSession, getRole } from "../auth";
import BrandMark from "./BrandMark";
import { navigate } from "../router";

const navItems = [
  { label: "Dashboard", path: "/dashboard", icon: "grid" },
  { label: "Classes", path: "/classes", icon: "book" },
  { label: "Attendance", path: "/attendance", icon: "check" },
  { label: "Reports", path: "/classes?reports=1", icon: "report" },
];

function Icon({ name }: { name: string }) {
  const paths: Record<string, ReactNode> = {
    grid: <><rect height="7" rx="1" width="7" x="3" y="3" /><rect height="7" rx="1" width="7" x="14" y="3" /><rect height="7" rx="1" width="7" x="3" y="14" /><rect height="7" rx="1" width="7" x="14" y="14" /></>,
    book: <><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" /><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2Z" /></>,
    check: <><path d="M9 11l3 3L22 4" /><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" /></>,
    report: <><path d="M4 19V9M10 19V5M16 19v-7M22 19H2" /></>,
    logout: <><path d="M10 17l5-5-5-5M15 12H3" /><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" /></>,
  };
  return (
    <svg aria-hidden="true" className="h-5 w-5" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.8" viewBox="0 0 24 24">
      {paths[name]}
    </svg>
  );
}

function Sidebar({
  currentPath,
  close,
}: {
  currentPath: string;
  close?: () => void;
}) {
  const role = getRole() || "user";
  const logout = () => {
    clearSession();
    close?.();
    navigate("/login", true);
  };

  return (
    <div className="flex h-full flex-col bg-[#123c2f] px-4 py-5 text-white">
      <button
        className="flex items-center gap-3 rounded-xl px-2 py-2 text-left"
        onClick={() => {
          close?.();
          navigate("/dashboard");
        }}
        type="button"
      >
        <BrandMark className="h-10 w-10 shrink-0 text-emerald-800" />
        <span>
          <span className="block font-bold tracking-tight">Vidya Track</span>
          <span className="block text-xs text-emerald-100/60">Learning, every day</span>
        </span>
      </button>

      <nav aria-label="Main navigation" className="mt-9 space-y-1">
        {navItems.map((item) => {
          const basePath = item.path.split("?")[0];
          const onReport =
            currentPath === "/student-report" ||
            currentPath === "/class-report" ||
            (currentPath === "/classes" &&
              new URLSearchParams(window.location.search).has("reports"));
          const active =
            item.label === "Reports"
              ? onReport
              : currentPath === basePath &&
                !(item.label === "Classes" && onReport);
          return (
            <button
              className={`flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm font-semibold transition ${
                active
                  ? "bg-white text-emerald-950 shadow-sm"
                  : "text-emerald-50/75 hover:bg-white/8 hover:text-white"
              }`}
              key={item.label}
              onClick={() => {
                close?.();
                navigate(item.path);
              }}
              type="button"
            >
              <Icon name={item.icon} />
              {item.label}
            </button>
          );
        })}
      </nav>

      <div className="mt-auto border-t border-white/10 pt-4">
        <div className="mb-3 flex items-center gap-3 px-2 py-2">
          <span className="flex h-9 w-9 items-center justify-center rounded-full bg-emerald-200 text-sm font-bold uppercase text-emerald-950">
            {role.slice(0, 1)}
          </span>
          <div>
            <p className="text-sm font-semibold capitalize">{role}</p>
            <p className="text-xs text-emerald-100/55">Signed in</p>
          </div>
        </div>
        <button
          className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm font-semibold text-emerald-50/75 hover:bg-white/8 hover:text-white"
          onClick={logout}
          type="button"
        >
          <Icon name="logout" />
          Logout
        </button>
      </div>
    </div>
  );
}

export default function Layout({
  children,
  currentPath,
}: {
  children: ReactNode;
  currentPath: string;
}) {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    const onEscape = (event: KeyboardEvent) => {
      if (event.key === "Escape") setOpen(false);
    };
    window.addEventListener("keydown", onEscape);
    return () => window.removeEventListener("keydown", onEscape);
  }, []);

  useEffect(() => setOpen(false), [currentPath]);

  return (
    <div className="min-h-screen bg-[#f7f8f5]">
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 lg:block">
        <Sidebar currentPath={currentPath} />
      </aside>

      {open && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <button
            aria-label="Close navigation"
            className="absolute inset-0 bg-slate-950/40 backdrop-blur-[2px]"
            onClick={() => setOpen(false)}
            type="button"
          />
          <aside className="relative h-full w-[min(82vw,19rem)] shadow-2xl">
            <Sidebar currentPath={currentPath} close={() => setOpen(false)} />
          </aside>
        </div>
      )}

      <div className="lg:pl-64">
        <header className="sticky top-0 z-20 flex h-16 items-center justify-between border-b border-slate-200/80 bg-white/90 px-4 backdrop-blur sm:px-7 lg:px-10">
          <button
            aria-label="Open navigation"
            className="flex h-10 w-10 items-center justify-center rounded-xl border border-slate-200 text-slate-700 lg:hidden"
            onClick={() => setOpen(true)}
            type="button"
          >
            <svg aria-hidden="true" className="h-5 w-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
              <path d="M4 7h16M4 12h16M4 17h16" strokeLinecap="round" />
            </svg>
          </button>
          <p className="hidden text-sm font-medium text-slate-500 lg:block">
            Student attendance management
          </p>
          <div className="ml-auto flex items-center gap-2 rounded-full bg-emerald-50 py-1.5 pl-2 pr-3 text-sm font-bold capitalize text-emerald-800">
            <span className="flex h-7 w-7 items-center justify-center rounded-full bg-emerald-700 text-xs text-white">
              {(getRole() || "U").slice(0, 1).toUpperCase()}
            </span>
            {getRole() || "User"}
          </div>
        </header>
        <main className="mx-auto max-w-[1440px] px-4 py-7 sm:px-7 sm:py-9 lg:px-10">
          {children}
        </main>
      </div>
    </div>
  );
}
