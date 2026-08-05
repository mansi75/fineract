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
package org.apache.fineract.portfolio.account.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;

@Getter
public class AccountTransferDTO {

    private final LocalDate transactionDate;
    private final BigDecimal transactionAmount;
    private final PortfolioAccountType fromAccountType;
    private final PortfolioAccountType toAccountType;
    private final Long fromAccountId;
    private final Long toAccountId;
    private final String description;
    private final Locale locale;
    private final DateTimeFormatter fmt;
    private final PaymentDetail paymentDetail;
    private final Integer fromTransferType;
    private final Integer toTransferType;
    private final Long chargeId;
    private final Integer loanInstallmentNumber;
    private final Integer transferType;
    private final AccountTransferDetails accountTransferDetails;
    private final String noteText;
    private final ExternalId txnExternalId;
    private final Boolean isRegularTransaction;
    private final Boolean isExceptionForBalanceCheck;

    public AccountTransferDTO(final LocalDate transactionDate, final BigDecimal transactionAmount,
            final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType, final Long fromAccountId,
            final Long toAccountId, final String description, final Locale locale, final DateTimeFormatter fmt,
            final PaymentDetail paymentDetail, final Integer fromTransferType, final Integer toTransferType, final Long chargeId,
            Integer loanInstallmentNumber, Integer transferType, final AccountTransferDetails accountTransferDetails, final String noteText,
            final ExternalId txnExternalId, final Boolean isRegularTransaction, Boolean isExceptionForBalanceCheck) {
        this.transactionDate = transactionDate;
        this.transactionAmount = transactionAmount;
        this.fromAccountType = fromAccountType;
        this.toAccountType = toAccountType;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.description = description;
        this.locale = locale;
        this.fmt = fmt;
        this.paymentDetail = paymentDetail;
        this.fromTransferType = fromTransferType;
        this.toTransferType = toTransferType;
        this.chargeId = chargeId;
        this.loanInstallmentNumber = loanInstallmentNumber;
        this.transferType = transferType;
        this.accountTransferDetails = accountTransferDetails;
        this.noteText = noteText;
        this.txnExternalId = txnExternalId;
        this.isRegularTransaction = isRegularTransaction;
        this.isExceptionForBalanceCheck = isExceptionForBalanceCheck;
    }

    public AccountTransferDTO(final LocalDate transactionDate, final BigDecimal transactionAmount,
            final PortfolioAccountType fromAccountType, final PortfolioAccountType toAccountType, final Long fromAccountId,
            final Long toAccountId, final String description, final Locale locale, final DateTimeFormatter fmt,
            final Integer fromTransferType, final Integer toTransferType, final ExternalId txnExternalId) {
        this(transactionDate, transactionAmount, fromAccountType, toAccountType, fromAccountId, toAccountId, description, locale, fmt, null,
                fromTransferType, toTransferType, null, null, null, null, null, txnExternalId, null, null);
    }

    public Boolean isRegularTransaction() {
        return this.isRegularTransaction;
    }

    public Boolean isExceptionForBalanceCheck() {
        return this.isExceptionForBalanceCheck;
    }
}
