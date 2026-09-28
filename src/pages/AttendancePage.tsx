import { useEffect, useMemo, useState, type FormEvent } from "react";
import {
  errorMessage,
  getAttendance,
  getClasses,
  getStudents,
  type AttendanceFilters,
  type AttendanceRecord,
  type NgoClass,
  type Student,
} from "../api";
import {
  Alert,
  EmptyState,
  FieldLabel,
  formatDate,
  PageHeader,
  Spinner,
  StatusBadge,
  classLabel,
} from "../components/Ui";

interface FilterState {
  classId: string;
  studentId: string;
  fromDate: string;
  toDate: string;
}

const emptyFilters: FilterState = {
  classId: "",
  studentId: "",
  fromDate: "",
  toDate: "",
};

export default function AttendancePage() {
  const [classes, setClasses] = useState<NgoClass[]>([]);
  const [students, setStudents] = useState<Student[]>([]);
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [filters, setFilters] = useState<FilterState>(emptyFilters);
  const [loading, setLoading] = useState(true);
  const [studentLoading, setStudentLoading] = useState(false);
  const [error, setError] = useState("");

  const classNames = useMemo(
    () => new Map(classes.map((item) => [item.id, classLabel(item)])),
    [classes],
  );

  useEffect(() => {
    const controller = new AbortController();
    Promise.all([getClasses(controller.signal), getAttendance({}, controller.signal)])
      .then(([classData, attendanceData]) => {
        setClasses(classData);
        setRecords(attendanceData);
      })
      .catch((requestError) => {
        if (!(requestError instanceof DOMException && requestError.name === "AbortError")) {
          setError(errorMessage(requestError));
        }
      })
      .finally(() => setLoading(false));
    return () => controller.abort();
  }, []);

  useEffect(() => {
    if (!filters.classId) {
      setStudents([]);
      return;
    }
    const controller = new AbortController();
    setStudentLoading(true);
    getStudents(Number(filters.classId), controller.signal)
      .then(setStudents)
      .catch((requestError) => {
        if (!(requestError instanceof DOMException && requestError.name === "AbortError")) {
          setError(errorMessage(requestError));
        }
      })
      .finally(() => setStudentLoading(false));
    return () => controller.abort();
  }, [filters.classId]);

  const search = async (event: FormEvent) => {
    event.preventDefault();
    setError("");
    if (filters.fromDate && filters.toDate && filters.fromDate > filters.toDate) {
      setError("From date cannot be after to date.");
      return;
    }
    const query: AttendanceFilters = {};
    if (filters.classId) query.ngo_class_id = Number(filters.classId);
    if (filters.studentId) query.student_id = Number(filters.studentId);
    if (filters.fromDate) query.from_date = filters.fromDate;
    if (filters.toDate) query.to_date = filters.toDate;
    setLoading(true);
    try {
      setRecords(await getAttendance(query));
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-7">
      <PageHeader
        description="Search attendance by class, student, or date range."
        eyebrow="Records"
        title="Attendance history"
      />

      <form className="card p-5 sm:p-6" onSubmit={search}>
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <div>
            <FieldLabel htmlFor="filter-class">Class</FieldLabel>
            <select
              className="input"
              id="filter-class"
              onChange={(event) =>
                setFilters((current) => ({
                  ...current,
                  classId: event.target.value,
                  studentId: "",
                }))
              }
              value={filters.classId}
            >
              <option value="">All classes</option>
              {classes.map((item) => (
                <option key={item.id} value={item.id}>
                  {classLabel(item)}
                </option>
              ))}
            </select>
          </div>
          <div>
            <FieldLabel htmlFor="filter-student">Student</FieldLabel>
            <select
              className="input"
              disabled={!filters.classId || studentLoading}
              id="filter-student"
              onChange={(event) =>
                setFilters((current) => ({
                  ...current,
                  studentId: event.target.value,
                }))
              }
              value={filters.studentId}
            >
              <option value="">
                {studentLoading ? "Loading students..." : "All students"}
              </option>
              {students.map((student) => (
                <option key={student.id} value={student.id}>
                  {student.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <FieldLabel htmlFor="from-date">From date</FieldLabel>
            <input
              className="input"
              id="from-date"
              onChange={(event) =>
                setFilters((current) => ({
                  ...current,
                  fromDate: event.target.value,
                }))
              }
              type="date"
              value={filters.fromDate}
            />
          </div>
          <div>
            <FieldLabel htmlFor="to-date">To date</FieldLabel>
            <input
              className="input"
              id="to-date"
              onChange={(event) =>
                setFilters((current) => ({
                  ...current,
                  toDate: event.target.value,
                }))
              }
              type="date"
              value={filters.toDate}
            />
          </div>
        </div>
        <div className="mt-5 flex flex-wrap gap-3">
          <button className="btn-primary min-w-28" disabled={loading} type="submit">
            Search
          </button>
          <button
            className="btn-secondary"
            onClick={() => setFilters(emptyFilters)}
            type="button"
          >
            Clear filters
          </button>
        </div>
      </form>

      {error && <Alert>{error}</Alert>}
      {loading ? (
        <Spinner label="Loading attendance..." />
      ) : records.length === 0 ? (
        <EmptyState
          description="Try changing or clearing the selected filters."
          title="No attendance records found."
        />
      ) : (
        <section className="card overflow-hidden">
          <div className="hidden overflow-x-auto sm:block">
            <table className="w-full min-w-[640px] text-left text-sm">
              <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                <tr>
                  <th className="px-6 py-3 font-semibold">Date</th>
                  <th className="px-6 py-3 font-semibold">Student</th>
                  <th className="px-6 py-3 font-semibold">Class</th>
                  <th className="px-6 py-3 font-semibold">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {records.map((record) => (
                  <tr key={record.id}>
                    <td className="px-6 py-4 text-slate-600">
                      {formatDate(record.attendance_date)}
                    </td>
                    <td className="px-6 py-4 font-semibold text-slate-900">
                      {record.student_name}
                    </td>
                    <td className="px-6 py-4 text-slate-600">
                      {classNames.get(record.class_section_id) ||
                        `Class #${record.class_section_id}`}
                    </td>
                    <td className="px-6 py-4">
                      <StatusBadge status={record.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="divide-y divide-slate-100 sm:hidden">
            {records.map((record) => (
              <article className="p-5" key={record.id}>
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="font-bold text-slate-900">{record.student_name}</p>
                    <p className="mt-1 text-sm text-slate-500">
                      {classNames.get(record.class_section_id) ||
                        `Class #${record.class_section_id}`}
                    </p>
                  </div>
                  <StatusBadge status={record.status} />
                </div>
                <p className="mt-4 text-sm font-medium text-slate-500">
                  {formatDate(record.attendance_date)}
                </p>
              </article>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
