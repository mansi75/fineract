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
package org.apache.fineract.portfolio.charge.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.shareaccounts.contract.ShareAccountChargeReadService;
import org.apache.fineract.portfolio.shareaccounts.contract.ShareChargeDefinitionData;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShareAccountChargeReadServiceImpl implements ShareAccountChargeReadService {

    private final ChargeRepositoryWrapper chargeRepository;
    private final ChargeReadPlatformService chargeReadPlatformService;

    @Override
    public ShareChargeDefinitionData findChargeDefinition(final Long chargeId) {
        final Charge charge = this.chargeRepository.findOneWithNotFoundDetection(chargeId);
        return new ShareChargeDefinitionData(charge.getId(), charge.getName(), charge.getCurrencyCode(), charge.getChargeTimeType(),
                charge.getChargeCalculation(), charge.getAmount(), charge.isActive());
    }

    @Override
    public List<ChargeData> retrieveSharesApplicableCharges() {
        return this.chargeReadPlatformService.retrieveSharesApplicableCharges();
    }

    @Override
    public List<ChargeData> retrieveShareProductCharges(final Long shareProductId) {
        return this.chargeReadPlatformService.retrieveShareProductCharges(shareProductId);
    }
}
