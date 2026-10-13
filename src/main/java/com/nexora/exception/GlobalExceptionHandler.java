package com.nexora.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexora.account.exception.AccountTransactionException;
import com.nexora.auth.exception.InvalidCredentialsException;
import com.nexora.business.exception.BusinessAlreadyExistsException;
import com.nexora.customer.exception.CustomerAlreadyExistsException;
import com.nexora.customer.exception.CustomerNotFoundException;
import com.nexora.expense.exception.ExpensePaymentException;
import com.nexora.invoice.exception.InvalidInvoiceException;
import com.nexora.invoice.exception.InvoiceNotFoundException;
import com.nexora.payment.exception.InvalidPaymentException;
import com.nexora.payment.exception.PaymentNotFoundException;
//import com.nexora.business.exception.ResourceNotFoundException;
import com.nexora.response.ApiResponse;
import com.nexora.tax.exception.TaxProfileAlreadyExistsException;
import com.nexora.user.exception.UserAlreadyExistsException;
import com.nexora.supplier.exception.SupplierAlreadyExistsException;
import com.nexora.purchase.exception.InvalidPurchaseException;
import com.nexora.purchase.exception.PurchaseNotFoundException;
import com.nexora.reconciliation.exception.ReconciliationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessAlreadyExists(
            BusinessAlreadyExistsException exception) {

        ApiResponse<Object> response = new ApiResponse<>(
                false,
                exception.getMessage(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserAlreadyExists(
            UserAlreadyExistsException exception) {

        ApiResponse<Object> response = new ApiResponse<>(
                false,
                exception.getMessage(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(
            ResourceNotFoundException exception) {

        ApiResponse<Object> response = new ApiResponse<>(
                false,
                exception.getMessage(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCredentials(
            InvalidCredentialsException exception) {

        ApiResponse<Object> response =
                new ApiResponse<>(false, exception.getMessage(), null);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException exception) {

        ApiResponse<Object> response =
                new ApiResponse<>(false, exception.getMessage(), null);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }
    @ExceptionHandler(TaxProfileAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleTaxProfileAlreadyExists(
            TaxProfileAlreadyExistsException ex) {

        ApiResponse<Object> response = new ApiResponse<>(
                false,
                ex.getMessage(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
    @ExceptionHandler(SupplierAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleSupplierAlreadyExists(
            SupplierAlreadyExistsException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                    new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null
                    )
                );
    }
    @ExceptionHandler(InvalidPurchaseException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleInvalidPurchase(
            InvalidPurchaseException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                    new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null
                    )
                );
    }
    @ExceptionHandler(PurchaseNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>>
    handlePurchaseNotFound(
            PurchaseNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                    new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null
                    )
                );
    }
    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomerAlreadyExists(
            CustomerAlreadyExistsException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new ApiResponse<>(
                                false,
                                ex.getMessage(),
                                null
                        )
                );
    }
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomerNotFound(
            CustomerNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null
                ));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed");

        ApiResponse<Void> response = new ApiResponse<>(
                false,
                message,
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvoiceNotFound(
            InvoiceNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new ApiResponse<>(
                                false,
                                ex.getMessage(),
                                null
                        )
                );
    }
    @ExceptionHandler(InvalidInvoiceException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidInvoice(
            InvalidInvoiceException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new ApiResponse<>(
                                false,
                                ex.getMessage(),
                                null
                        )
                );
    }
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentNotFound(
            PaymentNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new ApiResponse<>(
                                false,
                                ex.getMessage(),
                                null
                        )
                );
    }
    @ExceptionHandler(InvalidPaymentException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPayment(
            InvalidPaymentException ex) {

        return ResponseEntity
                .badRequest()
                .body(
                        new ApiResponse<>(
                                false,
                                ex.getMessage(),
                                null
                        )
                );
    }
    @ExceptionHandler(AccountTransactionException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccountTransactionException(
            AccountTransactionException ex) {

        return ResponseEntity
                .badRequest()
                .body(new ApiResponse<>(
                        false,
                        ex.getMessage(),
                        null
                ));
    }
    @ExceptionHandler(ExpensePaymentException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleExpensePaymentException(
            ExpensePaymentException ex) {

        return ResponseEntity
                .badRequest()
                .body(
                        new ApiResponse<>(
                                false,
                                ex.getMessage(),
                                null
                        )
                );
    }
    @ExceptionHandler(ReconciliationException.class)
    public ResponseEntity<ApiResponse<Object>>
    handleReconciliationException(
            ReconciliationException ex) {

        return ResponseEntity
                .badRequest()
                .body(
                        new ApiResponse<>(
                                false,
                                ex.getMessage(),
                                null
                        )
                );
    }
    
}