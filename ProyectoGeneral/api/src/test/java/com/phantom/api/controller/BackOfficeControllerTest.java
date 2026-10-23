package com.phantom.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantom.api.dto.*;
import com.phantom.api.dto.request.UsuarioRequestDTO;
import com.phantom.api.dto.request.VentaRequestDTO;
import com.phantom.api.exception.GlobalExceptionHandler;
import com.phantom.api.service.*;
import com.phantom.api.support.WebMvcSecurityTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static com.phantom.api.support.JwtTestUtils.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comportamiento del back-office. La matriz completa de permisos está en SecurityEndpointsTest;
 * aquí se comprueba lo que hace cada operación y que el servicio NO se ejecuta si no hay permiso.
 */
@WebMvcTest(BackOfficeController.class)
@Import({GlobalExceptionHandler.class, WebMvcSecurityTestConfig.class})
class BackOfficeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CategoriaService categoriaService;
    @MockBean private EtiquetaService etiquetaService;
    @MockBean private ProductoService productoService;
    @MockBean private UsuarioService usuarioService;
    @MockBean private PerfilService perfilService;
    @MockBean private VentaService ventaService;
    @MockBean private DetalleVentaService detalleVentaService;

    private static final String PRODUCTO = "{\"nombre\":\"Laptop\",\"precio\":10.50,\"stock\":3,\"categoriaId\":1}";

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    // ------------------------------------------------------------ catálogo

    @Test
    @DisplayName("un TRABAJADOR puede crear y editar productos")
    void trabajadorCreaYEditaProductos() throws Exception {
        when(productoService.crear(any())).thenReturn(ProductoDTO.builder().id(7L).nombre("Laptop").build());
        when(productoService.actualizar(eq(7L), any())).thenReturn(ProductoDTO.builder().id(7L).build());

        mockMvc.perform(post("/api/v1/backoffice/productos").with(trabajador())
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(7)));
        mockMvc.perform(put("/api/v1/backoffice/productos/7").with(trabajador())
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCTO))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("eliminar del catálogo es solo ADMIN: el TRABAJADOR recibe 403 y el servicio no se ejecuta")
    void eliminarCatalogoSoloAdmin() throws Exception {
        for (String ruta : List.of("productos", "categorias", "etiquetas")) {
            mockMvc.perform(delete("/api/v1/backoffice/" + ruta + "/1").with(trabajador()))
                    .andExpect(status().isForbidden());
            mockMvc.perform(delete("/api/v1/backoffice/" + ruta + "/1").with(admin()))
                    .andExpect(status().isNoContent());
        }

        verify(productoService, times(1)).eliminar(1L);
        verify(categoriaService, times(1)).eliminar(1L);
        verify(etiquetaService, times(1)).eliminar(1L);
    }

    @Test
    @DisplayName("crear categoría: valida el cuerpo (400) y exige personal (comprador 403, anónimo 401)")
    void crearCategoria() throws Exception {
        when(categoriaService.crear(any())).thenReturn(CategoriaDTO.builder().id(1L).nombre("Hogar").build());
        String valido = json(Map.of("nombre", "Hogar"));

        mockMvc.perform(post("/api/v1/backoffice/categorias").with(trabajador())
                .contentType(MediaType.APPLICATION_JSON).content(valido))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/backoffice/categorias").with(admin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validación Fallida")));
        mockMvc.perform(post("/api/v1/backoffice/categorias").with(user(5))
                .contentType(MediaType.APPLICATION_JSON).content(valido))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/backoffice/categorias")
                .contentType(MediaType.APPLICATION_JSON).content(valido))
                .andExpect(status().isUnauthorized());

        verify(categoriaService, times(1)).crear(any());
    }

    // ------------------------------------------------------------ ventas

    @Test
    @DisplayName("un TRABAJADOR lista ventas y registra una venta de mostrador para un cliente")
    void ventaDeMostrador() throws Exception {
        when(ventaService.obtenerTodas()).thenReturn(List.of(VentaDTO.builder().id(1L).build()));
        when(ventaService.crear(any())).thenReturn(VentaDTO.builder().id(2L).build());

        mockMvc.perform(get("/api/v1/backoffice/ventas").with(trabajador()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(post("/api/v1/backoffice/ventas").with(trabajador())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioId\":9,\"detalles\":[{\"productoId\":1,\"cantidad\":2}]}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<VentaRequestDTO> captor = ArgumentCaptor.forClass(VentaRequestDTO.class);
        verify(ventaService).crear(captor.capture());
        assertEquals(9L, captor.getValue().getUsuarioId());
    }

    @Test
    @DisplayName("venta de mostrador sin cliente (usuarioId omitido) es válida")
    void ventaSinCliente() throws Exception {
        when(ventaService.crear(any())).thenReturn(VentaDTO.builder().id(3L).build());

        mockMvc.perform(post("/api/v1/backoffice/ventas").with(trabajador())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"detalles\":[{\"productoId\":1,\"cantidad\":1}]}"))
                .andExpect(status().isCreated());

        ArgumentCaptor<VentaRequestDTO> captor = ArgumentCaptor.forClass(VentaRequestDTO.class);
        verify(ventaService).crear(captor.capture());
        assertNull(captor.getValue().getUsuarioId());
    }

    @Test
    @DisplayName("anular una venta y editar sus líneas es solo ADMIN")
    void anularYEditarLineasSoloAdmin() throws Exception {
        String linea = "{\"productoId\":1,\"cantidad\":1}";

        mockMvc.perform(delete("/api/v1/backoffice/ventas/1").with(trabajador())).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/backoffice/ventas/1/detalles").with(trabajador())
                .contentType(MediaType.APPLICATION_JSON).content(linea)).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/backoffice/detalles/1").with(trabajador())).andExpect(status().isForbidden());
        verifyNoInteractions(detalleVentaService);
        verify(ventaService, never()).eliminar(any());

        mockMvc.perform(delete("/api/v1/backoffice/ventas/1").with(admin())).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/v1/backoffice/detalles/1").with(admin())).andExpect(status().isNoContent());
        verify(ventaService).eliminar(1L);
        verify(detalleVentaService).eliminar(1L);
    }

    // ------------------------------------------------------------ usuarios (solo ADMIN)

    @Test
    @DisplayName("la gestión de usuarios y perfiles es solo ADMIN: el TRABAJADOR no ve datos personales")
    void usuariosSoloAdmin() throws Exception {
        when(usuarioService.obtenerTodas()).thenReturn(List.of(UsuarioDTO.builder().id(1L).build()));

        mockMvc.perform(get("/api/v1/backoffice/usuarios").with(trabajador())).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/backoffice/perfiles").with(trabajador())).andExpect(status().isForbidden());
        verifyNoInteractions(perfilService);

        mockMvc.perform(get("/api/v1/backoffice/usuarios").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("el ADMIN da de alta personal: crea un usuario con rol TRABAJADOR")
    void adminCreaTrabajador() throws Exception {
        when(usuarioService.crear(any())).thenReturn(UsuarioDTO.builder().id(10L).rol("TRABAJADOR").build());

        mockMvc.perform(post("/api/v1/backoffice/usuarios").with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Rosa\",\"email\":\"rosa@x.com\",\"password\":\"Secreta123\",\"rol\":\"TRABAJADOR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol", is("TRABAJADOR")));

        ArgumentCaptor<UsuarioRequestDTO> captor = ArgumentCaptor.forClass(UsuarioRequestDTO.class);
        verify(usuarioService).crear(captor.capture());
        assertEquals("TRABAJADOR", captor.getValue().getRol());
    }

    @Test
    @DisplayName("un rol inexistente se rechaza con 400")
    void rolInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/backoffice/usuarios").with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Rosa\",\"email\":\"rosa@x.com\",\"password\":\"Secreta123\",\"rol\":\"ROOT\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }
}
