import { useEffect, useMemo, useState, type FormEvent } from "react";
import {
  createNgoStudent,
  errorMessage,
  getClasses,
  getStudents,
  type NgoClass,
  type Student,
} from "../api";
import {
  Alert,
  EmptyState,
  FieldLabel,
  PageHeader,
  Spinner,
  classLabel,
} from "../components/Ui";
import { getPositiveId, navigate } from "../router";

export default function StudentsPage() {
  const classId = getPositiveId("class_id");
  const [students, setStudents] = useState<Student[]>([]);
  const [selectedClass, setSelectedClass] = useState<NgoClass | null>(null);
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(Boolean(classId));
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [rollNo, setRollNo] = useState("");
  const [studentName, setStudentName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNo, setPhoneNo] = useState("");

  useEffect(() => {
    if (!classId) return;
    const controller = new AbortController();
    Promise.all([getStudents(classId, controller.signal), getClasses(controller.signal)])
      .then(([studentData, classData]) => {
        setStudents(studentData);
        const currentClass = classData.find((item) => item.id === classId) || null;
        setSelectedClass(currentClass);
      })
      .catch((requestError) => {
        if (!(requestError instanceof DOMException && requestError.name === "AbortError")) {
          setError(errorMessage(requestError));
        }
      })
      .finally(() => setLoading(false));
    return () => controller.abort();
  }, [classId]);

  const submitStudent = async (event: FormEvent) => {
    event.preventDefault();
    if (!classId) return;
    setError("");
    setSuccess("");
    setSaving(true);
    try {
      await createNgoStudent({
        roll_no: rollNo.trim(),
        name: studentName.trim(),
        ngo_class_id: classId,
        ...(email.trim() && { email: email.trim() }),
        ...(phoneNo.trim() && { phone_no: phoneNo.trim() }),
      });
      setStudents(await getStudents(classId));
      setRollNo("");
      setStudentName("");
      setEmail("");
      setPhoneNo("");
      setSuccess("Student added successfully.");
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  const filtered = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return students;
    return students.filter(
      (student) =>
        student.name.toLowerCase().includes(normalized) ||
        student.student_id.toLowerCase().includes(normalized),
    );
  }, [query, students]);

  if (!classId) {
    return (
      <div className="space-y-6">
        <PageHeader title="Students" />
        <Alert>A valid class is required. Please choose a class first.</Alert>
        <button className="btn-primary" onClick={() => navigate("/classes")} type="button">
          Choose a class
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-7">
      <PageHeader
        action={
          <button
            className="btn-primary"
            onClick={() => navigate(`/mark-attendance?class_id=${classId}`)}
            type="button"
          >
            Mark attendance
          </button>
        }
        description={
          selectedClass
            ? `${classLabel(selectedClass)} · ${students.length} students`
            : "View class roster and individual reports."
        }
        eyebrow="Class roster"
        title="Students"
      />
      {error && <Alert>{error}</Alert>}
      {success && <Alert kind="success">{success}</Alert>}
      {loading ? (
        <Spinner label="Loading students..." />
      ) : (
        <div className="space-y-6">
          <form
            className="card grid gap-4 p-5 sm:grid-cols-2 xl:grid-cols-3 sm:p-6"
            onSubmit={submitStudent}
          >
            <div className="sm:col-span-2 xl:col-span-3">
              <h2 className="font-bold text-slate-950">Add a student</h2>
              <p className="mt-1 text-sm text-slate-500">
                This student will be added directly to the selected class.
              </p>
            </div>
            <div>
              <FieldLabel htmlFor="student-class">Class</FieldLabel>
              <input
                className="input"
                id="student-class"
                readOnly
                value={selectedClass ? classLabel(selectedClass) : `Class #${classId}`}
              />
            </div>
            <div>
              <FieldLabel htmlFor="student-roll-no">Roll number</FieldLabel>
              <input
                autoComplete="off"
                className="input"
                id="student-roll-no"
                onChange={(event) => setRollNo(event.target.value)}
                required
                value={rollNo}
              />
            </div>
            <div>
              <FieldLabel htmlFor="student-name">Student name</FieldLabel>
              <input
                autoComplete="name"
                className="input"
                id="student-name"
                onChange={(event) => setStudentName(event.target.value)}
                required
                value={studentName}
              />
            </div>
            <div>
              <FieldLabel htmlFor="student-email">Email</FieldLabel>
              <input
                autoComplete="email"
                className="input"
                id="student-email"
                onChange={(event) => setEmail(event.target.value)}
                type="email"
                value={email}
              />
            </div>
            <div>
              <FieldLabel htmlFor="student-phone">Phone number</FieldLabel>
              <input
                autoComplete="tel"
                className="input"
                id="student-phone"
                onChange={(event) => setPhoneNo(event.target.value)}
                type="tel"
                value={phoneNo}
              />
            </div>
            <button
              className="btn-primary sm:col-span-2 xl:col-span-3 xl:justify-self-end"
              disabled={saving}
              type="submit"
            >
              {saving ? "Adding student..." : "Add student"}
            </button>
          </form>

          {students.length === 0 ? (
            <EmptyState
              description="Add the first student to this class using the form above."
              title="No students found"
            />
          ) : (
            <section className="card overflow-hidden">
              <div className="border-b border-slate-100 p-5">
                <label className="sr-only" htmlFor="student-search">
                  Search students
                </label>
                <div className="relative max-w-md">
                  <svg aria-hidden="true" className="absolute left-3 top-3.5 h-4 w-4 text-slate-400" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                    <circle cx="11" cy="11" r="7" />
                    <path d="m20 20-4-4" />
                  </svg>
                  <input
                    className="input pl-10"
                    id="student-search"
                    onChange={(event) => setQuery(event.target.value)}
                    placeholder="Search by name or student ID"
                    type="search"
                    value={query}
                  />
                </div>
              </div>
              {filtered.length === 0 ? (
                <div className="p-5">
                  <EmptyState title="No students match your search" />
                </div>
              ) : (
                <div className="divide-y divide-slate-100">
                  {filtered.map((student) => (
                    <div
                      className="flex flex-col gap-4 px-5 py-4 sm:flex-row sm:items-center"
                      key={student.id}
                    >
                      <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-slate-100 text-sm font-bold text-slate-600">
                        {student.name
                          .split(" ")
                          .slice(0, 2)
                          .map((part) => part[0])
                          .join("")
                          .toUpperCase()}
                      </span>
                      <div className="min-w-0 flex-1">
                        <div className="flex flex-wrap items-center gap-2">
                          <p className="font-bold text-slate-900">{student.name}</p>
                          {!student.active && (
                            <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-semibold text-slate-600">
                              Inactive
                            </span>
                          )}
                        </div>
                        <p className="mt-0.5 text-sm text-slate-500">
                          Student ID: {student.student_id}
                        </p>
                      </div>
                      <button
                        className="btn-secondary"
                        onClick={() =>
                          navigate(`/student-report?student_id=${student.id}`)
                        }
                        type="button"
                      >
                        View attendance
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </section>
          )}
        </div>
      )}
    </div>
  );
}
