package com.utp.tienda.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utp.tienda.config.JwtConfig;
import com.utp.tienda.config.SecurityConfig;
import com.utp.tienda.exception.ApiExceptionHandler;
import com.utp.tienda.exception.ReglaNegocioException;
import com.utp.tienda.model.Producto;
import com.utp.tienda.service.ProductoService;

@WebMvcTest(ProductoController.class)
@Import({ ApiExceptionHandler.class, SecurityConfig.class, JwtConfig.class })
@WithMockUser(roles = "ADMIN")
class ProductoControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProductoService service;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void getBuscarDebeFiltrarPorNombre() throws Exception {
		Producto laptop = new Producto("Laptop Lenovo ThinkPad", "Tecnologia", new BigDecimal("4200.00"), 8);
		laptop.setId(1L);
		when(service.buscarPorNombre(eq("lap"))).thenReturn(List.of(laptop));

		mockMvc.perform(get("/api/productos/buscar").param("texto", "lap"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Laptop Lenovo ThinkPad"));
	}

	@Test
	void getRangoJpqlDebeRetornarProductosFiltrados() throws Exception {
		Producto mouse = new Producto("Mouse Logitech MX", "Tecnologia", new BigDecimal("320.00"), 20);
		when(service.buscarPorRango(new BigDecimal("100"), new BigDecimal("1000")))
				.thenReturn(List.of(mouse));

		mockMvc.perform(get("/api/productos/rango")
				.param("min", "100")
				.param("max", "1000"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nombre").value("Mouse Logitech MX"));
	}

	@Test
	void getPrecioInvertidoDebeRetornar400() throws Exception {
		when(service.buscarPorRango(new BigDecimal("2000"), new BigDecimal("100")))
				.thenThrow(new ReglaNegocioException("El precio minimo no puede superar al maximo"));

		mockMvc.perform(get("/api/productos/precio")
				.param("min", "2000")
				.param("max", "100"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void postSalidaDebeRetornar200() throws Exception {
		Producto actualizado = new Producto("Laptop Lenovo ThinkPad", "Tecnologia", new BigDecimal("4200.00"), 6);
		actualizado.setId(1L);
		when(service.registrarSalida(1L, 2)).thenReturn(actualizado);

		mockMvc.perform(post("/api/productos/1/salidas").param("cantidad", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.stock").value(6));
	}

	@Test
	void postSimularErrorDebeRetornar500() throws Exception {
		doThrow(new IllegalStateException("Error simulado: la transaccion debe hacer rollback"))
				.when(service).simularSalidaConError(1L, 1);

		mockMvc.perform(post("/api/productos/1/salidas/simular-error").param("cantidad", "1"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.status").value(500));
	}
}
