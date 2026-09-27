const POLL_INTERVAL_MS = 3000;

async function checkForAlarm() {
  const { latest } = await (await fetch("/api/latest")).json();
  if(latest.label === "falling" && latest.label === "fallen"){
    window.location.href = "/alarm";
  }
}

async function updateConnectionStatus() {
  const statusEl = document.getElementById("connection-status");
  if (!statusEl) return;

  const { connected } = await (await fetch("/api/get_connect_status")).json();
  statusEl.textContent = connected ? "Połączono" : "Niepołączono";
  statusEl.classList.toggle("connected", connected);
  statusEl.classList.toggle("not-connected", !connected);
}

setInterval(checkForAlarm, POLL_INTERVAL_MS);
setInterval(updateConnectionStatus, POLL_INTERVAL_MS);
updateConnectionStatus();


document.querySelector(".test-alarm")?.addEventListener("click", () => {
  console.log("test")
  window.location.href = "/alarm";
});
