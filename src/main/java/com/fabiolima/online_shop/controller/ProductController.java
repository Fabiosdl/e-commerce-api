package com.fabiolima.online_shop.controller;

import com.fabiolima.online_shop.model.Product;
import com.fabiolima.online_shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product theProduct){
        Product createdProduct = productService.saveProduct(theProduct);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> allProducts = productService.findAllProducts();
        return ResponseEntity.ok(allProducts);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable ("productId") Long productId){
        Product theProduct = productService.findProductById(productId);
        return ResponseEntity.ok(theProduct);
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable ("productId") Long productId,
                                        @RequestBody Map<String, Object> updates){
        Product updatedProduct = productService.patchUpdateProductById(productId, updates);

        return ResponseEntity.ok(updatedProduct);
    }
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") Long productId){
        productService.deleteProductById(productId);
        return ResponseEntity.noContent().build();
    }
}
