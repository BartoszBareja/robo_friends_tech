// RoboFriend — wariant "warm"

document.addEventListener('DOMContentLoaded', () => {
  // Mobile nav toggle
  const navToggle = document.getElementById('nav-toggle');
  const navMenuMobile = document.getElementById('nav-menu-mobile');

  if (navToggle && navMenuMobile) {
    navToggle.addEventListener('click', () => {
      const isOpen = navMenuMobile.classList.toggle('flex');
      navMenuMobile.classList.toggle('hidden', !isOpen);
      navToggle.setAttribute('aria-expanded', String(isOpen));
    });

    navMenuMobile.querySelectorAll('a').forEach((link) => {
      link.addEventListener('click', () => {
        navMenuMobile.classList.add('hidden');
        navMenuMobile.classList.remove('flex');
        navToggle.setAttribute('aria-expanded', 'false');
      });
    });
  }

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
});
