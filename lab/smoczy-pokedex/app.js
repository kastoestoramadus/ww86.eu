/* Smoczy Pokedex lab — renders the derived catalog (app/data/catalog.json).
   Truthful states only: an empty slot is shown as empty (PLAN.md: puste miejsce nie jest ukończoną ilustracją). */
"use strict";

const PL_STATUS = { ready: "gotowe", partial: "częściowe", absent: "brak" };

const state = { catalog: null, q: "", copyResetTimer: null };

function el(tag, attrs = {}, ...children) {
  const node = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) {
    if (k === "class") node.className = v;
    else if (k.startsWith("on")) node.addEventListener(k.slice(2), v);
    else node.setAttribute(k, v);
  }
  for (const c of children) node.append(c);
  return node;
}

function badge(status) {
  return el("span", { class: `badge ${status}` }, PL_STATUS[status] || status);
}

function slotNode(variant, pokemon) {
  const ph = el("div", { class: `ph ${variant.image_status === "ready" ? "" : "absent"}` });
  if (variant.image_status === "ready" && variant.image_file) {
    ph.append(el("img", { src: variant.image_file, alt: `Wariant ${variant.variant_id}` }));
  }
  const meta = el("div", { class: "meta" });
  meta.append(el("span", { class: "dex-slot" }, `Wariant ${variant.variant_id.slice(-2)}`));
  meta.append(badge(variant.image_status));
  if (variant.image_status === "ready" && variant.image_version) {
    meta.append(el("span", { class: "dim" }, `obraz v${variant.image_version}`));
    meta.append(el("span", { class: "dim" }, `użyty prompt v${variant.image_prompt_version}`));
  }
  meta.append(el("span", { class: "dim" }, `Prompt: ${PL_STATUS[variant.prompt_status]}`));
  const promptReady = (variant.prompt_status === "ready" || variant.prompt_status === "partial") && variant.prompt_text;
  if (promptReady) {
    meta.append(el("button", {
      type: "button",
      onclick: () => openPrompt(pokemon, variant),
    }, "Podgląd promptu"));
  } else {
    meta.append(el("span", { class: "missing" }, "prompt jeszcze niegotowy"));
  }
  if (variant.prompt_needs_reanalysis_review) {
    meta.append(el("span", { class: "warn" }, "wymaga przeglądu po zmianie analizy"));
  }
  return el("div", { class: "slot" }, ph, meta);
}

function analysisNode(pokemon) {
  const box = el("div", { class: "analysis" });
  if (pokemon.analysis_status === "absent" || !pokemon.has_record) {
    box.append(el("p", { class: "missing" }, "Analiza jeszcze niepowstała — luka jest zapisana, nic nie jest zmyślone."));
    return box;
  }
  box.append(el("p", {}, el("strong", {}, "Status analizy: "), PL_STATUS[pokemon.analysis_status]));
  const rec = pokemon.analysis;
  if (!rec) return box;
  const list = (title, items, withRefs) => {
    if (!items || !items.length) return;
    box.append(el("h3", {}, title));
    const ul = el("ul");
    for (const it of items) {
      if (typeof it === "string") ul.append(el("li", {}, it));
      else ul.append(el("li", {}, `${it.claim} `, el("em", {}, `[${it.reference_id}]`)));
    }
    box.append(ul);
  };
  list("Obserwacje (fakty z referencji)", rec.observations, true);
  list("Interpretacje", rec.inferences, false);
  list("Cechy kluczowe", rec.key_features, false);
  list("Pole swobodnej transformacji", rec.free_transform_axis, false);
  return box;
}

function cardNode(pokemon) {
  const tpl = document.getElementById("card-tpl");
  const node = tpl.content.cloneNode(true);
  node.querySelector(".dex").textContent = `#${pokemon.pokemon_id}`;
  node.querySelector(".name").textContent = pokemon.display_name;
  node.querySelector(".types").textContent = pokemon.types.join(" / ");
  const slots = node.querySelectorAll(".slot");
  pokemon.variants.forEach((v, i) => slots[i].replaceWith(slotNode(v, pokemon)));
  node.querySelector(".adult .analysis").replaceWith(analysisNode(pokemon));
  return node;
}

function matches(p, q) {
  if (!q) return true;
  const n = q.trim().toLowerCase();
  if (!n) return true;
  return p.display_name.toLowerCase().includes(n)
    || p.name.toLowerCase().includes(n)
    || p.pokemon_id === n
    || String(parseInt(n, 10)).padStart(3, "0") === p.pokemon_id && /^\d+$/.test(n);
}

function render() {
  const grid = document.getElementById("grid");
  const empty = document.getElementById("empty");
  grid.replaceChildren();
  const list = state.catalog.pokemon.filter((p) => matches(p, state.q));
  const frag = document.createDocumentFragment();
  for (const p of list) frag.append(cardNode(p));
  grid.append(frag);
  empty.hidden = list.length > 0;
  if (!list.length) document.getElementById("empty-q").textContent = state.q;
  const c = state.catalog;
  const ready = c.pokemon.reduce((a, p) => a + p.variants.filter((v) => v.image_status === "ready").length, 0);
  document.getElementById("summary").textContent =
    `${c.pokemon.length} Pokémonów · ${c.built_from.record_count} rekordów kanonicznych · ${ready} gotowych ilustracji z 453 miejsc`;
}

function openPrompt(pokemon, variant) {
  const dlg = document.getElementById("prompt-dlg");
  document.getElementById("dlg-title").textContent =
    `#${pokemon.pokemon_id} ${pokemon.display_name} — ${variant.variant_id} (v${variant.prompt_version}, ${PL_STATUS[variant.prompt_status]})`;
  const pre = document.getElementById("dlg-text");
  pre.textContent = variant.prompt_text || "";
  const copy = document.getElementById("dlg-copy");
  clearTimeout(state.copyResetTimer);
  copy.textContent = "Kopiuj";
  copy.onclick = async () => {
    clearTimeout(state.copyResetTimer);
    try {
      await navigator.clipboard.writeText(variant.prompt_text || "");
      copy.textContent = "Skopiowano ✓";
    } catch {
      // clipboard permission denied: select the text so the user can copy manually
      const r = document.createRange();
      r.selectNodeContents(pre);
      const s = getSelection();
      s.removeAllRanges();
      s.addRange(r);
      copy.textContent = "Zaznaczono — skopiuj ręcznie";
    }
    state.copyResetTimer = setTimeout(() => { copy.textContent = "Kopiuj"; }, 2000);
  };
  document.getElementById("dlg-download").onclick = () => {
    const blob = new Blob([variant.prompt_text || ""], { type: "text/plain" });
    const a = el("a", { href: URL.createObjectURL(blob), download: `${variant.variant_id}-prompt-v${variant.prompt_version}.txt` });
    a.click();
    URL.revokeObjectURL(a.href);
  };
  dlg.showModal();
}

async function main() {
  const res = await fetch("data/catalog.json");
  if (!res.ok) throw new Error(`catalog.json: HTTP ${res.status}`);
  state.catalog = await res.json();
  document.getElementById("q").addEventListener("input", (e) => {
    state.q = e.target.value;
    render();
  });
  document.getElementById("export").addEventListener("click", () => {
    const blob = new Blob([JSON.stringify(state.catalog, null, 1)], { type: "application/json" });
    const a = el("a", { href: URL.createObjectURL(blob), download: `smoczy-catalog-export-${state.catalog.format_version}.json` });
    a.click();
    URL.revokeObjectURL(a.href);
  });
  document.getElementById("dlg-close").addEventListener("click", () => document.getElementById("prompt-dlg").close());
  render();
}

main().catch((err) => {
  document.getElementById("summary").textContent = `Błąd wczytywania katalogu: ${err.message}`;
});
