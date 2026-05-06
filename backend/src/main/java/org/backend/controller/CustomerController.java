package org.backend.controller;

import org.backend.model.CrmCustomer;
import org.backend.model.CustomUserDetails;
import org.backend.model.Dto.PageResult;
import org.backend.model.Dto.customer.CustomerListItemDto;
import org.backend.model.Dto.customer.CustomerSaveRequest;
import org.backend.service.CustomerService;
import org.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public Result<PageResult<CustomerListItemDto>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String visitStatus) {
        return Result.success(customerService.list(page, size, keyword, city, visitStatus));
    }

    @GetMapping("/{id}")
    public Result<CustomerListItemDto> detail(@PathVariable Long id) {
        CustomerListItemDto customer = customerService.detail(id);
        if (customer == null) {
            return Result.error(404, "客户不存在");
        }
        return Result.success(customer);
    }

    @PostMapping
    @PreAuthorize("hasRole('OPP_ADMIN')")
    public Result<CrmCustomer> create(
            @RequestBody CustomerSaveRequest req,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            Long userId = currentUser != null ? currentUser.getUserId() : null;
            CrmCustomer customer = customerService.create(req, userId);
            return Result.success(customer);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPP_ADMIN', 'CUSTOMER_MANAGER')")
    public Result<CrmCustomer> update(@PathVariable Long id, @RequestBody CustomerSaveRequest req) {
        try {
            CrmCustomer customer = customerService.update(id, req);
            return Result.success(customer);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
