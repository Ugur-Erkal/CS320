from app.routes.auth import auth_bp
from app.routes.customer import customer_bp
from app.routes.restaurant import restaurant_bp
from app.routes.home import home_bp


def register_blueprints(app):
    app.register_blueprint(auth_bp)
    app.register_blueprint(customer_bp)
    app.register_blueprint(restaurant_bp)
    app.register_blueprint(home_bp)