// RoboFriend — wariant "medical"
// Nawigacja mobilna, akordeon FAQ i karuzela obsługiwane są natywnie przez Bootstrap 5 (data-bs-* atrybuty).

document.addEventListener('DOMContentLoaded', () => {
  const navMenu = document.getElementById('navMenu');
  const collapseInstance = navMenu ? bootstrap.Collapse.getOrCreateInstance(navMenu, { toggle: false }) : null;

  if (navMenu && collapseInstance) {
    navMenu.querySelectorAll('a.nav-link, a.btn').forEach((link) => {
      link.addEventListener('click', () => {
        if (navMenu.classList.contains('show')) {
          collapseInstance.hide();
        }
      });
    });
  }
});
