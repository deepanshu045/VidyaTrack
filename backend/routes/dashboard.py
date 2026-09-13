from datetime import date

from flask import Blueprint, jsonify, request
from models import Student, Teacher, Class, AttendanceSession, AttendanceRecord, User
from flask_jwt_extended import jwt_required, get_jwt_identity


dashboard_bp = Blueprint('dashboard', __name__)


def _attendance_percentage(present, total):
    return round((present / total) * 100, 2) if total else 0.0


def _require_role(role):
    user = get_jwt_identity()
    if not user or user.get('role') != role:
        return None, (jsonify({'message': 'Unauthorized'}), 403)
    return user, None


@dashboard_bp.route('/admin/stats', methods=['GET'])
@jwt_required()
def get_admin_stats():
    user, error = _require_role('ADMIN')
    if error:
        return error

    total_students = Student.query.count()
    active_students = Student.query.filter_by(status='Active').count()
    total_teachers = Teacher.query.count()
    total_classes = Class.query.count()

    today_sessions = AttendanceSession.query.filter_by(attendance_date=date.today()).all()
    today_records = []
    for session in today_sessions:
        today_records.extend(
            AttendanceRecord.query.filter_by(session_id=session.id).all()
        )

    today_present = sum(1 for record in today_records if record.status == 'Present')
    today_total = len(today_records)

    all_records = AttendanceRecord.query.all()
    overall_present = sum(1 for record in all_records if record.status == 'Present')
    overall_total = len(all_records)

    return jsonify({
        'total_students': total_students,
        'active_students': active_students,
        'total_teachers': total_teachers,
        'total_classes': total_classes,
        'today_attendance': _attendance_percentage(today_present, today_total),
        'today_present': today_present,
        'today_total': today_total,
        'overall_percentage': _attendance_percentage(overall_present, overall_total)
    }), 200


@dashboard_bp.route('/teacher/summary', methods=['GET'])
@jwt_required()
def get_teacher_summary():
    user, error = _require_role('TEACHER')
    if error:
        return error

    user_obj = User.query.get(user.get('id'))
    if not user_obj or not user_obj.teacher_id:
        return jsonify({'message': 'Teacher profile not found'}), 404

    teacher = Teacher.query.get(user_obj.teacher_id)
    if not teacher:
        return jsonify({'message': 'Teacher profile not found'}), 404

    classes_data = []
    teacher_records = []
    for class_obj in teacher.classes:
        student_count = len(class_obj.enrolled_students)
        sessions = AttendanceSession.query.filter_by(
            class_id=class_obj.id,
            teacher_id=teacher.id
        ).all()
        class_records = []
        for session in sessions:
            class_records.extend(
                AttendanceRecord.query.filter_by(session_id=session.id).all()
            )
        teacher_records.extend(class_records)
        present = sum(1 for record in class_records if record.status == 'Present')
        classes_data.append({
            'id': class_obj.id,
            'name': class_obj.name,
            'student_count': student_count,
            'attendance_percentage': _attendance_percentage(present, len(class_records))
        })

    teacher_present = sum(1 for record in teacher_records if record.status == 'Present')

    return jsonify({
        'teacher_name': teacher.full_name,
        'assigned_classes': classes_data,
        'overall_attendance_percentage': _attendance_percentage(
            teacher_present, len(teacher_records)
        )
    }), 200


@dashboard_bp.route('/class/<int:class_id>/performance', methods=['GET'])
@jwt_required()
def get_class_performance(class_id):
    class_obj = Class.query.get(class_id)
    if not class_obj:
        return jsonify({'message': 'Class not found'}), 404

    sessions = AttendanceSession.query.filter_by(class_id=class_id).all()
    session_ids = [session.id for session in sessions]
    students = class_obj.enrolled_students

    result = []
    for student in students:
        records = AttendanceRecord.query.filter(
            AttendanceRecord.session_id.in_(session_ids),
            AttendanceRecord.student_id == student.id
        ).all() if session_ids else []
        present = sum(1 for record in records if record.status == 'Present')
        total = len(records)
        result.append({
            'student_id': student.id,
            'student_name': student.full_name,
            'present': present,
            'absent': total - present,
            'total': total,
            'attendance_percentage': _attendance_percentage(present, total)
        })

    return jsonify({
        'class_id': class_id,
        'class_name': class_obj.name,
        'students': result
    }), 200


@dashboard_bp.route('/class/<int:class_id>/low-attendance', methods=['GET'])
@jwt_required()
def get_low_attendance(class_id):
    class_obj = Class.query.get(class_id)
    if not class_obj:
        return jsonify({'message': 'Class not found'}), 404

    try:
        threshold = float(request.args.get('threshold', 75))
    except ValueError:
        return jsonify({'message': 'Threshold must be a number'}), 400

    if threshold < 0 or threshold > 100:
        return jsonify({'message': 'Threshold must be between 0 and 100'}), 400

    performance = get_class_performance(class_id)
    if isinstance(performance, tuple):
        response, status = performance
        if status != 200:
            return response, status
        data = response.get_json()
    else:
        data = performance.get_json()

    low_attendance = [
        student for student in data['students']
        if student['total'] > 0 and student['attendance_percentage'] < threshold
    ]

    return jsonify({
        'class_id': class_id,
        'class_name': class_obj.name,
        'threshold': threshold,
        'students': low_attendance
    }), 200
