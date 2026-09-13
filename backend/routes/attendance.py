from flask import Blueprint, request, jsonify
from models import db, AttendanceSession, AttendanceRecord, Student, Class, User
from flask_jwt_extended import jwt_required, get_jwt_identity
from datetime import datetime

attendance_bp = Blueprint('attendance', __name__)


def _current_user():
    identity = get_jwt_identity() or {}
    if not isinstance(identity, dict):
        return None
    return User.query.get(identity.get('id'))


def _teacher_can_access_class(user, class_id):
    if not user:
        return False
    if user.role == 'ADMIN':
        return True
    if user.role != 'TEACHER' or not user.teacher_id:
        return False
    teacher = user.teacher
    return any(c.id == class_id for c in teacher.classes)


def _student_ids_for_class(class_obj):
    return {student.id for student in class_obj.students}


# 1. Mark / update attendance
@attendance_bp.route('', methods=['POST'])
@jwt_required()
def mark_attendance():
    data = request.get_json(silent=True) or {}
    user = _current_user()

    try:
        class_id = int(data['class_id'])
        date_str = data['date']
        attendance_date = datetime.strptime(date_str, '%Y-%m-%d').date()
        records = data.get('records', [])

        if not isinstance(records, list) or not records:
            return jsonify({'message': 'At least one attendance record is required'}), 400

        class_obj = Class.query.get(class_id)
        if not class_obj:
            return jsonify({'message': 'Class not found'}), 404
        if not _teacher_can_access_class(user, class_id):
            return jsonify({'message': 'You are not authorized to manage attendance for this class'}), 403

        valid_student_ids = _student_ids_for_class(class_obj)
        seen_student_ids = set()
        normalized_records = []
        for record in records:
            student_id = int(record['student_id'])
            status = record.get('status')
            if student_id not in valid_student_ids:
                return jsonify({'message': f'Student {student_id} is not assigned to this class'}), 400
            if student_id in seen_student_ids:
                return jsonify({'message': f'Duplicate attendance record for student {student_id}'}), 400
            if status not in ('Present', 'Absent'):
                return jsonify({'message': 'Status must be Present or Absent'}), 400
            seen_student_ids.add(student_id)
            normalized_records.append((student_id, status))

        teacher_id = user.teacher_id if user.role == 'TEACHER' else data.get('teacher_id')
        if not teacher_id:
            return jsonify({'message': 'Teacher ID is required'}), 400

        session = AttendanceSession.query.filter_by(
            class_id=class_id, attendance_date=attendance_date
        ).first()

        if session:
            # Keep the original teacher for an existing session unless an admin is editing it.
            if user.role == 'TEACHER' and session.teacher_id != user.teacher_id:
                return jsonify({'message': 'Only the teacher who created this session can edit it'}), 403
            AttendanceRecord.query.filter_by(session_id=session.id).delete()
        else:
            session = AttendanceSession(
                class_id=class_id,
                teacher_id=teacher_id,
                attendance_date=attendance_date
            )
            db.session.add(session)
            db.session.flush()

        for student_id, status in normalized_records:
            db.session.add(AttendanceRecord(
                session_id=session.id,
                student_id=student_id,
                status=status
            ))

        db.session.commit()
        return jsonify({'message': 'Attendance saved successfully', 'session_id': session.id}), 201
    except (KeyError, TypeError, ValueError) as exc:
        db.session.rollback()
        return jsonify({'message': f'Invalid attendance data: {exc}'}), 400
    except Exception as exc:
        db.session.rollback()
        return jsonify({'message': str(exc)}), 400


# 2. Get attendance for a class on a specific date
@attendance_bp.route('/class/<int:class_id>/date/<string:date_str>', methods=['GET'])
@jwt_required()
def get_class_attendance(class_id, date_str):
    try:
        user = _current_user()
        if not _teacher_can_access_class(user, class_id):
            return jsonify({'message': 'You are not authorized to view attendance for this class'}), 403

        attendance_date = datetime.strptime(date_str, '%Y-%m-%d').date()
        session = AttendanceSession.query.filter_by(
            class_id=class_id, attendance_date=attendance_date
        ).first()

        if not session:
            return jsonify({'message': 'No attendance record found for this date', 'records': []}), 200

        records = AttendanceRecord.query.filter_by(session_id=session.id).all()
        output = []
        for record in records:
            student = Student.query.get(record.student_id)
            if student:
                output.append({
                    'student_id': record.student_id,
                    'student_name': student.full_name,
                    'status': record.status
                })
        return jsonify({'session_id': session.id, 'records': output}), 200
    except ValueError:
        return jsonify({'message': 'Date must use YYYY-MM-DD format'}), 400
    except Exception as exc:
        return jsonify({'message': str(exc)}), 400


# 3. Get attendance history for a class
@attendance_bp.route('/class/<int:class_id>/history', methods=['GET'])
@jwt_required()
def get_class_attendance_history(class_id):
    user = _current_user()
    if not _teacher_can_access_class(user, class_id):
        return jsonify({'message': 'You are not authorized to view attendance for this class'}), 403

    sessions = AttendanceSession.query.filter_by(class_id=class_id).order_by(
        AttendanceSession.attendance_date.desc()
    ).all()
    output = []
    for session in sessions:
        present_count = AttendanceRecord.query.filter_by(
            session_id=session.id, status='Present'
        ).count()
        total_count = AttendanceRecord.query.filter_by(session_id=session.id).count()
        percentage = round((present_count / total_count) * 100, 2) if total_count else 0
        output.append({
            'id': session.id,
            'date': session.attendance_date.strftime('%Y-%m-%d'),
            'present_count': present_count,
            'total_count': total_count,
            'percentage': percentage
        })
    return jsonify(output), 200


# 4. Student attendance performance for a class
@attendance_bp.route('/class/<int:class_id>/performance', methods=['GET'])
@jwt_required()
def class_performance(class_id):
    user = _current_user()
    class_obj = Class.query.get(class_id)
    if not class_obj:
        return jsonify({'message': 'Class not found'}), 404
    if not _teacher_can_access_class(user, class_id):
        return jsonify({'message': 'You are not authorized to view this report'}), 403

    sessions = AttendanceSession.query.filter_by(class_id=class_id).all()
    total_sessions = len(sessions)
    result = []

    for student in class_obj.students:
        present = 0
        absent = 0
        for session in sessions:
            record = AttendanceRecord.query.filter_by(
                session_id=session.id, student_id=student.id
            ).first()
            if record and record.status == 'Present':
                present += 1
            elif record and record.status == 'Absent':
                absent += 1
        marked = present + absent
        percentage = round((present / marked) * 100, 2) if marked else 0
        result.append({
            'student_id': student.id,
            'student_name': student.full_name,
            'present_count': present,
            'absent_count': absent,
            'total_marked': marked,
            'total_sessions': total_sessions,
            'attendance_percentage': percentage
        })

    result.sort(key=lambda item: (item['attendance_percentage'], item['student_name']))
    return jsonify(result), 200


# 5. Students below the attendance threshold
@attendance_bp.route('/class/<int:class_id>/low-attendance', methods=['GET'])
@jwt_required()
def low_attendance(class_id):
    user = _current_user()
    class_obj = Class.query.get(class_id)
    if not class_obj:
        return jsonify({'message': 'Class not found'}), 404
    if not _teacher_can_access_class(user, class_id):
        return jsonify({'message': 'You are not authorized to view this report'}), 403

    try:
        threshold = float(request.args.get('threshold', 75))
    except ValueError:
        return jsonify({'message': 'Threshold must be a number'}), 400
    threshold = max(0, min(100, threshold))

    performance_response = class_performance(class_id)
    performance_data = performance_response[0].get_json() if isinstance(performance_response, tuple) else performance_response.get_json()
    return jsonify([item for item in performance_data if item['total_marked'] > 0 and item['attendance_percentage'] < threshold]), 200
