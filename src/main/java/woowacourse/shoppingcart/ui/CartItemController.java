package woowacourse.shoppingcart.ui;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import woowacourse.shoppingcart.application.CartService;
import woowacourse.shoppingcart.dto.CartItemRequest;
import woowacourse.shoppingcart.dto.CartItemResponse;
import woowacourse.shoppingcart.dto.CartItemsResponse;
import woowacourse.shoppingcart.dto.Request;

@RestController
@RequestMapping("/api/cartItems")
public class CartItemController {
    private final CartService cartService;

    public CartItemController(final CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartItemsResponse> getCartItems(HttpServletRequest request) {
        CartItemsResponse cartItemsResponse = cartService.findCartItemsByCustomerName(
            (String)request.getAttribute("username"));
        return ResponseEntity.ok().body(cartItemsResponse);
    }

    @PostMapping
    public ResponseEntity<CartItemResponse> addCartItem(@Validated(Request.id.class) @RequestBody final CartItemRequest cartItemRequest,
        HttpServletRequest request) {

        CartItemResponse cartItemResponse = cartService.addCart(cartItemRequest.getProductId(),
            cartItemRequest.getQuantity(),
            (String)request.getAttribute("username")
        );

        return ResponseEntity.ok().body(cartItemResponse);
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable final Long cartId, HttpServletRequest request) {
        cartService.deleteCart((String)request.getAttribute("username"), cartId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{cartItemId}")
    public ResponseEntity<Void> updateQuantity(@PathVariable final Long cartItemId, @RequestParam int quantity) {
        cartService.updateQuantity(cartItemId, quantity);
        return ResponseEntity.ok().build();
    }
}
