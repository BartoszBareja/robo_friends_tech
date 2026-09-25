setInterval(async () => {
  const p = await (await fetch("/api/latest")).json();
  console.log(p)
}, 3000);