// RoboFriend — wariant "cutting-edge"

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

  // Animated stat counters
  const counters = document.querySelectorAll('.counter');
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  const animateCounter = (el) => {
    const target = parseFloat(el.dataset.target);
    const suffix = el.dataset.suffix || '';
    const isInteger = Number.isInteger(target);

    if (reduceMotion) {
      el.textContent = target.toFixed(isInteger ? 0 : 1) + suffix;
      return;
    }

    const duration = 1200;
    const start = performance.now();

    const step = (now) => {
      const progress = Math.min((now - start) / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      const value = target * eased;
      el.textContent = value.toFixed(isInteger ? 0 : 1) + suffix;
      if (progress < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
  };

  if (counters.length) {
    const observer = new IntersectionObserver((entries, obs) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          animateCounter(entry.target);
          obs.unobserve(entry.target);
        }
      });
    }, { threshold: 0.4 });

    counters.forEach((counter) => observer.observe(counter));
  }
});
