import { useEffect, useMemo, useState } from "react";
import {
  errorMessage,
  getAttendance,
  getClasses,
  getStudents,
  type AttendanceRecord,
  type NgoClass,
} from "../api";
import {
  Alert,
  EmptyState,
  formatDate,
  localToday,
  PageHeader,
  Spinner,
  StatusBadge,
  SummaryCard,
  classLabel,
} from "../components/Ui";
import { navigate } from "../router";

export default function DashboardPage() {
  const [classes, setClasses] = useState<NgoClass[]>([]);
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [studentCount, setStudentCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const controller = new AbortController();
    async function load() {
      setLoading(true);
      setError("");
      try {
        const classData = await getClasses(controller.signal);
        setClasses(classData);
        const [attendanceResult, studentResults] = await Promise.all([
          getAttendance({}, controller.signal),
          Promise.allSettled(
            classData.map((item) => getStudents(item.id, controller.signal)),
          ),
        ]);
        setRecords(attendanceResult);
        const ids = new Set<number>();
        studentResults.forEach((result) => {
          if (result.status === "fulfilled") {
            result.value.forEach((student) => ids.add(student.id));
          }
        });
        setStudentCount(ids.size);
      } catch (requestError) {
        if (!(requestError instanceof DOMException && requestError.name === "AbortError")) {
          setError(errorMessage(requestError));
        }
      } finally {
        setLoading(false);
      }
    }
    void load();
    return () => controller.abort();
  }, []);

  const todayRecords = records.filter(
    (record) => record.attendance_date === localToday(),
  );
  const presentToday = todayRecords.filter((record) => record.status === "Present").length;
  const percentage = todayRecords.length
    ? Math.round((presentToday / todayRecords.length) * 100)
    : 0;
  const recent = [...records]
    .sort((a, b) => b.attendance_date.localeCompare(a.attendance_date))
    .slice(0, 8);
  const classNames = useMemo(
    () => new Map(classes.map((item) => [item.id, classLabel(item)])),
    [classes],
  );

  return (
    <div className="space-y-8">
      <PageHeader
        action={
          <button
            className="btn-primary"
            onClick={() => navigate("/classes")}
            type="button"
          >
            Mark attendance
          </button>
        }
        description="A clear view of your classes and student attendance."
        eyebrow="Overview"
        title="Welcome back"
      />

      {error && <Alert>{error}</Alert>}
      {loading ? (
        <Spinner label="Loading dashboard..." />
      ) : (
        <>
          <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <SummaryCard accent="blue" label="Total classes" value={classes.length} />
            <SummaryCard accent="amber" label="Total students" value={studentCount} />
            <SummaryCard label="Today's attendance records" value={todayRecords.length} />
            <SummaryCard label="Today's attendance (out of 100%)" value={`${percentage}%`} />
          </section>

          <section className="card overflow-hidden">
            <div className="flex items-center justify-between border-b border-slate-100 px-5 py-5 sm:px-6">
              <div>
                <h2 className="font-bold text-slate-950">Recent attendance</h2>
                <p className="mt-1 text-sm text-slate-500">
                  Latest records across all classes
                </p>
              </div>
              <button
                className="text-sm font-bold text-emerald-700 hover:text-emerald-900"
                onClick={() => navigate("/attendance")}
                type="button"
              >
                View all
              </button>
            </div>
            {recent.length === 0 ? (
              <div className="p-5">
                <EmptyState
                  description="Saved attendance will appear here."
                  title="No attendance records found."
                />
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full min-w-[620px] text-left text-sm">
                  <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                    <tr>
                      <th className="px-6 py-3 font-semibold">Student</th>
                      <th className="px-6 py-3 font-semibold">Class</th>
                      <th className="px-6 py-3 font-semibold">Date</th>
                      <th className="px-6 py-3 font-semibold">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {recent.map((record) => (
                      <tr key={record.id}>
                        <td className="px-6 py-4 font-semibold text-slate-900">
                          {record.student_name}
                        </td>
                        <td className="px-6 py-4 text-slate-600">
                          {classNames.get(record.class_section_id) ||
                            `Class #${record.class_section_id}`}
                        </td>
                        <td className="px-6 py-4 text-slate-600">
                          {formatDate(record.attendance_date)}
                        </td>
                        <td className="px-6 py-4">
                          <StatusBadge status={record.status} />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </>
      )}
    </div>
  );
}
