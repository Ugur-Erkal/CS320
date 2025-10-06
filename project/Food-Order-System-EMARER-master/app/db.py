import mysql.connector
from mysql.connector import Error
from app.config import DB_CONFIG

def get_db_connection():
    try:
        connection = mysql.connector.connect(
            host=DB_CONFIG['host'],
            user=DB_CONFIG['user'],
            password=DB_CONFIG['password'],
            database=DB_CONFIG['database']
        )
        return connection
    except Error as e:
        print("MySQL Connection Error:", e)
        return None