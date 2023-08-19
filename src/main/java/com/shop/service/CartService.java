package com.shop.service;

import com.shop.dto.CartItemDto;
import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CartRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import com.shop.dto.CartDetailDto;

import java.util.ArrayList;
import java.util.List;

import org.thymeleaf.util.StringUtils;
import com.shop.dto.CartOrderDto;
import com.shop.dto.OrderDto;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    public Long addCart(CartItemDto cartItemDto, String email) {

        Item item = itemRepository.findById(cartItemDto.getItemId())
                .orElseThrow(EntityNotFoundException::new); // 장바구니에 담을 상품 엔티티 조회
        Member member = memberRepository.findByEmail(email); // 현재 로그인한 회원의 회원 엔티티 조회

        Cart cart = cartRepository.findByMemberId(member.getId()); // 현재 로그인한 회원의 장바구니 엔티티 조회
        if (cart == null) { // 현재 로그인한 회원의 장바구니가 없는 경우 = 현재 로그인한 회원이 장바구니에 처음으로 상품을 담을 경우
            cart = Cart.createCart(member); // 해당 회원의 장바구니 엔티티 생성
            cartRepository.save(cart);
        }

        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId()); // 현재 장바구니에 넣은 상품이 이미 장바구니에 들어가 있는지 조회
        if (savedCartItem != null) { // 현재 장바구니에 담은 상품이 이미 장바구니에 있던 경우
            savedCartItem.addCount(cartItemDto.getCount());
            return savedCartItem.getId();
        } else {
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount()); // CartItem 엔티티 생성
            cartItemRepository.save(cartItem); // 장바구니에 들어갈 상품 저장
            return cartItem.getId();
        }
    }

    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String email) {

        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        Member member = memberRepository.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId()); // 현재 로그인한 회원의 장바구니 엔티티 조회
        if (cart == null) { // 현재 로그인한 회원의 장바구니 엔티티 조회 결과가 null일 때 = 장바구니에 상품을 한 번도 안 담은 경우
            return cartDetailDtoList; // 빈 리스트 반환
        }

        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());
        return cartDetailDtoList;
    }

    /**
     * 현재 로그인한 회원과 해당 장바구니 상품 저장한 회원이 동일한지 검사하는 메서드 <- JavaScript 코드에서 업데이트할 장바구니 상품 번호 조작 가능
     * @param cartItemId
     * @param email
     * @return
     */
    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email) {
        Member curMember = memberRepository.findByEmail(email); // 현재 로그인한 회원 엔티티 조회
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);
        Member savedMember = cartItem.getCart().getMember(); // 장바구니 상품을 저장한 회원 엔티티 조회

        if (!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())) {
            return false;
        }

        return true;
    }

    /**
     * 장바구니 상품 수량 업데이트하는 메서드
     * @param cartItemId
     * @param count
     */
    public void updateCartItemCount(Long cartItemId, int count) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);

        cartItem.updateCount(count);
    }

    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);
        cartItemRepository.delete(cartItem);
    }

    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email) {
        // 장바구니 페이지로부터 전달받은 주문 상품 번호를 이용해서, 주문 로직으로 전달할 OrderDto 객체 생성
        List<OrderDto> orderDtoList = new ArrayList<>();

        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository
                    .findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityNotFoundException::new);

            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);
        }

        Long orderId = orderService.orders(orderDtoList, email); // 주문 로직 호출 -> 장바구니에 담은 상품 주문

        // 금번 주문하는 상품들을 장바구니에서 제거
        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository
                    .findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityNotFoundException::new);
            cartItemRepository.delete(cartItem);
        }

        return orderId;
    }

}