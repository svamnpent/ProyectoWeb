import { diaLima, Informe, Movimiento, resumen } from './informe';

describe('Resumen comercial', () => {
  it('asigna la actividad al día de Lima', () => {
    expect(diaLima(new Date('2026-11-03T03:30:00Z'))).toBe('2026-11-02');
  });
  it('compensa ajustes y anulaciones sin contar movimientos de inventario como compras', () => {
    const base = {
      id: 1,
      fecha: '2026-11-03T03:30:00Z',
      referencia: 'A',
      recursoId: 1,
      clienteId: null,
      unidades: 1,
      stockFinal: null,
    };
    const movimientos: Movimiento[] = [
      { ...base, tipo: 'HISTORICA', importe: 10.15 },
      { ...base, tipo: 'VENTA', importe: 2.01 },
      { ...base, tipo: 'AJUSTE', importe: 1.12 },
      { ...base, tipo: 'ANULACION', importe: -3.13 },
      { ...base, tipo: 'STOCK', importe: null, stockFinal: 0 },
    ];
    const informe: Informe = {
      desde: '2026-11-01',
      hasta: '2026-11-03',
      consulta: base.fecha,
      movimientos,
      inventario: [{ id: 1, nombre: 'A', categoria: 'C', stock: 0 }],
    };
    expect(resumen(informe)).toMatchObject({
      neto: 10.15,
      ventas: 2,
      anulaciones: 1,
      agotados: 1,
      dias: [{ dia: '2026-11-02', importe: 10.15, ancho: 100 }],
    });
  });
});
