export interface Usuario {
  id: number;
  name: string;
  username: string;
  email: string;
  phone: string;
  website: string;
}

export type UsuarioEntrada = Omit<Usuario, 'id'>;
