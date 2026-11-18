import { Rol } from './rol';

export interface Perfil {
  id: number;
  avatarUrl: string | null;
  biografia: string | null;
  usuarioId: number;
}

export interface PerfilRequest {
  avatarUrl?: string;
  biografia?: string;
  usuarioId: number;
}

export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  rol: Rol;
  dni: string | null;
  direccion: string | null;
  numeroTelefono: string | null;
  fechaRegistro: string; // ISO-8601 con offset (-05:00)
  perfil: Perfil | null;
}

export interface UsuarioResumen {
  id: number;
  nombre: string;
  email: string;
}
