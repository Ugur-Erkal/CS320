from flask import Blueprint, render_template, session, redirect, request, flash
from app.db import get_db_connection

restaurant_bp = Blueprint('restaurant', __name__)


@restaurant_bp.route('/dashboard')
def dashboard():
    if 'user_id' not in session or session.get('usertype') != 'Manager':
        return redirect('/login')

    current_user = session['user_id']
    connection = get_db_connection()

    fetch_cursor = connection.cursor(dictionary=True, buffered=True)
    fetch_cursor.execute("""
        SELECT R.RestaurantID, R.RestaurantName
        FROM Restaurant R
        JOIN Manages M ON R.RestaurantID = M.RestaurantID
        WHERE M.UserID = %s
    """, (current_user,))
    dining_places = fetch_cursor.fetchall()

    selected_id = request.args.get('restaurant_id')
    if not selected_id and dining_places:
        selected_id = dining_places[0]['RestaurantID']

    order_cursor = connection.cursor(dictionary=True)
    order_cursor.execute("""
        SELECT Ca.CartID, U.Username, M.Name AS ItemName, C.Quantity, Ca.Status,
               (SELECT SUM(M2.Price * C2.Quantity)
                FROM Contains C2
                JOIN MenuItem M2 ON C2.MenuItemID = M2.MenuItemID
                WHERE C2.CartID = Ca.CartID) AS TotalPrice
        FROM Holds H
        JOIN Cart Ca ON H.CartID = Ca.CartID
        JOIN Contains C ON Ca.CartID = C.CartID
        JOIN MenuItem M ON C.MenuItemID = M.MenuItemID
        JOIN Belongs B ON Ca.CartID = B.CartID
        JOIN User U ON B.UserID = U.UserID
        WHERE H.RestaurantID = %s AND Ca.Status = 'Sent'
        ORDER BY Ca.CartID
    """, (selected_id,))
    tickets = order_cursor.fetchall()

    fetch_cursor.close()
    order_cursor.close()
    connection.close()

    return render_template("dashboard.html",
                           restaurants=dining_places,
                           selected_restaurant_id=int(selected_id),
                           orders=tickets)


@restaurant_bp.route('/accept-order/<int:cart_id>', methods=['POST'])
def accept_order(cart_id):
    if 'user_id' not in session or session.get('usertype') != 'Manager':
        return redirect('/login')

    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("""
        UPDATE Cart 
        SET Status = 'Accepted',
            AcceptedAt = NOW()
        WHERE CartID = %s
    """, (cart_id,))
    conn.commit()
    cursor.close()
    conn.close()

    return redirect('/dashboard')


@restaurant_bp.route('/statistics', methods=['GET'])
def statistics():
    if 'user_id' not in session or session.get('usertype') != 'Manager':
        return redirect('/login')

    manager_id = session['user_id']
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True, buffered=True)

    cursor.execute("""
        SELECT R.RestaurantID, R.RestaurantName
        FROM Restaurant R
        JOIN Manages M ON R.RestaurantID = M.RestaurantID
        WHERE M.UserID = %s
    """, (manager_id,))
    restaurants = cursor.fetchall()

    selected_restaurant_id = request.args.get('restaurant_id')
    if not selected_restaurant_id and restaurants:
        selected_restaurant_id = restaurants[0]['RestaurantID']

    if not selected_restaurant_id:
        cursor.close()
        conn.close()
        return "No managed restaurant found."

    cursor.execute("""
        SELECT COUNT(DISTINCT C.CartID) AS OrderCount,
               SUM(M.Price * Co.Quantity) AS TotalRevenue
        FROM Cart C
        JOIN Contains Co ON C.CartID = Co.CartID
        JOIN MenuItem M ON Co.MenuItemID = M.MenuItemID
        JOIN Holds H ON H.CartID = C.CartID
        WHERE H.RestaurantID = %s
          AND C.Status IN ('accepted')
    """, (selected_restaurant_id,))
    total_stats = cursor.fetchone()

    cursor.execute("""
        SELECT M.Name AS MenuItem,
               SUM(Co.Quantity) AS TotalQuantity,
               SUM(M.Price * Co.Quantity) AS Revenue
        FROM Contains Co
        JOIN MenuItem M ON Co.MenuItemID = M.MenuItemID
        JOIN Cart C ON Co.CartID = C.CartID
        JOIN Holds H ON H.CartID = C.CartID
        WHERE H.RestaurantID = %s
          AND C.Status IN ('accepted')
        GROUP BY M.MenuItemID
    """, (selected_restaurant_id,))
    item_stats = cursor.fetchall()

    cursor.execute("""
        SELECT U.Username, COUNT(DISTINCT C.CartID) AS OrderCount
        FROM Cart C
        JOIN Belongs B ON B.CartID = C.CartID
        JOIN User U ON B.UserID = U.UserID
        JOIN Holds H ON H.CartID = C.CartID
        WHERE H.RestaurantID = %s
          AND C.Status IN ('accepted')
        GROUP BY U.UserID, U.Username
        ORDER BY OrderCount DESC
        LIMIT 1
    """, (selected_restaurant_id,))
    top_customer = cursor.fetchone()

    cursor.execute("""
        SELECT U.Username, C.CartID, SUM(M.Price * Co.Quantity) AS TotalValue
        FROM Cart C
        JOIN Belongs B ON B.CartID = C.CartID
        JOIN User U ON B.UserID = U.UserID
        JOIN Contains Co ON C.CartID = Co.CartID
        JOIN MenuItem M ON Co.MenuItemID = M.MenuItemID
        JOIN Holds H ON H.CartID = C.CartID
        WHERE H.RestaurantID = %s
          AND C.Status IN ( 'accepted')
        GROUP BY C.CartID, U.Username
        ORDER BY TotalValue DESC
        LIMIT 1
    """, (selected_restaurant_id,))
    top_cart = cursor.fetchone()

    cursor.close()
    conn.close()

    return render_template("statistics.html",
                           restaurants=restaurants,
                           selected_restaurant_id=int(selected_restaurant_id),
                           total_stats=total_stats,
                           item_stats=item_stats,
                           top_customer=top_customer,
                           top_cart=top_cart)





@restaurant_bp.route('/add-menu-item', methods=['GET', 'POST'])
def add_menu_item():
    if 'user_id' not in session or session.get('usertype') != 'Manager':
        return redirect('/login')

    manager_id = session['user_id']
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True, buffered=True)

    cursor.execute("""
        SELECT R.RestaurantID, R.RestaurantName
        FROM Restaurant R
        JOIN Manages M ON R.RestaurantID = M.RestaurantID
        WHERE M.UserID = %s
    """, (manager_id,))
    restaurants = cursor.fetchall()

    if request.method == 'POST':
        name = request.form['name']
        description = request.form['description']
        price = request.form['price']
        restaurant_id = request.form['restaurant_id']

        image = request.form['image']

        cursor.execute("""
            INSERT INTO MenuItem (Name, Description, Price, Image)
            VALUES (%s, %s, %s, %s)
        """, (name, description, price, image))

        menu_item_id = cursor.lastrowid

        cursor.execute("""
            INSERT INTO Has (RestaurantID, MenuItemID)
            VALUES (%s, %s)
        """, (restaurant_id, menu_item_id))

        conn.commit()
        flash("Menu item added successfully!", "success")
        return redirect('/dashboard')

    cursor.close()
    conn.close()

    return render_template("add_menu_item.html", restaurants=restaurants)

@restaurant_bp.route('/edit-menu-item/<int:item_id>', methods=['GET', 'POST'])
def edit_menu_item(item_id):
    if 'user_id' not in session or session.get('usertype') != 'Manager':
        return redirect('/login')

    manager_id = session['user_id']
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True, buffered=True)

    cursor.execute("""
        SELECT M.MenuItemID, M.Name, M.Description, M.Price, R.RestaurantID
        FROM MenuItem M
        JOIN Has H ON M.MenuItemID = H.MenuItemID
        JOIN Restaurant R ON H.RestaurantID = R.RestaurantID
        JOIN Manages Mg ON R.RestaurantID = Mg.RestaurantID
        WHERE Mg.UserID = %s AND M.MenuItemID = %s
    """, (manager_id, item_id))
    item = cursor.fetchone()

    if not item:
        cursor.close()
        conn.close()
        flash("Item not found.", "error")
        return redirect('/manage-menu')

    if request.method == 'POST':
        name = request.form['name']
        description = request.form['description']
        price = request.form['price']

        cursor.execute("""
            UPDATE MenuItem
            SET Name = %s, Description = %s, Price = %s
            WHERE MenuItemID = %s
        """, (name, description, price, item_id))

        conn.commit()
        cursor.close()
        conn.close()

        flash("Menu item updated successfully!", "success")
        return redirect(f"/manage-menu?restaurant_id={item['RestaurantID']}")

    cursor.close()
    conn.close()

    return render_template("edit_menu_item.html", item=item)

@restaurant_bp.route('/manage-menu', methods=['GET', 'POST'])
def manage_menu():
    if 'user_id' not in session or session.get('usertype') != 'Manager':
        return redirect('/login')

    manager_id = session['user_id']
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True, buffered=True)

    cursor.execute("""
        SELECT R.RestaurantID, R.RestaurantName
        FROM Restaurant R
        JOIN Manages M ON R.RestaurantID = M.RestaurantID
        WHERE M.UserID = %s
    """, (manager_id,))
    restaurants = cursor.fetchall()

    selected_restaurant_id = request.args.get('restaurant_id')
    if not selected_restaurant_id and restaurants:
        selected_restaurant_id = restaurants[0]['RestaurantID']

    if request.method == 'POST':
        menu_item_id = request.form.get('delete_item_id')
        if selected_restaurant_id and menu_item_id:
            cursor.execute("DELETE FROM Has WHERE RestaurantID = %s AND MenuItemID = %s", (selected_restaurant_id, menu_item_id))
            cursor.execute("SELECT COUNT(*) AS count FROM Has WHERE MenuItemID = %s", (menu_item_id,))
            remaining = cursor.fetchone()['count']
            if remaining == 0:
                cursor.execute("DELETE FROM MenuItem WHERE MenuItemID = %s", (menu_item_id,))
            conn.commit()
            flash("Item deleted.", "success")
            return redirect(f"/manage-menu?restaurant_id={selected_restaurant_id}")

    menu_items = []
    if selected_restaurant_id:
        cursor.execute("""
            SELECT M.MenuItemID, M.Name, M.Description, M.Price
            FROM MenuItem M
            JOIN Has H ON M.MenuItemID = H.MenuItemID
            WHERE H.RestaurantID = %s
        """, (selected_restaurant_id,))
        menu_items = cursor.fetchall()

    cursor.close()
    conn.close()

    return render_template("manage_menu.html",
                           restaurants=restaurants,
                           selected_restaurant_id=int(selected_restaurant_id),
                           menu_items=menu_items)

@restaurant_bp.route('/view-ratings')
def view_ratings():
    if 'user_id' not in session or session.get('usertype') != 'Manager':
        return redirect('/login')

    manager_id = session['user_id']
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True, buffered=True)

    cursor.execute("""
        SELECT R.RestaurantID, R.RestaurantName
        FROM Restaurant R
        JOIN Manages M ON R.RestaurantID = M.RestaurantID
        WHERE M.UserID = %s
    """, (manager_id,))
    restaurants = cursor.fetchall()
    selected_restaurant_id = request.args.get('restaurant_id')
    if not selected_restaurant_id and restaurants:
        selected_restaurant_id = restaurants[0]['RestaurantID']
    selected_restaurant = next((r for r in restaurants if str(r['RestaurantID']) == str(selected_restaurant_id)), None)

    if not restaurants:
        cursor.close()
        conn.close()
        return "Restaurant not found."

    cursor.execute("""
        SELECT U.Username, Ra.Rating, Ra.Comment, Ra.RatingDate
        FROM Ratings Ra
        JOIN WrittenBy WB ON Ra.RatingID = WB.RatingID
        JOIN User U ON WB.UserID = U.UserID
        JOIN ForRestaurant FR ON Ra.RatingID = FR.RatingID
        WHERE FR.RestaurantID = %s
        ORDER BY Ra.RatingDate DESC
    """, (selected_restaurant_id,))
    ratings = cursor.fetchall()

    avg_rating = None
    if len(ratings) >= 10:
        total = sum([r['Rating'] for r in ratings])
        avg_rating = round(total / len(ratings), 2)

    cursor.close()
    conn.close()

    return render_template("ratings.html",
                           restaurants=restaurants,
                           selected_restaurant_id=int(selected_restaurant_id),
                           ratings=ratings,
                           avg_rating=avg_rating,
                           restaurant=selected_restaurant)

@restaurant_bp.route('/manage-keywords', methods=['GET', 'POST'])
def manage_keywords():
    if 'user_id' not in session or session.get('usertype') != 'Manager':
        return redirect('/login')

    manager_id = session['user_id']
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True, buffered=True)

    cursor.execute("""
        SELECT R.RestaurantID, R.RestaurantName
        FROM Restaurant R
        JOIN Manages M ON R.RestaurantID = M.RestaurantID
        WHERE M.UserID = %s
    """, (manager_id,))
    restaurants = cursor.fetchall()
    selected_restaurant_id = request.args.get('restaurant_id')
    if not selected_restaurant_id and restaurants:
        selected_restaurant_id = restaurants[0]['RestaurantID']
    selected_restaurant = next((r for r in restaurants if str(r['RestaurantID']) == str(selected_restaurant_id)), None)

    if not restaurants:
        cursor.close()
        conn.close()
        flash("Restaurant not found.", "error")
        return redirect('/dashboard')

    rest_id = selected_restaurant_id

    if request.method == 'POST':
        new_keyword = request.form.get('keyword', '').strip().lower()
        if new_keyword:
            cursor.execute("SELECT KeywordID FROM Keyword WHERE Keyword = %s", (new_keyword,))
            row = cursor.fetchone()

            if row:
                keyword_id = row['KeywordID']
            else:
                cursor.execute("INSERT INTO Keyword (Keyword) VALUES (%s)", (new_keyword,))
                keyword_id = cursor.lastrowid

            cursor.execute("""
                SELECT 1 FROM AssociatedWith 
                WHERE RestaurantID = %s AND KeywordID = %s
            """, (rest_id, keyword_id))
            exists = cursor.fetchone()

            if not exists:
                cursor.execute("""
                    INSERT INTO AssociatedWith (RestaurantID, KeywordID)
                    VALUES (%s, %s)
                """, (rest_id, keyword_id))
                conn.commit()
                flash("Keyword added successfully.", "success")
            else:
                flash("Keyword already assigned.", "info")

    cursor.execute("""
        SELECT K.KeywordID, K.Keyword 
        FROM AssociatedWith A
        JOIN Keyword K ON A.KeywordID = K.KeywordID
        WHERE A.RestaurantID = %s
    """, (rest_id,))
    keywords = cursor.fetchall()

    cursor.close()
    conn.close()
    return render_template("manage_keywords.html",
                           restaurants=restaurants,
                           selected_restaurant_id=int(selected_restaurant_id),
                           keywords=keywords,
                           restaurant=selected_restaurant)

@restaurant_bp.route('/manage-discounts', methods=['GET', 'POST'])
def manage_discounts():
    if 'user_id' not in session or session.get('usertype') != 'Manager':
        return redirect('/login')

    manager_id = session['user_id']
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True, buffered=True)

    cursor.execute("""
        SELECT R.RestaurantID, R.RestaurantName
        FROM Restaurant R
        JOIN Manages M ON R.RestaurantID = M.RestaurantID
        WHERE M.UserID = %s
    """, (manager_id,))
    restaurants = cursor.fetchall()
    selected_restaurant_id = request.args.get('restaurant_id')
    if not selected_restaurant_id and restaurants:
        selected_restaurant_id = restaurants[0]['RestaurantID']
    selected_restaurant = next((r for r in restaurants if str(r['RestaurantID']) == str(selected_restaurant_id)), None)

    if request.method == 'POST':
        discount_val = float(request.form['discount'])
        item_id = int(request.form['menu_item_id'])
        start = request.form['start_date']
        end = request.form['end_date']

        from datetime import datetime
        start_dt = datetime.strptime(start, "%Y-%m-%dT%H:%M")
        end_dt = datetime.strptime(end, "%Y-%m-%dT%H:%M")

        if start_dt >= end_dt:
            flash("End date must be after start date.", "error")
            return redirect(f"/manage-discounts?restaurant_id={selected_restaurant_id}")

        cursor.execute("""
            SELECT 1 FROM Applied
            WHERE MenuItemID = %s
              AND NOT (EndDate <= %s OR StartDate >= %s)
        """, (item_id, start, end))
        overlap = cursor.fetchone()

        if overlap:
            flash("This item already has a discount that overlaps with the selected time period.", "error")
            return redirect(f"/manage-discounts?restaurant_id={selected_restaurant_id}")

        cursor.execute("INSERT INTO Discount (Discount) VALUES (%s)", (discount_val,))
        discount_id = cursor.lastrowid

        cursor.execute("""
            INSERT INTO Applied (DiscountID, MenuItemID, StartDate, EndDate)
            VALUES (%s, %s, %s, %s)
        """, (discount_id, item_id, start, end))
        conn.commit()
        flash("Discount is applied successfully.", "success")
        return redirect(f"/manage-discounts?restaurant_id={selected_restaurant_id}")

    cursor.execute("""
        SELECT M.MenuItemID, M.Name, M.Description, M.Price AS BasePrice,
               D.Discount AS DiscountRate, A.StartDate, A.EndDate,
               CASE
                   WHEN NOW() BETWEEN A.StartDate AND A.EndDate THEN
                       ROUND(M.Price * (1 - D.Discount / 100), 2)
                   ELSE NULL
               END AS DiscountedPrice,
               CASE
                   WHEN NOW() BETWEEN A.StartDate AND A.EndDate THEN D.Discount
                   ELSE NULL
               END AS ActiveDiscount
        FROM MenuItem M
        JOIN Has H ON M.MenuItemID = H.MenuItemID
        LEFT JOIN Applied A ON A.MenuItemID = M.MenuItemID
        LEFT JOIN Discount D ON A.DiscountID = D.DiscountID
        WHERE H.RestaurantID = %s
        ORDER BY M.MenuItemID, A.StartDate DESC
    """, (selected_restaurant_id,))
    discounted_items = cursor.fetchall()

    cursor.execute("""
        SELECT DISTINCT M.MenuItemID, M.Name
        FROM MenuItem M
        JOIN Has H ON M.MenuItemID = H.MenuItemID
        WHERE H.RestaurantID = %s
    """, (selected_restaurant_id,))
    menu_items = cursor.fetchall()

    cursor.close()
    conn.close()

    return render_template("manage_discounts.html",
                           restaurants=restaurants,
                           selected_restaurant_id=int(selected_restaurant_id),
                           discounted_items=discounted_items,
                           menu_items=menu_items)