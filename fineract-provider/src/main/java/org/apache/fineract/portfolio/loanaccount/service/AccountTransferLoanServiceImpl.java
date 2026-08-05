/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.account.contract.AccountTransferAccountDetail;
import org.apache.fineract.portfolio.account.contract.AccountTransferLoanService;
import org.apache.fineract.portfolio.account.contract.LoanTransferChargePayment;
import org.apache.fineract.portfolio.account.contract.LoanTransferPayout;
import org.apache.fineract.portfolio.account.contract.LoanTransferRepayment;
import org.apache.fineract.portfolio.loanaccount.data.PaidInAdvanceData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentParameter;
import org.apache.fineract.portfolio.loanaccount.service.adjustment.LoanAdjustmentService;
import org.springframework.stereotype.Service;

/**
 * Loan side of the account transfer boundary: turns the account-transfer requests into loan domain operations.
 */
@Service
@RequiredArgsConstructor
public class AccountTransferLoanServiceImpl implements AccountTransferLoanService {

    private static final boolean IS_ACCOUNT_TRANSFER = true;
    private static final boolean IS_RECOVERY_REPAYMENT = false;
    private static final Boolean IS_HOLIDAY_VALIDATION_DONE = false;
    private static final String CHARGE_REFUND_CHARGE_TYPE = null;

    private final LoanAssembler loanAssembler;
    private final LoanAccountDomainService loanAccountDomainService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanAdjustmentService loanAdjustmentService;

    @Override
    public AccountTransferAccountDetail retrieveAccountDetail(final Long loanId) {
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        return new AccountTransferAccountDetail(loan.getId(), loan.getClientId(), loan.getOfficeId(), loan.getCurrency());
    }

    @Override
    public Long repay(final LoanTransferRepayment request) {
        final Loan loan = this.loanAssembler.assembleFrom(request.loanId());
        final LoanTransactionType transactionType = request.downPayment() ? LoanTransactionType.DOWN_PAYMENT
                : LoanTransactionType.REPAYMENT;

        return this.loanAccountDomainService.makeRepayment(transactionType, loan, request.transactionDate(), request.transactionAmount(),
                request.paymentDetail(), null, request.txnExternalId(), IS_RECOVERY_REPAYMENT, CHARGE_REFUND_CHARGE_TYPE,
                IS_ACCOUNT_TRANSFER, null, IS_HOLIDAY_VALIDATION_DONE, request.loanToLoanTransfer()).getId();
    }

    @Override
    public Long payCharge(final LoanTransferChargePayment request) {
        final Loan loan = this.loanAssembler.assembleFrom(request.loanId());

        return this.loanAccountDomainService
                .makeChargePayment(loan, request.chargeId(), request.transactionDate(), request.transactionAmount(),
                        request.paymentDetail(), null, request.txnExternalId(), request.transactionType(), request.installmentNumber())
                .getId();
    }

    @Override
    public Long refund(final LoanTransferPayout request) {
        return this.loanAccountDomainService.makeRefund(request.loanId(), new CommandProcessingResultBuilder(), request.transactionDate(),
                request.transactionAmount(), request.paymentDetail(), request.noteText(), request.txnExternalId()).getId();
    }

    @Override
    public Long refundForActiveLoan(final LoanTransferPayout request) {
        return this.loanAccountDomainService
                .makeRefundForActiveLoan(request.loanId(), new CommandProcessingResultBuilder(), request.transactionDate(),
                        request.transactionAmount(), request.paymentDetail(), request.noteText(), request.txnExternalId())
                .getId();
    }

    @Override
    public Long disburse(final LoanTransferPayout request) {
        return this.loanAccountDomainService
                .makeDisburseTransaction(request.loanId(), request.transactionDate(), request.transactionAmount(), request.paymentDetail(),
                        request.noteText(), request.txnExternalId(), request.loanToLoanTransfer())
                .getId();
    }

    @Override
    public void reverseTransfer(final Long loanTransactionId) {
        this.loanAccountDomainService.reverseTransfer(findTransaction(loanTransactionId));
    }

    @Override
    public void adjustTransferTransaction(final Long loanTransactionId, final ExternalId reversalTxnExternalId) {
        final LoanTransaction transactionToAdjust = findTransaction(loanTransactionId);
        final LoanAdjustmentParameter parameter = LoanAdjustmentParameter.builder() //
                .transactionAmount(BigDecimal.ZERO) //
                .paymentDetail(null) //
                .transactionDate(transactionToAdjust.getTransactionDate()) //
                .txnExternalId(transactionToAdjust.getExternalId()) //
                .reversalTxnExternalId(reversalTxnExternalId) //
                .noteText(null) //
                .build();

        this.loanAdjustmentService.adjustLoanTransaction(transactionToAdjust.getLoan(), transactionToAdjust, parameter, null,
                new HashMap<>());
    }

    @Override
    public BigDecimal retrievePaidInAdvance(final Long loanId) {
        final PaidInAdvanceData paidInAdvance = this.loanReadPlatformService.retrieveTotalPaidInAdvance(loanId);
        if (paidInAdvance == null || paidInAdvance.getPaidInAdvance() == null) {
            return BigDecimal.ZERO;
        }
        return paidInAdvance.getPaidInAdvance();
    }

    private LoanTransaction findTransaction(final Long loanTransactionId) {
        return this.loanTransactionRepository.findById(loanTransactionId)
                .orElseThrow(() -> new LoanTransactionNotFoundException(loanTransactionId));
    }
}
