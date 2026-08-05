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

import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromClientIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromOfficeIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toClientIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toOfficeIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.transferTypeParamName;

import com.google.gson.JsonElement;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.portfolio.account.contract.AccountTransferAccountDetail;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountTransferDetailAssembler {

    private final ClientRepositoryWrapper clientRepository;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final FromJsonHelper fromApiJsonHelper;

    public AccountTransferDetails assembleSavingsToSavingsTransfer(final JsonCommand command, final Long fromSavingsAccountId,
            final Long toSavingsAccountId) {

        final TransferParties parties = extractParties(command);

        return AccountTransferDetails.savingsToSavingsTransfer(parties.fromOffice(), parties.fromClient(), fromSavingsAccountId,
                parties.toOffice(), parties.toClient(), toSavingsAccountId, parties.transferType());
    }

    public AccountTransferDetails assembleSavingsToLoanTransfer(final JsonCommand command, final Long fromSavingsAccountId,
            final Long toLoanAccountId) {

        final TransferParties parties = extractParties(command);

        return AccountTransferDetails.savingsToLoanTransfer(parties.fromOffice(), parties.fromClient(), fromSavingsAccountId,
                parties.toOffice(), parties.toClient(), toLoanAccountId, parties.transferType());
    }

    public AccountTransferDetails assembleLoanToSavingsTransfer(final JsonCommand command, final Long fromLoanAccountId,
            final Long toSavingsAccountId) {

        final TransferParties parties = extractParties(command);

        return AccountTransferDetails.loanTosavingsTransfer(parties.fromOffice(), parties.fromClient(), fromLoanAccountId,
                parties.toOffice(), parties.toClient(), toSavingsAccountId, parties.transferType());
    }

    public AccountTransferDetails assembleSavingsToLoanTransfer(final AccountTransferAccountDetail fromSavingsAccount,
            final AccountTransferAccountDetail toLoanAccount, final Integer transferType) {

        return AccountTransferDetails.savingsToLoanTransfer(office(fromSavingsAccount), client(fromSavingsAccount),
                fromSavingsAccount.accountId(), office(toLoanAccount), client(toLoanAccount), toLoanAccount.accountId(), transferType);
    }

    public AccountTransferDetails assembleSavingsToSavingsTransfer(final AccountTransferAccountDetail fromSavingsAccount,
            final AccountTransferAccountDetail toSavingsAccount, final Integer transferType) {

        return AccountTransferDetails.savingsToSavingsTransfer(office(fromSavingsAccount), client(fromSavingsAccount),
                fromSavingsAccount.accountId(), office(toSavingsAccount), client(toSavingsAccount), toSavingsAccount.accountId(),
                transferType);
    }

    public AccountTransferDetails assembleLoanToSavingsTransfer(final AccountTransferAccountDetail fromLoanAccount,
            final AccountTransferAccountDetail toSavingsAccount, final Integer transferType) {

        return AccountTransferDetails.loanTosavingsTransfer(office(fromLoanAccount), client(fromLoanAccount), fromLoanAccount.accountId(),
                office(toSavingsAccount), client(toSavingsAccount), toSavingsAccount.accountId(), transferType);
    }

    public AccountTransferDetails assembleLoanToLoanTransfer(final AccountTransferAccountDetail fromLoanAccount,
            final AccountTransferAccountDetail toLoanAccount, final Integer transferType) {

        return AccountTransferDetails.loanToLoanTransfer(office(fromLoanAccount), client(fromLoanAccount), fromLoanAccount.accountId(),
                office(toLoanAccount), client(toLoanAccount), toLoanAccount.accountId(), transferType);
    }

    private TransferParties extractParties(final JsonCommand command) {
        final JsonElement element = command.parsedJson();

        final Long fromOfficeId = this.fromApiJsonHelper.extractLongNamed(fromOfficeIdParamName, element);
        final Long fromClientId = this.fromApiJsonHelper.extractLongNamed(fromClientIdParamName, element);
        final Long toOfficeId = this.fromApiJsonHelper.extractLongNamed(toOfficeIdParamName, element);
        final Long toClientId = this.fromApiJsonHelper.extractLongNamed(toClientIdParamName, element);
        final Integer transferType = this.fromApiJsonHelper.extractIntegerNamed(transferTypeParamName, element, Locale.getDefault());

        return new TransferParties(this.officeRepositoryWrapper.findOneWithNotFoundDetection(fromOfficeId),
                this.clientRepository.findOneWithNotFoundDetection(fromClientId),
                this.officeRepositoryWrapper.findOneWithNotFoundDetection(toOfficeId),
                this.clientRepository.findOneWithNotFoundDetection(toClientId), transferType);
    }

    private Office office(final AccountTransferAccountDetail account) {
        return account.officeId() == null ? null : this.officeRepositoryWrapper.findOneWithNotFoundDetection(account.officeId());
    }

    /**
     * Group owned accounts have no client, so this stays nullable exactly as the previous account-to-account navigation
     * was.
     */
    private Client client(final AccountTransferAccountDetail account) {
        return account.clientId() == null ? null : this.clientRepository.findOneWithNotFoundDetection(account.clientId());
    }

    private record TransferParties(Office fromOffice, Client fromClient, Office toOffice, Client toClient, Integer transferType) {
    }
}
