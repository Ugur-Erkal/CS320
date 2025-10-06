from app import create_app
from app.db import get_db_connection

app = create_app()

connection = get_db_connection()

if __name__ == '__main__':
    app.run(debug=True)
