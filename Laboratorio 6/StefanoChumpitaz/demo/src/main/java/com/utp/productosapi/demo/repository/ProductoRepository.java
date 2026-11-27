package com.utp.productosapi.demo.repository; 
 
import org.springframework.data.jpa.repository.JpaRepository; 
 
import com.utp.productosapi.demo.model.Producto; 
 
public interface ProductoRepository extends JpaRepository<Producto, Long> { 
}