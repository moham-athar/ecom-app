package com.app.ecom.services;

import com.app.ecom.dto.OrderItemDTO;
import com.app.ecom.dto.OrderResponse;
import com.app.ecom.model.*;
import com.app.ecom.repositories.OrderRepository;
import com.app.ecom.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public Optional<OrderResponse> createOrder(String userId) {
        //validate for cartItem
        List<CartItem> cartItems = cartService.getAllItems(userId);
        if  (cartItems == null || cartItems.isEmpty()) {
            return Optional.empty();
        }
        //validate for user
        Optional<User> userOptional = userRepository.findById(Long.valueOf(userId));
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }
        User  user = userOptional.get();
        //Calculate total price
        BigDecimal totalPrice = cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //Create Order
        Order order = new Order();
        order.setUser(user);
        order.setTotal(totalPrice);
        order.setStatus(OrderStatus.CONFIRMED);

        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> new OrderItem(
                        null,
                        item.getProduct(),
                        item.getQuantity(),
                        item.getPrice(),
                        order
                )).toList();
        order.setItems(orderItems);
        Order savedOrder = orderRepository.saveAndFlush(order);
        //Clear the cart
        cartService.clearCart(userId);

        return Optional.of(mapToOrderResponse(savedOrder));
    }

    private OrderResponse mapToOrderResponse(Order savedOrder) {
        return  new OrderResponse(
                savedOrder.getOrderId(),
                savedOrder.getTotal(),
                savedOrder.getStatus(),
                savedOrder.getItems().stream()
                        .map(item -> new OrderItemDTO(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getQuantity(),
                                item.getPrice(),
                                item.getPrice().multiply(new BigDecimal(item.getQuantity()))
                        )).toList(),
                savedOrder.getCreatedAt()
        );
    }
}
