from flask import Flask, request
import json
import os
app = Flask(__name__)
@app.route('/getNotes')
def getNotes():
    try:
        with open(request.get_json()["username"], 'r') as f: return f.read()
    except: return ""
@app.route('/saveNotes', methods = ['POST'])
def saveNotes():
    if os.path.isfile(request.get_json()["username"]):  os.remove(request.get_json()["username"])
    with open(request.get_json()["username"], 'x') as f: f.write(json.dumps(request.get_json()["object"]))
    return "YO"