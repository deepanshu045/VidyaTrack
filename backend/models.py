from flask_sqlalchemy import SQLAlchemy
from datetime import datetime
from flask_bcrypt import Bcrypt

db = SQLAlchemy()
bcrypt = Bcrypt()

# Junction Table for Student <-> Class (Many-to-Many)
student_classes = db.Table('student_classes',
    db.Column('student_id', db.Integer, db.ForeignKey('students.id'), primary_key=True),
    db.Column('class_id', db.Integer, db.ForeignKey('classes.id'), primary_key=True)
)

# Junction Table for Teacher <-> Class (Many-to-Many)
teacher_classes = db.Table('teacher_classes',
    db.Column('teacher_id', db.Integer, db.ForeignKey('teachers.id'), primary_key=True),
    db.Column('class_id', db.Integer, db.ForeignKey('classes.id'), primary_key=True)
)

class Teacher(db.Model):
    __tablename__ = 'teachers'
    id = db.Column(db.Integer, primary_key=True)
    full_name = db.Column(db.String(100), nullable=False)
    email = db.Column(db.String(100), unique=True, nullable=False)
    contact_number = db.Column(db.String(15))
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

    # Relationship: One teacher has one user account
    user = db.relationship('User', backref='teacher_profile', uselist=False)
    # Relationship: One teacher can have many classes
    classes = db.relationship('Class', secondary=teacher_classes, backref='assigned_teachers')

class User(db.Model):
    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(50), unique=True, nullable=False)
    password_hash = db.Column(db.String(255), nullable=False)
    role = db.Column(db.Enum('ADMIN', 'TEACHER'), nullable=False)
    teacher_id = db.Column(db.Integer, db.ForeignKey('teachers.id'), unique=True)

    def set_password(self, password):
        self.password_hash = bcrypt.generate_password_hash(password).decode('utf-8')

    def check_password(self, password):
        return bcrypt.check_password_hash(self.password_hash, password)

class Student(db.Model):
    __tablename__ = 'students'
    id = db.Column(db.Integer, primary_key=True)
    full_name = db.Column(db.String(100), nullable=False)
    dob = db.Column(db.Date, nullable=False)
    gender = db.Column(db.Enum('Male', 'Female', 'Other'), nullable=False)
    parent_name = db.Column(db.String(100))
    parent_contact = db.Column(db.String(15))
    address = db.Column(db.Text)
    admission_date = db.Column(db.Date, default=datetime.utcnow().date())
    status = db.Column(db.Enum('Active', 'Inactive'), default='Active')

    # Relationship: Many-to-Many with classes
    classes = db.relationship('Class', secondary=student_classes, backref='enrolled_students')

class Class(db.Model):
    __tablename__ = 'classes'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(50), nullable=False)
    description = db.Column(db.String(255))

class AttendanceSession(db.Model):
    __tablename__ = 'attendance_sessions'
    id = db.Column(db.Integer, primary_key=True)
    class_id = db.Column(db.Integer, db.ForeignKey('classes.id'), nullable=False)
    teacher_id = db.Column(db.Integer, db.ForeignKey('teachers.id'), nullable=False)
    attendance_date = db.Column(db.Date, nullable=False)

    # Unique constraint for class and date
    __table_args__ = (db.UniqueConstraint('class_id', 'attendance_date', name='unique_session'),)

class AttendanceRecord(db.Model):
    __tablename__ = 'attendance_records'
    id = db.Column(db.Integer, primary_key=True)
    session_id = db.Column(db.Integer, db.ForeignKey('attendance_sessions.id'), nullable=False)
    student_id = db.Column(db.Integer, db.ForeignKey('students.id'), nullable=False)
    status = db.Column(db.Enum('Present', 'Absent'), nullable=False)
