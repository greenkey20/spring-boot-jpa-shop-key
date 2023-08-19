package com.shop.repository;

import com.shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import com.shop.dto.CartDetailDto;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCartIdAndItemId(Long cartId, Long itemId);

    // 장바구니 페이지에 전달할 CartDetailDto 리스트를 쿼리 하나로 조회하는 JPQL문 = 성능 최적화가 필요한 경우 반환 값으로 DTO 객체 생성 vs 연관관계 매핑을 지연 로딩으로 설정한 경우 엔티티에 매핑된 다른 엔티티 조회 시 추가적으로 쿼리문 실행됨
    // 2023.8.19(토) 22h10 나의 생각 = 이 경우에 실제 실행되는 쿼리문 vs 아닌 경우를 직접 확인해보자
    // CartDetailDto의 생성자 이용해서 DTO 반환 시, 생성자의 파라미터 순서는 DTO 클래스에 명시한 순으로 작성
    @Query("select new com.shop.dto.CartDetailDto(ci.id, i.itemNm, i.price, ci.count, im.imgUrl) " +
            "from CartItem ci, ItemImg im " +
            "join ci.item i " +
            "where ci.cart.id = :cartId " +
            "and im.item.id = ci.item.id " +
            "and im.repimgYn = 'Y' " +
            "order by ci.regTime desc"
            )
    List<CartDetailDto> findCartDetailDtoList(Long cartId);

}