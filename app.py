from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer
import os

app = Flask(__name__)

# נשתמש במודל קומפקטי ויעיל
model = SentenceTransformer("all-MiniLM-L6-v2")

@app.route('/embed', methods=['POST'])
def embed():
    data = request.get_json()
    text = data.get('text', '')

    if not text:
        return jsonify({'error': 'Missing text'}), 400

    vector = model.encode(text).tolist()
    return jsonify({'embedding': vector})

if __name__ == '__main__':
    # ⚠️ שימוש בפורט דינמי שניתן על ידי Render
    port = int(os.environ.get('PORT', 10000))
    app.run(host='0.0.0.0', port=port)
