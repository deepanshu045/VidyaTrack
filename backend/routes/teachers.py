from flask import Blueprint, request, jsonify
from models import db, Teacher, User
from flask_jwt_extended import jwt_required

teachers_bp = Blueprint('teachers', __name__)

# 1. Get all teachers
@teachers_bp.route('', methods=['GET'])
@jwt_required()
def get_teachers():
    teachers = Teacher.query.all()
    output = []
    for t in teachers:
        output.append({
            "id": t.id,
            "full_name": t.full_name,
            "email": t.email,
            "contact_number": t.contact_number
        })
    return jsonify(output), 200

# 2. Add a new teacher (and their user account)
@teachers_bp.route('', methods=['POST'])
@jwt_required()
def add_teacher():
    data = request.get_json()

    try:
        # Create teacher profile
        new_teacher = Teacher(
            full_name=data['full_name'],
            email=data['email'],
            contact_number=data.get('contact_number')
        )
        db.session.add(new_teacher)
        db.session.flush() # To get the new_teacher.id

        # Create associated user account
        new_user = User(
            username=data['username'],
            role='TEACHER',
            teacher_id=new_teacher.id
        )
        new_user.set_password(data['password'])
        db.session.add(new_user)

        db.session.commit()
        return jsonify({"message": "Teacher added successfully", "id": new_teacher.id}), 201
    except Exception as e:
        db.session.rollback()
        return jsonify({"message": str(e)}), 400

# 3. Update teacher
@teachers_bp.route('/<int:id>', methods=['PUT'])
@jwt_required()
def update_teacher(id):
    teacher = Teacher.query.get_or_404(id)
    data = request.get_json()

    try:
        teacher.full_name = data.get('full_name', teacher.full_name)
        teacher.email = data.get('email', teacher.email)
        teacher.contact_number = data.get('contact_number', teacher.contact_number)

        db.session.commit()
        return jsonify({"message": "Teacher updated successfully"}), 200
    except Exception as e:
        db.session.rollback()
        return jsonify({"message": str(e)}), 400
