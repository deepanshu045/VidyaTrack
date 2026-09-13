from flask import Flask, request, jsonify
from flask_jwt_extended import JWTManager, create_access_token, jwt_required, get_jwt_identity
from models import db, User, Teacher, bcrypt
from config import Config
from flask_cors import CORS
from routes.students import students_bp
from routes.teachers import teachers_bp
from routes.classes import classes_bp
from routes.dashboard import dashboard_bp
from routes.attendance import attendance_bp

app = Flask(__name__)
app.config.from_object(Config)

# Initialize extensions
db.init_app(app)
bcrypt.init_app(app)
jwt = JWTManager(app)
CORS(app)

# Register Blueprints
app.register_blueprint(students_bp, url_prefix='/api/students')
app.register_blueprint(teachers_bp, url_prefix='/api/teachers')
app.register_blueprint(classes_bp, url_prefix='/api/classes')
app.register_blueprint(dashboard_bp, url_prefix='/api/dashboard')
app.register_blueprint(attendance_bp, url_prefix='/api/attendance')

# 1. Authentication Route (Login)
@app.route('/api/auth/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    if not username or not password:
        return jsonify({"message": "Username and password are required"}), 400

    user = User.query.filter_by(username=username).first()

    if user and user.check_password(password):
        access_token = create_access_token(identity={'username': user.username, 'role': user.role, 'id': user.id})

        # If it's a teacher, send teacher_id too
        response_data = {
            "token": access_token,
            "role": user.role,
            "username": user.username
        }
        if user.teacher_id:
            response_data['teacher_id'] = user.teacher_id

        return jsonify(response_data), 200

    return jsonify({"message": "Invalid credentials"}), 401

# 2. Basic Health Check
@app.route('/api/health', methods=['GET'])
def health_check():
    return jsonify({"status": "Backend is running"}), 200

# Function to initialize database and create an admin
def init_db():
    with app.app_context():
        db.create_all()
        # Create default admin if not exists
        if not User.query.filter_by(username='admin').first():
            admin = User(username='admin', role='ADMIN')
            admin.set_password('admin123')
            db.session.add(admin)
            db.session.commit()
            print("Default admin created: admin / admin123")

if __name__ == '__main__':
    # Initialize DB (Optional: can be run separately)
    # init_db()
    app.run(host='0.0.0.0', port=5000, debug=True)
