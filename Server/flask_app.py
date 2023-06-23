import random
from datetime import datetime
from hashlib import sha256

from flask import Flask, jsonify, request, abort
from sqlalchemy import or_, desc
from sqlalchemy.exc import NoResultFound

from models import db, User, Device, Lock, Pass, Status

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///database.db'
db.init_app(app)


# Functions(Lock)
@app.route('/first_config_l1', methods=['POST'])
def firstConfigL1():
    data = request.get_json()

    user_id = data['UserId']
    device_id = data["DeviceId"]
    device_pattern = data["DevicePattern"]

    print(user_id, device_id)

    validation = db.session.execute(
        db.select(Device).filter_by(user_id=user_id, id=device_id, pattern=device_pattern)).scalar_one()

    if validation is None:
        abort(403, description="Authentication failed")
        return

    lock = Lock(user_id=user_id)
    db.session.add(lock)
    db.session.commit()

    content = {
        "LockId": lock.id,
        "LoginHash": validation.login_hash,
    }

    return jsonify(content)


@app.route('/first_config_l2', methods=["POST"])
def firstConfigL2():
    data = request.get_json()

    user_id = data['UserId']
    lock_id = data["LockId"]

    try:
        validation = db.session.execute(db.select(Lock).filter_by(id=lock_id, user_id=user_id)).scalar_one()
        lock = validation
    except NoResultFound:
        abort(403, description="Authentication failed")
        return

    lock.active = True
    db.session.commit()

    content = {
        "Status": 1,
        "LockId": lock_id,
        "UserId": user_id,
    }

    return jsonify(content)


@app.route('/user_login', methods=["POST"])
def userLogin():
    data = request.get_json()

    user_email = data["UserEmail"].strip()
    user_email = user_email.lower()
    password = data["Pass"].strip()

    password = sha256(password.encode('utf-8')).hexdigest()

    pattern = data["Pattern"]

    if '@' in user_email:
        email = user_email

        try:
            validation = db.session.execute(db.select(User).filter_by(email=email, password=password)).scalar_one()

        except NoResultFound:
            abort(403, description="Authentication failed")
            return

        user = validation

    else:
        username = user_email

        try:
            validation = db.session.execute(
                db.select(User).filter_by(username=username, password=password)).scalar_one()

        except NoResultFound:
            abort(403, description="Authentication failed")
            return

        user = validation

    login_hash = random.getrandbits(128)
    print(user.id)
    print(str(login_hash))

    device = Device(
        user_id=user.id,
        pattern=pattern,
        login_hash=str(login_hash),
    )
    db.session.add(device)
    db.session.commit()

    content = {
        "UserId": user.id,
        "LoginHash": login_hash,
        "DeviceId": device.id,
    }

    return jsonify(content)


@app.route('/user_register', methods=["POST"])
def userRegister():
    data = request.get_json()

    username = data["Username"].strip().lower()
    password = data["Pass"].strip()
    email = data["Email"]

    try:
        validation = db.session.execute(
            db.select(User).filter((User.username == username) | (User.email == email))).scalar_one()

        abort(409, description="User Already exist with this email or username")
        return

    except NoResultFound:

        user = User(
            username=username,
            password=sha256(password.encode('utf-8')).hexdigest(),
            email=email,
        )

    db.session.add(user)
    db.session.commit()

    content = {
        "Status": 1,
    }

    return jsonify(content)


@app.route('/set_pin', methods=["POST"])
def setPin():
    data = request.get_json()

    valid_time = data["ValidTime"]
    uses = data["Uses"]
    user_id = data["UserId"]
    device_id = data["DeviceId"]
    login_hash = data["LoginHash"]
    lock_id = data["LockId"]

    try:
        validation = db.session.execute(
            db.select(Device).filter_by(id=device_id, user_id=user_id, login_hash=login_hash)).scalar_one()

        device = validation

    except NoResultFound:
        abort(403, description="Authentication failed")
        return

    try:
        validation = db.session.execute(
            db.select(Lock).filter_by(id=lock_id, user_id=user_id)).scalar_one()

        lock = validation

    # except NoResultFound:
    #     abort(403, description="Authentication failed")
    except Exception as e:
        abort(404, description=e)
        return


    if lock.active is False:
        abort(403, description="Lock is not active")
        return

    valid_time_final = datetime.strptime(valid_time, "%Y-%m-%d %H:%M:%S")

    pin = Pass(
        lock_id=lock_id,
        user_id=user_id,
        pin=random.randint(1000, 9999),
        valid_time=valid_time_final,  # format: 2021-09-09 15:44:15
        uses=int(uses),
    )

    db.session.add(pin)
    db.session.commit()

    content = {
        "Pin": pin.pin,
        "Pattern": device.pattern,
    }

    return jsonify(content)



@app.route('/unlock_lock', methods=["POST"])
def unlockLock():
    data = request.get_json()

    lock_id = data["LockId"]
    user_id = data["UserId"]

    try:
        validation = db.session.execute(
            db.select(Pass).filter_by(user_id=user_id, lock_id=lock_id).order_by(desc(Pass.created_date)).limit(
                1)).scalar_one()

        pin = validation

    except NoResultFound:
        # except Exception as e:
        abort(404, description="No pins found")
        # abort(404, description=e)
        return

    if pin.valid_time < datetime.now():
        abort(404, description="No pins found")
        return

    if pin.uses <= 0:
        abort(404, description="No pins found")
        return

    pin.uses -= 1
    db.session.commit()

    content = {
        "Pin": pin.pin,
        "Status": 1,
        "UserId": user_id,
    }

    return jsonify(content)


@app.route('/proc_status', methods=["POST"])
def procStatus():
    data = request.get_json()

    lock_id = data["LockId"]
    user_id = data["UserId"]
    proc_status = data["Status"]

    status_labels = ['Locked', 'Unlocked', 'Successful attempt', 'Unsuccessful attempt', 'Error', 'First config',
                     'Wi-Fi change', 'Owner change']

    status = Status(
        lock_id=lock_id,
        status=status_labels[int(proc_status)],
    )

    db.session.add(status)
    db.session.commit()

    content = {
        "Status": 1,
    }

    return jsonify(content)


@app.route('/get_proc_status', methods=["POST"])
def getProcStatus():
    data = request.get_json()

    user_id = data["UserId"]
    device_id = data["DeviceId"]
    login_hash = data["LoginHash"]
    lock_id = data["LockId"]

    try:
        validation = db.session.execute(
            db.select(Device).filter_by(user_id=user_id, id=device_id, login_hash=login_hash)).scalar_one()

    except NoResultFound:
        abort(403, description="Authentication failed")
        return

    try:
        sql_command = f"SELECT * FROM status WHERE lock_id='{lock_id}' ORDER BY created_date DESC;"

        statuses = db.session.execute(sql_command)

    except NoResultFound:
        abort(404, description="No locks found")
        return

    statuses_final = []
    for tmp in statuses:
        statuses_final.append({
            "Id": tmp.id,
            "LockId": lock_id,
            "Status": tmp.status,
            "CreatedDate": tmp.created_date,
        })

    content = {
        "Status": 1,
        "Statuses": statuses_final,
    }

    return jsonify(content)


@app.route('/remove_lock', methods=["POST"])
def removeLock():
    data = request.get_json()

    lock_id = data["LockId"]
    user_id = data["UserId"]

    try:
        validation = db.session.execute(
            db.select(Lock).filter_by(user_id=user_id, id=lock_id)).scalar_one()

        lock = validation

    except NoResultFound:
        abort(403, description="Authentication failed")
        return

    try:
        passes = db.session.execute(
            db.select(Pass).filter_by(user_id=user_id, lock_id=lock_id)).scalars()

        for pin in passes:
            db.session.delete(pin)

    except NoResultFound:
        pass

    try:
        statuses = db.session.execute(
            db.select(Status).filter_by(lock_id=lock_id)).scalars()

        for status in statuses:
            db.session.delete(status)

    except NoResultFound:
        pass

    db.session.delete(lock)
    db.session.commit()

    content = {
        "Status": 1,
    }

    return jsonify(content)


@app.route('/user_logout', methods=["POST"])
def userLogout():
    data = request.get_json()

    user_id = data["UserId"]
    login_hash = data["LoginHash"]
    device_id = data["DeviceId"]

    try:
        validation = db.session.execute(
            db.select(Device).filter_by(user_id=user_id, id=device_id, login_hash=login_hash)).scalar_one()

        device = validation

    except NoResultFound:
        abort(403, description="Authentication failed")
        return

    db.session.delete(device)
    db.session.commit()

    content = {
        "Status": 1,
    }

    return jsonify(content)


@app.route('/list_of_locks', methods=["POST"])
def listOfLocks():
    data = request.get_json()

    user_id = data["UserId"]
    device_id = data["DeviceId"]
    login_hash = data["LoginHash"]

    try:
        validation = db.session.execute(
            db.select(Device).filter_by(user_id=user_id, id=device_id, login_hash=login_hash)).scalar_one()

    except NoResultFound:
        abort(403, description="Authentication failed")
        return

    try:
        sql_command = f"SELECT * FROM lock WHERE user_id='{user_id}';"

        locks = db.session.execute(sql_command)

    except NoResultFound:
        abort(404, description="No locks found")
        return

    # try:
    #     for lock in locks:
    #         print(lock)

    # except Exception as e:
    #     abort(405, description=locks)
    #     return

    locks_final = []
    for lock in locks:
        locks_final.append({
            "LockId": lock.id,
            "UserId": user_id,
            "Name": lock.name,
            "Active": bool(lock.active),
        })

    content = {
        "Status": 1,
        "Locks": locks_final,
    }

    return jsonify(content)


@app.route('/get_last_pin', methods=["POST"])
def getLastPin():
    data = request.get_json()

    user_id = data["UserId"]
    lock_id = data["LockId"]
    device_id = data["DeviceId"]
    login_hash = data["LoginHash"]

    try:
        validation = db.session.execute(
            db.select(Device).filter_by(user_id=user_id, id=device_id, login_hash=login_hash)).scalar_one()

    except NoResultFound:
    # except Exception as e:
        # abort(403, description=e)
        abort(403, description="Authentication failed")
        return

    try:
        pin = db.session.execute(
            db.select(Pass).filter_by(user_id=user_id, lock_id=lock_id).order_by(desc(Pass.created_date)).limit(
                1)).scalar_one()

    # except NoResultFound:
    #     abort(404, description="No Pins found")
    except Exception as e:
        abort(403, description=e)
        return

    valid_time_final = pin.valid_time.strftime("%Y-%m-%d %H:%M:%S")

    content = {
        "Status": 1,
        "PinId": pin.id,
        "Pin": pin.pin,
        "Expiry": valid_time_final,
        "AU": pin.uses,
        "LockId": pin.lock_id,
    }

    return jsonify(content)


@app.route('/change_lock_name', methods=["POST"])
def changeLockName():
    data = request.get_json()

    lock_id = data["LockId"]
    user_id = data["UserId"]
    device_id = data["DeviceId"]
    login_hash = data["LoginHash"]
    name = data["Name"]

    try:
        validation = db.session.execute(
            db.select(Device).filter_by(user_id=user_id, id=device_id, login_hash=login_hash)).scalar_one()

    except NoResultFound:
        abort(403, description="Authentication failed")
        return

    try:
        validation = db.session.execute(
            db.select(Lock).filter_by(user_id=user_id, id=lock_id)).scalar_one()

        lock = validation

    except NoResultFound:
        abort(403, description="Authentication failed")
        return

    lock.name = name
    db.session.commit()

    content = {
        "Status": 1,
    }

    return jsonify(content)


# Main app
if __name__ == '__main__':
    app.run(debug=True)
