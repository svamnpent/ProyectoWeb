package com.phantom.api.controller;

import com.phantom.api.exception.GlobalExceptionHandler;
import com.phantom.api.exception.StockInsuficienteException;
import com.phantom.api.service.VentaService;
import com.phantom.api.support.WebMvcSecurityTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.phantom.api.support.JwtTestUtils.admin;
import static com.phantom.api.support.JwtTestUtils.user;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que cada tipo de error produce el estado HTTP correcto con el formato ErrorResponse.
 * Antes de la seguridad, un AccessDeniedException o un JSON mal formado acababan en HTTP 500.
 */
@WebMvcTest({VentaController.class, BackOfficeController.class})
@Import({GlobalExceptionHandler.class, WebMvcSecurityTestConfig.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private VentaService ventaService;
    // Dependencias de BackOfficeController (no se usan en estos tests)
    @MockBean private com.phantom.api.service.CategoriaService categoriaService;
    @MockBean private com.phantom.api.service.EtiquetaService etiquetaService;
    @MockBean private com.phantom.api.service.ProductoService productoService;
    @MockBean private com.phantom.api.service.UsuarioService usuarioService;
    @MockBean private com.phantom.api.service.PerfilService perfilService;
    @MockBean private com.phantom.api.service.DetalleVentaService detalleVentaService;

    private static final String VENTA_VALIDA = "{\"detalles\":[{\"productoId\":1,\"cantidad\":2}]}";

    @Test
    @DisplayName("StockInsuficienteException -> 400 con todos los mensajes")
    void stockInsuficiente() throws Exception {
        when(ventaService.crear(any())).thenThrow(
                new StockInsuficienteException(List.of("Stock insuficiente para 'A'", "Stock insuficiente para 'B'")));

        mockMvc.perform(post("/api/v1/ventas").with(user(5))
                .contentType(MediaType.APPLICATION_JSON).content(VENTA_VALIDA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Stock Insuficiente")))
                .andExpect(jsonPath("$.message", containsString("'A'")))
                .andExpect(jsonPath("$.message", containsString("'B'")));
    }

    @Test
    @DisplayName("AccessDeniedException lanzada por un servicio -> 403 (no 500)")
    void accessDeniedDesdeServicio() throws Exception {
        when(ventaService.obtenerPorId(9L)).thenThrow(new AccessDeniedException("no es tu venta"));

        mockMvc.perform(get("/api/v1/ventas/9").with(user(5)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("Forbidden")))
                .andExpect(jsonPath("$.timestamp", org.hamcrest.Matchers.endsWith("-05:00")));
    }

    @Test
    @DisplayName("DataIntegrityViolationException -> 409 sin filtrar detalles internos de la BD")
    void integridadDeDatos() throws Exception {
        doThrow(new DataIntegrityViolationException("ERROR: update or delete on table \"productos\" violates fk"))
                .when(ventaService).eliminar(1L);

        mockMvc.perform(delete("/api/v1/backoffice/ventas/1").with(admin()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", not(containsString("productos"))));
    }

    @Test
    @DisplayName("JSON mal formado -> 400")
    void jsonMalFormado() throws Exception {
        mockMvc.perform(post("/api/v1/ventas").with(user(5))
                .contentType(MediaType.APPLICATION_JSON).content("{esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("ID con tipo incorrecto -> 400")
    void tipoIncorrecto() throws Exception {
        mockMvc.perform(get("/api/v1/ventas/abc").with(user(5)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Método no permitido -> 405")
    void metodoNoPermitido() throws Exception {
        mockMvc.perform(put("/api/v1/ventas/1").with(admin())
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Venta sin detalles -> 400 de validación")
    void ventaSinDetalles() throws Exception {
        mockMvc.perform(post("/api/v1/ventas").with(user(5))
                .contentType(MediaType.APPLICATION_JSON).content("{\"detalles\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validación Fallida")));
    }

    @Test
    @DisplayName("Cantidad menor a 1 -> 400 de validación")
    void cantidadInvalida() throws Exception {
        mockMvc.perform(post("/api/v1/ventas").with(user(5))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"detalles\":[{\"productoId\":1,\"cantidad\":0}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validación Fallida")));
    }
}
