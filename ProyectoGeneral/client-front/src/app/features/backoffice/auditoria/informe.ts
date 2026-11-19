export interface Movimiento {
  id: number;
  fecha: string;
  tipo: 'VENTA' | 'HISTORICA' | 'AJUSTE' | 'ANULACION' | 'ALTA' | 'BAJA' | 'STOCK';
  referencia: string;
  recursoId: number;
  clienteId: number | null;
  unidades: number;
  importe: number | null;
  stockFinal: number | null;
}

export interface Informe {
  desde: string;
  hasta: string;
  consulta: string;
  movimientos: Movimiento[];
  inventario: { id: number; nombre: string; categoria: string; stock: number }[];
}

const fechaLima = new Intl.DateTimeFormat('sv-SE', {
  timeZone: 'America/Lima',
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
});
export function diaLima(fecha: Date) {
  return fechaLima.format(fecha);
}

export function resumen(datos: Informe) {
  let centimos = 0,
    ventas = 0,
    anulaciones = 0;
  const dias = new Map<string, number>();
  for (const m of datos.movimientos) {
    if (m.importe === null) continue;
    const dia = diaLima(new Date(m.fecha));
    const cambio = Math.round(m.importe * 100);
    centimos += cambio;
    dias.set(dia, (dias.get(dia) ?? 0) + cambio);
    if (m.tipo === 'VENTA' || m.tipo === 'HISTORICA') ventas++;
    if (m.tipo === 'ANULACION') anulaciones++;
  }
  const serie = [...dias].sort(([a], [b]) => a.localeCompare(b));
  const maximo = Math.max(1, ...serie.map(([, valor]) => Math.abs(valor)));
  return {
    neto: centimos / 100,
    ventas,
    anulaciones,
    dias: serie.map(([dia, valor]) => ({
      dia,
      importe: valor / 100,
      ancho: (100 * Math.abs(valor)) / maximo,
    })),
    unidades: datos.inventario.reduce((total, p) => total + p.stock, 0),
    agotados: datos.inventario.filter((p) => p.stock === 0).length,
  };
}
