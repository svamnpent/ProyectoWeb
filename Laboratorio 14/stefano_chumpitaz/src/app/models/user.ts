export interface User {
  id: number;
  name: string;
  username: string;
  email: string;
  phone: string;
}

/** Datos que se envían al crear o actualizar (el id lo asigna el servidor). */
export type UserPayload = Omit<User, 'id'>;
