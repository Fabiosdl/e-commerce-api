package com.fabiolima.e_commerce.service;

import com.fabiolima.e_commerce.exceptions.ForbiddenException;
import com.fabiolima.e_commerce.exceptions.NotFoundException;
import com.fabiolima.e_commerce.model.Order;
import com.fabiolima.e_commerce.model.User;
import com.fabiolima.e_commerce.model.enums.OrderStatus;
import com.fabiolima.e_commerce.repository.OrderRepository;
import com.fabiolima.e_commerce.service.implementation.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static com.fabiolima.e_commerce.model.enums.OrderStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderServiceImplTest {

    @MockitoBean
    private OrderRepository orderRepository;
    @MockitoBean
    private UserService userService;

    @Autowired
    private OrderServiceImpl orderService;

    /*@Test
    void saveOrderAndAddToUser_ShouldReturnTheLastAddedOrder() {
        //given

        User user = new User();
        //creating orders to add to orders list in user entity
        Order order1 = new Order();
        Order order2 = new Order();
        user.addOrderToUser(order1);
        user.addOrderToUser(order2);

        Order expectedOrder = new Order();

        when(userService.findUserByUserId(anyLong())).thenReturn(user);

        //Simulating saving the user and adding the expected order
        when(userService.saveUser(user)).thenAnswer(invocationOnMock -> {
            User savedUser = invocationOnMock.getArgument(0);
            //simulate order addition.
            savedUser.addOrderToUser(expectedOrder);
            return savedUser;
        });

        //when
        Order actualOrder = orderService.createOrderAndAddToUser(new Basket());

        //then
        assertAll(
                () -> assertNotNull(actualOrder),
                () -> assertEquals(expectedOrder, actualOrder),  // Ensure the returned Basket is the same one added to the User
                () -> assertTrue(user.getOrders().contains(expectedOrder))  // Ensure the Basket was added to the User's baskets
        );

        // Verify that userService.saveUser() was called exactly once
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).saveUser(userCaptor.capture());

    }*/
//
//    @Test
//    void getUserOrders_ShouldReturnListOfOrders_WhenUserExists() {
//        //given
//        User user = new User();
//        user.addOrderToUser(new TheOrder());
//        user.addOrderToUser(new TheOrder());
//
//        when(userService.findUserByUserId(anyLong())).thenReturn(user);
//        List<TheOrder> expectedOrders = user.getOrders();
//
//        //when
//        List<TheOrder> actualOrders = orderService.getUserOrders(1L);
//
//        //then
//        assertEquals(2,user.getOrders().size());
//        assertEquals(expectedOrders,actualOrders);
//        verify(userService,times(1)).findUserByUserId(1L);
//
//    }

    @Test
    void getUserOrderById_ShouldReturnTheOrderByItsId_WhenOrderBelongsToUser() {
        //given
        Order order = new Order();

        when(orderRepository.findOrderByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(order));

        //when
        Order actualOrder = orderService.getUserOrderById(1L,1L);

        //then
        assertEquals(order,actualOrder);
        verify(orderRepository,times(1)).findOrderByIdAndUserId(1L,1L);
    }

    @Test
    void getUserOrderById_ShouldThrowNotFoundException_WhenOrderDoesNotBelongToUser(){

        //given
        when(orderRepository.findOrderByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.empty());

        //when
        Executable executable = () -> orderService.getUserOrderById(1L,1L);

        //then
        assertThrows(NotFoundException.class,executable,"Order with Id 1 does not belong to User with Id 1");
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
        Order expected = new Order();
        //set currentStatus to expected order
        expected.setOrderStatus(OrderStatus.valueOf(currentStatus));

        //mocking method findOrderByIdAndUserId and returning the order, which contains the current status,
        // that will be used by the actual order to update to the new status
        when(orderRepository.findOrderByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(expected));

        when(orderRepository.save(expected)).thenReturn(expected);

        //when
        Order actual = orderService.updateOrderStatus(1L,1L, newStatus);

        //then
        //I'm allowing newStatus to uppercase to mimic the enum method
        assertEquals(OrderStatus.fromString(newStatus), actual.getOrderStatus());
        verify(orderRepository,times(1)).findOrderByIdAndUserId(1L,1L);
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
        when(orderRepository.findOrderByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(expected));

        //when
        Executable executable = () -> orderService.updateOrderStatus(1L,1L, newStatus);

        //then
        assertThrows(ForbiddenException.class, executable, exceptionMessage);
        verify(orderRepository,times(1)).findOrderByIdAndUserId(1L,1L);
    }

    @ParameterizedTest
    @CsvSource({"Test1",
            "Test2",
            "afafadfa"
    })
    void updateStatusOrder_ShouldThrowIllegalArguments_WhenNewIsInvalidStatus(String newStatus) {

        //when
        Executable executable = () -> orderService.updateOrderStatus(1L,1L, newStatus);

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

        when(orderRepository.findOrderByIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(order));

        //when
        Executable executable = () -> orderService.updateOrderStatus(1L,1L, "cancelled");

        //then
        assertThrows(ForbiddenException.class, executable, "Please, use the method cancelOrder to cancel an order.");
    }

    @Test
    void cancelOrder_ShouldReturnOrderWithStatusCancelled_WhenCurrentStatusIsPending() {
        //given
        Order expected = new Order();
        expected.setOrderStatus(PENDING);

        when(orderRepository.findOrderByIdAndUserId(anyLong(),anyLong()))
                .thenReturn(Optional.of(expected));
        when(orderRepository.save(any())).thenReturn(expected);

        //when
        Order actual = orderService.cancelOrder(1L,1L);

        //then
        assertNotNull(actual);
        assertEquals(expected, actual);
        assertEquals(CANCELLED,actual.getOrderStatus());
        verify(orderRepository,times(1)).findOrderByIdAndUserId(1L,1L);
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

        when(orderRepository.findOrderByIdAndUserId(anyLong(),anyLong()))
                .thenReturn(Optional.of(expected));

        //when
        Executable executable = () -> orderService.cancelOrder(1L,1L);

        //then
        assertThrows(ForbiddenException.class, executable,"Only orders with status PENDING can be cancelled");
        verify(orderRepository,times(1)).findOrderByIdAndUserId(1L,1L);

    }

    @ParameterizedTest
    @CsvSource({
            "CANCELLED",
            "PENDING",
            "PAID",
            "COMPLETED",
            "cancelled"
    })
    void getOrdersByStatus_ShouldReturnListOfOrdersWithJustOneTypeOfStatus_WhenStatusIsValid(String status) {
        //given
        //create user to suffice userService.findUserById method
        User user = new User();

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

        // mock method and return user
        when(userService.findUserByUserId(anyLong())).thenReturn(user);

        // extract a list from user get orders just with the desired status
        List<Order> expected = user.getOrders().stream().filter(
                order -> order.getOrderStatus() == OrderStatus.fromString(status))
                .toList();

        //when
        List<Order> actual = orderService.getOrdersByStatus(1L,status);

        //then
        assertNotNull(actual);
        assertEquals(expected.size(),actual.size());
        assertEquals(expected,actual);
        verify(userService,times(1)).findUserByUserId(1L);

    }

    @Test
    void findOrderById_ShouldReturnOrder() {
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
    void findOrderById_ShouldThrowNotFoundException_WhenOrderIsNotFound() {
        //given
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        //when
        Executable executable = () -> orderService.findOrderById(1L);

        //then
        assertThrows(NotFoundException.class,executable,"Order with Id 1 not found.");
    }
}