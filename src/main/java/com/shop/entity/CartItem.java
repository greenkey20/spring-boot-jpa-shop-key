package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Getter @Setter
@Table(name="cart_item")
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private int count;

    public static CartItem createCartItem(Cart cart, Item item, int count) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setItem(item);
        cartItem.setCount(count);
        return cartItem;
    }

    /**
     * 장바구니에 이미 담겨있는 상품인데, 해당 상품을 추가로 장바구니에 담을 때, 기존 수량에 현재 담는 수량을 더해주기 위해 사용하는 메서드
     * @param count
     */
    public void addCount(int count){
        this.count += count;
    }

    public void updateCount(int count){
        this.count = count;
    }

}