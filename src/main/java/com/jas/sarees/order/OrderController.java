package com.jas.sarees.order;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.jas.sarees.saree.Saree;
import com.jas.sarees.saree.SareeRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final SareeOrderRepository orders;
    private final SareeRepository sarees;
    private final SupplierMessage supplierMessage;
    private final BigDecimal codMinPrice;

    public OrderController(SareeOrderRepository orders, SareeRepository sarees, SupplierMessage supplierMessage,
                           @Value("${app.cod-min-price:500}") BigDecimal codMinPrice) {
        this.orders = orders;
        this.sarees = sarees;
        this.supplierMessage = supplierMessage;
        this.codMinPrice = codMinPrice;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@Valid @RequestBody OrderRequest request) {
        Saree saree = sarees.findById(request.sareeId())
                .filter(s -> s.getStatus() == Saree.Status.LIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saree not available"));

        // Cash on delivery only above the minimum price; UPI needs the transaction ID to verify later.
        if (request.paymentMethod() == SareeOrder.PaymentMethod.COD) {
            if (saree.getPrice().compareTo(codMinPrice) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cash on delivery is not available for this saree");
            }
        } else if (request.transactionId() == null || request.transactionId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction ID is required for UPI payments");
        }

        SareeOrder order = new SareeOrder();
        order.setSaree(saree);
        order.setPaymentMethod(request.paymentMethod());
        if (request.paymentMethod() == SareeOrder.PaymentMethod.UPI) {
            order.setTransactionId(request.transactionId().trim().toUpperCase());
        }
        order.setCustomerName(request.customerName().trim());
        order.setCustomerPhone(request.customerPhone());
        order.setAddress(request.address().trim());
        order.setPincode(request.pincode());
        order.setPriceAtOrder(saree.getPrice());
        return OrderResponse.from(orders.save(order));
    }

    @GetMapping
    public List<AdminOrderResponse> list() {
        return orders.findAllByOrderByCreatedAtDesc().stream()
                .map(o -> AdminOrderResponse.from(o, supplierMessage.link(o)))
                .toList();
    }

    public record StatusRequest(@NotNull(message = "status is required") SareeOrder.Status status) {}

    public record PaymentRequest(@NotNull(message = "verified is required") Boolean verified) {}

    @PatchMapping("/{id}/payment")
    public AdminOrderResponse updatePayment(@PathVariable Long id,
                                            @Valid @RequestBody PaymentRequest request) {
        SareeOrder order = orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        order.setPaymentVerified(request.verified());
        SareeOrder saved = orders.save(order);
        return AdminOrderResponse.from(saved, supplierMessage.link(saved));
    }

    @PatchMapping("/{id}/status")
    public AdminOrderResponse updateStatus(@PathVariable Long id,
                                           @Valid @RequestBody StatusRequest request) {
        SareeOrder order = orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        order.setStatus(request.status());
        SareeOrder saved = orders.save(order);
        return AdminOrderResponse.from(saved, supplierMessage.link(saved));
    }
}