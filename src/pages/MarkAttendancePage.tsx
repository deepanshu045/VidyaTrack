import { useEffect, useMemo, useState } from "react";
import {
  errorMessage,
  getAttendance,
  getClasses,
  getStudents,
  saveAttendance,
  type AttendanceStatus,
  type NgoClass,
  type Student,
} from "../api";
import {
  Alert,
  EmptyState,
  FieldLabel,
  localToday,
  PageHeader,
  Spinner,
  classLabel,
} from "../components/Ui";
import { getPositiveId, navigate } from "../router";

export default function MarkAttendancePage() {
  const classId = getPositiveId("class_id");
  const [students, setStudents] = useState<Student[]>([]);
  const [selectedClass, setSelectedClass] = useState<NgoClass | null>(null);
  const [date, setDate] = useState(localToday());
  const [statuses, setStatuses] = useState<Record<number, AttendanceStatus>>({});
  const [rosterLoading, setRosterLoading] = useState(Boolean(classId));
  const [dateLoading, setDateLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    if (!classId) return;
    const controller = new AbortController();
    setRosterLoading(true);
    Promise.all([getStudents(classId, controller.signal), getClasses(controller.signal)])
      .then(([studentData, classData]) => {
        setStudents(studentData);
        setSelectedClass(classData.find((item) => item.id === classId) || null);
      })
      .catch((requestError) => {
        if (!(requestError instanceof DOMException && requestError.name === "AbortError")) {
          setError(errorMessage(requestError));
        }
      })
      .finally(() => setRosterLoading(false));
    return () => controller.abort();
  }, [classId]);

  useEffect(() => {
    if (!classId || rosterLoading || students.length === 0) return;
    const controller = new AbortController();
    setDateLoading(true);
    setError("");
    setSuccess("");
    getAttendance(
      { ngo_class_id: classId, from_date: date, to_date: date },
      controller.signal,
    )
      .then((records) => {
        const existing = new Map(records.map((record) => [record.student_id, record.status]));
        setStatuses(
          Object.fromEntries(
            students.map((student) => [
              student.id,
              existing.get(student.id) || "Present",
            ]),
          ),
        );
      })
      .catch((requestError) => {
        if (!(requestError instanceof DOMException && requestError.name === "AbortError")) {
          setError(errorMessage(requestError));
        }
      })
      .finally(() => setDateLoading(false));
    return () => controller.abort();
  }, [classId, date, rosterLoading, students]);

  const counts = useMemo(
    () =>
      students.reduce(
        (total, student) => {
          const status = statuses[student.id] || "Present";
          total[status] += 1;
          return total;
        },
        { Present: 0, Absent: 0 },
      ),
    [students, statuses],
  );
  const attendancePercent = students.length
    ? Math.round((counts.Present / students.length) * 100)
    : 0;

  const markAll = (status: AttendanceStatus) => {
    setStatuses(Object.fromEntries(students.map((student) => [student.id, status])));
  };

  const submit = async () => {
    if (!classId) return;
    setSaving(true);
    setError("");
    setSuccess("");
    try {
      const result = await saveAttendance({
        ngo_class_id: classId,
        attendance_date: date,
        records: students.map((student) => ({
          student_id: student.id,
          status: statuses[student.id] || "Present",
        })),
      });
      setSuccess(result.message);
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setSaving(false);
    }
  };

  if (!classId) {
    return (
      <div className="space-y-6">
        <PageHeader title="Mark attendance" />
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
        description={
          selectedClass
            ? classLabel(selectedClass)
            : "Record daily student attendance."
        }
        eyebrow="Daily register"
        title="Mark attendance"
      />
      {error && <Alert>{error}</Alert>}
      {success && <Alert kind="success">{success}</Alert>}

      {rosterLoading ? (
        <Spinner label="Loading class roster..." />
      ) : students.length === 0 ? (
        <EmptyState
          description="Attendance cannot be recorded until students are added."
          title="No students in this class"
        />
      ) : (
        <>
          <section className="card p-5 sm:p-6">
            <div className="grid gap-5 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-end">
              <div className="max-w-xs">
                <FieldLabel htmlFor="attendance-date">Attendance date</FieldLabel>
                <input
                  className="input"
                  id="attendance-date"
                  onChange={(event) => setDate(event.target.value)}
                  type="date"
                  value={date}
                />
              </div>
              <div className="flex gap-3">
                <div className="rounded-xl bg-emerald-50 px-4 py-2.5 text-center">
                  <p className="text-xs font-semibold text-emerald-700">Present</p>
                  <p className="text-xl font-bold text-emerald-900">{counts.Present}</p>
                </div>
                <div className="rounded-xl bg-red-50 px-4 py-2.5 text-center">
                  <p className="text-xs font-semibold text-red-700">Absent</p>
                  <p className="text-xl font-bold text-red-900">{counts.Absent}</p>
                </div>
              </div>
              <div className="sm:col-span-2">
                <div className="mb-2 flex flex-wrap items-center justify-between gap-2 text-sm">
                  <span className="font-semibold text-slate-700">Attendance</span>
                  <span className="font-bold text-slate-950">
                    {counts.Present} of {students.length} present · {attendancePercent}% / 100%
                  </span>
                </div>
                <div
                  aria-label={`Attendance: ${attendancePercent}% out of 100%`}
                  aria-valuemax={100}
                  aria-valuemin={0}
                  aria-valuenow={attendancePercent}
                  className="h-2.5 overflow-hidden rounded-full bg-slate-100"
                  role="progressbar"
                >
                  <div
                    className="h-full rounded-full bg-emerald-600 transition-[width]"
                    style={{ width: `${attendancePercent}%` }}
                  />
                </div>
              </div>
            </div>
          </section>

          {dateLoading ? (
            <Spinner label="Checking saved attendance..." />
          ) : (
            <section className="card overflow-hidden">
              <div className="flex flex-col gap-3 border-b border-slate-100 px-5 py-4 sm:flex-row sm:items-center sm:justify-between">
                <p className="font-bold text-slate-900">
                  Student attendance
                  <span className="ml-2 text-sm font-medium text-slate-400">
                    {students.length} students
                  </span>
                </p>
                <div className="flex flex-wrap gap-2">
                  <button
                    className="btn-secondary min-h-10 px-3 py-2 text-xs"
                    onClick={() => markAll("Present")}
                    type="button"
                  >
                    Mark all present
                  </button>
                  <button
                    className="btn-secondary min-h-10 px-3 py-2 text-xs"
                    onClick={() => markAll("Absent")}
                    type="button"
                  >
                    Mark all absent
                  </button>
                </div>
              </div>
              <div className="divide-y divide-slate-100">
                {students.map((student, index) => {
                  const status = statuses[student.id] || "Present";
                  return (
                    <div
                      className="flex flex-col gap-4 px-4 py-5 sm:flex-row sm:items-center sm:px-6"
                      key={student.id}
                    >
                      <div className="flex min-w-0 flex-1 items-center gap-3">
                        <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-slate-100 text-sm font-bold text-slate-500">
                          {index + 1}
                        </span>
                        <div>
                          <p className="font-bold text-slate-900">{student.name}</p>
                          <p className="text-sm text-slate-500">
                            ID {student.student_id}
                          </p>
                        </div>
                      </div>
                      <fieldset>
                        <legend className="sr-only">
                          Attendance for {student.name}
                        </legend>
                        <div className="grid grid-cols-2 gap-2 rounded-xl bg-slate-100 p-1">
                          {(["Present", "Absent"] as AttendanceStatus[]).map(
                            (option) => (
                              <label
                                className={`flex min-h-11 cursor-pointer items-center justify-center rounded-lg px-5 text-sm font-bold transition ${
                                  status === option
                                    ? option === "Present"
                                      ? "bg-white text-emerald-700 shadow-sm ring-1 ring-emerald-200"
                                      : "bg-white text-red-700 shadow-sm ring-1 ring-red-200"
                                    : "text-slate-500 hover:text-slate-700"
                                }`}
                                key={option}
                              >
                                <input
                                  checked={status === option}
                                  className="sr-only"
                                  name={`attendance-${student.id}`}
                                  onChange={() =>
                                    setStatuses((current) => ({
                                      ...current,
                                      [student.id]: option,
                                    }))
                                  }
                                  type="radio"
                                  value={option}
                                />
                                {option}
                              </label>
                            ),
                          )}
                        </div>
                      </fieldset>
                    </div>
                  );
                })}
              </div>
              <div className="flex flex-col gap-3 border-t border-slate-100 bg-slate-50 px-5 py-5 sm:flex-row sm:items-center sm:justify-between">
                <p className="text-sm text-slate-500">
                  Review the register before saving.
                </p>
                <button
                  className="btn-primary sm:min-w-44"
                  disabled={saving || dateLoading}
                  onClick={submit}
                  type="button"
                >
                  {saving && (
                    <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white" />
                  )}
                  {saving ? "Saving..." : "Save attendance"}
                </button>
              </div>
            </section>
          )}
        </>
      )}
    </div>
  );
}
