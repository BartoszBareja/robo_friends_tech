import base64
import hashlib
import io
import os
import socket
from datetime import datetime, timezone

import qrcode
from fastapi import FastAPI, Request
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from pydantic import BaseModel

app = FastAPI()

app.mount("/static", StaticFiles(directory="static"), name="static")
templates = Jinja2Templates(directory="templates")


def static_version(path: str) -> str:
    """Short content hash of a file under static/, used as a cache-busting query
    param. Based on content rather than mtime, since Docker's build cache and
    COPY can preserve or reset file timestamps in ways that don't reliably
    track an actual content change."""
    with open(os.path.join("static", path), "rb") as f:
        return hashlib.sha1(f.read()).hexdigest()[:10]


templates.env.globals["static_version"] = static_version

ROBOT_NAME = "RoboFriend"

latest: dict | None = None

connected = False


class YoloPrediction(BaseModel):
    cls: int | None = None
    label: str | None = None


def get_lan_ip() -> str:
    """Best-effort LAN IP of this machine, so the QR code never points at localhost."""
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        sock.connect(("8.8.8.8", 80))
        return sock.getsockname()[0]
    except OSError:
        return socket.gethostbyname(socket.gethostname())
    finally:
        sock.close()


@app.get("/")
def home(request: Request):
    global connected

    # request.client is the real TCP peer address, unlike the Host header
    # (request.url.hostname), which the client controls and can't be trusted.
    is_local = request.client is not None and request.client.host in ("127.0.0.1", "::1")

    site_url = f"{request.url.scheme}://{get_lan_ip()}:{request.url.port}/"

    qr_image = qrcode.make(site_url)
    buffer = io.BytesIO()
    qr_image.save(buffer, format="PNG")
    qr_code_base64 = base64.b64encode(buffer.getvalue()).decode()

    return templates.TemplateResponse(
        request=request,
        name="kiosk.html",
        context={
            "site_url": site_url,
            "qr_code_base64": qr_code_base64,
            "connected": connected,
        },
    )


@app.get("/alarm")
def alarm(request: Request):
    return templates.TemplateResponse(
        request=request,
        name="emergency.html",
    )


@app.get("/api/connect")
def api_connect(request: Request):
    """Hit by the mobile app right after it scans the QR code. Marks the
    robot as connected and hands back the info the app shows on its
    "connected" screen."""
    global connected

    connected = True

    return {
        "status": "connected",
        "robot_name": ROBOT_NAME,
        "jetson_ip": get_lan_ip(),
        "connected_at": datetime.now(timezone.utc).isoformat(),
        "client_ip": request.client.host if request.client else None,
    }

@app.get("/api/get_connect_status")
def api_get_connect_status():
    """Polled by the kiosk page to refresh the connection indicator without a
    full page reload. Read-only: unlike /api/connect, it must not flip
    `connected` itself, or the status would go "connected" the instant the
    kiosk starts polling."""
    return {"connected": connected}


@app.post("/api/yolo")
def api_yolo(prediction: YoloPrediction):
    """Hit by yolo_model every 15 seconds with its latest detection result."""
    global latest

    latest = {
        "cls": prediction.cls,
        "label": prediction.label,
        "received_at": datetime.now(timezone.utc).isoformat(),
    }
    return {"status": "received"}


@app.get("/api/latest")
def api_latest():
    """Returns the most recent yolo_model prediction, or null if none has arrived yet."""
    return {"latest": latest}



if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
