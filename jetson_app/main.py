import base64
import io
import secrets
import socket
from datetime import datetime, timezone

import qrcode
from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import HTMLResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates

app = FastAPI()

app.mount("/static", StaticFiles(directory="static"), name="static")
templates = Jinja2Templates(directory="templates")

ROBOT_NAME = "RoboFriend"

connected = False

# Random per-run secret. Only requests carrying this token (i.e. whoever scanned
# the QR code) are let in from outside this machine, so opening the firewall to
# the LAN doesn't hand control of the robot to anyone else on the network.
ACCESS_TOKEN = secrets.token_urlsafe(16)


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
def home(request: Request, token: str | None = None):
    global connected

    # request.client is the real TCP peer address, unlike the Host header
    # (request.url.hostname), which the client controls and can't be trusted.
    is_local = request.client is not None and request.client.host in ("127.0.0.1", "::1")

    if not is_local:
        if not token or not secrets.compare_digest(token, ACCESS_TOKEN):
            raise HTTPException(status_code=403, detail="Forbidden")
        connected = True
        return templates.TemplateResponse(request=request, name="welcome.html")

    site_url = (
        f"{request.url.scheme}://{get_lan_ip()}:{request.url.port}/"
        f"?token={ACCESS_TOKEN}"
    )

    qr_image = qrcode.make(site_url)
    buffer = io.BytesIO()
    qr_image.save(buffer, format="PNG")
    qr_code_base64 = base64.b64encode(buffer.getvalue()).decode()

    return templates.TemplateResponse(
        request=request,
        name="base.html",
        context={
            "site_url": site_url,
            "qr_code_base64": qr_code_base64,
            "connected": connected,
        },
    )


@app.get("/api/connect")
def api_connect(request: Request, token: str | None = None):
    """Hit by the mobile app right after it scans the QR code. Confirms the
    token, marks the robot as connected, and hands back the info the app
    shows on its "connected" screen."""
    global connected

    if not token or not secrets.compare_digest(token, ACCESS_TOKEN):
        raise HTTPException(status_code=403, detail="Forbidden")

    connected = True

    return {
        "status": "connected",
        "robot_name": ROBOT_NAME,
        "jetson_ip": get_lan_ip(),
        "connected_at": datetime.now(timezone.utc).isoformat(),
        "client_ip": request.client.host if request.client else None,
    }


if __name__ == "__main__":
    import uvicorn

    # Bind explicitly to 0.0.0.0: uvicorn's own default is 127.0.0.1
    # (localhost-only), which would make the site unreachable from a phone
    # no matter what the firewall allows.
    uvicorn.run(app, host="0.0.0.0", port=8000)
