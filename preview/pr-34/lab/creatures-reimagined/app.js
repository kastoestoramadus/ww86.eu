/* Pokédex galeria — static gallery of two styles (Smoki, Pojazdy) of the same 151 species.
   Truthful states only: a missing image shows a placeholder, never a fake.
   State lives in the URL (?style=&id=&v=); pairing in "Oba" is strictly by pokemon_id. */
"use strict";

const OBA = "oba";
const DEFAULT_STYLE = "pojazdy"; // what a bare URL loads, when this key exists
const state = { style: null, id: null, v: 1 };
const ui = {
  styles: null,          // data/styles.json
  indexes: new Map(),    // style key -> StyleIndex
  onlyImgTouched: false,
  pushed: 0,             // history entries this session pushed for detail views
  q: "",
};

const $ = (sel) => document.querySelector(sel);

function el(tag, attrs = {}, ...children) {
  const node = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (k === "class") node.className = v;
    else if (k === "text") node.textContent = v;
    else if (k.startsWith("on")) node.addEventListener(k.slice(2), v);
    else node.setAttribute(k, v);
  }
  for (const c of children) if (c != null) node.append(c);
  return node;
}

async function fetchJson(url) {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`${url}: HTTP ${res.status}`);
  return res.json();
}

async function styleIndex(key) {
  if (!ui.indexes.has(key)) ui.indexes.set(key, await fetchJson(`data/${key}.json`));
  return ui.indexes.get(key);
}

/* ---------- URL state ---------- */

function defaultStyleKey() {
  if (!ui.styles) return null;
  const preferred = ui.styles.styles.find((s) => s.key === DEFAULT_STYLE);
  return (preferred || ui.styles.styles[0] || {}).key || null;
}

function parseUrl() {
  const p = new URLSearchParams(location.search);
  const style = p.get("style") || defaultStyleKey();
  const id = p.get("id");
  const v = Math.min(3, Math.max(1, parseInt(p.get("v") || "1", 10) || 1));
  return { style, id, v };
}

function writeUrl(push) {
  const p = new URLSearchParams();
  if (state.style) p.set("style", state.style);
  if (state.id) p.set("id", state.id);
  if (state.id && state.v > 1) p.set("v", String(state.v));
  const url = `${location.pathname}?${p.toString()}`;
  if (push) { history.pushState({ ...state }, "", url); ui.pushed += 1; }
  else history.replaceState({ ...state }, "", url);
}

function applyState() {
  const { style, id, v } = parseUrl();
  state.style = style; state.id = id; state.v = v;
  const sel = $("#style");
  const want = sel && (sel.querySelector(`option[value="${style}"]`) ? style : defaultStyleKey());
  state.style = want;
  if (sel) sel.value = want;
  render();
}

/* ---------- filtering ---------- */

function matches(p, q) {
  if (!q) return true;
  const n = q.trim().toLowerCase();
  if (!n) return true;
  return p.display_name.toLowerCase().includes(n)
    || p.name.toLowerCase().includes(n)
    || p.pokemon_id === n
 || (/^\d+$/.test(n) && String(parseInt(n, 10)).padStart(3, "0") === p.pokemon_id);
}

function onlyImgDefault() {
  if (!ui.onlyImgTouched) {
    let any = 0;
    if (state.style === OBA) {
      for (const s of ui.styles.styles) any += s.images;
    } else {
      const s = ui.styles.styles.find((x) => x.key === state.style);
      any = s ? s.images : 0;
    }
    $("#only-img").checked = any > 0;
  }
}

function visiblePokemon(list) {
  const only = $("#only-img").checked;
  return list.filter((p) => (!only || p.image_count > 0) && matches(p, ui.q));
}

/* ---------- grid view ---------- */

function firstImage(p) {
  for (const v of p.variants) if (v.image_status === "ready" && v.thumb_src) return v;
  return null;
}

function cardNode(p) {
  const img = firstImage(p);
  const visual = img
    ? el("img", { src: img.thumb_src, alt: `${p.display_name} — podgląd`, loading: "lazy", decoding: "async" })
    : el("div", { class: "ph", text: "Brak obrazka" });
  return el("li", {},
    el("div", { class: "card", tabindex: "0", role: "link",
      "aria-label": `${p.display_name} #${p.pokemon_id}`,
      onclick: () => openDetail(p.pokemon_id, 1),
      onkeydown: (e) => { if (e.key === "Enter") openDetail(p.pokemon_id, 1); } },
      el("div", { class: "head" }, el("span", { class: "dex", text: `#${p.pokemon_id}` }),
        el("span", { class: "types", text: p.types.join(" / ") })),
      el("h2", { class: "name", text: p.display_name }),
      visual,
      el("p", { class: "count", text: `obrazki: ${p.image_count}/${p.variant_count}` })));
}

function renderGrid(list) {
  const grid = $("#grid");
  grid.replaceChildren();
  const vis = visiblePokemon(list);
  for (const p of vis) grid.append(cardNode(p));
  $("#empty").hidden = vis.length > 0;
  $("#summary").textContent =
    `${vis.length} z ${list.length} gatunków · obrazki: ` +
    `${list.reduce((a, p) => a + p.image_count, 0)}/${list.reduce((a, p) => a + p.variant_count, 0)}`;
}

/* ---------- Oba view: pairing strictly by pokemon_id ---------- */

async function renderOba() {
  const keys = ui.styles.styles.map((s) => s.key);
  const indexes = await Promise.all(keys.map(styleIndex));
  const maps = keys.map((k, i) => [k, new Map(indexes[i].pokemon.map((p) => [p.pokemon_id, p]))]);
  const ref = indexes[0].pokemon; // id sets are equal (build check), ref order is the numeric order
  const head = $("#oba-head");
  head.replaceChildren(el("div", { class: "seg", text: ui.styles.styles[0].label }),
    el("div", { class: "seg", text: ui.styles.styles[1] ? ui.styles.styles[1].label : "" }));
  const rows = $("#oba-rows");
  rows.replaceChildren();
  const only = $("#only-img").checked;
  let shown = 0;
  for (const p of ref) {
    if (!matches(p, ui.q)) continue;
    const cells = maps.map(([k, m]) => [k, m.get(p.pokemon_id)]);
    const anyImage = cells.some(([, c]) => c && c.image_count > 0);
    if (only && !anyImage) continue;
    shown += 1;
    const row = el("li", { class: "oba-row", "data-id": p.pokemon_id },
      el("div", { class: "lbl" }, `#${p.pokemon_id}`, el("span", { class: "nm", text: p.display_name })));
    const perStyle = cells.map(([k, c]) => ({ k, c, variants: c ? c.variants : [null] })); // no record → one "brak danych" cell
    const depth = Math.max(...perStyle.map((s) => s.variants.length));
    for (let i = 0; i < depth; i += 1) // a row of pairs: the same variant of each style, side by side
      for (const s of perStyle) row.append(obaCell(s.k, p.pokemon_id, s.c, s.variants[i] || null, i));
    rows.append(row);
  }
  $("#oba-empty").hidden = shown > 0;
  $("#summary").textContent = `Oba: ${shown} z ${ref.length} gatunków · pary łączone po pokemon_id`;
}

function obaCell(key, pid, p, varr, vIdx) {
  const img = varr && varr.image_status === "ready" && varr.thumb_src
    ? el("img", { src: varr.thumb_src, alt: `${key} ${pid} — wariant ${vIdx + 1}`, loading: "lazy", decoding: "async" })
    : null;
  return el("div", { class: "cell", tabindex: "0", role: "link", "data-style": key, "data-id": pid,
    onclick: () => { state.style = key; openDetail(pid, vIdx + 1, true); },
    onkeydown: (e) => { if (e.key === "Enter") { state.style = key; openDetail(pid, vIdx + 1, true); } } }, img
      || el("div", { class: "ph", text: p ? "Brak obrazka" : "brak danych" }));
}

/* ---------- detail view ---------- */

function navList(list) {
  const vis = visiblePokemon(list);
  return vis.some((p) => p.pokemon_id === state.id) ? vis : list;
}

function openDetail(id, v, styleChanged) {
  state.id = id;
  state.v = v || 1;
  if (styleChanged) $("#style").value = state.style;
  writeUrl(true);
  render();
}

function closeDetail() {
  state.id = null;
  if (ui.pushed > 0) { ui.pushed -= 1; history.back(); } // popstate re-renders
  else { writeUrl(false); render(); }
}

function step(delta) {
  return styleIndex(state.style).then((ix) => {
    const list = navList(ix.pokemon);
    let i = list.findIndex((p) => p.pokemon_id === state.id);
    if (i === -1) i = 0;
    const next = list[(i + delta + list.length) % list.length];
    state.id = next.pokemon_id;
    state.v = 1;
    writeUrl(true);
    renderDetail(ix);
  });
}

function renderDetail(ix) {
  const p = ix.pokemon.find((x) => x.pokemon_id === state.id);
  if (!p) { closeDetail(); return; }
  $("#d-dex").textContent = `#${p.pokemon_id}`;
  $("#d-name").textContent = `${p.display_name} · ${ix.label}`;
  $("#d-types").textContent = p.types.join(" / ");
  const vIdx = Math.min(state.v, p.variants.length) - 1;
  const varr = p.variants[vIdx];
  const img = $("#d-img"), ph = $("#d-ph");
  if (varr && varr.image_status === "ready" && varr.image_src) {
    img.src = varr.image_src; img.alt = `${p.display_name} — wariant ${vIdx + 1} (${ix.label})`;
    img.hidden = false; ph.hidden = true;
  } else {
    img.removeAttribute("src"); img.hidden = true; ph.hidden = false;
    ph.textContent = varr ? `Brak obrazka (wariant ${vIdx + 1})` : "Brak wariantu";
  }
  $("#d-summary").textContent = varr && varr.concept_summary ? varr.concept_summary : "";
  const vr = $("#d-variants");
  vr.replaceChildren();
  p.variants.forEach((vv, i) => vr.append(el("button", { type: "button",
    "aria-pressed": String(i === vIdx),
    onclick: () => { state.v = i + 1; writeUrl(false); renderDetail(ix); } },
    `Wariant ${i + 1}`,
    el("span", { class: "badge", text: vv.image_status === "ready" ? "obraz" : "brak" }))));
  $("#prev").disabled = $("#next").disabled = p.variants.length === 0;
}

/* ---------- render root ---------- */

async function render() {
  const obaOk = ui.styles.pairing_ok && ui.styles.styles.length >= 2;
  if (state.style === OBA && !obaOk) state.style = ui.styles.styles[0].key;
  onlyImgDefault();
  const grid = $("#grid-view"), oba = $("#oba-view"), detail = $("#detail-view");
  if (state.style === OBA) {
    grid.hidden = true; detail.hidden = true; oba.hidden = false;
    await renderOba();
  } else {
    oba.hidden = true;
    const ix = await styleIndex(state.style);
    if (state.id) {
      grid.hidden = true; detail.hidden = false;
      renderDetail(ix);
    } else {
      detail.hidden = true; grid.hidden = false;
      renderGrid(ix.pokemon);
    }
  }
}

/* ---------- events ---------- */

function wire() {
  $("#style").addEventListener("change", (e) => {
    state.style = e.target.value;
    writeUrl(false);
    render();
  });
  $("#q").addEventListener("input", (e) => { ui.q = e.target.value; render(); });
  $("#only-img").addEventListener("change", () => { ui.onlyImgTouched = true; render(); });
  $("#back").addEventListener("click", (e) => { e.preventDefault(); closeDetail(); });
  $("#prev").addEventListener("click", () => step(-1));
  $("#next").addEventListener("click", () => step(1));
  document.addEventListener("keydown", (e) => {
    if (e.target && ["INPUT", "SELECT", "TEXTAREA"].includes(e.target.tagName)) return;
    if (!state.id || state.style === OBA) return;
    if (e.key === "ArrowLeft") step(-1);
    else if (e.key === "ArrowRight") step(1);
    else if (e.key === "Escape") closeDetail();
    else if (["1", "2", "3"].includes(e.key)) {
      state.v = parseInt(e.key, 10);
      writeUrl(false);
      styleIndex(state.style).then(renderDetail);
    }
  });
  const stage = $("#d-stage");
  let tx = null, ty = null;
  stage.addEventListener("touchstart", (e) => {
    tx = e.touches[0].clientX; ty = e.touches[0].clientY;
  }, { passive: true });
  stage.addEventListener("touchend", (e) => {
    if (tx === null) return;
    const dx = e.changedTouches[0].clientX - tx;
    const dy = e.changedTouches[0].clientY - ty;
    if (Math.abs(dx) > 48 && Math.abs(dx) > Math.abs(dy) && state.id) step(dx < 0 ? 1 : -1);
    tx = null;
  }, { passive: true });
  window.addEventListener("popstate", applyState);
}

async function main() {
  ui.styles = await fetchJson("data/styles.json");
  const sel = $("#style");
  sel.replaceChildren();
  for (const s of ui.styles.styles) sel.append(el("option", { value: s.key, text: `${s.label} (${s.images} obrazków)` }));
  if (ui.styles.pairing_ok && ui.styles.styles.length >= 2) {
    sel.append(el("option", { value: OBA, text: `Oba (${ui.styles.styles.map((s) => s.label).join(" + ")})` }));
  }
  wire();
  applyState();
}

main().catch((err) => {
  document.getElementById("summary").textContent = `Błąd ładowania danych: ${err.message}`;
});
