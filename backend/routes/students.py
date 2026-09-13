from flask import Blueprint, request, jsonify
from models import db, Student
from flask_jwt_extended import jwt_required
from auth_utils import role_required
from datetime import datetime

students_bp = Blueprint('students', __name__)

# All authenticated users can view students.
@students_bp.route('', methods=['GET'])
@jwt_required()
def get_students():
    students = Student.query.all()
    output = []
    for s in students:
        output.append({
            "id": s.id,
            "full_name": s.full_name,
            "dob": s.dob.strftime('%Y-%m-%d') if s.dob else None,
            "gender": s.gender,
            "status": s.status
        })
    return jsonify(output), 200

# Student management is an admin operation.
@students_bp.route('', methods=['POST'])
@role_required('ADMIN')
def add_student():
    data = request.get_json(silent=True) or {}
    required = ('full_name', 'dob', 'gender')
    if any(not data.get(field) for field in required):
        return jsonify({"message": "full_name, dob and gender are required"}), 400

    try:
        new_student = Student(
            full_name=data['full_name'].strip(),
            dob=datetime.strptime(data['dob'], '%Y-%m-%d').date(),
            gender=data['gender'],
            parent_name=data.get('parent_name'),
            parent_contact=data.get('parent_contact'),
            address=data.get('address'),
            status=data.get('status', 'Active')
        )
        if new_student.status not in ('Active', 'Inactive'):
            return jsonify({"message": "status must be Active or Inactive"}), 400
        db.session.add(new_student)
        db.session.commit()
        return jsonify({"message": "Student added successfully", "id": new_student.id}), 201
    except ValueError:
        db.session.rollback()
        return jsonify({"message": "dob must use YYYY-MM-DD format"}), 400
    except Exception as e:
        db.session.rollback()
        return jsonify({"message": str(e)}), 400

@students_bp.route('/<int:id>', methods=['GET'])
@jwt_required()
def get_student(id):
    student = Student.query.get_or_404(id)
    return jsonify({
        "id": student.id,
        "full_name": student.full_name,
        "dob": student.dob.strftime('%Y-%m-%d') if student.dob else None,
        "gender": student.gender,
        "parent_name": student.parent_name,
        "parent_contact": student.parent_contact,
        "address": student.address,
        "admission_date": student.admission_date.strftime('%Y-%m-%d') if student.admission_date else None,
        "status": student.status
    }), 200

@students_bp.route('/<int:id>', methods=['PUT'])
@role_required('ADMIN')
def update_student(id):
    student = Student.query.get_or_404(id)
    data = request.get_json(silent=True) or {}

    try:
        if 'full_name' in data:
            student.full_name = data['full_name'].strip()
        if 'dob' in data:
            student.dob = datetime.strptime(data['dob'], '%Y-%m-%d').date()
        if 'gender' in data:
            student.gender = data['gender']
        if 'parent_name' in data:
            student.parent_name = data['parent_name']
        if 'parent_contact' in data:
            student.parent_contact = data['parent_contact']
        if 'address' in data:
            student.address = data['address']
        if 'status' in data:
            if data['status'] not in ('Active', 'Inactive'):
                return jsonify({"message": "status must be Active or Inactive"}), 400
            student.status = data['status']

        db.session.commit()
        return jsonify({"message": "Student updated successfully"}), 200
    except ValueError:
        db.session.rollback()
        return jsonify({"message": "dob must use YYYY-MM-DD format"}), 400
    except Exception as e:
        db.session.rollback()
        return jsonify({"message": str(e)}), 400

# Deactivate instead of hard-deleting a student so attendance/history is preserved.
@students_bp.route('/<int:id>', methods=['DELETE'])
@role_required('ADMIN')
def delete_student(id):
    student = Student.query.get_or_404(id)
    try:
        student.status = 'Inactive'
        db.session.commit()
        return jsonify({"message": "Student deactivated successfully"}), 200
    except Exception as e:
        db.session.rollback()
        return jsonify({"message": str(e)}), 400
