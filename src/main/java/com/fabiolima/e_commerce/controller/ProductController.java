package com.fabiolima.e_commerce.controller;

import com.fabiolima.e_commerce.model.Product;
import com.fabiolima.e_commerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController (ProductService productService){
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product theProduct){
        Product createdProduct = productService.saveProduct(theProduct);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(@RequestParam(defaultValue = "0") int pgNum,
                                                        @RequestParam(defaultValue = "25") int pgSize) {
        Page<Product> allProducts = productService.findAllProducts(pgNum, pgSize);
        return ResponseEntity.ok(allProducts);
    }

    @GetMapping("/category")
    public ResponseEntity<Page<Product>> getProductsByCategory(@RequestParam(defaultValue = "0") int pgNum,
                                                               @RequestParam(defaultValue = "25") int pgSize,
                                                               @RequestParam("category") String category){
        Page<Product> productsByCategory = productService.findProductsByCategory(pgNum, pgSize, category);
        return ResponseEntity.ok(productsByCategory);
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
