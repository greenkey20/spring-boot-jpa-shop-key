package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.*;

@Entity
@Table(name = "cart")
@Getter @Setter
@ToString
public class Cart extends BaseEntity {

    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    // 2023.8.19(토) 21h45 나의 생각/질문 = 도메인/엔티티 영역에 있는, 이렇게 엔티티의 상태를 변경하는 메서드들을 static이 아닌 것으로 만들면 어떤 일이 일어나는가?
    public static Cart createCart(Member member){
        Cart cart = new Cart();
        cart.setMember(member);
        return cart;
    }

}