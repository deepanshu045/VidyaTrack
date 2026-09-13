from flask import Blueprint, jsonify
from models import Student, Teacher, Class, AttendanceSession, AttendanceRecord
from flask_jwt_extended import jwt_required, get_jwt_identity
from sqlalchemy import func

dashboard_bp = Blueprint('dashboard', __name__)

@dashboard_bp.route('/admin/stats', methods=['GET'])
@jwt_required()
def get_admin_stats():
    user = get_jwt_identity()
    if user['role'] != 'ADMIN':
        return jsonify({"message": "Unauthorized"}), 403

    total_students = Student.query.count()
    active_students = Student.query.filter_by(status='Active').count()
    total_teachers = Teacher.query.count()
    total_classes = Class.query.count()

    return jsonify({
        "total_students": total_students,
        "active_students": active_students,
        "total_teachers": total_teachers,
        "total_classes": total_classes,
        "today_attendance": 0, # Placeholder until attendance is implemented
        "overall_percentage": 0 # Placeholder
    }), 200

@dashboard_bp.route('/teacher/summary', methods=['GET'])
@jwt_required()
def get_teacher_summary():
    user = get_jwt_identity()
    if user['role'] != 'TEACHER':
        return jsonify({"message": "Unauthorized"}), 403

    teacher_id = user.get('id') # This is the user id, we need teacher_id
    # Wait, the token identity was {'username': user.username, 'role': user.role, 'id': user.id}
    # We need to find the teacher_id associated with this user_id
    from models import User
    user_obj = User.query.get(teacher_id)
    if not user_obj or not user_obj.teacher_id:
        return jsonify({"message": "Teacher profile not found"}), 404

    teacher = Teacher.query.get(user_obj.teacher_id)
    assigned_classes = teacher.classes

    classes_data = []
    for c in assigned_classes:
        student_count = len(c.enrolled_students)
        classes_data.append({
            "id": c.id,
            "name": c.name,
            "student_count": student_count
        })

    return jsonify({
        "teacher_name": teacher.full_name,
        "assigned_classes": classes_data
    }), 200
