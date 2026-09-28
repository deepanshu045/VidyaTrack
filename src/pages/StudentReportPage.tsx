import { useEffect, useState } from "react";
import {
  errorMessage,
  getAttendance,
  getStudentSummary,
  type AttendanceRecord,
  type StudentSummary,
} from "../api";
import {
  Alert,
  EmptyState,
  formatDate,
  PageHeader,
  Spinner,
  StatusBadge,
  SummaryCard,
} from "../components/Ui";
import { getPositiveId, navigate } from "../router";

export default function StudentReportPage() {
  const studentId = getPositiveId("student_id");
  const [summary, setSummary] = useState<StudentSummary | null>(null);
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [loading, setLoading] = useState(Boolean(studentId));
  const [summaryError, setSummaryError] = useState("");
  const [historyError, setHistoryError] = useState("");

  useEffect(() => {
    if (!studentId) return;
    const controller = new AbortController();
    async function load() {
      const [summaryResult, historyResult] = await Promise.allSettled([
        getStudentSummary(studentId!, controller.signal),
        getAttendance({ student_id: studentId! }, controller.signal),
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
        setRecords(
          [...historyResult.value].sort((a, b) =>
            b.attendance_date.localeCompare(a.attendance_date),
          ),
        );
      } else if (
        !(historyResult.reason instanceof DOMException &&
          historyResult.reason.name === "AbortError")
      ) {
        setHistoryError(errorMessage(historyResult.reason));
      }
      setLoading(false);
    }
    void load();
    return () => controller.abort();
  }, [studentId]);

  if (!studentId) {
    return (
      <div className="space-y-6">
        <PageHeader title="Student report" />
        <Alert>A valid student is required. Open a student from a class roster.</Alert>
        <button className="btn-primary" onClick={() => navigate("/classes")} type="button">
          View classes
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-7">
      <PageHeader
        description="Individual attendance performance and daily records."
        eyebrow="Student report"
        title={summary?.student_name || "Attendance summary"}
      />
      {loading ? (
        <Spinner label="Loading student report..." />
      ) : (
        <>
          {summaryError && <Alert>{summaryError}</Alert>}
          {summary && (
            <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
              <SummaryCard accent="blue" label="Total days" value={summary.total_classes} />
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
              <h2 className="font-bold text-slate-950">Attendance history</h2>
              <p className="mt-1 text-sm text-slate-500">
                Day-by-day attendance for this student
              </p>
            </div>
            {historyError ? (
              <div className="p-5">
                <Alert>{historyError}</Alert>
              </div>
            ) : records.length === 0 ? (
              <div className="p-5">
                <EmptyState title="No attendance records found." />
              </div>
            ) : (
              <div className="divide-y divide-slate-100">
                {records.map((record) => (
                  <div
                    className="flex items-center justify-between gap-4 px-5 py-4 sm:px-6"
                    key={record.id}
                  >
                    <div>
                      <p className="font-semibold text-slate-900">
                        {formatDate(record.attendance_date)}
                      </p>
                      <p className="mt-1 text-sm text-slate-500">
                        Class #{record.class_section_id}
                      </p>
                    </div>
                    <StatusBadge status={record.status} />
                  </div>
                ))}
              </div>
            )}
          </section>
        </>
      )}
    </div>
  );
}
