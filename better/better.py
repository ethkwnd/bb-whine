import torch
from flask import Flask, jsonify, request
from model import AudioClassifier, AudioUtil

app = Flask(__name__)
model = AudioClassifier()
model.load_state_dict(torch.load("weights.pt"))
model.eval()


def transform_audio(file):
    aud = AudioUtil.open(file)
    aud = AudioUtil.pad_trunc(aud, 6960)
    aud = AudioUtil.resample(aud, 8000)
    aud = AudioUtil.rechannel(aud, 1)
    spec = AudioUtil.spectro_gram(aud, n_mels=64, n_fft=1024, hop_len=None)
    return spec


def get_prediction(file):
    spec = transform_audio(file)
    outputs = model(spec.unsqueeze(0))
    _, y_hat = outputs.max(1)
    return ["belly pain", "burping", "discomfort", "hungry", "tired"][y_hat.item()]


@app.route('/upload', methods=['POST'])
def predict():
    if request.method == 'POST':
        file = request.files['file']
        class_name = get_prediction(file)
        return jsonify({'class_name': class_name})


if __name__ == '__main__':
    app.run()
