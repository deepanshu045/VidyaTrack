from flask import Blueprint, request, jsonify
from models import db, Student
from flask_jwt_extended import jwt_required
from datetime import datetime

students_bp = Blueprint('students', __name__)

# 1. Get all students
@students_bp.route('', methods=['GET'])
@jwt_required()
def get_students():
    students = Student.query.all()
    output = []
    for s in students:
        output.append({
            "id": s.id,
            "full_name": s.full_name,
            "dob": s.dob.strftime('%Y-%m-%d'),
            "gender": s.gender,
            "status": s.status
        })
    return jsonify(output), 200

# 2. Add a new student
@students_bp.route('', methods=['POST'])
@jwt_required()
def add_student():
    data = request.get_json()

    try:
        new_student = Student(
            full_name=data['full_name'],
            dob=datetime.strptime(data['dob'], '%Y-%m-%d').date(),
            gender=data['gender'],
            parent_name=data.get('parent_name'),
            parent_contact=data.get('parent_contact'),
            address=data.get('address'),
            status=data.get('status', 'Active')
        )
        db.session.add(new_student)
        db.session.commit()
        return jsonify({"message": "Student added successfully", "id": new_student.id}), 201
    except Exception as e:
        db.session.rollback()
        return jsonify({"message": str(e)}), 400

# 3. Get student by ID
@students_bp.route('/<int:id>', methods=['GET'])
@jwt_required()
def get_student(id):
    student = Student.query.get_or_404(id)
    return jsonify({
        "id": student.id,
        "full_name": student.full_name,
        "dob": student.dob.strftime('%Y-%m-%d'),
        "gender": student.gender,
        "parent_name": student.parent_name,
        "parent_contact": student.parent_contact,
        "address": student.address,
        "admission_date": student.admission_date.strftime('%Y-%m-%d'),
        "status": student.status
    }), 200

# 4. Update student
@students_bp.route('/<int:id>', methods=['PUT'])
@jwt_required()
def update_student(id):
    student = Student.query.get_or_404(id)
    data = request.get_json()

    try:
        student.full_name = data.get('full_name', student.full_name)
        if 'dob' in data:
            student.dob = datetime.strptime(data['dob'], '%Y-%m-%d').date()
        student.gender = data.get('gender', student.gender)
        student.parent_name = data.get('parent_name', student.parent_name)
        student.parent_contact = data.get('parent_contact', student.parent_contact)
        student.address = data.get('address', student.address)
        student.status = data.get('status', student.status)

        db.session.commit()
        return jsonify({"message": "Student updated successfully"}), 200
    except Exception as e:
        db.session.rollback()
        return jsonify({"message": str(e)}), 400

# 5. Delete student (or deactivate)
@students_bp.route('/<int:id>', methods=['DELETE'])
@jwt_required()
def delete_student(id):
    student = Student.query.get_or_404(id)
    try:
        # Instead of hard delete, we often just set status to Inactive
        # but for this API we'll do a hard delete as requested
        db.session.delete(student)
        db.session.commit()
        return jsonify({"message": "Student deleted successfully"}), 200
    except Exception as e:
        db.session.rollback()
        return jsonify({"message": str(e)}), 400
