/**
 * Fetches JVM metrics from /api/metrics and updates the Live Metrics dashboard.
 * Gracefully degrades if the API is not available.
 */
(function () {
  const heapUsedEl = document.getElementById('heap-used');
  const heapBarEl = document.getElementById('heap-bar');
  const heapMaxEl = document.getElementById('heap-max');
  const threadCountEl = document.getElementById('thread-count');
  const threadSparkEl = document.getElementById('thread-spark');

  if (!heapUsedEl || !heapBarEl) return;

  const formatBytes = (bytes) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  const updateMetrics = (data) => {
    const used = data.heapUsed || 0;
    const max = data.heapMax || 1;
    const pct = Math.min(100, Math.round((used / max) * 100));

    heapUsedEl.textContent = formatBytes(used);
    heapMaxEl.textContent = formatBytes(max);
    heapBarEl.style.width = pct + '%';

    if (threadCountEl) threadCountEl.textContent = data.threadCount ?? '—';
    if (threadSparkEl) threadSparkEl.textContent = data.threadCount ?? '—';
  };

  const poll = () => {
    fetch('/api/metrics')
      .then((res) => (res.ok ? res.json() : Promise.reject()))
      .then(updateMetrics)
      .catch(() => {
        heapUsedEl.textContent = '—';
        heapMaxEl.textContent = '—';
        heapBarEl.style.width = '0%';
        if (threadCountEl) threadCountEl.textContent = '—';
        if (threadSparkEl) threadSparkEl.textContent = '—';
      });
  };

  poll();
  setInterval(poll, 2000);
})();
