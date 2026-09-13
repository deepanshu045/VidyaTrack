from flask import Blueprint, request, jsonify
from models import db, Class, Student, Teacher
from flask_jwt_extended import jwt_required, get_jwt

classes_bp = Blueprint('classes', __name__)


def _require_admin():
    claims = get_jwt()
    return claims.get('role') == 'ADMIN'


# 1. Get all classes
@classes_bp.route('', methods=['GET'])
@jwt_required()
def get_classes():
    classes = Class.query.all()
    output = []
    for c in classes:
        output.append({
            "id": c.id,
            "name": c.name,
            "description": c.description
        })
    return jsonify(output), 200

# 2. Add a new class
@classes_bp.route('', methods=['POST'])
@jwt_required()
def add_class():
    if not _require_admin():
        return jsonify({"message": "Admin access required"}), 403

    data = request.get_json(silent=True) or {}
    try:
        name = (data.get('name') or '').strip()
        if not name:
            return jsonify({"message": "Class name is required"}), 400
        new_class = Class(
            name=name,
            description=data.get('description')
        )
        db.session.add(new_class)
        db.session.commit()
        return jsonify({"message": "Class created successfully", "id": new_class.id}), 201
    except Exception as e:
        db.session.rollback()
        return jsonify({"message": str(e)}), 400

# 3. Update a class
@classes_bp.route('/<int:id>', methods=['PUT'])
@jwt_required()
def update_class(id):
    if not _require_admin():
        return jsonify({"message": "Admin access required"}), 403

    cls = Class.query.get_or_404(id)
    data = request.get_json(silent=True) or {}
    name = (data.get('name') or '').strip()
    if not name:
        return jsonify({"message": "Class name is required"}), 400
    try:
        cls.name = name
        if 'description' in data:
            cls.description = data.get('description')
        db.session.commit()
        return jsonify({
            "message": "Class updated successfully",
            "id": cls.id,
            "name": cls.name,
            "description": cls.description
        }), 200
    except Exception as e:
        db.session.rollback()
        return jsonify({"message": str(e)}), 400

# 4. Delete a class
@classes_bp.route('/<int:id>', methods=['DELETE'])
@jwt_required()
def delete_class(id):
    if not _require_admin():
        return jsonify({"message": "Admin access required"}), 403

    cls = Class.query.get_or_404(id)
    try:
        db.session.delete(cls)
        db.session.commit()
        return jsonify({"message": "Class deleted successfully"}), 200
    except Exception as e:
        db.session.rollback()
        return jsonify({"message": str(e)}), 400

# 5. Assign student to class
@classes_bp.route('/<int:class_id>/students/<int:student_id>', methods=['POST'])
@jwt_required()
def assign_student(class_id, student_id):
    if not _require_admin():
        return jsonify({"message": "Admin access required"}), 403

    cls = Class.query.get_or_404(class_id)
    student = Student.query.get_or_404(student_id)

    if student not in cls.enrolled_students:
        cls.enrolled_students.append(student)
        db.session.commit()
        return jsonify({"message": "Student assigned to class"}), 200
    return jsonify({"message": "Student already in class"}), 400

# 6. Assign teacher to class
@classes_bp.route('/<int:class_id>/teachers/<int:teacher_id>', methods=['POST'])
@jwt_required()
def assign_teacher(class_id, teacher_id):
    if not _require_admin():
        return jsonify({"message": "Admin access required"}), 403

    cls = Class.query.get_or_404(class_id)
    teacher = Teacher.query.get_or_404(teacher_id)

    if teacher not in cls.assigned_teachers:
        cls.assigned_teachers.append(teacher)
        db.session.commit()
        return jsonify({"message": "Teacher assigned to class"}), 200
    return jsonify({"message": "Teacher already assigned to class"}), 400

# 7. Get students in a class
@classes_bp.route('/<int:id>/students', methods=['GET'])
@jwt_required()
def get_class_students(id):
    cls = Class.query.get_or_404(id)
    students = cls.enrolled_students
    output = []
    for s in students:
        output.append({
            "id": s.id,
            "full_name": s.full_name,
            "status": s.status
        })
    return jsonify(output), 200
