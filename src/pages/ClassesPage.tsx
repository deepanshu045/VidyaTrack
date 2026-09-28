import { useEffect, useState, type FormEvent } from "react";
import {
  createNgoClass,
  errorMessage,
  getClasses,
  type NgoClass,
} from "../api";
import {
  Alert,
  EmptyState,
  FieldLabel,
  PageHeader,
  Spinner,
  classLabel,
} from "../components/Ui";
import { navigate } from "../router";

export default function ClassesPage() {
  const [classes, setClasses] = useState<NgoClass[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [department, setDepartment] = useState("");
  const [className, setClassName] = useState("");
  const [section, setSection] = useState("");
  const reportMode = new URLSearchParams(window.location.search).has("reports");

  useEffect(() => {
    const controller = new AbortController();
    getClasses(controller.signal)
      .then(setClasses)
      .catch((requestError) => {
        if (!(requestError instanceof DOMException && requestError.name === "AbortError")) {
          setError(errorMessage(requestError));
        }
      })
      .finally(() => setLoading(false));
    return () => controller.abort();
  }, []);

  const submitClass = async (event: FormEvent) => {
    event.preventDefault();
    setError("");
    setSuccess("");
    setSaving(true);
    try {
      await createNgoClass({
        department: department.trim(),
        class_name: className.trim(),
        section: section.trim(),
      });
      setClasses(await getClasses());
      setDepartment("");
      setClassName("");
      setSection("");
      setSuccess("Class created successfully.");
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-7">
      <PageHeader
        description={
          reportMode
            ? "Choose a class to open its attendance report."
            : "View rosters, take daily attendance, or open a class report."
        }
        eyebrow={reportMode ? "Reports" : "Manage"}
        title={reportMode ? "Choose a class" : "Classes"}
      />
      {error && <Alert>{error}</Alert>}
      {success && <Alert kind="success">{success}</Alert>}
      {!reportMode && (
        <form
          className="card grid gap-4 p-5 sm:grid-cols-2 sm:p-6 xl:grid-cols-[1fr_1fr_1fr_auto] xl:items-end"
          onSubmit={submitClass}
        >
          <div className="sm:col-span-2 xl:col-span-4">
            <h2 className="font-bold text-slate-950">Create a class</h2>
            <p className="mt-1 text-sm text-slate-500">
              Add a department, class name, and section to start a roster.
            </p>
          </div>
          <div>
            <FieldLabel htmlFor="class-department">Department</FieldLabel>
            <input
              className="input"
              id="class-department"
              onChange={(event) => setDepartment(event.target.value)}
              placeholder="e.g. Science"
              required
              value={department}
            />
          </div>
          <div>
            <FieldLabel htmlFor="class-name">Class name</FieldLabel>
            <input
              className="input"
              id="class-name"
              onChange={(event) => setClassName(event.target.value)}
              placeholder="e.g. Class 1"
              required
              value={className}
            />
          </div>
          <div>
            <FieldLabel htmlFor="class-section">Section</FieldLabel>
            <input
              className="input"
              id="class-section"
              onChange={(event) => setSection(event.target.value)}
              placeholder="e.g. A"
              required
              value={section}
            />
          </div>
          <button
            className="btn-primary sm:col-span-2 xl:col-span-1"
            disabled={saving}
            type="submit"
          >
            {saving ? "Adding..." : "Add class"}
          </button>
        </form>
      )}
      {loading ? (
        <Spinner label="Loading classes..." />
      ) : classes.length === 0 ? (
        <EmptyState
          description="No classes are currently assigned to your account."
          title="No classes found"
        />
      ) : (
        <div className="grid gap-4 xl:grid-cols-2">
          {classes.map((item, index) => (
            <article
              className="card p-5 sm:p-6"
              key={item.id ? `${item.id}-${index}` : `${item.class_name}-${item.section}-${index}`}
            >
              <div className="flex items-start gap-4">
                <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-emerald-50 font-bold text-emerald-700">
                  {item.class_name.slice(0, 1).toUpperCase()}
                </div>
                <div className="min-w-0">
                  <h2 className="text-lg font-bold text-slate-950">
                    {classLabel(item)}
                  </h2>
                  <p className="mt-1 text-sm text-slate-500">
                    {item.department || "General department"}
                  </p>
                </div>
              </div>
              <div className="mt-6 grid gap-2 sm:grid-cols-3">
                {!reportMode && (
                  <>
                    <button
                      className="btn-secondary"
                      onClick={() => navigate(`/students?class_id=${item.id}`)}
                      type="button"
                    >
                      Students
                    </button>
                    <button
                      className="btn-primary"
                      onClick={() =>
                        navigate(`/mark-attendance?class_id=${item.id}`)
                      }
                      type="button"
                    >
                      Mark attendance
                    </button>
                  </>
                )}
                <button
                  className={reportMode ? "btn-primary sm:col-span-3" : "btn-secondary"}
                  onClick={() => navigate(`/class-report?class_id=${item.id}`)}
                  type="button"
                >
                  View report
                </button>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
