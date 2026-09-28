export function navigate(path: string, replace = false) {
  if (replace) {
    window.history.replaceState({}, "", path);
  } else {
    window.history.pushState({}, "", path);
  }
  window.dispatchEvent(new Event("app:navigate"));
}

export function getPositiveId(key: string) {
  const raw = new URLSearchParams(window.location.search).get(key);
  if (!raw || !/^\d+$/.test(raw)) return null;
  const value = Number(raw);
  return value > 0 ? value : null;
}
