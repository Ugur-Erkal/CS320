from flask import Blueprint, render_template, request, redirect, flash, session
from app.db import get_db_connection

auth_bp = Blueprint('auth', __name__)

@auth_bp.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        usertype = request.form['usertype']

        conn = get_db_connection()
        cursor = conn.cursor()

        cursor.execute("SELECT * FROM User WHERE Username = %s", (username,))
        existing = cursor.fetchone()

        if existing:
            flash("This username is already taken.", "error")
            cursor.close()
            conn.close()
            return redirect('/register')

        if usertype == 'Customer':
            address = request.form['address']
            phone = request.form['phone']
            city = request.form['city']

            cursor.execute("INSERT INTO User (Username, Password, UserType) VALUES (%s, %s, %s)",
                           (username, password, 'Customer'))
            user_id = cursor.lastrowid

            cursor.execute("INSERT INTO UserAddress (Address, City) VALUES (%s, %s)", (address, city))
            address_id = cursor.lastrowid

            cursor.execute("INSERT INTO Lives (UserID, AddressID) VALUES (%s, %s)", (user_id, address_id))

            cursor.execute("INSERT INTO UserPhoneNumber (PhoneNumber) VALUES (%s)", (phone,))
            phone_id = cursor.lastrowid

            cursor.execute("INSERT INTO HasPhoneNum (UserID, PhoneID) VALUES (%s, %s)", (user_id, phone_id))

        elif usertype == 'Restaurant':
            restaurant_name = request.form['restaurant_name']
            restaurant_address = request.form['restaurant_address']
            restaurant_city = request.form['restaurant_city']
            cuisine_type = request.form['cuisine_type']

            cursor.execute("INSERT INTO User (Username, Password, UserType) VALUES (%s, %s, %s)",
                           (username, password, 'Manager'))
            user_id = cursor.lastrowid

            cursor.execute("INSERT INTO Restaurant (RestaurantName, Address, City, CuisineType) VALUES (%s, %s, %s, %s)",
                (restaurant_name, restaurant_address, restaurant_city, cuisine_type))
            restaurant_id = cursor.lastrowid

            cursor.execute("INSERT INTO Manages (UserID, RestaurantID) VALUES (%s, %s)",
                           (user_id, restaurant_id))

        conn.commit()
        cursor.close()
        conn.close()

        flash("Registration successful! Please log in.", "success")
        return redirect('/login')

    return render_template("register.html")




@auth_bp.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        usertype = request.form['usertype']

        conn = get_db_connection()

        cursor1 = conn.cursor(dictionary=True, buffered=True)
        cursor1.execute("""
            SELECT * FROM User 
            WHERE Username = %s AND Password = %s AND UserType = %s
        """, (username, password, usertype))
        user = cursor1.fetchone()
        cursor1.close()

        if user:
            if usertype == 'Manager':
                cursor2 = conn.cursor(dictionary=True, buffered=True)
                cursor2.execute("""
                    SELECT * FROM Manages 
                    WHERE UserID = %s
                """, (user['UserID'],))
                manages = cursor2.fetchone()
                cursor2.close()

                if not manages:
                    flash("You are not assigned to manage any restaurant.", "error")
                    conn.close()
                    return redirect('/login')

            session['user_id'] = user['UserID']
            session['username'] = user['Username']
            session['usertype'] = user['UserType']
            flash("Login successful!", "success")
            conn.close()

            if usertype == 'Manager':
                return redirect('/dashboard')
            else:
                return redirect('/search')
        else:
            flash("Invalid credentials or user type mismatch.", "error")
            conn.close()

    return render_template("login.html")


@auth_bp.route('/logout')
def logout():
    session.clear()
    flash("Successfully logged out!", "success")
    return redirect('/login')
