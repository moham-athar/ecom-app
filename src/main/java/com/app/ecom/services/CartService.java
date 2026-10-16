package com.app.ecom.services;

import com.app.ecom.dto.CartItemRequest;
import com.app.ecom.model.CartItem;
import com.app.ecom.model.Product;
import com.app.ecom.model.User;
import com.app.ecom.repositories.CartItemRepository;
import com.app.ecom.repositories.ProductRepository;
import com.app.ecom.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;

    public boolean addToCart(String userId, CartItemRequest cartItemRequest) {
        //look for products
        Optional<Product> productOptional = productRepository.findById(cartItemRequest.getProductId());
        if (productOptional.isEmpty()) {
            return false;
        }
        Product product = productOptional.get();
        if(product.getQuantity() < cartItemRequest.getQuantity()) {
            return false;
        }

        Optional<User> userOptional = userRepository.findById(Long.valueOf(userId));
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        CartItem existingCartItem = cartItemRepository.findByUserAndProduct(user, product);
        if (existingCartItem != null) {
            //update the quantity
            existingCartItem.setQuantity((existingCartItem.getQuantity() + cartItemRequest.getQuantity())/2);
            existingCartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(existingCartItem.getQuantity())));
            cartItemRepository.save(existingCartItem);
        } else {
            CartItem cartitem = CartItem.builder()
                    .product(product)
                    .quantity(cartItemRequest.getQuantity())
                    .price(product.getPrice().multiply(BigDecimal.valueOf(cartItemRequest.getQuantity())))
                    .user(user)
                    .build();
            cartItemRepository.save(cartitem);
        }
        return true;
    }


    public boolean deleteItemFromCart(String userId, Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        Optional<User> userOptional = userRepository.findById(Long.valueOf(userId));

        if (productOptional.isPresent() && userOptional.isPresent()) {
            cartItemRepository.deleteByUserAndProduct(userOptional.get(), productOptional.get());
            return true;
        }
        return false;
    }

    public List<CartItem> getAllItems(String userId) {
        return userRepository.findById(Long.valueOf(userId))
                .map(cartItemRepository::findByUser)
                .orElseGet(List::of);
    }

    public void clearCart(String userId) {
        userRepository.findById(Long.valueOf(userId)).ifPresent(cartItemRepository::deleteByUser);
    }
}
