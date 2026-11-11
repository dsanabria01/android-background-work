#!/usr/bin/env python3
"""Tiny local server so both demo apps can run without any external backend.

  GET /dites/        -> JSON list consumed by workmanager-notifications
  GET /audio/<file>  -> static files from tools/media/audio (foreground-service-audio)

Usage:  python3 tools/mock_server.py   (listens on 0.0.0.0:8080)
The Android emulator reaches the host machine at http://10.0.2.2:8080/
"""
import json
import os
from http.server import HTTPServer, SimpleHTTPRequestHandler

MEDIA_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "media")

DITES = [
    {
        "id": 1,
        "dita": "A bon entenedor, poques paraules basten",
        "explicacio": "Qui entén bé les coses no necessita explicacions llargues.",
    }
]


class Handler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=MEDIA_DIR, **kwargs)

    def do_GET(self):
        if self.path.rstrip("/") == "/dites":
            body = json.dumps(DITES).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)
        else:
            super().do_GET()


if __name__ == "__main__":
    print("Mock server on http://0.0.0.0:8080  (Ctrl+C to stop)")
    HTTPServer(("0.0.0.0", 8080), Handler).serve_forever()
