// Live star count on every star button. Cached for an hour; on any failure the count stays hidden.
(async () => {
  const KEY = 'tidytv-stars';
  let n = null;
  try { const c = JSON.parse(localStorage.getItem(KEY) || 'null'); if (c && Date.now() - c.t < 36e5) n = c.n; } catch (e) {}
  if (n == null) {
    try {
      const r = await fetch('https://api.github.com/repos/jolan94/tidytv', { headers: { Accept: 'application/vnd.github+json' } });
      if (r.ok) n = (await r.json()).stargazers_count;
      try { localStorage.setItem(KEY, JSON.stringify({ n, t: Date.now() })); } catch (e) {}
    } catch (e) {}
  }
  if (typeof n !== 'number' || n < 1) return;
  const label = n >= 1000 ? (n / 1000).toFixed(n >= 10000 ? 0 : 1).replace(/\.0$/, '') + 'k' : String(n);
  document.querySelectorAll('[data-star]').forEach(a => {
    a.classList.add('has-stars');
    a.querySelector('[data-count]').textContent = label;
    a.setAttribute('aria-label', a.textContent.replace(label, '').trim() + ', ' + n + ' stars');
  });
})();
