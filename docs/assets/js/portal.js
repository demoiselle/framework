(() => {
  'use strict';

  const root = document.documentElement;
  const liveRegion = document.querySelector('[data-live-region]');
  const notify = (message) => {
    if (!liveRegion) return;
    liveRegion.textContent = '';
    window.setTimeout(() => { liveRegion.textContent = message; }, 30);
  };

  const preferredTheme = () => {
    try {
      const stored = localStorage.getItem('demoiselle-theme');
      if (stored === 'light' || stored === 'dark') return stored;
    } catch (_) {}
    return window.matchMedia?.('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
  };

  const themeButton = document.querySelector('[data-theme-toggle]');
  const applyTheme = (theme) => {
    root.dataset.theme = theme;
    if (themeButton) {
      const next = theme === 'dark' ? 'claro' : 'escuro';
      themeButton.setAttribute('aria-label', `Ativar tema ${next}`);
      themeButton.setAttribute('title', `Ativar tema ${next}`);
      const icon = themeButton.querySelector('.theme-icon');
      if (icon) icon.textContent = theme === 'dark' ? '☼' : '☾';
    }
  };

  applyTheme(root.dataset.theme || preferredTheme());
  themeButton?.addEventListener('click', () => {
    const theme = root.dataset.theme === 'dark' ? 'light' : 'dark';
    applyTheme(theme);
    try { localStorage.setItem('demoiselle-theme', theme); } catch (_) {}
    notify(`Tema ${theme === 'dark' ? 'escuro' : 'claro'} ativado.`);
  });

  const header = document.querySelector('[data-site-header]');
  const updateHeader = () => header?.classList.toggle('is-scrolled', window.scrollY > 12);
  updateHeader();
  window.addEventListener('scroll', updateHeader, { passive: true });

  const navDisclosure = document.querySelector('[data-nav-disclosure]');
  navDisclosure?.querySelectorAll('a').forEach((link) => {
    link.addEventListener('click', () => navDisclosure.removeAttribute('open'));
  });

  document.querySelectorAll('[data-copy-target]').forEach((button) => {
    button.addEventListener('click', async () => {
      const target = document.getElementById(button.dataset.copyTarget);
      if (!target) return;
      const text = target.textContent.trim();
      try {
        if (navigator.clipboard?.writeText) {
          await navigator.clipboard.writeText(text);
        } else {
          const area = document.createElement('textarea');
          area.value = text;
          area.setAttribute('readonly', '');
          area.style.position = 'fixed';
          area.style.opacity = '0';
          document.body.appendChild(area);
          area.select();
          document.execCommand('copy');
          area.remove();
        }
        const original = button.textContent;
        button.textContent = 'Copiado';
        notify('Trecho Maven copiado para a área de transferência.');
        window.setTimeout(() => { button.textContent = original; }, 1800);
      } catch (_) {
        notify('Não foi possível copiar automaticamente. Selecione o trecho manualmente.');
      }
    });
  });

  const revealTargets = document.querySelectorAll('.reveal-target');
  if ('IntersectionObserver' in window && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    revealTargets.forEach((target) => target.classList.add('reveal-ready'));
    const revealObserver = new IntersectionObserver((entries, observer) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return;
        entry.target.classList.add('is-visible');
        observer.unobserve(entry.target);
      });
    }, { rootMargin: '0px 0px -8% 0px', threshold: 0.08 });
    revealTargets.forEach((target) => revealObserver.observe(target));
  }

  const sectionLinks = [...document.querySelectorAll('[data-section-link]')];
  const sections = sectionLinks
    .map((link) => document.getElementById(link.dataset.sectionLink))
    .filter(Boolean);
  if ('IntersectionObserver' in window && sections.length) {
    const sectionObserver = new IntersectionObserver((entries) => {
      const visible = entries.filter((entry) => entry.isIntersecting)
        .sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0];
      if (!visible) return;
      sectionLinks.forEach((link) => {
        if (link.dataset.sectionLink === visible.target.id) link.setAttribute('aria-current', 'true');
        else link.removeAttribute('aria-current');
      });
    }, { rootMargin: '-25% 0px -60% 0px', threshold: [0.05, 0.25, 0.5] });
    sections.forEach((section) => sectionObserver.observe(section));
  }

  document.querySelectorAll('[data-current-year]').forEach((node) => {
    node.textContent = new Date().getFullYear();
  });
})();
