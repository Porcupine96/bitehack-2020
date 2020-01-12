from flask import Flask, jsonify, request, redirect, Response
from flask_cors import CORS

app = Flask(__name__)
CORS(app)


@app.route("/link", methods=["GET"])
def link():
    resp = jsonify(
        {
            "link": "https://accounts.spotify.com/authorize?client_id=596cdb1559184f948fd26d2ad07d7941&response_type=token&redirect_uri=http://localhost:1234/login&scope=user-read-private%20user-top-read%20user-library-read"
        }
    )

    return resp


@app.route("/login", methods=["POST"])
def login():
    r = request.json["spotify_token"]
    print(r)
    resp = jsonify(
        {
            "token": "ala lubi lody"
        }
    )
    return resp


@app.route("/me", methods=["GET"])
def me():
    if "Authentication" not in request.headers.keys():
        return Response(status=401)

    token = request.headers["Authentication"].replace("Bearer ", "")
    print(token)

    resp = jsonify(
        {
            "displayName": "displayName",
            "country": "countrystring",
            "spotifyUrl": "spotifyUrl",
            "userId": "userIdString",
            "image": "imageString", #Option
        }
    )

    return resp


if __name__ == "__main__":
    app.run(debug=True, port=12345)
