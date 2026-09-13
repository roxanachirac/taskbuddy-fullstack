import { test, expect, Page } from '@playwright/test';

/**
 * Helper: register a user (accepting the alert) and log in.
 * Returns the username so callers can use it for assertions.
 * Uses a random suffix to avoid collisions when tests run in parallel.
 */
async function registerAndLogin(page: Page): Promise<string> {
  const randomSuffix = Math.random().toString(36).substring(2, 8);
  const username = `taskuser-${Date.now()}-${randomSuffix}`;

  // Set up dialog handler BEFORE any page interaction to avoid race conditions
  const dialogHandler = async (dialog: import('@playwright/test').Dialog) => {
    await dialog.accept();
  };
  page.on('dialog', dialogHandler);

  await page.goto('/');
  await page.getByText('Înregistrează-te').click();
  await expect(page.getByRole('heading', { name: 'Crează un cont nou' })).toBeVisible();
  await page.getByPlaceholder('Utilizator').fill(username);
  await page.getByPlaceholder('Parolă').fill('SecurePass123!');

  await page.getByRole('button', { name: 'Înregistrare' }).click();

  // After alert is accepted, verify login view is shown
  await expect(page.getByRole('heading', { name: 'Autentifică-te' })).toBeVisible();

  // Remove the dialog handler now that registration alert is handled
  page.off('dialog', dialogHandler);

  await page.getByPlaceholder('Utilizator').fill(username);
  await page.getByPlaceholder('Parolă').fill('SecurePass123!');
  await page.getByRole('button', { name: 'Conectare' }).click();
  await expect(page.getByText(`Salut, ${username}`)).toBeVisible();

  return username;
}

test.describe('Task Management', () => {
  test('should create a new task and display it in the list', async ({ page }) => {
    await registerAndLogin(page);

    const taskTitle = `Task E2E - ${Date.now()}`;
    const taskDesc = 'A test task created via Playwright E2E test';

    await expect(page.getByRole('heading', { name: 'Adaugă un Task nou' })).toBeVisible();
    await page.getByPlaceholder('Ce ai de făcut astăzi?').fill(taskTitle);
    await page.getByPlaceholder('Adaugă detalii sau descriere').fill(taskDesc);
    await page.getByRole('button', { name: '+ Adaugă în listă' }).click();

    await expect(page.getByText(taskTitle)).toBeVisible();
    await expect(page.getByText(taskDesc)).toBeVisible();
    await expect(page.getByText('1 task')).toBeVisible();
  });

  test('should toggle task completion state', async ({ page }) => {
    await registerAndLogin(page);

    const taskTitle = `Toggle Task - ${Date.now()}`;
    await page.getByPlaceholder('Ce ai de făcut astăzi?').fill(taskTitle);
    await page.getByRole('button', { name: '+ Adaugă în listă' }).click();

    // Click the task card to toggle
    await page.getByText(taskTitle).click();

    // Verify state changed to "✓ Gata"
    await expect(page.getByText('✓ Gata')).toBeVisible();

    // Click again to toggle back
    await page.getByText(taskTitle).click();
    await expect(page.getByText('⏳ În lucru')).toBeVisible();
  });

  test('should delete a task', async ({ page }) => {
    await registerAndLogin(page);

    const taskTitle = `Delete Task - ${Date.now()}`;
    await page.getByPlaceholder('Ce ai de făcut astăzi?').fill(taskTitle);
    await page.getByRole('button', { name: '+ Adaugă în listă' }).click();
    await expect(page.getByText(taskTitle)).toBeVisible();

    // Accept the window.confirm() dialog that fires before deletion
    page.once('dialog', async (dialog) => {
      await dialog.accept();
    });

    // Click the delete button — it's not in AX tree (no accessible name)
    // Use the task title to find the card, then click the last button
    const taskRow = page.locator('div').filter({ hasText: taskTitle });
    await taskRow.locator('button').count();
    // Click the last button (delete) — first is AI Split, last is delete
    await taskRow.locator('button').last().click();

    // Wait for the task to be removed from the DOM
    await expect(page.getByText(taskTitle)).not.toBeVisible({ timeout: 10000 });
  });

  test('should sort tasks with completed ones at the bottom', async ({ page }) => {
    await registerAndLogin(page);

    const task1 = `Sort Task A - ${Date.now()}`;
    const task2 = `Sort Task B - ${Date.now()}`;

    // Create task 1
    await page.getByPlaceholder('Ce ai de făcut astăzi?').fill(task1);
    await page.getByRole('button', { name: '+ Adaugă în listă' }).click();
    await expect(page.getByText(task1)).toBeVisible();

    // Small delay to ensure the first task is fully persisted and rendered
    await page.waitForTimeout(300);

    // Create task 2
    await page.getByPlaceholder('Ce ai de făcut astăzi?').fill(task2);
    await page.getByRole('button', { name: '+ Adaugă în listă' }).click();
    await expect(page.getByText(task2)).toBeVisible();

    // Verify both tasks are visible
    await expect(page.getByText(task1)).toBeVisible();
    await expect(page.getByText(task2)).toBeVisible();

    // Toggle task 1 to completed
    await page.getByText(task1).click();

    // Wait for the API call and re-render to complete
    await page.waitForTimeout(500);

    // Verify sorting: completed tasks go to bottom by checking visual position
    const headingA = page.getByRole('heading', { name: /Sort Task A/ });
    const headingB = page.getByRole('heading', { name: /Sort Task B/ });

    // Get bounding boxes to determine visual order (top = first)
    const boxA = await headingA.boundingBox();
    const boxB = await headingB.boundingBox();

    // The uncompleted task (B) should have a smaller Y (higher on page) than completed (A)
    expect(boxB!.y).toBeLessThan(boxA!.y);
  });
});
