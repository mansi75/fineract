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

import static org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants.transferAmountParamName;
import static org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants.transferDateParamName;
import static org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants.transferDescriptionParamName;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.account.contract.AccountTransferAccountDetail;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountTransferAssembler {

    private final AccountTransferDetailAssembler accountTransferDetailAssembler;

    public AccountTransferDetails assembleSavingsToSavingsTransfer(final JsonCommand command,
            final AccountTransferAccountDetail fromSavingsAccount, final AccountTransferAccountDetail toSavingsAccount,
            final Long withdrawalId, final Long depositId) {

        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler.assembleSavingsToSavingsTransfer(command,
                fromSavingsAccount.accountId(), toSavingsAccount.accountId());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed(transferDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(transferAmountParamName);
        final Money transactionMonetaryAmount = Money.of(fromSavingsAccount.currency(), transactionAmount);

        final String description = command.stringValueOfParameterNamed(transferDescriptionParamName);
        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.savingsToSavingsTransfer(accountTransferDetails,
                withdrawalId, depositId, transactionDate, transactionMonetaryAmount, description);
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleSavingsToLoanTransfer(final JsonCommand command,
            final AccountTransferAccountDetail fromSavingsAccount, final AccountTransferAccountDetail toLoanAccount,
            final Long withdrawalId, final Long loanRepaymentTransactionId) {

        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler.assembleSavingsToLoanTransfer(command,
                fromSavingsAccount.accountId(), toLoanAccount.accountId());
        final LocalDate transactionDate = command.localDateValueOfParameterNamed(transferDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(transferAmountParamName);
        final Money transactionMonetaryAmount = Money.of(fromSavingsAccount.currency(), transactionAmount);

        final String description = command.stringValueOfParameterNamed(transferDescriptionParamName);

        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.savingsToLoanTransfer(accountTransferDetails,
                withdrawalId, loanRepaymentTransactionId, transactionDate, transactionMonetaryAmount, description);
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleLoanToSavingsTransfer(final JsonCommand command,
            final AccountTransferAccountDetail fromLoanAccount, final AccountTransferAccountDetail toSavingsAccount, final Long depositId,
            final Long loanRefundTransactionId) {

        final AccountTransferDetails accountTransferDetails = this.accountTransferDetailAssembler.assembleLoanToSavingsTransfer(command,
                fromLoanAccount.accountId(), toSavingsAccount.accountId());

        final LocalDate transactionDate = command.localDateValueOfParameterNamed(transferDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(transferAmountParamName);
        final Money transactionMonetaryAmount = Money.of(toSavingsAccount.currency(), transactionAmount);

        final String description = command.stringValueOfParameterNamed(transferDescriptionParamName);

        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.loanTosavingsTransfer(accountTransferDetails,
                depositId, loanRefundTransactionId, transactionDate, transactionMonetaryAmount, description);
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleSavingsToLoanTransfer(final AccountTransferDTO accountTransferDTO,
            final AccountTransferAccountDetail fromSavingsAccount, final AccountTransferAccountDetail toLoanAccount,
            final Long savingsTransactionId, final Long loanTransactionId) {
        final Money transactionMonetaryAmount = Money.of(fromSavingsAccount.currency(), accountTransferDTO.getTransactionAmount());
        AccountTransferDetails accountTransferDetails = accountTransferDTO.getAccountTransferDetails();
        if (accountTransferDetails == null) {
            accountTransferDetails = this.accountTransferDetailAssembler.assembleSavingsToLoanTransfer(fromSavingsAccount, toLoanAccount,
                    accountTransferDTO.getTransferType());
        }
        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.savingsToLoanTransfer(accountTransferDetails,
                savingsTransactionId, loanTransactionId, accountTransferDTO.getTransactionDate(), transactionMonetaryAmount,
                accountTransferDTO.getDescription());
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleSavingsToSavingsTransfer(final AccountTransferDTO accountTransferDTO,
            final AccountTransferAccountDetail fromSavingsAccount, final AccountTransferAccountDetail toSavingsAccount,
            final Long withdrawalId, final Long depositId) {
        final Money transactionMonetaryAmount = Money.of(fromSavingsAccount.currency(), accountTransferDTO.getTransactionAmount());
        AccountTransferDetails accountTransferDetails = accountTransferDTO.getAccountTransferDetails();
        if (accountTransferDetails == null) {
            accountTransferDetails = this.accountTransferDetailAssembler.assembleSavingsToSavingsTransfer(fromSavingsAccount,
                    toSavingsAccount, accountTransferDTO.getTransferType());
        }

        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.savingsToSavingsTransfer(accountTransferDetails,
                withdrawalId, depositId, accountTransferDTO.getTransactionDate(), transactionMonetaryAmount,
                accountTransferDTO.getDescription());
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleLoanToSavingsTransfer(final AccountTransferDTO accountTransferDTO,
            final AccountTransferAccountDetail fromLoanAccount, final AccountTransferAccountDetail toSavingsAccount, final Long depositId,
            final Long loanRefundTransactionId) {
        final Money transactionMonetaryAmount = Money.of(fromLoanAccount.currency(), accountTransferDTO.getTransactionAmount());
        AccountTransferDetails accountTransferDetails = accountTransferDTO.getAccountTransferDetails();
        if (accountTransferDetails == null) {
            accountTransferDetails = this.accountTransferDetailAssembler.assembleLoanToSavingsTransfer(fromLoanAccount, toSavingsAccount,
                    accountTransferDTO.getTransferType());
        }
        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.loanTosavingsTransfer(accountTransferDetails,
                depositId, loanRefundTransactionId, accountTransferDTO.getTransactionDate(), transactionMonetaryAmount,
                accountTransferDTO.getDescription());
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }

    public AccountTransferDetails assembleLoanToLoanTransfer(final AccountTransferDTO accountTransferDTO,
            final AccountTransferAccountDetail fromLoanAccount, final AccountTransferAccountDetail toLoanAccount,
            final Long disburseTransactionId, final Long repaymentTransactionId) {
        final Money transactionMonetaryAmount = Money.of(fromLoanAccount.currency(), accountTransferDTO.getTransactionAmount());
        AccountTransferDetails accountTransferDetails = accountTransferDTO.getAccountTransferDetails();
        if (accountTransferDetails == null) {
            accountTransferDetails = this.accountTransferDetailAssembler.assembleLoanToLoanTransfer(fromLoanAccount, toLoanAccount,
                    accountTransferDTO.getFromTransferType());
        }
        AccountTransferTransaction accountTransferTransaction = AccountTransferTransaction.loanToLoanTransfer(accountTransferDetails,
                disburseTransactionId, repaymentTransactionId, accountTransferDTO.getTransactionDate(), transactionMonetaryAmount,
                accountTransferDTO.getDescription());
        accountTransferDetails.addAccountTransferTransaction(accountTransferTransaction);
        return accountTransferDetails;
    }
}
