from datetime import datetime
from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()


class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    password = db.Column(db.String(120), nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    created_date = db.Column(db.DateTime, default=datetime.utcnow, nullable=False)

    devices = db.relationship('Device', backref='user', lazy=True)
    locks = db.relationship('Lock', backref='user', lazy=True)

    def __repr__(self):
        return f"User: {self.username}"


class Device(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    pattern = db.Column(db.String(120), unique=True, nullable=False)
    login_hash = db.Column(db.String(120), nullable=False)

    created_date = db.Column(db.DateTime, default=datetime.utcnow)

    def __repr__(self):
        return f"Device#: {self.id}"


class Lock(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(80), nullable=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    active = db.Column(db.Boolean, default=False)
    created_date = db.Column(db.DateTime, default=datetime.utcnow)

    passes = db.relationship('Pass', backref='lock', lazy=True)
    statuses = db.relationship('Status', backref='lock', lazy=True)

    def __repr__(self):
        return f"Lock#: {self.id}"


class Pass(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    lock_id = db.Column(db.Integer, db.ForeignKey('lock.id'), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    pin = db.Column(db.String(120), nullable=False)
    valid_time = db.Column(db.DateTime, nullable=False)
    uses = db.Column(db.Integer, nullable=False)
    created_date = db.Column(db.DateTime, default=datetime.utcnow)

    def __repr__(self):
        return f"Pass#: {self.id}"


class Status(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    lock_id = db.Column(db.Integer, db.ForeignKey('lock.id'), nullable=False)
    status = db.Column(
        db.Enum('Locked', 'Unlocked', 'Successful attempt', 'Unsuccessful attempt', 'Error', 'First config',
                'Wi-Fi change', 'Owner change', name='status_type'), nullable=False)
    created_date = db.Column(db.DateTime, default=datetime.utcnow)

    def __repr__(self):
        return f"Status#: {self.id}"
