import { clearSession, getToken } from "./auth";

export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL?.replace(/\/+$/, "") || "http://localhost:8000";

export interface LoginCredentials {
  college_slug: string;
  username: string;
  password: string;
}

export interface CollegeRegistrationPayload {
  college_name: string;
  college_slug: string;
  username: string;
  name: string;
  email: string;
  password: string;
}

export interface RegistrationResponse {
  message: string;
}

export interface LoginResponse {
  access_token: string;
  token_type: string;
  role: "admin" | "teacher";
}

export interface NgoClass {
  id: number;
  department: string;
  class_name: string;
  section: string;
}

export interface CreateNgoClassPayload {
  department: string;
  class_name: string;
  section: string;
}

export interface Student {
  id: number;
  student_id: string;
  name: string;
  email: string;
  phone_no: string;
  active: boolean;
}

export interface CreateNgoStudentPayload {
  roll_no: string;
  name: string;
  ngo_class_id: number;
  email?: string;
  phone_no?: string;
}

export type AttendanceStatus = "Present" | "Absent";

export interface AttendanceRecord {
  id: number;
  student_id: number;
  student_name: string;
  class_section_id: number;
  attendance_date: string;
  status: AttendanceStatus;
}

export interface AttendancePayload {
  ngo_class_id: number;
  attendance_date: string;
  records: Array<{ student_id: number; status: AttendanceStatus }>;
}

export interface AttendanceSaveResponse {
  message: string;
}

export interface StudentSummary {
  student_id: number;
  student_name: string;
  total_classes: number;
  present: number;
  absent: number;
  percentage: number;
}

export interface ClassSummary {
  class_section_id: number;
  class_name: string;
  total_attendance_records: number;
  present: number;
  absent: number;
  percentage: number;
}

export interface AttendanceFilters {
  ngo_class_id?: number;
  student_id?: number;
  from_date?: string;
  to_date?: string;
}

export class ApiError extends Error {
  status: number;

  constructor(message: string, status = 0) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

async function messageForError(response: Response) {
  if (response.status === 401) return "Your session has expired. Please log in again.";
  if (response.status === 403)
    return "You don't have permission to access this resource.";
  if (response.status === 404) return "Resource not found.";
  if (response.status >= 500) return "Server error. Please try again.";
  if (response.status === 409 || response.status === 422) {
    try {
      const body = await response.json();
      const detail = body?.detail;
      if (typeof detail === "string") return detail;
      if (Array.isArray(detail) && typeof detail[0]?.msg === "string") {
        return detail[0].msg;
      }
    } catch {
      // Use a safe status-specific message if the error body is unavailable.
    }
    if (response.status === 409) {
      return "A student with this roll number may already exist in this class. Check the roll number and try again.";
    }
    return "Please check the information and try again.";
  }
  return "Something went wrong. Please try again.";
}

async function request<T>(
  path: string,
  options: RequestInit = {},
  protectedRequest = true,
): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set("Accept", "application/json");
  if (options.body) headers.set("Content-Type", "application/json");
  if (protectedRequest) {
    const token = getToken();
    if (token) headers.set("Authorization", `Bearer ${token}`);
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError") throw error;
    throw new ApiError(
      "Backend unavailable. Please check your connection and try again.",
    );
  }

  if (!response.ok) {
    const message = await messageForError(response);
    if (protectedRequest && response.status === 401) {
      clearSession();
      window.dispatchEvent(new Event("auth:expired"));
    }
    throw new ApiError(message, response.status);
  }

  const responseText = await response.text();
  if (!responseText) return undefined as T;
  try {
    return JSON.parse(responseText) as T;
  } catch {
    throw new ApiError("The server returned an invalid response.", response.status);
  }
}

export async function login(credentials: LoginCredentials, signal?: AbortSignal) {
  try {
    return await request<LoginResponse>(
      "/auth/login",
      { method: "POST", body: JSON.stringify(credentials), signal },
      false,
    );
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      throw new ApiError("Invalid username or password.", 401);
    }
    throw error;
  }
}

export async function registerCollege(
  payload: CollegeRegistrationPayload,
  signal?: AbortSignal,
) {
  try {
    return await request<RegistrationResponse>(
      "/auth/register-college",
      { method: "POST", body: JSON.stringify(payload), signal },
      false,
    );
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) {
      throw new ApiError("The server rejected this registration request.", 401);
    }
    if (error instanceof ApiError && error.status === 409) {
      throw new ApiError("An organization or account with these details already exists.", 409);
    }
    if (error instanceof ApiError && error.status === 422) {
      throw new ApiError("Please check the registration details and try again.", 422);
    }
    throw error;
  }
}

export function getClasses(signal?: AbortSignal) {
  return request<
    Array<{
      id: number;
      name?: string;
      department?: string;
      class_name?: string;
      section?: string;
    }>
  >("/ngo/classes", { signal }).then((classes) =>
    classes.map((item) => ({
      id: item.id,
      department: item.department || "",
      class_name: item.class_name || item.name || `Class #${item.id}`,
      section: item.section || "",
    })),
  );
}

export async function createNgoClass(payload: CreateNgoClassPayload) {
  await request<unknown>("/ngo/classes", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function getStudents(classId: number, signal?: AbortSignal) {
  return request<
    Array<{
      id?: number;
      name?: string;
      student_name?: string;
      student_id?: string;
      roll_no?: string;
      email?: string | null;
      phone_no?: string | null;
      active?: boolean;
    }>
  >(
    `/ngo/classes/${classId}/students`,
    { signal },
  ).then((students) =>
    students.map((student, index) => {
      const id = student.id ?? (Number(student.student_id) || index + 1);
      const studentId = student.student_id || student.roll_no || String(id);
      return {
        id,
        student_id: studentId,
        name: student.name || student.student_name || student.roll_no || `Student #${studentId}`,
        email: student.email || "",
        phone_no: student.phone_no || "",
        active: student.active ?? true,
      };
    }),
  );
}

export async function createNgoStudent(payload: CreateNgoStudentPayload) {
  await request<unknown>("/ngo/students", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function saveAttendance(payload: AttendancePayload) {
  return request<Partial<AttendanceSaveResponse> | undefined>("/ngo/attendance", {
    method: "POST",
    body: JSON.stringify(payload),
  }).then((response) => ({
    message: response?.message || "Attendance saved successfully.",
  }));
}

export function getAttendance(
  filters: AttendanceFilters = {},
  signal?: AbortSignal,
) {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== "") params.set(key, String(value));
  });
  const query = params.size ? `?${params.toString()}` : "";
  return request<
    Array<
      Omit<AttendanceRecord, "student_name" | "class_section_id"> & {
        student_name?: string;
        class_section_id?: number;
        ngo_class_id?: number;
      }
    >
  >(`/ngo/attendance${query}`, { signal }).then((records) =>
    records.map((record) => ({
      ...record,
      attendance_date: record.attendance_date.slice(0, 10),
      status: record.status.toLowerCase() === "present" ? "Present" : "Absent",
      student_name: record.student_name || `Student #${record.student_id}`,
      class_section_id: record.ngo_class_id ?? record.class_section_id ?? 0,
    })),
  );
}

export function getStudentSummary(studentId: number, signal?: AbortSignal) {
  return request<{
    student_id: number;
    total_lectures?: number;
    total?: number;
    present: number;
    absent?: number;
    percentage: number;
    roll_no?: string;
    name?: string;
  }>(
    `/ngo/students/${studentId}/attendance-summary`,
    { signal },
  ).then((summary) => {
    const total = summary.total_lectures ?? summary.total ?? 0;
    return {
      ...summary,
      absent: summary.absent ?? Math.max(total - summary.present, 0),
      student_name: summary.name || summary.roll_no || `Student #${summary.student_id}`,
      total_classes: total,
    };
  });
}

export function getClassSummary(classId: number, signal?: AbortSignal) {
  return request<{
    ngo_class_id?: number;
    class_section_id?: number;
    total?: number;
    total_attendance_records?: number;
    present: number;
    absent: number;
    percentage: number;
  }>(
    `/ngo/classes/${classId}/attendance-summary`,
    { signal },
  ).then((summary) => ({
    ...summary,
    class_section_id: summary.ngo_class_id ?? summary.class_section_id ?? classId,
    class_name: `Class #${summary.ngo_class_id ?? summary.class_section_id ?? classId}`,
    total_attendance_records: summary.total_attendance_records ?? summary.total ?? 0,
  }));
}

export function errorMessage(error: unknown) {
  if (error instanceof ApiError) return error.message;
  return "Something went wrong. Please try again.";
}
