export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  nombre: string;
  email: string;
  password: string; // 8–72 caracteres
  dni?: string; // ^[0-9]{8,20}$
  direccion?: string;
  numeroTelefono?: string; // ^\+?[0-9]{7,15}$
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number; // segundos
}
