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
package org.apache.fineract.accounting.provisioning.service;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.provisioning.contract.ProvisioningEntryReadService;
import org.springframework.stereotype.Service;

/**
 * Accounting implementation of the contract declared by the organisation module. Keeping the implementation here means
 * the organisation module depends only on the interface it owns, and the accounting module supplies the details at
 * runtime.
 */
@Service
@RequiredArgsConstructor
public class ProvisioningEntryReadServiceImpl implements ProvisioningEntryReadService {

    private final ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService;

    @Override
    public boolean existsProvisioningEntryForCriteria(final Long criteriaId) {
        return provisioningEntriesReadPlatformService.retrieveProvisioningEntryDataByCriteriaId(criteriaId) != null;
    }
}
