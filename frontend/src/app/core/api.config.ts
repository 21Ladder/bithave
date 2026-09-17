import { InjectionToken } from '@angular/core';

// Base URL for the API, so the host is not baked into the nginx image
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => '/api/v1',
});
