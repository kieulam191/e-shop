package com.dev.e_shop.cart;

import com.dev.e_shop.cart.dto.CartDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

public interface UserCartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT new  com.dev.e_shop.cart.dto.CartDto(id, productId, quantity) FROM Cart WHERE userId = :userId")
    Set<CartDto> findItemsByUserid(@Param("userId") long userId);

    @Query("SELECT SUM(p.price * c.quantity) FROM Cart c JOIN Product p ON c.productId = p.id WHERE c.userId = :userId")
    BigDecimal calculateTotalPriceByUserId(@Param("userId") Long userId);

    Optional<Cart> findByProductIdAndUserId(long productId, long userId);

    Optional<Cart> findByIdAndUserId(long id, long userId);
}
