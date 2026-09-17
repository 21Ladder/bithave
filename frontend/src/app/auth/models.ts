export type UserRole = 'CUSTOMER' | 'ADMIN';

export interface CurrentUser {
  email: string;
  role: UserRole;
}

export interface Credentials {
  email: string;
  password: string;
}
