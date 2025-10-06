from flask import Blueprint, redirect, session

home_bp = Blueprint('home', __name__)

@home_bp.route('/')
def index():
    session.clear()
    return redirect('/login')