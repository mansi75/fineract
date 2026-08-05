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
package org.apache.fineract.portfolio.shareaccounts.contract;

import java.util.List;
import org.apache.fineract.portfolio.charge.data.ChargeData;

/**
 * Read contract the share account feature needs from the charge feature. Declared here so share accounts never compile
 * against the charge aggregate; the charge feature owns the implementation.
 */
public interface ShareAccountChargeReadService {

    /**
     * @throws org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException
     *             when no charge exists for the given id
     */
    ShareChargeDefinitionData findChargeDefinition(Long chargeId);

    /**
     * @return the charges that can be attached to a share account, empty when there are none
     */
    List<ChargeData> retrieveSharesApplicableCharges();

    /**
     * @return the charges configured on the given share product, empty when there are none
     */
    List<ChargeData> retrieveShareProductCharges(Long shareProductId);
}
