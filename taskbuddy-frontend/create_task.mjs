// Ruleaza cu: node create_task.mjs
//
// Acest script foloseste Playwright (chromium) pentru a crea un task nou in TaskBuddy
// la http://localhost:5173/. Se logheaza automat cu username="rox", password="123",
// completeaza formularul cu titlul task-ului si apasa butonul de submit.
//
// Cerinte:
//   - npm install playwright   (deja instalat in package.json)
//   - npx playwright install chromium   (browser-ul chromium trebuie sa fie instalat)
//   - Aplicatia front-end sa ruleze la http://localhost:5173/

import { chromium } from 'playwright';

const TASK_TITLE = 'Ce este React Vite Hydrate';
const USERNAME = 'rox';
const PASSWORD = '123';

async function main() {
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext({ viewport: { width: 1280, height: 720 } });
  const page = await context.newPage();

  // Navigheaza la aplicatie
  await page.goto('http://localhost:5173/');
  await page.waitForTimeout(2000); // asteapta hidratarea React

  // Verifica daca e logat
  const bodyText = await page.textContent('body');
  if (!bodyText.includes('Salut, rox')) {
    console.log('Nu sunt logat — facand login...');

    await page.fill('input[placeholder="Utilizator"]', USERNAME);
    console.log('Username introdus');

    await page.fill('input[placeholder="Parolă"]', PASSWORD);
    console.log('Parola introdusa');

    await page.click('button:has-text("Conectare")');
    console.log('Buton Conectare apasat');

    await page.waitForTimeout(3000); // asteapta redirect
  } else {
    console.log('Deja logat ca rox');
  }

  // Verifica daca task-ul exista deja in UI
  const existingText = await page.textContent('body');
  if (existingText.includes(TASK_TITLE)) {
    console.log(`\n✅ Taskul "${TASK_TITLE}" exista deja. Nu creez duplicat.`);
    await browser.close();
    return;
  }

  // Completeaza formularul de task
  const inputs = await page.$$('input');
  console.log(`Am gasit ${inputs.length} inputuri text`);

  // Primul input este campul de titlu: "Ce ai de făcut astăzi?"
  if (inputs.length > 0) {
    await inputs[0].fill(TASK_TITLE);
    console.log(`Titlu task introdus: "${TASK_TITLE}"`);
  }

  // Screenshot de verificare
  await page.screenshot({ path: '/tmp/task_filled.png', fullPage: true });
  console.log('Screenshot salvat: /tmp/task_filled.png');

  // Apasa butonul de submit
  const buttons = await page.$$('button');
  for (const btn of buttons) {
    const text = await btn.textContent();
    if (text && text.includes('Adaugă')) {
      await btn.click();
      console.log('Buton "+ Adaugă în listă" apasat');
      break;
    }
  }

  await page.waitForTimeout(3000);

  // Screenshot final
  await page.screenshot({ path: '/tmp/task_after_submit.png', fullPage: true });
  console.log('Screenshot final: /tmp/task_after_submit.png');

  // Verifica rezultatul
  const finalText = await page.textContent('body');
  if (finalText.includes(TASK_TITLE)) {
    console.log(`\n✅ SUCCESS: Taskul "${TASK_TITLE}" a fost creat cu succes!`);
  } else {
    console.log(`\n⚠️ Nu am gasit textul task-ului in UI. Verifica screenshot-urile.`);
    console.log('Body text (primele 600 caractere):', finalText.substring(0, 600));
  }

  await browser.close();
  console.log('Done');
}

main().catch(err => {
  console.error('Eroare:', err);
  process.exit(1);
});
