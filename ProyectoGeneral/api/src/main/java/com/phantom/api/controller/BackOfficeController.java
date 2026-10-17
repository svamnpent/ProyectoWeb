package com.phantom.api.controller;

import com.phantom.api.dto.*;
import com.phantom.api.dto.request.*;
import com.phantom.api.security.Roles;
import com.phantom.api.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BACK-OFFICE: todas las operaciones INTERNAS de la tienda, separadas de lo que hace el comprador.
 *
 * Todo cuelga de /api/v1/backoffice y exige ser personal de la tienda (ADMIN o TRABAJADOR);
 * SecurityConfig lo valida por URL y aquí, por método, se afina quién puede cada cosa:
 *
 * <pre>
 *  Recurso                         TRABAJADOR   ADMIN
 *  ------------------------------  -----------  -----
 *  Catálogo: crear / editar            si         si
 *  Catálogo: eliminar                  no         si
 *  Ventas: listar / ver / mostrador    si         si     (venta de mostrador = POST /ventas)
 *  Ventas: anular (repone stock)       no         si
 *  Líneas de venta: ver                si         si
 *  Líneas de venta: agregar / quitar   no         si
 *  Usuarios (todo) y perfiles          no         si     (datos personales y roles)
 * </pre>
 *
 * Para cambiar un permiso basta con cambiar la constante de la anotación de ese método.
 * Lo que hace el comprador (catálogo, su cuenta, sus compras) está en los demás controllers.
 */
@Tag(name = "Back-office", description = "Operaciones internas: solo ADMIN o TRABAJADOR")
@RestController
@RequestMapping("/api/v1/backoffice")
@RequiredArgsConstructor
@PreAuthorize(BackOfficeController.PERSONAL)
public class BackOfficeController {

    /** ADMIN o TRABAJADOR. */
    static final String PERSONAL = Roles.HAS_STAFF;
    /** Solo ADMIN. */
    static final String SOLO_ADMIN = Roles.HAS_ADMIN;

    private final CategoriaService categoriaService;
    private final EtiquetaService etiquetaService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final PerfilService perfilService;
    private final VentaService ventaService;
    private final DetalleVentaService detalleVentaService;

    // ======================================================================= PRODUCTOS

    @PostMapping("/productos")
    public ResponseEntity<ProductoDTO> crearProducto(@Valid @RequestBody ProductoRequestDTO request) {
        return new ResponseEntity<>(productoService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(@PathVariable Long id,
                                                          @Valid @RequestBody ProductoRequestDTO request) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    @PreAuthorize(SOLO_ADMIN)
    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ======================================================================= CATEGORÍAS

    @PostMapping("/categorias")
    public ResponseEntity<CategoriaDTO> crearCategoria(@Valid @RequestBody CategoriaRequestDTO request) {
        return new ResponseEntity<>(categoriaService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/categorias/{id}")
    public ResponseEntity<CategoriaDTO> actualizarCategoria(@PathVariable Long id,
                                                            @Valid @RequestBody CategoriaRequestDTO request) {
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    @PreAuthorize(SOLO_ADMIN)
    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ======================================================================= ETIQUETAS

    @PostMapping("/etiquetas")
    public ResponseEntity<EtiquetaDTO> crearEtiqueta(@Valid @RequestBody EtiquetaRequestDTO request) {
        return new ResponseEntity<>(etiquetaService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/etiquetas/{id}")
    public ResponseEntity<EtiquetaDTO> actualizarEtiqueta(@PathVariable Long id,
                                                          @Valid @RequestBody EtiquetaRequestDTO request) {
        return ResponseEntity.ok(etiquetaService.actualizar(id, request));
    }

    @PreAuthorize(SOLO_ADMIN)
    @DeleteMapping("/etiquetas/{id}")
    public ResponseEntity<Void> eliminarEtiqueta(@PathVariable Long id) {
        etiquetaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ======================================================================= VENTAS

    @GetMapping("/ventas")
    public ResponseEntity<List<VentaDTO>> listarVentas() {
        return ResponseEntity.ok(ventaService.obtenerTodas());
    }

    @GetMapping("/ventas/{id}")
    public ResponseEntity<VentaDTO> obtenerVenta(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }

    /**
     * Venta de mostrador: el personal registra la compra de un cliente (usuarioId) o una venta sin
     * cliente (usuarioId omitido). Descuenta stock igual que una compra online.
     */
    @PostMapping("/ventas")
    public ResponseEntity<VentaDTO> registrarVenta(@Valid @RequestBody VentaRequestDTO request) {
        return new ResponseEntity<>(ventaService.crear(request), HttpStatus.CREATED);
    }

    /** Anula la venta y repone el stock. */
    @PreAuthorize(SOLO_ADMIN)
    @DeleteMapping("/ventas/{id}")
    public ResponseEntity<Void> eliminarVenta(@PathVariable Long id) {
        ventaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ======================================================================= LÍNEAS DE VENTA

    @GetMapping("/ventas/{ventaId}/detalles")
    public ResponseEntity<List<DetalleVentaDTO>> listarDetalles(@PathVariable Long ventaId) {
        return ResponseEntity.ok(detalleVentaService.obtenerPorVentaId(ventaId));
    }

    @PreAuthorize(SOLO_ADMIN)
    @PostMapping("/ventas/{ventaId}/detalles")
    public ResponseEntity<DetalleVentaDTO> agregarDetalle(@PathVariable Long ventaId,
                                                          @Valid @RequestBody DetalleVentaRequestDTO request) {
        return new ResponseEntity<>(detalleVentaService.crear(ventaId, request), HttpStatus.CREATED);
    }

    @PreAuthorize(SOLO_ADMIN)
    @DeleteMapping("/detalles/{id}")
    public ResponseEntity<Void> eliminarDetalle(@PathVariable Long id) {
        detalleVentaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ======================================================================= USUARIOS (solo ADMIN)

    @PreAuthorize(SOLO_ADMIN)
    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodas());
    }

    @PreAuthorize(SOLO_ADMIN)
    @GetMapping("/usuarios/buscar")
    public ResponseEntity<UsuarioDTO> buscarUsuarioPorEmail(@RequestParam String email) {
        return ResponseEntity.ok(usuarioService.buscarPorEmail(email));
    }

    @PreAuthorize(SOLO_ADMIN)
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioDTO> obtenerUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    /** Crea usuarios con cualquier rol (USER, TRABAJADOR o ADMIN): así se da de alta al personal. */
    @PreAuthorize(SOLO_ADMIN)
    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioDTO> crearUsuario(@Valid @RequestBody UsuarioRequestDTO request) {
        return new ResponseEntity<>(usuarioService.crear(request), HttpStatus.CREATED);
    }

    /** Edita cualquier usuario, incluido su rol (el servicio solo permite cambiar roles a un ADMIN). */
    @PreAuthorize(SOLO_ADMIN)
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(@PathVariable Long id,
                                                        @Valid @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @PreAuthorize(SOLO_ADMIN)
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ======================================================================= PERFILES (solo ADMIN)

    @PreAuthorize(SOLO_ADMIN)
    @GetMapping("/perfiles")
    public ResponseEntity<List<PerfilDTO>> listarPerfiles() {
        return ResponseEntity.ok(perfilService.obtenerTodos());
    }
}
