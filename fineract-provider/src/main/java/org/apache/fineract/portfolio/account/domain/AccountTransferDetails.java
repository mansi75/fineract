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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.client.domain.Client;

@Entity
@Table(name = "m_account_transfer_details")
@Getter
public class AccountTransferDetails extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "from_office_id", nullable = false)
    private Office fromOffice;

    @ManyToOne
    @JoinColumn(name = "from_client_id", nullable = false)
    private Client fromClient;

    @Column(name = "from_savings_account_id")
    private Long fromSavingsAccountId;

    @ManyToOne
    @JoinColumn(name = "to_office_id", nullable = false)
    private Office toOffice;

    @ManyToOne
    @JoinColumn(name = "to_client_id", nullable = false)
    private Client toClient;

    @Column(name = "to_savings_account_id")
    private Long toSavingsAccountId;

    @Column(name = "to_loan_account_id")
    private Long toLoanAccountId;

    @Column(name = "from_loan_account_id")
    private Long fromLoanAccountId;

    @Column(name = "transfer_type")
    private Integer transferType;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "accountTransferDetails", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AccountTransferTransaction> accountTransferTransactions = new ArrayList<>();

    @OneToOne(mappedBy = "accountTransferDetails", cascade = CascadeType.ALL, optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
    private AccountTransferStandingInstruction accountTransferStandingInstruction;

    public static AccountTransferDetails savingsToSavingsTransfer(final Office fromOffice, final Client fromClient,
            final Long fromSavingsAccountId, final Office toOffice, final Client toClient, final Long toSavingsAccountId,
            Integer transferType) {

        return new AccountTransferDetails(fromOffice, fromClient, fromSavingsAccountId, null, toOffice, toClient, toSavingsAccountId, null,
                transferType, null);
    }

    public static AccountTransferDetails savingsToLoanTransfer(final Office fromOffice, final Client fromClient,
            final Long fromSavingsAccountId, final Office toOffice, final Client toClient, final Long toLoanAccountId,
            Integer transferType) {
        return new AccountTransferDetails(fromOffice, fromClient, fromSavingsAccountId, null, toOffice, toClient, null, toLoanAccountId,
                transferType, null);
    }

    public static AccountTransferDetails loanTosavingsTransfer(final Office fromOffice, final Client fromClient,
            final Long fromLoanAccountId, final Office toOffice, final Client toClient, final Long toSavingsAccountId,
            Integer transferType) {
        return new AccountTransferDetails(fromOffice, fromClient, null, fromLoanAccountId, toOffice, toClient, toSavingsAccountId, null,
                transferType, null);
    }

    protected AccountTransferDetails() {
        //
    }

    private AccountTransferDetails(final Office fromOffice, final Client fromClient, final Long fromSavingsAccountId,
            final Long fromLoanAccountId, final Office toOffice, final Client toClient, final Long toSavingsAccountId,
            final Long toLoanAccountId, final Integer transferType,
            final AccountTransferStandingInstruction accountTransferStandingInstruction) {
        this.fromOffice = fromOffice;
        this.fromClient = fromClient;
        this.fromSavingsAccountId = fromSavingsAccountId;
        this.fromLoanAccountId = fromLoanAccountId;
        this.toOffice = toOffice;
        this.toClient = toClient;
        this.toSavingsAccountId = toSavingsAccountId;
        this.toLoanAccountId = toLoanAccountId;
        this.transferType = transferType;
        this.accountTransferStandingInstruction = accountTransferStandingInstruction;
    }

    public void addAccountTransferTransaction(AccountTransferTransaction accountTransferTransaction) {
        this.accountTransferTransactions.add(accountTransferTransaction);
    }

    public void updateAccountTransferStandingInstruction(final AccountTransferStandingInstruction accountTransferStandingInstruction) {
        this.accountTransferStandingInstruction = accountTransferStandingInstruction;
    }

    public AccountTransferStandingInstruction accountTransferStandingInstruction() {
        return this.accountTransferStandingInstruction;
    }

    public AccountTransferType transferType() {
        return AccountTransferType.fromInt(this.transferType);
    }

    public static AccountTransferDetails loanToLoanTransfer(Office fromOffice, Client fromClient, Long fromLoanAccountId, Office toOffice,
            Client toClient, Long toLoanAccountId, Integer transferType) {
        return new AccountTransferDetails(fromOffice, fromClient, null, fromLoanAccountId, toOffice, toClient, null, toLoanAccountId,
                transferType, null);
    }
}
