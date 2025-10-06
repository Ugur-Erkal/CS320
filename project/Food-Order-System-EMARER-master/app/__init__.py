from flask import Flask
from app.routes.initializeRoutes import register_blueprints

def create_app():
    app = Flask(__name__)
    app.secret_key = "emarerfoodordersystem"

    register_blueprints(app)

    return app