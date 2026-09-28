import { type FormEvent, useEffect, useRef, useState } from "react";
import { errorMessage, registerCollege } from "../api";
import BrandMark from "../components/BrandMark";
import { Alert, FieldLabel } from "../components/Ui";
import { navigate } from "../router";

export default function SignupPage() {
  const [collegeName, setCollegeName] = useState("");
  const [collegeSlug, setCollegeSlug] = useState("");
  const [fullName, setFullName] = useState("");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const controller = useRef<AbortController | null>(null);

  useEffect(() => () => controller.current?.abort(), []);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setLoading(true);
    controller.current = new AbortController();

    try {
      const registration = await registerCollege(
        {
          college_name: collegeName.trim(),
          college_slug: collegeSlug.trim(),
          username: username.trim(),
          name: fullName.trim(),
          email: email.trim(),
          password,
        },
        controller.current.signal,
      );
      setSuccess(registration.message);
    } catch (requestError) {
      if (
        !(requestError instanceof DOMException && requestError.name === "AbortError")
      ) {
        setError(errorMessage(requestError));
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-[#f3f7f2] px-4 py-10">
      <div className="absolute -left-24 top-[-8rem] h-80 w-80 rounded-full bg-emerald-200/30 blur-3xl" />
      <div className="absolute -bottom-40 right-[-4rem] h-96 w-96 rounded-full bg-teal-200/30 blur-3xl" />

      <div className="relative grid w-full max-w-4xl overflow-hidden rounded-[1.75rem] border border-white/80 bg-white shadow-[0_24px_80px_rgba(15,61,47,0.12)] md:grid-cols-[0.9fr_1.1fr]">
        <section className="hidden bg-[#123c2f] p-10 text-white md:flex md:flex-col md:justify-between">
          <div className="flex items-center gap-3">
            <BrandMark className="h-11 w-11 text-emerald-800" />
            <span className="font-bold">Vidya Track</span>
          </div>
          <div>
            <p className="text-sm font-bold uppercase tracking-[0.18em] text-emerald-300">
              Join the team
            </p>
            <h1 className="mt-4 text-4xl font-bold leading-tight tracking-tight">
              Build a
              <br />
              healthier routine.
            </h1>
            <p className="mt-5 max-w-xs text-sm leading-6 text-emerald-50/70">
              Set up your organization access to track attendance and stay aligned
              with every learner.
            </p>
          </div>
          <p className="text-xs text-emerald-100/50">
            Secure attendance management
          </p>
        </section>

        <main className="p-6 sm:p-10 md:p-12">
          <div className="mb-8 md:hidden">
            <BrandMark className="h-11 w-11 text-emerald-800" />
          </div>
          <p className="text-xs font-bold uppercase tracking-[0.18em] text-emerald-700">
            New here
          </p>
          <h2 className="mt-2 text-3xl font-bold tracking-tight text-slate-950">
            Create your account
          </h2>
          <p className="mt-2 text-sm text-slate-500">
            Start by setting your organization and login details.
          </p>

          <form className="mt-8 space-y-5" onSubmit={submit}>
            {error && <Alert>{error}</Alert>}
            {success && <Alert kind="success">{success}</Alert>}

            <div>
              <FieldLabel htmlFor="college-name">Organization name</FieldLabel>
              <input
                autoComplete="organization"
                className="input"
                id="college-name"
                onChange={(event) => setCollegeName(event.target.value)}
                placeholder="Enter your organization name"
                required
                value={collegeName}
              />
            </div>

            <div>
              <FieldLabel htmlFor="college-slug">Organization slug</FieldLabel>
              <input
                autoComplete="off"
                className="input"
                id="college-slug"
                onChange={(event) => setCollegeSlug(event.target.value)}
                placeholder="your-organization"
                required
                value={collegeSlug}
              />
            </div>

            <div>
              <FieldLabel htmlFor="full-name">Full name</FieldLabel>
              <input
                autoComplete="name"
                className="input"
                id="full-name"
                onChange={(event) => setFullName(event.target.value)}
                placeholder="Enter your full name"
                required
                value={fullName}
              />
            </div>

            <div>
              <FieldLabel htmlFor="signup-username">Username</FieldLabel>
              <input
                autoComplete="username"
                className="input"
                id="signup-username"
                onChange={(event) => setUsername(event.target.value)}
                placeholder="Choose a username"
                required
                value={username}
              />
            </div>

            <div>
              <FieldLabel htmlFor="email">Email</FieldLabel>
              <input
                autoComplete="email"
                className="input"
                id="email"
                onChange={(event) => setEmail(event.target.value)}
                placeholder="you@example.com"
                required
                type="email"
                value={email}
              />
            </div>

            <div className="grid gap-5 sm:grid-cols-2">
              <div>
                <FieldLabel htmlFor="password">Password</FieldLabel>
                <input
                  autoComplete="new-password"
                  className="input"
                  id="password"
                  onChange={(event) => setPassword(event.target.value)}
                  placeholder="Create a password"
                  required
                  type="password"
                  value={password}
                />
              </div>
              <div>
                <FieldLabel htmlFor="confirm-password">Confirm password</FieldLabel>
                <input
                  autoComplete="new-password"
                  className="input"
                  id="confirm-password"
                  onChange={(event) => setConfirmPassword(event.target.value)}
                  placeholder="Re-enter password"
                  required
                  type="password"
                  value={confirmPassword}
                />
              </div>
            </div>

            <button className="btn-primary mt-2 w-full" disabled={loading} type="submit">
              {loading && (
                <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
              )}
              {loading ? "Creating account..." : "Create account"}
            </button>
          </form>

          <p className="mt-7 text-center text-sm text-slate-500">
            Already have an account?{" "}
            <button
              className="font-semibold text-emerald-700 underline decoration-emerald-700/40 underline-offset-4"
              onClick={() => navigate("/login", true)}
              type="button"
            >
              Sign in
            </button>
          </p>
        </main>
      </div>
    </div>
  );
}
