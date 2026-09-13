from functools import wraps

from flask import jsonify
from flask_jwt_extended import get_jwt, verify_jwt_in_request


def role_required(*allowed_roles):
    """Require a valid JWT whose role is one of the supplied roles."""
    allowed = {role.upper() for role in allowed_roles}

    def decorator(fn):
        @wraps(fn)
        def wrapper(*args, **kwargs):
            verify_jwt_in_request()
            claims = get_jwt()
            role = str(claims.get("role", "")).upper()
            if role not in allowed:
                return jsonify({"message": "You do not have permission to perform this action"}), 403
            return fn(*args, **kwargs)

        return wrapper

    return decorator
