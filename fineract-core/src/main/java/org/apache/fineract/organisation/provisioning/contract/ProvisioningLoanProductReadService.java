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
package org.apache.fineract.organisation.provisioning.contract;

import java.util.List;
import java.util.Set;

/**
 * Core read-contract exposing only the loan product lookups required by the provisioning feature, so that the
 * organisation module does not need a compile-time dependency on the loan product domain or its read service.
 * Implemented by the loan product read service.
 */
public interface ProvisioningLoanProductReadService {

    List<Long> retrieveAllLoanProductIds();

    List<ProvisioningLoanProductData> retrieveActiveLoanProductsForLookup();

    List<ProvisioningLoanProductData> retrieveLoanProductsByIds(Set<Long> loanProductIds);
}
