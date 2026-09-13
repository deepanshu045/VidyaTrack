import os

class Config:
    # Database Configuration
    # Format: mysql+mysqlconnector://username:password@localhost/db_name
    SQLALCHEMY_DATABASE_URI = os.environ.get('DATABASE_URL') or \
        'mysql+mysqlconnector://root:Deepanshu%40Mysql%239920@localhost/vidyatrack_db'
    SQLALCHEMY_TRACK_MODIFICATIONS = False

    # Security
    SECRET_KEY = os.environ.get('SECRET_KEY') or 'vidyatrack-very-secret-key'
    JWT_SECRET_KEY = os.environ.get('JWT_SECRET_KEY') or 'jwt-secret-key'
