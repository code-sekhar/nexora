package com.nexora.customer.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexora.business.entity.Business;
import com.nexora.customer.dto.CustomerRequest;
import com.nexora.customer.dto.CustomerResponse;
import com.nexora.customer.entity.Customer;
import com.nexora.customer.entity.CustomerLedger;
import com.nexora.customer.exception.CustomerAlreadyExistsException;
import com.nexora.customer.exception.CustomerNotFoundException;
import com.nexora.customer.repository.CustomerLedgerRepository;
import com.nexora.customer.repository.CustomerRepository;
import com.nexora.user.entity.User;
import com.nexora.user.repository.UserRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerLedgerRepository customerLedgerRepository;
    private final UserRepository userRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerLedgerRepository customerLedgerRepository,
            UserRepository userRepository) {

        this.customerRepository =
                customerRepository;

        this.customerLedgerRepository =
                customerLedgerRepository;

        this.userRepository =
                userRepository;
    }

    @Transactional
    public CustomerResponse createCustomer(
            CustomerRequest request,
            String email) {

        User user = getUser(email);

        Business business =
                user.getBusiness();

        if (customerRepository.existsByPhoneAndBusinessId(
                request.getPhone(),
                business.getId())) {

            throw new CustomerAlreadyExistsException(
                    "Customer with this phone already exists"
            );
        }

        Customer customer =
                new Customer();

        customer.setName(
                request.getName()
        );

        customer.setPhone(
                request.getPhone()
        );

        customer.setEmail(
                request.getEmail()
        );

        customer.setAddress(
                request.getAddress()
        );

        customer.setCity(
                request.getCity()
        );

        customer.setState(
                request.getState()
        );

        customer.setCountry(
                request.getCountry()
        );

        customer.setTaxRegistrationNumber(
                request.getTaxRegistrationNumber()
        );

        BigDecimal openingDue =
                request.getOpeningDue() != null
                        ? request.getOpeningDue()
                        : BigDecimal.ZERO;

        if (openingDue.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Opening due cannot be negative"
            );
        }

        customer.setOpeningDue(
                openingDue
        );

        customer.setNotes(
                request.getNotes()
        );

        customer.setActive(true);

        customer.setBusiness(
                business
        );

        customer.setCreatedAt(
                LocalDateTime.now()
        );

        customer.setUpdatedAt(
                LocalDateTime.now()
        );

        Customer savedCustomer =
                customerRepository.save(customer);

        /*
         * CREATE OPENING BALANCE LEDGER
         *
         * If customer starts with an outstanding
         * amount, create an opening balance entry.
         */
        if (openingDue.compareTo(BigDecimal.ZERO) > 0) {

            createOpeningBalanceLedger(
                    business,
                    savedCustomer,
                    openingDue
            );
        }

        return mapToResponse(
                savedCustomer
        );
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers(
            String email) {

        User user =
                getUser(email);

        Long businessId =
                user.getBusiness().getId();

        return customerRepository
                .findByBusinessId(businessId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(
            Long id,
            String email) {

        User user =
                getUser(email);

        Long businessId =
                user.getBusiness().getId();

        Customer customer =
                customerRepository
                        .findByIdAndBusinessId(
                                id,
                                businessId
                        )
                        .orElseThrow(() ->
                                new CustomerNotFoundException(
                                        "Customer not found"
                                )
                        );

        return mapToResponse(
                customer
        );
    }

    @Transactional
    public CustomerResponse updateCustomer(
            Long id,
            CustomerRequest request,
            String email) {

        User user =
                getUser(email);

        Long businessId =
                user.getBusiness().getId();

        Customer customer =
                customerRepository
                        .findByIdAndBusinessId(
                                id,
                                businessId
                        )
                        .orElseThrow(() ->
                                new CustomerNotFoundException(
                                        "Customer not found"
                                )
                        );

        if (!customer.getPhone()
                .equals(request.getPhone())
                && customerRepository
                        .existsByPhoneAndBusinessIdAndIdNot(
                                request.getPhone(),
                                businessId,
                                id
                        )) {

            throw new CustomerAlreadyExistsException(
                    "Customer with this phone already exists"
            );
        }

        customer.setName(
                request.getName()
        );

        customer.setPhone(
                request.getPhone()
        );

        customer.setEmail(
                request.getEmail()
        );

        customer.setAddress(
                request.getAddress()
        );

        customer.setCity(
                request.getCity()
        );

        customer.setState(
                request.getState()
        );

        customer.setCountry(
                request.getCountry()
        );

        customer.setTaxRegistrationNumber(
                request.getTaxRegistrationNumber()
        );

        BigDecimal newOpeningDue =
                request.getOpeningDue() != null
                        ? request.getOpeningDue()
                        : BigDecimal.ZERO;

        if (newOpeningDue.compareTo(
                BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Opening due cannot be negative"
            );
        }

        customer.setOpeningDue(
                newOpeningDue
        );

        customer.setNotes(
                request.getNotes()
        );

        customer.setUpdatedAt(
                LocalDateTime.now()
        );

        Customer updatedCustomer =
                customerRepository.save(
                        customer
                );

        /*
         * IMPORTANT:
         *
         * We do NOT automatically create another
         * opening ledger here.
         *
         * Otherwise every customer update could
         * create duplicate opening balances.
         *
         * Opening balance changes will be handled
         * through ledger adjustment/reconciliation.
         */

        return mapToResponse(
                updatedCustomer
        );
    }

    @Transactional
    public CustomerResponse updateStatus(
            Long id,
            Boolean active,
            String email) {

        User user =
                getUser(email);

        Long businessId =
                user.getBusiness().getId();

        Customer customer =
                customerRepository
                        .findByIdAndBusinessId(
                                id,
                                businessId
                        )
                        .orElseThrow(() ->
                                new CustomerNotFoundException(
                                        "Customer not found"
                                )
                        );

        customer.setActive(
                active
        );

        customer.setUpdatedAt(
                LocalDateTime.now()
        );

        Customer updatedCustomer =
                customerRepository.save(
                        customer
                );

        return mapToResponse(
                updatedCustomer
        );
    }

    /*
     * INITIALIZE OPENING BALANCE
     *
     * Used for existing customers.
     *
     * It checks whether an OPENING_BALANCE
     * ledger already exists before creating one.
     */
    @Transactional
    public void initializeOpeningBalance(
            Business business,
            Customer customer) {

        BigDecimal openingDue =
                customer.getOpeningDue();

        if (openingDue == null ||
                openingDue.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            return;
        }

        List<CustomerLedger> entries =
                customerLedgerRepository
                        .findByCustomerIdAndBusinessIdOrderByCreatedAtAsc(
                                customer.getId(),
                                business.getId()
                        );

        boolean alreadyExists =
                entries.stream()
                        .anyMatch(entry ->
                                entry.getType()
                                        == CustomerLedger.LedgerType.OPENING_BALANCE
                        );

        if (alreadyExists) {
            return;
        }

        /*
         * IMPORTANT:
         *
         * For an existing customer whose ledger
         * already contains transactions, simply
         * setting balance = openingDue would make
         * the running balance incorrect.
         *
         * Therefore this method is intended for
         * initialization/reconciliation only.
         */
        createOpeningBalanceLedger(
                business,
                customer,
                openingDue
        );
    }

    /*
     * CREATE OPENING BALANCE LEDGER
     */
    private CustomerLedger createOpeningBalanceLedger(
            Business business,
            Customer customer,
            BigDecimal openingDue) {

        CustomerLedger ledger =
                new CustomerLedger();

        ledger.setBusiness(
                business
        );

        ledger.setCustomer(
                customer
        );

        ledger.setType(
                CustomerLedger.LedgerType.OPENING_BALANCE
        );

        ledger.setDebit(
                openingDue
        );

        ledger.setCredit(
                BigDecimal.ZERO
        );

        /*
         * For a brand-new customer there are
         * no previous transactions.
         */
        ledger.setBalance(
                openingDue
        );

        ledger.setDescription(
                "Customer opening balance"
        );

        return customerLedgerRepository.save(
                ledger
        );
    }

    private User getUser(
            String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "User not found"
                        )
                );
    }

    private CustomerResponse mapToResponse(
            Customer customer) {

        CustomerResponse response =
                new CustomerResponse();

        response.setId(
                customer.getId()
        );

        response.setName(
                customer.getName()
        );

        response.setPhone(
                customer.getPhone()
        );

        response.setEmail(
                customer.getEmail()
        );

        response.setAddress(
                customer.getAddress()
        );

        response.setCity(
                customer.getCity()
        );

        response.setState(
                customer.getState()
        );

        response.setCountry(
                customer.getCountry()
        );

        response.setTaxRegistrationNumber(
                customer.getTaxRegistrationNumber()
        );

        response.setOpeningDue(
                customer.getOpeningDue()
        );

        response.setNotes(
                customer.getNotes()
        );

        response.setActive(
                customer.isActive()
        );

        response.setCreatedAt(
                customer.getCreatedAt()
        );

        response.setUpdatedAt(
                customer.getUpdatedAt()
        );

        return response;
    }
}