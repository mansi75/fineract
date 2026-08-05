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
package org.apache.fineract.portfolio.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;

@Entity
@Table(name = "m_account_transfer_transaction")
@Getter
public class AccountTransferTransaction extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "account_transfer_details_id", nullable = true)
    private AccountTransferDetails accountTransferDetails;

    @Column(name = "from_savings_transaction_id")
    private Long fromSavingsTransactionId;

    @Column(name = "to_savings_transaction_id")
    private Long toSavingsTransactionId;

    @Column(name = "to_loan_transaction_id")
    private Long toLoanTransactionId;

    @Column(name = "from_loan_transaction_id")
    private Long fromLoanTransactionId;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed = false;

    @Column(name = "transaction_date")
    private LocalDate date;

    @Embedded
    private MonetaryCurrency currency;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "description", length = 100)
    private String description;

    public static AccountTransferTransaction savingsToSavingsTransfer(final AccountTransferDetails accountTransferDetails,
            final Long withdrawalId, final Long depositId, final LocalDate transactionDate, final Money transactionAmount,
            final String description) {

        return new AccountTransferTransaction(accountTransferDetails, withdrawalId, depositId, null, null, transactionDate,
                transactionAmount, description);
    }

    public static AccountTransferTransaction savingsToLoanTransfer(final AccountTransferDetails accountTransferDetails,
            final Long withdrawalId, final Long loanRepaymentTransactionId, final LocalDate transactionDate, final Money transactionAmount,
            final String description) {
        return new AccountTransferTransaction(accountTransferDetails, withdrawalId, null, loanRepaymentTransactionId, null, transactionDate,
                transactionAmount, description);
    }

    public static AccountTransferTransaction loanTosavingsTransfer(final AccountTransferDetails accountTransferDetails,
            final Long depositId, final Long loanRefundTransactionId, final LocalDate transactionDate, final Money transactionAmount,
            final String description) {
        return new AccountTransferTransaction(accountTransferDetails, null, depositId, null, loanRefundTransactionId, transactionDate,
                transactionAmount, description);
    }

    protected AccountTransferTransaction() {
        //
    }

    private AccountTransferTransaction(final AccountTransferDetails accountTransferDetails, final Long withdrawalId, final Long depositId,
            final Long loanRepaymentTransactionId, final Long loanRefundTransactionId, final LocalDate transactionDate,
            final Money transactionAmount, final String description) {
        this.accountTransferDetails = accountTransferDetails;
        this.fromLoanTransactionId = loanRefundTransactionId;
        this.fromSavingsTransactionId = withdrawalId;
        this.toSavingsTransactionId = depositId;
        this.toLoanTransactionId = loanRepaymentTransactionId;
        this.date = transactionDate;
        this.currency = transactionAmount.getCurrency();
        this.amount = transactionAmount.getAmountDefaultedToNullIfZero();
        this.description = description;
    }

    public void reverse() {
        this.reversed = true;
    }

    public void updateToLoanTransaction(Long toLoanTransactionId) {
        this.toLoanTransactionId = toLoanTransactionId;
    }

    public AccountTransferDetails accountTransferDetails() {
        return this.accountTransferDetails;
    }

    public static AccountTransferTransaction loanToLoanTransfer(AccountTransferDetails accountTransferDetails, Long disburseTransactionId,
            Long repaymentTransactionId, LocalDate transactionDate, Money transactionMonetaryAmount, String description) {
        return new AccountTransferTransaction(accountTransferDetails, null, null, repaymentTransactionId, disburseTransactionId,
                transactionDate, transactionMonetaryAmount, description);
    }
}
