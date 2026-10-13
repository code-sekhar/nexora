package com.nexora.sales.returnmodule.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.customer.entity.Customer;
import com.nexora.customer.service.CustomerLedgerService;
import com.nexora.inventory.service.InventoryService;
import com.nexora.invoice.entity.Invoice;
import com.nexora.invoice.entity.InvoiceItem;
import com.nexora.invoice.repository.InvoiceItemRepository;
import com.nexora.invoice.repository.InvoiceRepository;
import com.nexora.sales.returnmodule.dto.SalesReturnItemRequest;
import com.nexora.sales.returnmodule.dto.SalesReturnItemResponse;
import com.nexora.sales.returnmodule.dto.SalesReturnRequest;
import com.nexora.sales.returnmodule.dto.SalesReturnResponse;
import com.nexora.sales.returnmodule.entity.SalesReturn;
import com.nexora.sales.returnmodule.entity.SalesReturnItem;
import com.nexora.sales.returnmodule.repository.SalesReturnItemRepository;
import com.nexora.sales.returnmodule.repository.SalesReturnRepository;

@Service
@Transactional
public class SalesReturnService {

    private final SalesReturnRepository salesReturnRepository;

    private final SalesReturnItemRepository salesReturnItemRepository;

    private final InvoiceRepository invoiceRepository;

    private final InvoiceItemRepository invoiceItemRepository;

    private final InventoryService inventoryService;

    private final CustomerLedgerService customerLedgerService;

    public SalesReturnService(
            SalesReturnRepository salesReturnRepository,
            SalesReturnItemRepository salesReturnItemRepository,
            InvoiceRepository invoiceRepository,
            InvoiceItemRepository invoiceItemRepository,
            InventoryService inventoryService,
            CustomerLedgerService customerLedgerService) {

        this.salesReturnRepository = salesReturnRepository;
        this.salesReturnItemRepository = salesReturnItemRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.inventoryService = inventoryService;
        this.customerLedgerService = customerLedgerService;
    }

    public SalesReturn createReturn(
            Business business,
            SalesReturnRequest request) {

        if (request.getInvoiceId() == null) {
            throw new IllegalArgumentException(
                    "Invoice ID is required"
            );
        }

        if (request.getItems() == null
                || request.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one return item is required"
            );
        }

        Invoice invoice =
                invoiceRepository
                        .findByIdAndBusinessId(
                                request.getInvoiceId(),
                                business.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invoice not found"
                                )
                        );

        if (invoice.getStatus()
                == Invoice.InvoiceStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "Cannot return a cancelled invoice"
            );
        }

        Customer customer =
                invoice.getCustomer();

        Set<Long> invoiceItemIds =
                new HashSet<>();

        SalesReturn salesReturn =
                new SalesReturn();

        salesReturn.setReturnNumber(
                generateReturnNumber()
        );

        salesReturn.setBusiness(
                business
        );

        salesReturn.setInvoice(
                invoice
        );

        salesReturn.setCustomer(
                customer
        );

        salesReturn.setReturnDate(
                request.getReturnDate() != null
                        ? request.getReturnDate()
                        : LocalDate.now()
        );

        salesReturn.setReason(
                request.getReason()
        );

        salesReturn.setNotes(
                request.getNotes()
        );

        salesReturn.setStatus(
                SalesReturn.ReturnStatus.COMPLETED
        );

        BigDecimal subtotal =
                BigDecimal.ZERO;

        BigDecimal taxAmount =
                BigDecimal.ZERO;

        BigDecimal totalAmount =
                BigDecimal.ZERO;

        for (SalesReturnItemRequest itemRequest
                : request.getItems()) {

            if (itemRequest.getInvoiceItemId() == null) {
                throw new IllegalArgumentException(
                        "Invoice item ID is required"
                );
            }

            if (!invoiceItemIds.add(
                    itemRequest.getInvoiceItemId())) {

                throw new IllegalArgumentException(
                        "Duplicate invoice item in return request"
                );
            }

            if (itemRequest.getQuantity() == null
                    || itemRequest.getQuantity()
                            .compareTo(BigDecimal.ZERO) <= 0) {

                throw new IllegalArgumentException(
                        "Return quantity must be greater than zero"
                );
            }

            InvoiceItem invoiceItem =
                    invoiceItemRepository
                            .findById(
                                    itemRequest.getInvoiceItemId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Invoice item not found"
                                    )
                            );

            if (!invoiceItem.getInvoice()
                    .getId()
                    .equals(invoice.getId())) {

                throw new IllegalArgumentException(
                        "Invoice item does not belong to invoice"
                );
            }

            BigDecimal alreadyReturned =
                    salesReturnItemRepository
                            .getReturnedQuantity(
                                    invoiceItem.getId()
                            );

            BigDecimal availableQuantity =
                    invoiceItem.getQuantity()
                            .subtract(alreadyReturned);

            if (itemRequest.getQuantity()
                    .compareTo(availableQuantity) > 0) {

                throw new IllegalArgumentException(
                        "Return quantity exceeds available quantity for product "
                        + invoiceItem.getProduct().getName()
                );
            }

            BigDecimal quantity =
                    itemRequest.getQuantity();

            BigDecimal unitPrice =
                    invoiceItem.getUnitPrice();

            BigDecimal taxableAmount =
                    unitPrice.multiply(quantity);

            BigDecimal originalQuantity =
                    invoiceItem.getQuantity();

            BigDecimal originalTax =
                    invoiceItem.getTaxAmount();

            BigDecimal itemTax =
                    BigDecimal.ZERO;

            if (originalQuantity.compareTo(
                    BigDecimal.ZERO) > 0) {

                itemTax =
                        originalTax
                                .multiply(quantity)
                                .divide(
                                        originalQuantity,
                                        2,
                                        RoundingMode.HALF_UP
                                );
            }

            BigDecimal itemTotal =
                    taxableAmount.add(itemTax);

            SalesReturnItem returnItem =
                    new SalesReturnItem();

            returnItem.setSalesReturn(
                    salesReturn
            );

            returnItem.setInvoiceItem(
                    invoiceItem
            );

            returnItem.setProduct(
                    invoiceItem.getProduct()
            );

            returnItem.setQuantity(
                    quantity
            );

            returnItem.setUnitPrice(
                    unitPrice
            );

            returnItem.setTaxableAmount(
                    taxableAmount
            );

            returnItem.setTaxAmount(
                    itemTax
            );

            returnItem.setTotalAmount(
                    itemTotal
            );

            salesReturn.getItems()
                    .add(returnItem);

            subtotal =
                    subtotal.add(taxableAmount);

            taxAmount =
                    taxAmount.add(itemTax);

            totalAmount =
                    totalAmount.add(itemTotal);
        }

        salesReturn.setSubtotal(subtotal);
        salesReturn.setTaxAmount(taxAmount);
        salesReturn.setTotalAmount(totalAmount);

        SalesReturn savedReturn =
                salesReturnRepository.save(
                        salesReturn
                );

        /*
         * Sales return adds stock back.
         */
        for (SalesReturnItem item
                : savedReturn.getItems()) {

            inventoryService.addSaleReturnStock(
                    item.getProduct(),
                    business,
                    item.getQuantity(),
                    savedReturn.getId()
            );
        }

        /*
         * Customer ledger credit.
         */
        customerLedgerService.createSalesReturnEntry(
                business,
                customer,
                savedReturn
        );

        return savedReturn;
    }

    public SalesReturnResponse mapToResponse(
            SalesReturn salesReturn) {

        SalesReturnResponse response =
                new SalesReturnResponse();

        response.setId(
                salesReturn.getId()
        );

        response.setReturnNumber(
                salesReturn.getReturnNumber()
        );

        response.setInvoiceId(
                salesReturn.getInvoice().getId()
        );

        response.setInvoiceNumber(
                salesReturn.getInvoice().getInvoiceNumber()
        );

        response.setCustomerId(
                salesReturn.getCustomer().getId()
        );

        response.setCustomerName(
                salesReturn.getCustomer().getName()
        );

        response.setReturnDate(
                salesReturn.getReturnDate()
        );

        response.setSubtotal(
                salesReturn.getSubtotal()
        );

        response.setTaxAmount(
                salesReturn.getTaxAmount()
        );

        response.setTotalAmount(
                salesReturn.getTotalAmount()
        );

        response.setStatus(
                salesReturn.getStatus().name()
        );

        response.setReason(
                salesReturn.getReason()
        );

        response.setNotes(
                salesReturn.getNotes()
        );

        List<SalesReturnItemResponse> items =
                salesReturn.getItems()
                        .stream()
                        .map(item -> {

                            SalesReturnItemResponse itemResponse =
                                    new SalesReturnItemResponse();

                            itemResponse.setId(
                                    item.getId()
                            );

                            itemResponse.setInvoiceItemId(
                                    item.getInvoiceItem().getId()
                            );

                            itemResponse.setProductId(
                                    item.getProduct().getId()
                            );

                            itemResponse.setProductName(
                                    item.getProduct().getName()
                            );

                            itemResponse.setSku(
                                    item.getProduct().getSku()
                            );

                            itemResponse.setQuantity(
                                    item.getQuantity()
                            );

                            itemResponse.setUnitPrice(
                                    item.getUnitPrice()
                            );

                            itemResponse.setTaxableAmount(
                                    item.getTaxableAmount()
                            );

                            itemResponse.setTaxAmount(
                                    item.getTaxAmount()
                            );

                            itemResponse.setTotalAmount(
                                    item.getTotalAmount()
                            );

                            return itemResponse;
                        })
                        .toList();

        response.setItems(items);

        return response;
    }

    private String generateReturnNumber() {

        return "SR-"
                + System.currentTimeMillis();
    }
}