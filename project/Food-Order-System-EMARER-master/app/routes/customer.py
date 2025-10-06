from flask import Blueprint, render_template, request, redirect, session, flash
from app.db import get_db_connection

customer_bp = Blueprint('customer', __name__)

@customer_bp.route('/search', methods=['GET'])
def search():
    if 'user_id' not in session:
        return redirect('/login')

    query = request.args.get('q', '').strip()
    results = []

    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True, buffered=True)

    cursor.execute("""
        SELECT UA.City
        FROM Lives L
        JOIN UserAddress UA ON L.AddressID = UA.AddressID
        WHERE L.UserID = %s
    """, (session['user_id'],))
    city_row = cursor.fetchone()

    if not city_row:
        flash("Could not determine your registered city.", "error")
        cursor.close()
        conn.close()
        return redirect("/")

    user_city = city_row['City']

    if query:
        search_term = f"%{query}%"
        cursor.execute("""
            SELECT 
                R.RestaurantID, R.RestaurantName, R.CuisineType, R.Address, R.City,
                M.MenuItemID, M.Name AS MenuItem, M.Description, M.Price, M.Image,
                COUNT(DISTINCT Ra.RatingID) AS RatingCount,
                AVG(Ra.Rating) AS AvgRating,
                (
                    SELECT D.Discount
                    FROM Applied A2
                    JOIN Discount D ON A2.DiscountID = D.DiscountID
                    WHERE A2.MenuItemID = M.MenuItemID AND NOW() BETWEEN A2.StartDate AND A2.EndDate
                    ORDER BY A2.StartDate DESC
                    LIMIT 1
                ) AS ActiveDiscount
            FROM Restaurant R
            JOIN Has H ON R.RestaurantID = H.RestaurantID
            JOIN MenuItem M ON H.MenuItemID = M.MenuItemID
            LEFT JOIN ForRestaurant FR ON FR.RestaurantID = R.RestaurantID
            LEFT JOIN Ratings Ra ON FR.RatingID = Ra.RatingID
            LEFT JOIN AssociatedWith AW ON R.RestaurantID = AW.RestaurantID
            LEFT JOIN Keyword K ON AW.KeywordID = K.KeywordID
            WHERE (
                R.RestaurantName LIKE %s OR
                M.Name LIKE %s OR
                M.Description LIKE %s OR
                K.Keyword LIKE %s)
              AND R.City = %s
            GROUP BY R.RestaurantID, M.MenuItemID
            ORDER BY (AvgRating IS NULL), AvgRating DESC
        """, (search_term, search_term, search_term, search_term, user_city))

        raw_results = cursor.fetchall()

        for row in raw_results:
            if row['ActiveDiscount'] is not None:
                discount = float(row['ActiveDiscount'])
                original_price = float(row['Price'])
                discounted_price = round(original_price * (1 - discount / 100), 2)
                row['DiscountedPrice'] = discounted_price
            else:
                row['DiscountedPrice'] = None

        results = raw_results

    cursor.close()
    conn.close()

    return render_template("search.html", query=query, results=results)

@customer_bp.route('/add-to-cart', methods=['POST'])
def add_to_cart():
    if 'user_id' not in session:
        return redirect('/login')

    user_id = session['user_id']
    menu_item_id = int(request.form['menu_item_id'])
    quantity = int(request.form.get('quantity', 1))

    conn = get_db_connection()

    try:
        cursor_rest = conn.cursor()
        cursor_rest.execute("""
            SELECT RestaurantID FROM Has 
            WHERE MenuItemID = %s
        """, (menu_item_id,))
        row = cursor_rest.fetchone()
        cursor_rest.close()

        if not row:
            conn.close()
            flash("menu item is invalid.", "error")
            return redirect("/search")

        selected_restaurant_id = row[0]

        cursor1 = conn.cursor()
        cursor1.execute("""
            SELECT C.CartID FROM Cart C
            JOIN Belongs B ON C.CartID = B.CartID
            WHERE B.UserID = %s AND C.Status = 'preparing'
        """, (user_id,))
        cart_row = cursor1.fetchone()
        cursor1.close()

        if cart_row:
            cart_id = cart_row[0]

            cursor2 = conn.cursor()
            cursor2.execute("""
                SELECT DISTINCT H.RestaurantID FROM Contains C
                JOIN Has H ON C.MenuItemID = H.MenuItemID
                WHERE C.CartID = %s
            """, (cart_id,))
            existing_rest_row = cursor2.fetchone()
            cursor2.close()

            if existing_rest_row and existing_rest_row[0] != selected_restaurant_id:
                cursor3 = conn.cursor()
                cursor3.execute("DELETE FROM Contains WHERE CartID = %s", (cart_id,))
                cursor3.execute("DELETE FROM Belongs WHERE CartID = %s", (cart_id,))
                cursor3.execute("DELETE FROM Holds WHERE CartID = %s", (cart_id,))
                cursor3.execute("DELETE FROM Cart WHERE CartID = %s", (cart_id,))
                conn.commit()
                cursor3.close()
                cart_id = None

        if not cart_row or (existing_rest_row and existing_rest_row[0] != selected_restaurant_id):
            cursor4 = conn.cursor()
            cursor4.execute("INSERT INTO Cart (Status) VALUES ('preparing')")
            cart_id = cursor4.lastrowid
            cursor4.execute("INSERT INTO Belongs (UserID, CartID) VALUES (%s, %s)", (user_id, cart_id))
            cursor4.close()

        cursor5 = conn.cursor()
        cursor5.execute("""
            SELECT Quantity FROM Contains 
            WHERE CartID = %s AND MenuItemID = %s
        """, (cart_id, menu_item_id))
        existing = cursor5.fetchone()

        if existing:
            cursor5.execute("""
                UPDATE Contains SET Quantity = Quantity + %s
                WHERE CartID = %s AND MenuItemID = %s
            """, (quantity, cart_id, menu_item_id))
        else:
            cursor5.execute("""
                INSERT INTO Contains (CartID, MenuItemID, Quantity)
                VALUES (%s, %s, %s)
            """, (cart_id, menu_item_id, quantity))

        conn.commit()
        cursor5.close()
        conn.close()

        flash("Item added to cart.", "success")
        return redirect("/search")

    except Exception as e:
        conn.rollback()
        conn.close()
        return "Error: " + str(e), 500


@customer_bp.route('/cart')
def cart():
    if 'user_id' not in session:
        return redirect('/login')

    user_id = session['user_id']
    conn = get_db_connection()

    try:
        cursor1 = conn.cursor(dictionary=True)
        cursor1.execute("""
            SELECT Cart.CartID
            FROM Cart
            JOIN Belongs ON Cart.CartID = Belongs.CartID
            WHERE Belongs.UserID = %s AND Cart.Status = 'preparing'
        """, (user_id,))
        cart_row = cursor1.fetchone()
        cursor1.close()

        if not cart_row:
            conn.close()
            return render_template("cart.html", items=[], total=0)

        cart_id = cart_row['CartID']

        cursor2 = conn.cursor(dictionary=True)
        cursor2.execute("""
            SELECT M.MenuItemID, M.Name, M.Description, M.Price, M.Image, C.Quantity, R.RestaurantName,
                (
                    SELECT D.Discount
                    FROM Applied A
                    JOIN Discount D ON A.DiscountID = D.DiscountID
                    WHERE A.MenuItemID = M.MenuItemID AND NOW() BETWEEN A.StartDate AND A.EndDate
                    ORDER BY A.StartDate DESC
                    LIMIT 1
                ) AS ActiveDiscount
            FROM Contains C
            JOIN MenuItem M ON C.MenuItemID = M.MenuItemID
            JOIN Has H ON M.MenuItemID = H.MenuItemID
            JOIN Restaurant R ON H.RestaurantID = R.RestaurantID
            WHERE C.CartID = %s
        """, (cart_id,))
        raw_items = cursor2.fetchall()
        cursor2.close()
        conn.close()

        items = []
        total = 0
        for item in raw_items:
            item = dict(item)
            if item['ActiveDiscount'] is not None:
                discount = float(item['ActiveDiscount'])
                original_price = float(item['Price'])
                discounted_price = round(original_price * (1 - discount / 100), 2)
                item['OriginalPrice'] = original_price
                item['Price'] = discounted_price
            else:
                item['OriginalPrice'] = None
            total += float(item['Price']) * item['Quantity']
            items.append(item)

        restaurant_name = raw_items[0]['RestaurantName'] if raw_items else None
        return render_template("cart.html", items=items, total=round(total, 2), restaurant_name=restaurant_name)

    except Exception as e:
        print("EROR:", e)
        conn.close()
        return "Internal Server Error: " + str(e), 500

    except Exception as e:
        try:
            cursor.fetchall()
        except:
            pass
        try:
            cursor.close()
            conn.close()
        except:
            pass
        print("ERROR in /cart:", str(e))
        return "Internal error: " + str(e), 500


@customer_bp.route('/place-order', methods=['POST'])
def place_order():
    user_id = session['user_id']

    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("""
        SELECT Cart.CartID
        FROM Cart
        JOIN Belongs ON Cart.CartID = Belongs.CartID
        WHERE Belongs.UserID = %s AND Cart.Status = 'preparing'
    """, (user_id,))
    row = cursor.fetchone()

    if not row:
        flash("No active cart found.", "error")
        return redirect("/cart")

    cart_id = row[0]

    cursor.execute("""
        SELECT DISTINCT H.RestaurantID
        FROM Contains C
        JOIN Has H ON C.MenuItemID = H.MenuItemID
        WHERE C.CartID = %s
    """, (cart_id,))
    rest_row = cursor.fetchone()

    if rest_row:
        restaurant_id = rest_row[0]
        cursor.execute("""
            INSERT INTO Holds (RestaurantID, CartID)
            VALUES (%s, %s)
        """, (restaurant_id, cart_id))

    cursor.execute("UPDATE Cart SET Status = 'Sent' WHERE CartID = %s", (cart_id,))

    conn.commit()
    cursor.close()
    conn.close()

    flash("Order placed successfully.", "success")
    return redirect("/search")

@customer_bp.route('/remove-from-cart', methods=['POST'])
def remove_from_cart():
    if 'user_id' not in session:
        return redirect('/login')

    user_id = session['user_id']
    menu_item_id = int(request.form['menu_item_id'])

    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("""
        SELECT C.CartID FROM Cart C
        JOIN Belongs B ON C.CartID = B.CartID
        WHERE B.UserID = %s AND C.Status = 'preparing'
    """, (user_id,))
    row = cursor.fetchone()

    if not row:
        conn.close()
        flash("No active cart.", "error")
        return redirect("/cart")

    cart_id = row[0]

    cursor.execute("""
        SELECT Quantity FROM Contains 
        WHERE CartID = %s AND MenuItemID = %s
    """, (cart_id, menu_item_id))
    item = cursor.fetchone()

    if not item:
        cursor.close()
        conn.close()
        flash("Item not found in cart.", "error")
        return redirect("/cart")

    qty = item[0]

    if qty > 1:
        cursor.execute("""
            UPDATE Contains 
            SET Quantity = Quantity - 1 
            WHERE CartID = %s AND MenuItemID = %s
        """, (cart_id, menu_item_id))
    else:
        cursor.execute("""
            DELETE FROM Contains 
            WHERE CartID = %s AND MenuItemID = %s
        """, (cart_id, menu_item_id))

    conn.commit()
    cursor.close()
    conn.close()

    return redirect("/cart")

@customer_bp.route('/my-orders')
def my_orders():
    if 'user_id' not in session:
        return redirect('/login')

    user_id = session['user_id']
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    cursor.execute("""
        SELECT C.CartID, R.RestaurantName, C.Status, 
               TIMESTAMPDIFF(HOUR, C.AcceptedAt, NOW()) AS HoursSinceAccepted,
               EXISTS (
                SELECT 1 FROM WrittenBy WB 
                JOIN Ratings RT ON WB.RatingID = RT.RatingID
                WHERE WB.UserID = %s AND RT.CartID = C.CartID) AS AlreadyRated
        FROM Cart C
        JOIN Holds H ON C.CartID = H.CartID
        JOIN Restaurant R ON H.RestaurantID = R.RestaurantID
        JOIN Belongs B ON C.CartID = B.CartID
        WHERE B.UserID = %s AND C.Status = 'Accepted'
        ORDER BY C.AcceptedAt DESC
    """, (user_id, user_id))

    orders = cursor.fetchall()
    cursor.close()
    conn.close()

    return render_template("my_orders.html", orders=orders)

@customer_bp.route('/rate-order/<int:cart_id>', methods=['GET', 'POST'])
def rate_order(cart_id):
    if 'user_id' not in session:
        return redirect('/login')

    user_id = session['user_id']
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    cursor.execute("""
        SELECT C.CartID, R.RestaurantID, R.RestaurantName, C.AcceptedAt,
               TIMESTAMPDIFF(HOUR, C.AcceptedAt, NOW()) AS HoursPassed
        FROM Cart C
        JOIN Holds H ON C.CartID = H.CartID
        JOIN Restaurant R ON H.RestaurantID = R.RestaurantID
        JOIN Belongs B ON C.CartID = B.CartID
        WHERE C.CartID = %s AND B.UserID = %s AND C.Status = 'Accepted'
    """, (cart_id, user_id))
    order = cursor.fetchone()

    if not order:
        conn.close()
        flash("You can't rate this order.", "error")
        return redirect('/my-orders')

    if order['HoursPassed'] > 24:
        conn.close()
        flash("Rating time is expired.", "error")
        return redirect('/my-orders')

    if request.method == 'POST':
        rating = int(request.form['rating'])
        comment = request.form['comment'].strip()

        cursor.execute("""
            INSERT INTO Ratings (Rating, Comment, RatingDate, CartID)
            VALUES (%s, %s, NOW(), %s)
        """, (rating, comment, cart_id))
        rating_id = cursor.lastrowid

        cursor.execute("INSERT INTO WrittenBy (RatingID, UserID) VALUES (%s, %s)", (rating_id, user_id))
        cursor.execute("INSERT INTO ForRestaurant (RatingID, RestaurantID) VALUES (%s, %s)",
                       (rating_id, order['RestaurantID']))

        conn.commit()
        cursor.close()
        conn.close()

        flash("Thanks for your feedback!", "success")
        return redirect('/my-orders')

    cursor.close()
    conn.close()
    return render_template("rate_order.html", order=order)
