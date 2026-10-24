#!/usr/bin/env python3
"""Refresh the APK's offline snapshot from the sibling website checkout, without network access."""
from pathlib import Path
import argparse, json, re, shutil
root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('--website', type=Path, default=root.parent / 'gastronomy-task3-yaz.')
args = parser.parse_args()
website = args.website.resolve()
source = (website / 'data.js').read_text()
foods = json.loads(source.split('const foodCatalog = ', 1)[1].split(';\nconst recipes', 1)[0])
recipes = json.loads(source.split('const recipes = ', 1)[1].rstrip(';\n'))
assert len(recipes) == 80 and len({r['id'] for r in recipes}) == 80
assets = root / 'app/src/main/assets'
assets.mkdir(parents=True, exist_ok=True)
for name, data in [('food-catalog.json', foods), ('recipes.json', recipes)]:
    (assets / name).write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n')
index = (website / 'index.html').read_text()
index = re.sub(r'<link rel="manifest"[^>]*>', '', index)
index = re.sub(r'<link rel="apple-touch-icon"[^>]*>', '', index)
index = re.sub(r'<button id="installApp".*?</button>', '', index)
index = re.sub(r'<p id="offlineStatus".*?</p>', '', index)
index = index.replace('<script defer src="pwa.js"></script>', '')
(assets / 'index.html').write_text(index)
(assets / 'data.js').write_text('''"use strict";
var foodCatalog;
var recipes;
window.catalogReady = Promise.all([
    fetch('./food-catalog.json').then(r => { if (!r.ok) throw new Error('Ingredient data unavailable'); return r.json(); }),
    fetch('./recipes.json').then(r => { if (!r.ok) throw new Error('Recipe data unavailable'); return r.json(); })
]).then(([foods, items]) => { foodCatalog = foods; recipes = items; });
''')
(assets / 'app.js').write_text('''"use strict";
(async () => {
await window.catalogReady;
''' + (website / 'app.js').read_text() + '''
})().catch(() => {
 document.getElementById('resultSummary').textContent = 'Recipes could not load. / Tarifler yüklenemedi.';
});
''')
for name in ['style.css', 'icon.svg']:
    shutil.copy2(website / name, assets / name)
shutil.copytree(website / 'images', assets / 'images', dirs_exist_ok=True)
shutil.copy2(website / 'icons/icon-192.png', root / 'app/src/main/res/mipmap/ic_launcher.png')
print(f'Updated offline snapshot: {len(recipes)} recipes, EN/TR UI, nutrition, filters and original artwork.')
