// RoboFriend — wariant "japandi-editorial"

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

  // Carousel — glass slides cycling through the main categories
  const track = document.getElementById('category-carousel');
  if (track) {
    const slides = Array.from(track.children);
    const dots = Array.from(document.querySelectorAll('.carousel-dot'));
    const prevBtn = document.querySelector('.carousel-prev');
    const nextBtn = document.querySelector('.carousel-next');
    let currentIndex = 0;

    const goToSlide = (index) => {
      const wrapped = (index + slides.length) % slides.length;
      slides[wrapped].scrollIntoView({ behavior: 'smooth', inline: 'start', block: 'nearest' });
    };
    prevBtn?.addEventListener('click', () => goToSlide(currentIndex - 1));
    nextBtn?.addEventListener('click', () => goToSlide(currentIndex + 1));

    dots.forEach((dot, i) => {
      dot.addEventListener('click', () => goToSlide(i));
    });

    const setActiveDot = (index) => {
      currentIndex = index;
      dots.forEach((dot, i) => dot.classList.toggle('active', i === index));
    };

    if ('IntersectionObserver' in window) {
      const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              setActiveDot(slides.indexOf(entry.target));
            }
          });
        },
        { root: track, threshold: 0.6 }
      );
      slides.forEach((slide) => observer.observe(slide));
    }

    // Drag-to-scroll with the mouse
    let isDown = false;
    let dragged = false;
    let startX = 0;
    let startScrollLeft = 0;

    track.addEventListener('mousedown', (e) => {
      isDown = true;
      dragged = false;
      track.classList.add('dragging');
      startX = e.pageX;
      startScrollLeft = track.scrollLeft;
      e.preventDefault();
    });

    window.addEventListener('mouseup', () => {
      isDown = false;
      track.classList.remove('dragging');
    });

    track.addEventListener('mouseleave', () => {
      isDown = false;
      track.classList.remove('dragging');
    });

    track.addEventListener('mousemove', (e) => {
      if (!isDown) return;
      e.preventDefault();
      const delta = e.pageX - startX;
      if (Math.abs(delta) > 5) dragged = true;
      track.scrollLeft = startScrollLeft - delta;
    });

    slides.forEach((slide) => {
      slide.addEventListener(
        'click',
        (e) => {
          if (dragged) {
            e.preventDefault();
            e.stopPropagation();
          }
        },
        true
      );
    });
  }
});
