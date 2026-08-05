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
package org.apache.fineract.accounting.producttoaccountmapping.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.fixeddeposit.contract.FixedDepositAccountingWriteService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.springframework.stereotype.Service;

/**
 * Accounting module implementation of the contract declared by fineract-core for the fixed deposit feature. The
 * {@link DepositAccountType} discriminator is applied here so the fixed deposit write service passes only the product
 * id and the command across the boundary.
 */
@Service
@RequiredArgsConstructor
public class FixedDepositAccountingWriteServiceImpl implements FixedDepositAccountingWriteService {

    private final ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService;

    @Override
    public void createProductToGLAccountMapping(final Long productId, final JsonCommand command) {
        this.accountMappingWritePlatformService.createSavingProductToGLAccountMapping(productId, command, DepositAccountType.FIXED_DEPOSIT);
    }

    @Override
    public Map<String, Object> updateProductToGLAccountMapping(final Long productId, final JsonCommand command,
            final boolean accountingRuleChanged, final Integer accountingRuleType) {
        return this.accountMappingWritePlatformService.updateSavingsProductToGLAccountMapping(productId, command, accountingRuleChanged,
                accountingRuleType, DepositAccountType.FIXED_DEPOSIT);
    }
}
