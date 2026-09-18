package com.app.ecom.controllers;

import com.app.ecom.dto.CartItemRequest;
import com.app.ecom.model.CartItem;
import com.app.ecom.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;


    @PostMapping
    public ResponseEntity<String> addToCart(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CartItemRequest cartItemRequest
    ) {
        cartService.addToCart(userId, cartItemRequest);
        if (!cartService.addToCart(userId, cartItemRequest)) {
            return ResponseEntity.badRequest().body("Product out of stock or user not found");
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("productId") Long productId
    ) {
        boolean deleted = cartService.deleteItemFromCart(userId, productId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().build();
    }

    @GetMapping
    public ResponseEntity<List<CartItem>> getAllCartItems(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(cartService.getAllItems(userId));
    }
}
