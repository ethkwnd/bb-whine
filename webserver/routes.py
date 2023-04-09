import os
from flask import Flask, request, Blueprint, jsonify
from werkzeug.utils import secure_filename
from brain import brain


UPLOAD_FOLDER = './upload_folder'
ALLOWED_EXTENSIONS = {'wav'}

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

routes = Blueprint('routes', __name__)


def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@routes.route("/upload", methods=['POST'])
def upload():
    #if 'sussy' not in request.form:
    #    return jsonify({"response": "not valid"})
    #if request.form['sussy'] != 'amongus':
    #    return jsonify({"response": "not valid"})
    if 'file' not in request.files:
        return jsonify({"response": "no file"})
    file = request.files['file']
    if file and allowed_file(file.filename):
        filename = secure_filename(file.filename)
        file_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(file_path)
        error_code = brain(file_path)
        return jsonify({"response": error_code})
    return jsonify({"response": "not valid"})
