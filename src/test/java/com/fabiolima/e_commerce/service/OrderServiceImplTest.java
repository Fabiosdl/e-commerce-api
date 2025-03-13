package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.exceptions.ForbiddenException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.*;
import com.fabiolima.e_commerce.model.enums.OrderStatus;
import com.fabiolima.e_commerce.repository.OrderRepository;
import com.fabiolima.e_commerce.repository.ProductRepository;
import com.fabiolima.e_commerce.service.implementation.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.fabiolima.e_commerce.model.enums.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderServiceImplTest {

    @MockitoBean
    private OrderRepository orderRepository;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private ProductRepository productRepository;

    @Autowired
    private OrderServiceImpl orderService;

    @Test
    void createOrderAndAddToUser_ShouldReturnTheLastAddedOrder() {
        //given
        Long userId = 1L;

        Basket basket = new Basket();
        User user = new User();
        user.setId(userId);
        basket.setUser(user);

        //creating orders to add to orders list in user entity
        Order order1 = new Order();
        Order order2 = new Order();
        user.addOrderToUser(order1);
        user.addOrderToUser(order2);

        when(userService.findUserByUserId(anyLong())).thenReturn(user);

        //mock converting basket to order
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order argumentOrder = invocation.getArgument(0);
            argumentOrder.setId(1L);
            return argumentOrder;
        });

        //when
        Order actualOrder = orderService.createOrderAndAddToUser(userId, basket);

        //then
        assertAll(
                () -> assertNotNull(actualOrder),
                () -> assertEquals(PENDING, actualOrder.getOrderStatus()),
                () -> assertTrue(user.getOrders().contains(actualOrder))
        );

    }

    @Test
    void convertBasketToOrder_ShouldReturnNewOrderCreated_WhenBasketDoeNotHaveAnOrder(){
        // Given
        Product product1 = Product.builder().id(1L).productName("Product A")
                .productPrice(new BigDecimal("10.00"))
                .build();
        Product product2 = Product.builder().id(2L).productName("Product B")
                .productPrice(new BigDecimal("20.00"))
                .build();

        Basket basket = new Basket();
        User user = new User();
        basket.setUser(user);
        basket.addBasketItemToBasket(BasketItem.builder().id(1L).product(product1).quantity(2).build());
        basket.addBasketItemToBasket(BasketItem.builder().id(2L).product(product2).quantity(1).build());

        Order savedOrder = new Order();
        savedOrder.setUser(user);
        savedOrder.setBasket(basket);
        savedOrder.setItems(new ArrayList<>());

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order argumentOrder = invocation.getArgument(0);
            argumentOrder.setId(1L); // simulate DB assigning an Id
            return argumentOrder;
        });

        // When
        Order result = orderService.convertBasketToOrder(basket);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        assertEquals(new BigDecimal("40.00"), result.getTotalPrice()); // 10*2 + 20*1
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void convertBasketToOrder_ShouldReturnOrderWithNewItems_WhenBasketHaveAnOrder(){
        // Given
        Product product = Product.builder().id(1L).productName("Product C")
                .productPrice(new BigDecimal("15.00"))
                .build();

        Basket basket = new Basket();
        User user = new User();
        Order existingOrder = new Order();
        existingOrder.setUser(user);
        existingOrder.setBasket(basket);
        existingOrder.setItems(new ArrayList<>()); // Existing order has items (to be cleared)

        basket.setUser(user);
        basket.setOrder(existingOrder);
        basket.addBasketItemToBasket(BasketItem.builder().id(1L).product(product).quantity(3).build());

        //mock creating a new order
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order argumentOrder = invocation.getArgument(0);
            argumentOrder.setId(1L);
            return argumentOrder;
        });

        // When
        Order result = orderService.convertBasketToOrder(basket);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(new BigDecimal("45.00"), result.getTotalPrice()); // 15 * 3

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void returnNewestPendingOrder_ShouldReturnNewestPendingOrder(){
        // Given
        Order order1 = Order.builder()
                .id(1L).orderStatus(PENDING)
                .createdAt(LocalDateTime.now().minusDays(1)).build();

        Order order2 = Order.builder()
                .id(2L).orderStatus(PENDING)
                .createdAt(LocalDateTime.now().minusDays(2)).build();

        Order order3 = Order.builder()
                .id(3L).orderStatus(PENDING)
                .createdAt(LocalDateTime.now().minusDays(3)).build();

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.addOrderToUser(order3);
        user.addOrderToUser(order1);
        user.addOrderToUser(order2);

        when(userService.findUserByUserId(userId)).thenReturn(user);

        // When

        Order actualOrder = orderService.returnNewestPendingOrder(userId);

        // Then
        assertNotNull(actualOrder);
        assertEquals(PENDING, actualOrder.getOrderStatus());
        assertEquals(1L,actualOrder.getId()); //order with id = 1 is the newest order

        verify(userService, times(1)).findUserByUserId(userId);
    }

    @Test
    void returnNewestPendingOrder_ShouldThrowNotFoundException_WhenTheresNoPendingOrder() {
        // Given
        Order order1 = Order.builder()
                .id(1L).orderStatus(PAID)
                .createdAt(LocalDateTime.now().minusDays(1)).build();

        Order order2 = Order.builder()
                .id(2L).orderStatus(CANCELLED)
                .createdAt(LocalDateTime.now().minusDays(2)).build();

        Order order3 = Order.builder()
                .id(3L).orderStatus(COMPLETED)
                .createdAt(LocalDateTime.now().minusDays(3)).build();

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.addOrderToUser(order3);
        user.addOrderToUser(order1);
        user.addOrderToUser(order2);

        when(userService.findUserByUserId(userId)).thenReturn(user);

        // When

        Executable executalbe = () -> orderService.returnNewestPendingOrder(userId);

        // Then
        assertThrows(NotFoundException.class, executalbe, "Order with status PENDING not found");

        verify(userService, times(1)).findUserByUserId(userId);
    }

    @Test
    void getUserOrders_ShouldReturnListOfOrders_WhenUserExists() {

        //given
        Long userId = 1L;
        int pgNum = 0;
        int pgSize = 2;

        User user = new User();
        user.setId(userId);
        user.addOrderToUser(Order.builder().id(1L).build());
        user.addOrderToUser(Order.builder().id(2L).build());

        Pageable pageable = PageRequest.of(pgNum, pgSize);
        Page<Order> orderPage = new PageImpl<>(user.getOrders(), pageable, user.getOrders().size());

        when(userService.findUserByUserId(userId)).thenReturn(user);
        when(orderRepository.findAllByUserId(userId, pageable))
                .thenReturn(orderPage);

        // When
        Page<Order> result = orderService.getUserOrders(pgNum, pgSize, userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals(2L, result.getContent().get(1).getId());

        verify(userService,times(1)).findUserByUserId(1L);

    }

    @ParameterizedTest
    @CsvSource({
            "CANCELLED",
            "PENDING",
            "PAID",
            "COMPLETED",
            "cancelled"
    })
    void getUserOrdersByStatus_ShouldReturnListOfOrdersWithJustOneTypeOfStatus_WhenStatusIsValid(String status) {
        //given
        //create user to suffice userService.findUserById method
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        //add different orders with different status to a user to create a list of orders with different status
        Order order1 = new Order();
        order1.setOrderStatus(CANCELLED);
        user.addOrderToUser(order1);

        Order order2 = new Order();
        order2.setOrderStatus(PENDING);
        user.addOrderToUser(order2);

        Order order3 = new Order();
        order3.setOrderStatus(PENDING);
        user.addOrderToUser(order3);

        Order order4 = new Order();
        order4.setOrderStatus(PAID);
        user.addOrderToUser(order4);

        Order order5 = new Order();
        order5.setOrderStatus(PAID);
        user.addOrderToUser(order5);

        Order order6 = new Order();
        order6.setOrderStatus(PAID);
        user.addOrderToUser(order6);

        Order order7 = new Order();
        order7.setOrderStatus(COMPLETED);
        user.addOrderToUser(order7);

        Order order8 = new Order();
        order8.setOrderStatus(COMPLETED);
        user.addOrderToUser(order8);

        //expected Page<Order>
        int pgNum = 0;
        int pgSize = 2;

        //mockito doesn't execute queries, so I have to do that manually
        List<Order> orderList = user.getOrders().stream()
                .filter(order -> OrderStatus.fromString(status).equals(order.getOrderStatus()))
                .toList();

        Pageable pageable = PageRequest.of(pgNum, pgSize);
        Page<Order> orderPage = new PageImpl<>(orderList,pageable,orderList.size());

        // mock method and return user
        when(orderRepository.findByOrderStatusAndUserId(OrderStatus.fromString(status), user.getId(), pageable))
                .thenReturn(orderPage);

        //when
        Page<Order> actualOrderPage = orderService.getUserOrdersByStatus(pgNum,pgSize,userId,status);

        //then
        assertNotNull(actualOrderPage);
        assertThat(actualOrderPage)
                .allSatisfy(order -> assertEquals(OrderStatus.fromString(status), order.getOrderStatus(),
                        "Found an order with incorrect status: " + order.getOrderStatus()));


        verify(orderRepository,times(1)).findByOrderStatusAndUserId(OrderStatus.fromString(status), user.getId(), pageable);

    }

    @Test
    void findOrderById_ShouldReturnTheOrderByItsId_WhenOrderExists() {
        //given
        Order order = new Order();

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        //when
        Order actualOrder = orderService.findOrderById(1L);

        //then
        assertEquals(order,actualOrder);
        verify(orderRepository,times(1)).findById(1L);
    }

    @Test
    void findOrderById_ShouldThrowNotFoundException_WhenOrderDoesNotExist(){

        //given
        when(orderRepository.findOrderByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.empty());

        //when
        Executable executable = () -> orderService.findOrderById(1L);

        //then
        assertThrows(NotFoundException.class,executable,"Order with Id 1 not found");
    }

    @ParameterizedTest
    @CsvSource({"PENDING, PAID",
            "PAID, CoMpLeTeD",
    }) /*currentStatus will always be uppercase, because it comes from the Enum OrderStatus
    which is all in uppercase*/
    void updateStatusOrder_ShouldReturnOrderWithNewStatus_WhenUpdateIsAllowedStatus(String currentStatus,
                                                                                    String newStatus) {
        //given

        //create new order
        Long orderId = 1L;
        Order expected = new Order();
        expected.setId(orderId);
        //set currentStatus to expected order
        expected.setOrderStatus(OrderStatus.valueOf(currentStatus));

        //mocking method findByIdId and returning the order, which contains the current status,
        // that will be used by the actual order to update to the new status
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(expected));

        when(orderRepository.save(expected)).thenReturn(expected);

        //when
        Order actual = orderService.updateOrderStatus(orderId, newStatus);

        //then
        //I'm allowing newStatus to uppercase to mimic the enum method
        assertEquals(OrderStatus.fromString(newStatus), actual.getOrderStatus());
        verify(orderRepository,times(1)).findById(orderId);
        verify(orderRepository,times(1)).save(expected);
    }

    @ParameterizedTest
    @CsvSource({"PENDING, COMPLETED, Current Status PENDING can only be updated to PAID or CANCELLED.",
            "PENDING, PENDING, Current Status PENDING can only be updated to PAID or CANCELLED.",
            "PAID, PENDING, Current Status PAID can only be updated to COMPLETED.",
            "PAID, CANCELLED, Current Status PAID can only be updated to COMPLETED.",
            "PAID, PAID, Current Status PAID can only be updated to COMPLETED.",
            "COMPLETED, PENDING, Current Status COMPLETED cannot be updated.",
            "CANCELLED, PENDING, Current Status CANCELLED cannot be updated."
    })
    void updateStatusOrder_ShouldThrowForbiddenException_WhenUpdateIsNotAllowedStatus(String currentStatus,
                                                                                      String newStatus,
                                                                                      String exceptionMessage) {
        //given

        //create new order
        Order expected = new Order();
        //set currentStatus to expected order
        expected.setOrderStatus(OrderStatus.valueOf(currentStatus));

        //mocking method findOrderByIdAndUserId and returning the order, which contains the current status, that will be used by the actual order to update to the new status
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(expected));

        //when
        Executable executable = () -> orderService.updateOrderStatus(1L, newStatus);

        //then
        assertThrows(ForbiddenException.class, executable, exceptionMessage);
        verify(orderRepository,times(1)).findById(1L);
    }

    @ParameterizedTest
    @CsvSource({"Test1",
            "Test2",
            "afafadfa"
    })
    void updateStatusOrder_ShouldThrowIllegalArguments_WhenNewStatusIsInvalid(String newStatus) {

        //when
        Executable executable = () -> orderService.updateOrderStatus(1L, newStatus);

        //then
        assertThrows(IllegalArgumentException.class, executable, "Invalid order status " + newStatus);
    }

    @Test
    void updateOrder_Status_ShouldThrowForbiddenException_WhenUpdatingFromPendingToCancelled() {

        //given
        User user = new User();
        Order order = new Order();
        order.setOrderStatus(PENDING);
        user.addOrderToUser(order);

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        //when
        Executable executable = () -> orderService.updateOrderStatus(1L, "cancelled");

        //then
        assertThrows(ForbiddenException.class, executable, "Please, use the method cancelOrder to cancel an order.");
    }

    @Test
    void cancelOrder_ShouldReturnOrderWithStatusCancelled_WhenCurrentStatusIsPending() {
        //given
        int quantity = 3;
        Order expected = new Order();
        expected.setOrderStatus(PENDING);
        expected.setId(1L);

        when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.of(expected));

        //mock incrementStocksWhenOrderIsCancelled
        Basket basket = new Basket();
        basket.setId(1L);
        basket.setOrder(expected);
        expected.setBasket(basket);

        basket.addBasketItemToBasket(BasketItem.builder().quantity(quantity)
                .product(Product.builder().stock(7)
                                .productPrice(new BigDecimal("4.50")).build()
                        ).build());
        when(productRepository.save(any(Product.class))).thenReturn(new Product());

        when(orderRepository.save(any())).thenReturn(expected);

        //when
        Order actual = orderService.cancelOrder(1L);

        //then
        assertNotNull(actual);
        assertEquals(expected, actual);
        assertEquals(CANCELLED,actual.getOrderStatus());
        verify(orderRepository,times(1)).findById(1L);
        verify(orderRepository,times(1)).save(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "COMPLETED",
            "PAID",
            "CANCELLED"
    })
    void cancelOrder_ShouldThrowForbiddenException_WhenCurrentStatusIsNotPending(String currentStatus) {
        //given
        Order expected = new Order();
        expected.setOrderStatus(OrderStatus.valueOf(currentStatus));

        when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.of(expected));

        //when
        Executable executable = () -> orderService.cancelOrder(1L);

        //then
        assertThrows(ForbiddenException.class, executable,"Only orders with status PENDING can be cancelled");
        verify(orderRepository,times(1)).findById(1L);

    }

}