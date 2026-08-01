import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright test configuration for TaskBuddy frontend E2E tests.
 *
 * Usage:
 *   npx playwright test                    # run all tests
 *   npx playwright test --ui               # interactive UI mode
 *   npx playwright test tests/e2e/login.spec.ts  # single file
 *   npx playwright test --headed           # see the browser
 *   npx playwright test --debug            # debug mode (slow-mo + inspector)
 */
export default defineConfig({
  testDir: './tests/e2e',
  testMatch: /.*\.spec\.ts/,
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',

  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  // Start Vite dev server before running tests (runs in background)
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
    stdout: 'pipe',
    stderr: 'pipe',
  },
});
