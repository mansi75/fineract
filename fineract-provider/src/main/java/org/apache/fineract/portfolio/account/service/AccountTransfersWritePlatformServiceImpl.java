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
package org.apache.fineract.portfolio.account.service;

import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountTypeParamName;
import static org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants.transferAmountParamName;
import static org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants.transferDateParamName;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.contract.AccountTransferAccountDetail;
import org.apache.fineract.portfolio.account.contract.AccountTransferLoanService;
import org.apache.fineract.portfolio.account.contract.AccountTransferSavingsService;
import org.apache.fineract.portfolio.account.contract.LoanTransferChargePayment;
import org.apache.fineract.portfolio.account.contract.LoanTransferPayout;
import org.apache.fineract.portfolio.account.contract.LoanTransferRepayment;
import org.apache.fineract.portfolio.account.contract.SavingsTransferDeposit;
import org.apache.fineract.portfolio.account.contract.SavingsTransferWithdrawal;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.AccountTransfersDataValidator;
import org.apache.fineract.portfolio.account.domain.AccountTransferAssembler;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetailRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.AccountTransferRepository;
import org.apache.fineract.portfolio.account.domain.AccountTransferTransaction;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.exception.AccountTransferNotFoundException;
import org.apache.fineract.portfolio.account.exception.DifferentCurrenciesException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.InvalidPaidInAdvanceAmountException;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class AccountTransfersWritePlatformServiceImpl implements AccountTransfersWritePlatformService {

    private final AccountTransfersDataValidator accountTransfersDataValidator;
    private final AccountTransferAssembler accountTransferAssembler;
    private final AccountTransferRepository accountTransferRepository;
    private final AccountTransferSavingsService accountTransferSavingsService;
    private final AccountTransferLoanService accountTransferLoanService;
    private final AccountTransferDetailRepository accountTransferDetailRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final ExternalIdFactory externalIdFactory;
    private final FineractProperties fineractProperties;

    @Transactional
    @Override
    public CommandProcessingResult create(final JsonCommand command) {
        this.accountTransfersDataValidator.validate(command);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed(transferDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(transferAmountParamName);

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

        final Integer fromAccountTypeId = command.integerValueSansLocaleOfParameterNamed(fromAccountTypeParamName);
        final PortfolioAccountType fromAccountType = PortfolioAccountType.fromInt(fromAccountTypeId);

        final Integer toAccountTypeId = command.integerValueSansLocaleOfParameterNamed(toAccountTypeParamName);
        final PortfolioAccountType toAccountType = PortfolioAccountType.fromInt(toAccountTypeId);

        final PaymentDetail paymentDetail = null;
        Long fromSavingsAccountId = null;
        Long transferDetailId = null;
        Long fromLoanAccountId = null;
        final boolean isRegularTransaction = true;

        if (isSavingsToSavingsAccountTransfer(fromAccountType, toAccountType)) {

            fromSavingsAccountId = command.longValueOfParameterNamed(fromAccountIdParamName);
            final AccountTransferAccountDetail fromSavingsAccount = this.accountTransferSavingsService
                    .retrieveAccountDetail(fromSavingsAccountId);

            final Long withdrawalId = this.accountTransferSavingsService.withdraw(SavingsTransferWithdrawal.builder()
                    .savingsAccountId(fromSavingsAccountId).transactionDate(transactionDate).transactionAmount(transactionAmount)
                    .formatter(fmt).paymentDetail(paymentDetail).regularTransaction(isRegularTransaction).build());

            final Long toSavingsId = command.longValueOfParameterNamed(toAccountIdParamName);
            final AccountTransferAccountDetail toSavingsAccount = this.accountTransferSavingsService.retrieveAccountDetail(toSavingsId);

            final Long depositId = this.accountTransferSavingsService.deposit(SavingsTransferDeposit.builder().savingsAccountId(toSavingsId)
                    .transactionDate(transactionDate).transactionAmount(transactionAmount).formatter(fmt).paymentDetail(paymentDetail)
                    .regularTransaction(isRegularTransaction).build());

            if (!fromSavingsAccount.currency().getCode().equals(toSavingsAccount.currency().getCode())) {
                throw new DifferentCurrenciesException(fromSavingsAccount.currency().getCode(), toSavingsAccount.currency().getCode());
            }

            final AccountTransferDetails accountTransferDetails = this.accountTransferAssembler.assembleSavingsToSavingsTransfer(command,
                    fromSavingsAccount, toSavingsAccount, withdrawalId, depositId);
            this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
            transferDetailId = accountTransferDetails.getId();

        } else if (isSavingsToLoanAccountTransfer(fromAccountType, toAccountType)) {

            fromSavingsAccountId = command.longValueOfParameterNamed(fromAccountIdParamName);
            final AccountTransferAccountDetail fromSavingsAccount = this.accountTransferSavingsService
                    .retrieveAccountDetail(fromSavingsAccountId);

            final Long withdrawalId = this.accountTransferSavingsService.withdraw(SavingsTransferWithdrawal.builder()
                    .savingsAccountId(fromSavingsAccountId).transactionDate(transactionDate).transactionAmount(transactionAmount)
                    .formatter(fmt).paymentDetail(paymentDetail).regularTransaction(isRegularTransaction).build());

            final Long toLoanAccountId = command.longValueOfParameterNamed(toAccountIdParamName);
            final AccountTransferAccountDetail toLoanAccount = this.accountTransferLoanService.retrieveAccountDetail(toLoanAccountId);

            final Long loanRepaymentTransactionId = this.accountTransferLoanService.repay(LoanTransferRepayment.builder()
                    .loanId(toLoanAccountId).transactionDate(transactionDate).transactionAmount(transactionAmount)
                    .paymentDetail(paymentDetail).txnExternalId(externalIdFactory.create()).build());

            final AccountTransferDetails accountTransferDetails = this.accountTransferAssembler.assembleSavingsToLoanTransfer(command,
                    fromSavingsAccount, toLoanAccount, withdrawalId, loanRepaymentTransactionId);
            this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
            transferDetailId = accountTransferDetails.getId();

        } else if (isLoanToSavingsAccountTransfer(fromAccountType, toAccountType)) {
            // FIXME - kw - ADD overpaid loan to savings account transfer
            // support.

            fromLoanAccountId = command.longValueOfParameterNamed(fromAccountIdParamName);
            final AccountTransferAccountDetail fromLoanAccount = this.accountTransferLoanService.retrieveAccountDetail(fromLoanAccountId);

            final Long loanRefundTransactionId = this.accountTransferLoanService.refund(LoanTransferPayout.builder()
                    .loanId(fromLoanAccountId).transactionDate(transactionDate).transactionAmount(transactionAmount)
                    .paymentDetail(paymentDetail).txnExternalId(externalIdFactory.create()).build());

            final Long toSavingsAccountId = command.longValueOfParameterNamed(toAccountIdParamName);
            final AccountTransferAccountDetail toSavingsAccount = this.accountTransferSavingsService
                    .retrieveAccountDetail(toSavingsAccountId);

            final Long depositId = this.accountTransferSavingsService.deposit(SavingsTransferDeposit.builder()
                    .savingsAccountId(toSavingsAccountId).transactionDate(transactionDate).transactionAmount(transactionAmount)
                    .formatter(fmt).paymentDetail(paymentDetail).regularTransaction(isRegularTransaction).build());

            final AccountTransferDetails accountTransferDetails = this.accountTransferAssembler.assembleLoanToSavingsTransfer(command,
                    fromLoanAccount, toSavingsAccount, depositId, loanRefundTransactionId);
            this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
            transferDetailId = accountTransferDetails.getId();

        }

        final CommandProcessingResultBuilder builder = new CommandProcessingResultBuilder() //
                .withEntityId(transferDetailId);

        if (PortfolioAccountType.SAVINGS.equals(fromAccountType)) {
            builder.withSavingsId(fromSavingsAccountId);
        }
        if (PortfolioAccountType.LOAN.equals(fromAccountType)) {
            builder.withLoanId(fromLoanAccountId);
        }

        return builder.build();
    }

    @Override
    @Transactional
    public void reverseTransfersWithFromAccountType(final Long accountNumber, final PortfolioAccountType accountTypeId) {
        List<AccountTransferTransaction> accountTransfers = null;
        if (PortfolioAccountType.LOAN.equals(accountTypeId)) {
            accountTransfers = this.accountTransferRepository.findByFromLoanId(accountNumber);
        }
        if (accountTransfers != null && !accountTransfers.isEmpty()) {
            undoTransactions(accountTransfers);
        }

    }

    @Override
    @Transactional
    public void reverseTransfersWithFromAccountTransactions(final List<Long> fromTransactionIds, final PortfolioAccountType accountTypeId) {
        List<AccountTransferTransaction> accountTransfers = new ArrayList<>();
        if (PortfolioAccountType.LOAN.equals(accountTypeId)) {
            List<List<Long>> partitions = Lists.partition(fromTransactionIds,
                    fineractProperties.getQuery().getInClauseParameterSizeLimit());
            partitions.forEach(partition -> accountTransfers.addAll(this.accountTransferRepository.findByFromLoanTransactions(partition)));
        }
        if (!accountTransfers.isEmpty()) {
            undoTransactions(accountTransfers);
        }
    }

    @Override
    @Transactional
    public void reverseAllTransactions(final Long accountId, final PortfolioAccountType accountTypeId) {
        List<AccountTransferTransaction> accountTransfers = null;
        if (PortfolioAccountType.LOAN.equals(accountTypeId)) {
            accountTransfers = this.accountTransferRepository.findAllByLoanId(accountId);
        }
        if (accountTransfers != null && !accountTransfers.isEmpty()) {
            undoTransactions(accountTransfers);
        }
    }

    private void undoTransactions(final List<AccountTransferTransaction> accountTransfers) {
        for (final AccountTransferTransaction accountTransfer : accountTransfers) {
            if (accountTransfer.getFromLoanTransactionId() != null) {
                this.accountTransferLoanService.reverseTransfer(accountTransfer.getFromLoanTransactionId());
            }
            if (accountTransfer.getToLoanTransactionId() != null) {
                this.accountTransferLoanService.reverseTransfer(accountTransfer.getToLoanTransactionId());
            }
            if (accountTransfer.getFromSavingsTransactionId() != null) {
                this.accountTransferSavingsService.undoTransferTransaction(
                        accountTransfer.accountTransferDetails().getFromSavingsAccountId(), accountTransfer.getFromSavingsTransactionId());
            }
            if (accountTransfer.getToSavingsTransactionId() != null) {
                this.accountTransferSavingsService.undoTransferTransaction(accountTransfer.accountTransferDetails().getToSavingsAccountId(),
                        accountTransfer.getToSavingsTransactionId());
            }
            accountTransfer.reverse();
            this.accountTransferRepository.save(accountTransfer);
        }
    }

    @Override
    @Transactional
    public Long transferFunds(final AccountTransferDTO accountTransferDTO) {
        Long transferTransactionId = null;
        final boolean isRegularTransaction = accountTransferDTO.isRegularTransaction();
        AccountTransferDetails accountTransferDetails = accountTransferDTO.getAccountTransferDetails();

        if (isSavingsToLoanAccountTransfer(accountTransferDTO.getFromAccountType(), accountTransferDTO.getToAccountType())) {

            final AccountTransferType transferType = AccountTransferType.fromInt(accountTransferDTO.getTransferType());
            final AccountTransferAccountDetail fromSavingsAccount = this.accountTransferSavingsService
                    .retrieveAccountDetail(fromSavingsAccountId(accountTransferDTO));
            final AccountTransferAccountDetail toLoanAccount = this.accountTransferLoanService
                    .retrieveAccountDetail(toLoanAccountId(accountTransferDTO));

            final Long withdrawalId = this.accountTransferSavingsService.withdraw(SavingsTransferWithdrawal.builder()
                    .savingsAccountId(fromSavingsAccount.accountId()).transactionDate(accountTransferDTO.getTransactionDate())
                    .transactionAmount(accountTransferDTO.getTransactionAmount()).formatter(accountTransferDTO.getFmt())
                    .paymentDetail(accountTransferDTO.getPaymentDetail()).regularTransaction(isRegularTransaction)
                    .interestTransfer(transferType.isInterestTransfer())
                    .exceptionForBalanceCheck(accountTransferDTO.isExceptionForBalanceCheck()).build());

            // Safety net (it might need to generate new one)
            final ExternalId externalId = externalIdFactory.create(accountTransferDTO.getTxnExternalId().getValue());

            final Long loanTransactionId;
            if (transferType.isChargePayment()) {
                loanTransactionId = this.accountTransferLoanService.payCharge(LoanTransferChargePayment.builder()
                        .loanId(toLoanAccount.accountId()).chargeId(accountTransferDTO.getChargeId())
                        .transactionDate(accountTransferDTO.getTransactionDate())
                        .transactionAmount(accountTransferDTO.getTransactionAmount()).paymentDetail(accountTransferDTO.getPaymentDetail())
                        .txnExternalId(externalId).transactionType(accountTransferDTO.getToTransferType())
                        .installmentNumber(accountTransferDTO.getLoanInstallmentNumber()).build());
            } else {
                loanTransactionId = this.accountTransferLoanService.repay(LoanTransferRepayment.builder().loanId(toLoanAccount.accountId())
                        .transactionDate(accountTransferDTO.getTransactionDate())
                        .transactionAmount(accountTransferDTO.getTransactionAmount()).paymentDetail(accountTransferDTO.getPaymentDetail())
                        .txnExternalId(externalId).downPayment(transferType.isLoanDownPayment()).build());
            }

            accountTransferDetails = this.accountTransferAssembler.assembleSavingsToLoanTransfer(accountTransferDTO, fromSavingsAccount,
                    toLoanAccount, withdrawalId, loanTransactionId);
            this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
            transferTransactionId = accountTransferDetails.getId();

        } else if (isSavingsToSavingsAccountTransfer(accountTransferDTO.getFromAccountType(), accountTransferDTO.getToAccountType())) {

            final AccountTransferType transferType = AccountTransferType.fromInt(accountTransferDTO.getTransferType());
            final AccountTransferAccountDetail fromSavingsAccount = this.accountTransferSavingsService
                    .retrieveAccountDetail(fromSavingsAccountId(accountTransferDTO));
            final AccountTransferAccountDetail toSavingsAccount = this.accountTransferSavingsService
                    .retrieveAccountDetail(toSavingsAccountId(accountTransferDTO));

            LocalDate transactionDate = accountTransferDTO.getTransactionDate();
            if (configurationDomainService.isSavingsInterestPostingAtCurrentPeriodEnd()
                    && configurationDomainService.isNextDayFixedDepositInterestTransferEnabledForPeriodEnd()
                    && transferType.isInterestTransfer()) {
                transactionDate = transactionDate.plusDays(1);
            }

            final Long withdrawalId = this.accountTransferSavingsService
                    .withdraw(SavingsTransferWithdrawal.builder().savingsAccountId(fromSavingsAccount.accountId())
                            .transactionDate(transactionDate).transactionAmount(accountTransferDTO.getTransactionAmount())
                            .formatter(accountTransferDTO.getFmt()).paymentDetail(accountTransferDTO.getPaymentDetail())
                            .regularTransaction(isRegularTransaction).interestTransfer(transferType.isInterestTransfer())
                            .exceptionForBalanceCheck(accountTransferDTO.isExceptionForBalanceCheck()).build());

            final Long depositId = this.accountTransferSavingsService.deposit(
                    SavingsTransferDeposit.builder().savingsAccountId(toSavingsAccount.accountId()).transactionDate(transactionDate)
                            .transactionAmount(accountTransferDTO.getTransactionAmount()).formatter(accountTransferDTO.getFmt())
                            .paymentDetail(accountTransferDTO.getPaymentDetail()).regularTransaction(isRegularTransaction).build());

            accountTransferDetails = this.accountTransferAssembler.assembleSavingsToSavingsTransfer(accountTransferDTO, fromSavingsAccount,
                    toSavingsAccount, withdrawalId, depositId);
            this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
            transferTransactionId = accountTransferDetails.getId();

        } else if (isLoanToSavingsAccountTransfer(accountTransferDTO.getFromAccountType(), accountTransferDTO.getToAccountType())) {

            final AccountTransferAccountDetail fromLoanAccount = this.accountTransferLoanService
                    .retrieveAccountDetail(fromLoanAccountId(accountTransferDTO));
            final AccountTransferAccountDetail toSavingsAccount = this.accountTransferSavingsService
                    .retrieveAccountDetail(toSavingsAccountId(accountTransferDTO));

            // Safety net (it might need to generate new one)
            final ExternalId externalId = externalIdFactory.create(accountTransferDTO.getTxnExternalId().getValue());

            final LoanTransferPayout payout = LoanTransferPayout.builder().loanId(fromLoanAccount.accountId())
                    .transactionDate(accountTransferDTO.getTransactionDate()).transactionAmount(accountTransferDTO.getTransactionAmount())
                    .paymentDetail(accountTransferDTO.getPaymentDetail()).noteText(accountTransferDTO.getNoteText())
                    .txnExternalId(externalId).build();

            final Long loanTransactionId;
            if (LoanTransactionType.DISBURSEMENT.getValue().equals(accountTransferDTO.getFromTransferType())) {
                loanTransactionId = this.accountTransferLoanService.disburse(payout);
            } else {
                loanTransactionId = this.accountTransferLoanService.refund(payout);
            }

            final Long depositId = this.accountTransferSavingsService.deposit(SavingsTransferDeposit.builder()
                    .savingsAccountId(toSavingsAccount.accountId()).transactionDate(accountTransferDTO.getTransactionDate())
                    .transactionAmount(accountTransferDTO.getTransactionAmount()).formatter(accountTransferDTO.getFmt())
                    .paymentDetail(accountTransferDTO.getPaymentDetail()).regularTransaction(isRegularTransaction).build());

            accountTransferDetails = this.accountTransferAssembler.assembleLoanToSavingsTransfer(accountTransferDTO, fromLoanAccount,
                    toSavingsAccount, depositId, loanTransactionId);
            this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);
            transferTransactionId = accountTransferDetails.getId();

            // if the savings account is GSIM, update its parent as well
            this.accountTransferSavingsService.addToGsimParentDeposit(toSavingsAccount.accountId(),
                    accountTransferDTO.getTransactionAmount());
        } else {
            throw new GeneralPlatformDomainRuleException("error.msg.accounttransfer.loan.to.loan.not.supported",
                    "Account transfer from loan to another loan is not supported");
        }

        return transferTransactionId;
    }

    @Override
    public AccountTransferDetails repayLoanWithTopup(AccountTransferDTO accountTransferDTO) {
        final AccountTransferAccountDetail fromLoanAccount = this.accountTransferLoanService
                .retrieveAccountDetail(accountTransferDTO.getFromAccountId());
        final AccountTransferAccountDetail toLoanAccount = this.accountTransferLoanService
                .retrieveAccountDetail(accountTransferDTO.getToAccountId());

        final Long disburseTransactionId = this.accountTransferLoanService.disburse(
                LoanTransferPayout.builder().loanId(fromLoanAccount.accountId()).transactionDate(accountTransferDTO.getTransactionDate())
                        .transactionAmount(accountTransferDTO.getTransactionAmount()).paymentDetail(accountTransferDTO.getPaymentDetail())
                        .noteText(accountTransferDTO.getNoteText()).txnExternalId(accountTransferDTO.getTxnExternalId())
                        .loanToLoanTransfer(true).build());

        final Long repayTransactionId = this.accountTransferLoanService.repay(
                LoanTransferRepayment.builder().loanId(toLoanAccount.accountId()).transactionDate(accountTransferDTO.getTransactionDate())
                        .transactionAmount(accountTransferDTO.getTransactionAmount()).paymentDetail(accountTransferDTO.getPaymentDetail())
                        .txnExternalId(externalIdFactory.create()).loanToLoanTransfer(true).build());

        AccountTransferDetails accountTransferDetails = this.accountTransferAssembler.assembleLoanToLoanTransfer(accountTransferDTO,
                fromLoanAccount, toLoanAccount, disburseTransactionId, repayTransactionId);
        this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);

        return accountTransferDetails;
    }

    @Override
    public CommandProcessingResult undo(JsonCommand command) {
        AccountTransferDetails accountTransferDetails = accountTransferDetailRepository.findById(command.entityId())
                .orElseThrow(() -> new AccountTransferNotFoundException(command.entityId()));

        if (accountTransferDetails.getAccountTransferTransactions().stream().anyMatch(AccountTransferTransaction::isReversed)) {
            throw new GeneralPlatformDomainRuleException("error.msg.account.transfer.already.reversed",
                    "Account transfer is already reverted", command.entityId());
        }

        PortfolioAccountType fromAccountType = accountTransferDetails.getFromLoanAccountId() != null ? PortfolioAccountType.LOAN
                : accountTransferDetails.getFromSavingsAccountId() != null ? PortfolioAccountType.SAVINGS : throwUnsupported();

        PortfolioAccountType toAccountType = accountTransferDetails.getToLoanAccountId() != null ? PortfolioAccountType.LOAN
                : accountTransferDetails.getToSavingsAccountId() != null ? PortfolioAccountType.SAVINGS : throwUnsupported();

        if (isSavingsToSavingsAccountTransfer(fromAccountType, toAccountType)) {
            accountTransferDetails.getAccountTransferTransactions().forEach(transaction -> {
                this.accountTransferSavingsService.undoTransferTransaction(accountTransferDetails.getFromSavingsAccountId(),
                        transaction.getFromSavingsTransactionId());
                this.accountTransferSavingsService.undoTransferTransaction(accountTransferDetails.getToSavingsAccountId(),
                        transaction.getToSavingsTransactionId());
                transaction.reverse();
            });
        } else if (isSavingsToLoanAccountTransfer(fromAccountType, toAccountType)) {
            accountTransferDetails.getAccountTransferTransactions().forEach(transaction -> {
                this.accountTransferSavingsService.undoTransferTransaction(accountTransferDetails.getFromSavingsAccountId(),
                        transaction.getFromSavingsTransactionId());
                this.accountTransferLoanService.adjustTransferTransaction(transaction.getToLoanTransactionId(), externalIdFactory.create());
                transaction.reverse();
            });
        } else if (isLoanToSavingsAccountTransfer(fromAccountType, toAccountType)) {
            throw new UnsupportedOperationException("Undo Loan to Savings Account Transfer is not implemented");
        }

        final CommandProcessingResultBuilder builder = new CommandProcessingResultBuilder() //
                .withEntityId(accountTransferDetails.getId());

        return builder.build();
    }

    private static PortfolioAccountType throwUnsupported() {
        throw new UnsupportedOperationException("Undo account transfer only be supported between Loan and Saving accounts");
    }

    private Long fromSavingsAccountId(final AccountTransferDTO accountTransferDTO) {
        final AccountTransferDetails details = accountTransferDTO.getAccountTransferDetails();
        return details == null ? accountTransferDTO.getFromAccountId() : details.getFromSavingsAccountId();
    }

    private Long toSavingsAccountId(final AccountTransferDTO accountTransferDTO) {
        final AccountTransferDetails details = accountTransferDTO.getAccountTransferDetails();
        return details == null ? accountTransferDTO.getToAccountId() : details.getToSavingsAccountId();
    }

    private Long fromLoanAccountId(final AccountTransferDTO accountTransferDTO) {
        final AccountTransferDetails details = accountTransferDTO.getAccountTransferDetails();
        return details == null ? accountTransferDTO.getFromAccountId() : details.getFromLoanAccountId();
    }

    private Long toLoanAccountId(final AccountTransferDTO accountTransferDTO) {
        final AccountTransferDetails details = accountTransferDTO.getAccountTransferDetails();
        return details == null ? accountTransferDTO.getToAccountId() : details.getToLoanAccountId();
    }

    private boolean isLoanToSavingsAccountTransfer(final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
        return PortfolioAccountType.LOAN.equals(fromAccountType) && PortfolioAccountType.SAVINGS.equals(toAccountType);
    }

    private boolean isSavingsToLoanAccountTransfer(final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType) {
        return PortfolioAccountType.SAVINGS.equals(fromAccountType) && PortfolioAccountType.LOAN.equals(toAccountType);
    }

    private boolean isSavingsToSavingsAccountTransfer(final PortfolioAccountType fromAccountType,
            final PortfolioAccountType toAccountType) {
        return PortfolioAccountType.SAVINGS.equals(fromAccountType) && PortfolioAccountType.SAVINGS.equals(toAccountType);
    }

    @Override
    @Transactional
    public CommandProcessingResult refundByTransfer(JsonCommand command) {
        this.accountTransfersDataValidator.validate(command);

        final LocalDate transactionDate = command.localDateValueOfParameterNamed(transferDateParamName);
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed(transferAmountParamName);

        final Locale locale = command.extractLocale();
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);

        final PaymentDetail paymentDetail = null;

        final Long fromLoanAccountId = command.longValueOfParameterNamed(fromAccountIdParamName);
        final AccountTransferAccountDetail fromLoanAccount = this.accountTransferLoanService.retrieveAccountDetail(fromLoanAccountId);

        final BigDecimal overpaid = this.accountTransferLoanService.retrievePaidInAdvance(fromLoanAccountId);

        if (overpaid.compareTo(BigDecimal.ZERO) == 0 || transactionAmount.floatValue() > overpaid.floatValue()) {
            throw new InvalidPaidInAdvanceAmountException(overpaid.toPlainString());
        }

        final Long loanRefundTransactionId = this.accountTransferLoanService.refundForActiveLoan(
                LoanTransferPayout.builder().loanId(fromLoanAccountId).transactionDate(transactionDate).transactionAmount(transactionAmount)
                        .paymentDetail(paymentDetail).txnExternalId(externalIdFactory.create()).build());

        final Long toSavingsAccountId = command.longValueOfParameterNamed(toAccountIdParamName);
        final AccountTransferAccountDetail toSavingsAccount = this.accountTransferSavingsService.retrieveAccountDetail(toSavingsAccountId);

        final Long depositId = this.accountTransferSavingsService
                .deposit(SavingsTransferDeposit.builder().savingsAccountId(toSavingsAccountId).transactionDate(transactionDate)
                        .transactionAmount(transactionAmount).formatter(fmt).paymentDetail(paymentDetail).regularTransaction(true).build());

        final AccountTransferDetails accountTransferDetails = this.accountTransferAssembler.assembleLoanToSavingsTransfer(command,
                fromLoanAccount, toSavingsAccount, depositId, loanRefundTransactionId);
        this.accountTransferDetailRepository.saveAndFlush(accountTransferDetails);

        return new CommandProcessingResultBuilder() //
                .withEntityId(accountTransferDetails.getId()) //
                .withSavingsId(toSavingsAccountId) //
                .build();
    }
}
