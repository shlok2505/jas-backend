package com.jas.sarees.order;

import java.util.List;

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

    public OrderController(SareeOrderRepository orders, SareeRepository sarees,SupplierMessage supplierMessage) {
        this.orders = orders;
        this.sarees = sarees;
        this.supplierMessage = supplierMessage;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@Valid @RequestBody OrderRequest request) {
        Saree saree = sarees.findById(request.sareeId())
                .filter(s -> s.getStatus() == Saree.Status.LIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saree not available"));

        SareeOrder order = new SareeOrder();
        order.setSaree(saree);
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