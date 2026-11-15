import { afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';

// Mock scrollIntoView for JSDOM
window.HTMLElement.prototype.scrollIntoView = function () {};

afterEach(() => {
  cleanup();
});