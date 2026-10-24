"use strict";
var foodCatalog;
var recipes;
window.catalogReady = Promise.all([
    fetch('./food-catalog.json').then(r => { if (!r.ok) throw new Error('Ingredient data unavailable'); return r.json(); }),
    fetch('./recipes.json').then(r => { if (!r.ok) throw new Error('Recipe data unavailable'); return r.json(); })
]).then(([foods, items]) => { foodCatalog = foods; recipes = items; });
