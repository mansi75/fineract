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

/**
 * Core read-contract answering whether provisioning entries have already been generated for a provisioning criteria, so
 * that the organisation module can refuse to delete a criteria in use without a compile-time dependency on the
 * accounting provisioning entry types. Implemented by the accounting module.
 */
public interface ProvisioningEntryReadService {

    boolean existsProvisioningEntryForCriteria(Long criteriaId);
}
