import { useEffect, useMemo, useState } from "react";
import {
  errorMessage,
  getAttendance,
  getClasses,
  getClassSummary,
  type AttendanceRecord,
  type ClassSummary,
} from "../api";
import {
  Alert,
  classLabel,
  EmptyState,
  formatDate,
  PageHeader,
  Spinner,
  SummaryCard,
} from "../components/Ui";
import { getPositiveId, navigate } from "../router";

interface DailyOverview {
  date: string;
  present: number;
  absent: number;
}

export default function ClassReportPage() {
  const classId = getPositiveId("class_id");
  const [summary, setSummary] = useState<ClassSummary | null>(null);
  const [classTitle, setClassTitle] = useState("");
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [loading, setLoading] = useState(Boolean(classId));
  const [summaryError, setSummaryError] = useState("");
  const [historyError, setHistoryError] = useState("");

  useEffect(() => {
    if (!classId) return;
    const controller = new AbortController();
    async function load() {
      const [summaryResult, historyResult, classesResult] = await Promise.allSettled([
        getClassSummary(classId!, controller.signal),
        getAttendance({ ngo_class_id: classId! }, controller.signal),
        getClasses(controller.signal),
      ]);
      if (summaryResult.status === "fulfilled") {
        setSummary(summaryResult.value);
      } else if (
        !(summaryResult.reason instanceof DOMException &&
          summaryResult.reason.name === "AbortError")
      ) {
        setSummaryError(errorMessage(summaryResult.reason));
      }
      if (historyResult.status === "fulfilled") {
        setRecords(historyResult.value);
      } else if (
        !(historyResult.reason instanceof DOMException &&
          historyResult.reason.name === "AbortError")
      ) {
        setHistoryError(errorMessage(historyResult.reason));
      }
      if (classesResult.status === "fulfilled") {
        const selectedClass = classesResult.value.find((item) => item.id === classId);
        if (selectedClass) setClassTitle(classLabel(selectedClass));
      }
      setLoading(false);
    }
    void load();
    return () => controller.abort();
  }, [classId]);

  const daily = useMemo(() => {
    const groups = new Map<string, DailyOverview>();
    records.forEach((record) => {
      const row = groups.get(record.attendance_date) || {
        date: record.attendance_date,
        present: 0,
        absent: 0,
      };
      if (record.status === "Present") row.present += 1;
      else row.absent += 1;
      groups.set(record.attendance_date, row);
    });
    return [...groups.values()].sort((a, b) => b.date.localeCompare(a.date));
  }, [records]);

  if (!classId) {
    return (
      <div className="space-y-6">
        <PageHeader title="Class report" />
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
        description="Class-wide attendance performance by day."
        eyebrow="Class report"
        title={classTitle || summary?.class_name || "Class report"}
      />

      {loading ? (
        <Spinner label="Loading class report..." />
      ) : (
        <>
          {summaryError && <Alert>{summaryError}</Alert>}
          {summary && (
            <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
              <SummaryCard
                accent="blue"
                label="Total records"
                value={summary.total_attendance_records}
              />
              <SummaryCard label="Present" value={summary.present} />
              <SummaryCard accent="red" label="Absent" value={summary.absent} />
              <SummaryCard
                accent="amber"
                label="Attendance (out of 100%)"
                value={`${summary.percentage.toFixed(1).replace(".0", "")}%`}
              />
            </section>
          )}

          <section className="card overflow-hidden">
            <div className="border-b border-slate-100 px-5 py-5 sm:px-6">
              <h2 className="font-bold text-slate-950">Daily overview</h2>
              <p className="mt-1 text-sm text-slate-500">
                Present and absent totals for each recorded day
              </p>
            </div>
            {historyError ? (
              <div className="p-5">
                <Alert>{historyError}</Alert>
              </div>
            ) : daily.length === 0 ? (
              <div className="p-5">
                <EmptyState title="No attendance records found." />
              </div>
            ) : (
              <>
                <div className="hidden overflow-x-auto sm:block">
                  <table className="w-full min-w-[560px] text-left text-sm">
                    <thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500">
                      <tr>
                        <th className="px-6 py-3 font-semibold">Date</th>
                        <th className="px-6 py-3 font-semibold">Present</th>
                        <th className="px-6 py-3 font-semibold">Absent</th>
                        <th className="px-6 py-3 font-semibold">Total</th>
                        <th className="px-6 py-3 font-semibold">Attendance / 100%</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {daily.map((row) => {
                        const total = row.present + row.absent;
                        return (
                          <tr key={row.date}>
                            <td className="px-6 py-4 font-semibold text-slate-900">
                              {formatDate(row.date)}
                            </td>
                            <td className="px-6 py-4 text-emerald-700">{row.present}</td>
                            <td className="px-6 py-4 text-red-700">{row.absent}</td>
                            <td className="px-6 py-4 text-slate-600">{total}</td>
                            <td className="px-6 py-4 font-bold text-slate-900">
                              {Math.round((row.present / total) * 100)}%
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
                <div className="divide-y divide-slate-100 sm:hidden">
                  {daily.map((row) => {
                    const total = row.present + row.absent;
                    return (
                      <article className="p-5" key={row.date}>
                        <div className="flex items-center justify-between">
                          <p className="font-bold text-slate-900">{formatDate(row.date)}</p>
                          <p className="text-lg font-bold text-emerald-700">
                            {Math.round((row.present / total) * 100)}%
                          </p>
                        </div>
                        <div className="mt-3 flex gap-4 text-sm">
                          <span className="text-emerald-700">{row.present} present</span>
                          <span className="text-red-700">{row.absent} absent</span>
                          <span className="text-slate-500">{total} total</span>
                        </div>
                      </article>
                    );
                  })}
                </div>
              </>
            )}
          </section>
        </>
      )}
    </div>
  );
}
