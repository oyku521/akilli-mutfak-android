'use strict';
(async () => {
const response = await fetch('./recipes.json');
if (!response.ok) throw new Error('Tarif verisi yüklenemedi');
const recipes = await response.json();
const ingredients = ["Yumurta", "Domates", "Biber", "Peynir", "Un", "Süt", "Makarna", "Tavuk", "Patates", "Soğan", "Sarımsak", "Pirinç", "Mercimek", "Nohut", "Havuç", "Kıyma", "Patlıcan", "Salatalık", "Yoğurt", "Ekmek", "Kabak", "Ispanak", "Mantar", "Limon"];
'use strict';
const $ = id => document.getElementById(id);
const normalize = value => value.toLocaleLowerCase('tr-TR');
function restore(key, allowed) {
  try { const value = JSON.parse(localStorage.getItem(key) || '[]'); return new Set(Array.isArray(value) ? value.filter(item => allowed.includes(item)) : []); }
  catch { return new Set(); }
}
const selected = restore('kitchen-ingredients', ingredients);
const favorites = restore('kitchen-favorites', recipes.map(r => r.name));
let category = 'Tümü', onlyFavorites = false, limit = 9;
const icons = {'Kahvaltı':'🍳','Ana Yemek':'🍲','Atıştırmalık':'🥪','Çorba':'🥣','Tatlı':'🍮','Salata':'🥗','Hamur İşi':'🥐','Garnitür':'🥔','Meze':'🧆'};
const tints = ['#eee8d8','#e4ebdb','#f2e1d7','#eee7dc','#e1e9e2'];
function persist() {
  try { localStorage.setItem('kitchen-ingredients', JSON.stringify([...selected])); localStorage.setItem('kitchen-favorites', JSON.stringify([...favorites])); } catch { /* Private mode still supports this session. */ }
}
function renderIngredients() {
  const query = normalize($('ingredientSearch').value);
  $('ingredients').replaceChildren();
  ingredients.filter(name => normalize(name).includes(query)).forEach(name => {
    const label = document.createElement('label'); label.className = 'ingredient';
    const input = document.createElement('input'); input.type = 'checkbox'; input.checked = selected.has(name);
    input.addEventListener('change', () => { input.checked ? selected.add(name) : selected.delete(name); limit = 9; persist(); render(); });
    label.append(input, document.createTextNode(name)); $('ingredients').append(label);
  });
  if (!$('ingredients').children.length) $('ingredients').textContent = 'Malzeme bulunamadı.';
}
function renderCategories() {
  $('categories').replaceChildren();
  ['Tümü', ...new Set(recipes.map(r => r.category))].forEach(name => {
    const button = document.createElement('button'); button.textContent = name; button.className = category === name ? 'active' : ''; button.setAttribute('aria-pressed', String(category === name));
    button.onclick = () => { category = name; limit = 9; render(); }; $('categories').append(button);
  });
}
function render() {
  renderCategories(); $('selectedCount').textContent = selected.size; $('favoriteCount').textContent = favorites.size;
  $('favoritesNav').setAttribute('aria-pressed', String(onlyFavorites));
  $('resultsTitle').textContent = onlyFavorites ? 'Sevdiğin lezzetler' : selected.size ? 'Mutfağına uygun tarifler' : 'Sofrana ilham olsun';
  const query = normalize($('recipeSearch').value);
  const matches = recipes.map(r => ({...r, missing:r.ingredients.filter(i => !selected.has(i)), have:r.ingredients.filter(i => selected.has(i)).length}))
    .filter(r => (category === 'Tümü' || r.category === category) && (!$('vegan').checked || r.vegan) && (!onlyFavorites || favorites.has(r.name)) && normalize(r.name).includes(query) && (!selected.size || (r.have > 0 && r.missing.length <= 1)))
    .sort((a,b) => selected.size ? a.missing.length-b.missing.length || b.have-a.have || a.name.localeCompare(b.name,'tr') : 0);
  $('resultSummary').textContent = `${recipes.length} tarif arasından ${matches.length} lezzet${selected.size ? ' malzemelerinle eşleşti.' : ' keşfetmeni bekliyor.'}`;
  $('results').replaceChildren();
  matches.slice(0,limit).forEach((r,index) => {
    const card = document.createElement('article'); card.className = 'recipe-card';
    card.innerHTML = `<div class="recipe-visual" style="--tint:${tints[index%tints.length]}"><span class="dish" aria-hidden="true">${icons[r.category]}</span>${r.vegan ? '<span class="vegan-badge">🌱 Vegan</span>' : ''}<button class="favorite" aria-pressed="${favorites.has(r.name)}" aria-label="${r.name}: ${favorites.has(r.name) ? 'favorilerden çıkar' : 'favorilere ekle'}">${favorites.has(r.name) ? '♥' : '♡'}</button></div><div class="recipe-info"><div class="recipe-meta"><span>${r.category}</span><span>${r.calories} kcal*</span></div><h3>${r.name}</h3><p>${r.ingredients.join(' · ')}</p><p>Dolap dışında: ${r.extras.join(', ')}</p><div class="match ${selected.size && r.missing.length ? 'partial' : ''}">${!selected.size ? '↗ Malzemelerini seç, uygunluğunu gör' : r.missing.length ? '◔ Eksik: '+r.missing.join(', ') : '✓ Tüm ana malzemeler elinde!'}</div></div>`;
    card.querySelector('button').onclick = () => { favorites.has(r.name) ? favorites.delete(r.name) : favorites.add(r.name); persist(); render(); };
    $('results').append(card);
  });
  if (!matches.length) { const empty = document.createElement('p'); empty.className='empty'; empty.textContent = onlyFavorites ? 'Bu filtrelerle favori tarif bulunamadı. Tariflerdeki kalbe dokunarak favorilerini oluşturabilirsin.' : 'Uygun tarif bulunamadı. Daha fazla malzeme seçmeyi veya filtrelerini değiştirmeyi dene.'; $('results').append(empty); }
  $('showMore').hidden = matches.length <= limit;
}
$('ingredientSearch').oninput = renderIngredients;
$('recipeSearch').oninput = () => { limit=9; render(); };
$('vegan').onchange = () => { limit=9; render(); };
$('clear').onclick = () => { selected.clear(); persist(); renderIngredients(); render(); };
$('favoritesNav').onclick = () => { onlyFavorites = !onlyFavorites; limit=9; render(); $('discover').scrollIntoView({behavior:matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth'}); };
$('showMore').onclick = () => { limit += 9; render(); };
renderIngredients(); render();

})().catch(() => {
 document.getElementById('resultSummary').textContent = 'Tarifler yüklenemedi. Uygulamayı yeniden açmayı dene.';
});
