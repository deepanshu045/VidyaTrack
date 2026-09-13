from flask import Blueprint, request, jsonify
from models import db, AttendanceSession, AttendanceRecord, Student, Class
from flask_jwt_extended import jwt_required, get_jwt_identity
from datetime import datetime

attendance_bp = Blueprint('attendance', __name__)

# 1. Mark Attendance
@attendance_bp.route('', methods=['POST'])
@jwt_required()
def mark_attendance():
    data = request.get_json()
    user = get_jwt_identity()

    # In a real app, we'd verify the teacher is assigned to this class
    # For now, we'll allow any authenticated user (mostly teachers)

    try:
        class_id = data['class_id']
        teacher_id = data['teacher_id']
        date_str = data['date']
        attendance_date = datetime.strptime(date_str, '%Y-%m-%d').date()
        records = data['records'] # List of {student_id: X, status: 'Present'/'Absent'}

        # Check if session already exists for this class and date
        session = AttendanceSession.query.filter_by(class_id=class_id, attendance_date=attendance_date).first()

        if session:
            # If exists, update records (or delete and recreate)
            # For simplicity, we'll delete old records and recreate
            AttendanceRecord.query.filter_by(session_id=session.id).delete()
        else:
            session = AttendanceSession(
                class_id=class_id,
                teacher_id=teacher_id,
                attendance_date=attendance_date
            )
            db.session.add(session)
            db.session.flush() # Get session.id

        for r in records:
            new_record = AttendanceRecord(
                session_id=session.id,
                student_id=r['student_id'],
                status=r['status']
            )
            db.session.add(new_record)

        db.session.commit()
        return jsonify({"message": "Attendance saved successfully"}), 201
    except Exception as e:
        db.session.rollback()
        return jsonify({"message": str(e)}), 400

# 2. Get attendance for a class on a specific date
@attendance_bp.route('/class/<int:class_id>/date/<string:date_str>', methods=['GET'])
@jwt_required()
def get_class_attendance(class_id, date_str):
    try:
        attendance_date = datetime.strptime(date_str, '%Y-%m-%d').date()
        session = AttendanceSession.query.filter_by(class_id=class_id, attendance_date=attendance_date).first()

        if not session:
            return jsonify({"message": "No attendance record found for this date", "records": []}), 200

        records = AttendanceRecord.query.filter_by(session_id=session.id).all()
        output = []
        for r in records:
            student = Student.query.get(r.student_id)
            output.append({
                "student_id": r.student_id,
                "student_name": student.full_name,
                "status": r.status
            })
        return jsonify({"session_id": session.id, "records": output}), 200
    except Exception as e:
        return jsonify({"message": str(e)}), 400

# 3. Get attendance history for a class
@attendance_bp.route('/class/<int:class_id>/history', methods=['GET'])
@jwt_required()
def get_class_attendance_history(class_id):
    sessions = AttendanceSession.query.filter_by(class_id=class_id).order_by(AttendanceSession.attendance_date.desc()).all()
    output = []
    for s in sessions:
        # Calculate summary for this session
        present_count = AttendanceRecord.query.filter_by(session_id=s.id, status='Present').count()
        total_count = AttendanceRecord.query.filter_by(session_id=s.id).count()

        output.append({
            "id": s.id,
            "date": s.attendance_date.strftime('%Y-%m-%d'),
            "present_count": present_count,
            "total_count": total_count
        })
    return jsonify(output), 200
