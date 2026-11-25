package com.utp.productosapi.demo.repository; 
 
import static org.assertj.core.api.Assertions.assertThat; 
 
import org.junit.jupiter.api.Test; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest; 
 
import com.utp.productosapi.demo.model.Producto; 
 
@DataJpaTest 
class ProductoRepositoryTest { 
 
    @Autowired 
    private ProductoRepository repository; 
 
    @Test 
 void debeGuardarYRecuperarProducto() { 
        Producto producto = new Producto(null, "Monitor", 900.0, 5); 
 
        Producto guardado = repository.save(producto); 
 
        assertThat(guardado.getId()).isNotNull(); 
        assertThat(repository.findById(guardado.getId())).isPresent(); 
    } 
}