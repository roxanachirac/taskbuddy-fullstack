import { test, expect, Page } from '@playwright/test';

/**
 * Helper: accept the registration alert and return the username that was just registered.
 */
async function registerUser(page: Page, username: string, password: string): Promise<void> {
  // Accept any alert dialog that appears during registration
  page.on('dialog', async (dialog) => {
    await dialog.accept();
  });

  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'TaskBuddy' })).toBeVisible();

  // Switch to registration mode
  await page.getByText('Înregistrează-te').click();
  await expect(page.getByRole('heading', { name: 'Crează un cont nou' })).toBeVisible();

  await page.getByPlaceholder('Utilizator').fill(username);
  await page.getByPlaceholder('Parolă').fill(password);
  await page.getByRole('button', { name: 'Înregistrare' }).click();

  // After alert is accepted, we should see the login heading again
  await expect(page.getByRole('heading', { name: 'Autentifică-te' })).toBeVisible();
}

/**
 * Helper: login with existing credentials and verify dashboard.
 */
async function loginUser(page: Page, username: string): Promise<void> {
  await page.getByPlaceholder('Utilizator').fill(username);
  await page.getByPlaceholder('Parolă').fill('SecurePass123!');
  await page.getByRole('button', { name: 'Conectare' }).click();

  await expect(page.getByText(`Salut, ${username}`)).toBeVisible();
  await expect(page.getByRole('heading', { name: 'TaskBuddy' })).toBeVisible();
  await expect(page.getByText('Task-urile tale')).toBeVisible();
}

test.describe('Authentication Flow', () => {
  test('should register a new user successfully', async ({ page }) => {
    const uniqueUsername = `testuser-${Date.now()}`;
    await registerUser(page, uniqueUsername, 'SecurePass123!');
  });

  test('should login with valid credentials and show dashboard', async ({ page }) => {
    const uniqueUsername = `loginuser-${Date.now()}`;
    await registerUser(page, uniqueUsername, 'SecurePass123!');
    await loginUser(page, uniqueUsername);
  });

  test('should show error for invalid credentials', async ({ page }) => {
    await page.goto('/');

    await page.getByPlaceholder('Utilizator').fill('nonexistentuser');
    await page.getByPlaceholder('Parolă').fill('wrongpassword');
    await page.getByRole('button', { name: 'Conectare' }).click();

    // Verify error message is displayed (error shows as "⚠️ {message}")
    await expect(page.getByText(/Utilizatorul nu a fost găsit/i)).toBeVisible();
  });

  test('should logout and return to login view', async ({ page }) => {
    const uniqueUsername = `logoutuser-${Date.now()}`;
    await registerUser(page, uniqueUsername, 'SecurePass123!');
    await loginUser(page, uniqueUsername);

    // Logout
    await page.getByRole('button', { name: /Deconectare/ }).click();

    // Verify we're back to login view
    await expect(page.getByRole('heading', { name: 'Autentifică-te' })).toBeVisible();
    await expect(page.getByPlaceholder('Utilizator')).toBeVisible();
  });
});
