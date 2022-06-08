package woowacourse.shoppingcart.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import woowacourse.shoppingcart.dao.CartItemDao;
import woowacourse.shoppingcart.dao.CustomerDao;
import woowacourse.shoppingcart.dao.ProductDao;
import woowacourse.shoppingcart.domain.CartItem;
import woowacourse.shoppingcart.domain.Product;
import woowacourse.shoppingcart.dto.CartItemResponse;
import woowacourse.shoppingcart.dto.CartItemsResponse;
import woowacourse.shoppingcart.exception.InvalidProductException;
import woowacourse.shoppingcart.exception.NotInCustomerCartItemException;

@Service
@Transactional(rollbackFor = Exception.class)
public class CartService {

    private final CartItemDao cartItemDao;
    private final CustomerDao customerDao;
    private final ProductDao productDao;

    public CartService(final CartItemDao cartItemDao, final CustomerDao customerDao, final ProductDao productDao) {
        this.cartItemDao = cartItemDao;
        this.customerDao = customerDao;
        this.productDao = productDao;
    }

    public CartItemsResponse findCartItemsByCustomerName(final String customerName) {
        final Long customerId = customerDao.findIdByUserName(customerName);
        List<CartItem> cartItems = cartItemDao.findAllByCustomerId(customerId);
        List<CartItemResponse> cartItemResponses = cartItems.stream()
            .map(CartItemResponse::from)
            .collect(Collectors.toList());
        return new CartItemsResponse(cartItemResponses);
    }

    private List<Long> findCartIdsByCustomerName(final String customerName) {
        final Long customerId = customerDao.findIdByUserName(customerName);
        return cartItemDao.findIdsByCustomerId(customerId);
    }

    public CartItemResponse addCart(final Long productId, final int stock, final String customerName) {
        final Long customerId = customerDao.findIdByUserName(customerName);
        Product product = productDao.findProductById(productId);
        removeProductStock(product, stock);
        try {
            Long cartItemId = cartItemDao.addCartItem(customerId, productId, stock);
            return new CartItemResponse(
                cartItemId,
                productId,
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl()
            );
        } catch (Exception e) {
            throw new InvalidProductException();
        }
    }

    private void removeProductStock(Product product, int stock) {
        product.removeStock(stock);
        updateProduct(product);
    }

    public void deleteCart(final String customerName, final Long cartId) {
        validateCustomerCart(cartId, customerName);
        CartItem cartItem = cartItemDao.findById(cartId);
        Product product = productDao.findProductById(cartItemDao.findProductIdById(cartItem.getProductId()));
        addProductStock(product, cartItem.getStock());
        cartItemDao.deleteCartItem(cartId);
    }

    private void addProductStock(Product product, int stock) {
        product.addStock(stock);
        updateProduct(product);
    }

    private void updateProduct(Product product) {
        try {
            productDao.update(product);
        } catch (Exception e) {
            throw new InvalidProductException();
        }
    }

    private void validateCustomerCart(final Long cartId, final String customerName) {
        final List<Long> cartIds = findCartIdsByCustomerName(customerName);
        if (cartIds.contains(cartId)) {
            return;
        }
        throw new NotInCustomerCartItemException();
    }

    public void updateQuantity(final Long cartId, final int quantity) {
        CartItem cartItem = cartItemDao.findById(cartId);
        Product product = productDao.findProductById(cartItem.getProductId());

        product.addStock(cartItem.getStock() - quantity);
        cartItem.updateStock(quantity);

        productDao.update(product);
        cartItemDao.update(cartItem);
    }
}
