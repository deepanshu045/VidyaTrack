import { useEffect, useState } from "react";
import { clearSession, isLoggedIn } from "./auth";
import Layout from "./components/Layout";
import AttendancePage from "./pages/AttendancePage";
import ClassReportPage from "./pages/ClassReportPage";
import ClassesPage from "./pages/ClassesPage";
import DashboardPage from "./pages/DashboardPage";
import LoginPage from "./pages/LoginPage";
import MarkAttendancePage from "./pages/MarkAttendancePage";
import SignupPage from "./pages/SignupPage";
import StudentReportPage from "./pages/StudentReportPage";
import StudentsPage from "./pages/StudentsPage";
import { navigate } from "./router";

function useLocation() {
  const [location, setLocation] = useState(() => ({
    pathname: window.location.pathname,
    search: window.location.search,
  }));

  useEffect(() => {
    const update = () =>
      setLocation({
        pathname: window.location.pathname,
        search: window.location.search,
      });
    window.addEventListener("popstate", update);
    window.addEventListener("app:navigate", update);
    return () => {
      window.removeEventListener("popstate", update);
      window.removeEventListener("app:navigate", update);
    };
  }, []);

  return location;
}

function NotFoundPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-stone-50 px-6">
      <div className="max-w-md text-center">
        <p className="text-sm font-bold uppercase tracking-[0.18em] text-emerald-700">
          404
        </p>
        <h1 className="mt-3 text-3xl font-bold text-slate-900">
          Page not found
        </h1>
        <p className="mt-3 text-slate-600">
          The page you are looking for does not exist.
        </p>
        <button
          className="btn-primary mt-6"
          onClick={() => navigate("/dashboard")}
          type="button"
        >
          Go to dashboard
        </button>
      </div>
    </div>
  );
}

export default function App() {
  const location = useLocation();
  const authenticated = isLoggedIn();

  useEffect(() => {
    const handleExpired = () => {
      clearSession();
      navigate("/login", true);
    };
    window.addEventListener("auth:expired", handleExpired);
    return () => window.removeEventListener("auth:expired", handleExpired);
  }, []);

  useEffect(() => {
    if (location.pathname === "/") {
      navigate(authenticated ? "/dashboard" : "/login", true);
    } else if (
      (location.pathname === "/login" || location.pathname === "/signup") &&
      authenticated
    ) {
      navigate("/dashboard", true);
    } else if (
      location.pathname !== "/login" &&
      location.pathname !== "/signup" &&
      !authenticated
    ) {
      navigate("/login", true);
    }
  }, [authenticated, location.pathname]);

  if (location.pathname === "/login") {
    return authenticated ? null : <LoginPage />;
  }

  if (location.pathname === "/signup") {
    return authenticated ? null : <SignupPage />;
  }

  if (!authenticated || location.pathname === "/") return null;

  const pages: Record<string, React.ReactNode> = {
    "/dashboard": <DashboardPage />,
    "/classes": <ClassesPage />,
    "/students": <StudentsPage />,
    "/mark-attendance": <MarkAttendancePage />,
    "/attendance": <AttendancePage />,
    "/student-report": <StudentReportPage />,
    "/class-report": <ClassReportPage />,
  };

  const page = pages[location.pathname];
  if (!page) return <NotFoundPage />;

  return <Layout currentPath={location.pathname}>{page}</Layout>;
}
