// RoboFriend — wariant "warm-hybrid"

document.addEventListener('DOMContentLoaded', () => {
  // FAQ accordion
  document.querySelectorAll('.faq-item').forEach((item) => {
    const trigger = item.querySelector('.faq-trigger');
    trigger.addEventListener('click', () => {
      const isOpen = item.classList.contains('open');

      document.querySelectorAll('.faq-item.open').forEach((openItem) => {
        if (openItem !== item) {
          openItem.classList.remove('open');
          openItem.querySelector('.faq-trigger').setAttribute('aria-expanded', 'false');
        }
      });

      item.classList.toggle('open', !isOpen);
      trigger.setAttribute('aria-expanded', String(!isOpen));
    });
  });

  // Tab navigation — buttons swap the visible content panel, no page reload
  const tabButtons = document.querySelectorAll('.tab-btn');
  const tabPanels = document.querySelectorAll('.tab-panel');
  const panelIds = Array.from(tabPanels).map((panel) => panel.id);

  const tabBarNav = document.getElementById('tab-bar-nav');

  const activateTab = (id) => {
    if (!panelIds.includes(id)) return;
    tabPanels.forEach((panel) => {
      panel.hidden = panel.id !== id;
    });
    tabButtons.forEach((btn) => {
      const isActive = btn.getAttribute('href') === '#' + id;
      btn.classList.toggle('active', isActive);
      btn.setAttribute('aria-selected', String(isActive));
    });
    if (tabBarNav) {
      tabBarNav.hidden = id === 'glowna';
    }
  };

  document.querySelectorAll('a[href^="#"]').forEach((link) => {
    const id = link.getAttribute('href').slice(1);
    if (!panelIds.includes(id)) return;
    link.addEventListener('click', () => activateTab(id));
  });

  window.addEventListener('hashchange', () => {
    activateTab(location.hash.slice(1));
  });

  const initialId = location.hash.slice(1);
  activateTab(panelIds.includes(initialId) ? initialId : panelIds[0]);

  // Kontakt form — opens the visitor's mail client with a pre-filled message
  const kontaktForm = document.getElementById('kontakt-form');
  if (kontaktForm) {
    kontaktForm.addEventListener('submit', (e) => {
      e.preventDefault();
      const name = document.getElementById('kontakt-name').value.trim();
      const email = document.getElementById('kontakt-email').value.trim();
      const message = document.getElementById('kontakt-message').value.trim();

      const subject = encodeURIComponent(
        name ? `Wiadomość ze strony RoboFriend od ${name}` : 'Wiadomość ze strony RoboFriend'
      );
      const bodyLines = [];
      if (name) bodyLines.push(`Imię i nazwisko: ${name}`);
      if (email) bodyLines.push(`E-mail: ${email}`);
      if (name || email) bodyLines.push('');
      bodyLines.push(message);
      const body = encodeURIComponent(bodyLines.join('\n'));

      window.location.href = `mailto:krystian.skrzypalik@robofriendstech.pl?subject=${subject}&body=${body}`;
    });
  }
});
