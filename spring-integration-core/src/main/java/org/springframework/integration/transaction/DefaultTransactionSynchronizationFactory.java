/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link TransactionSynchronizationFactory} which takes an instance of
 * {@link TransactionSynchronizationProcessor} allowing you to create a {@link TransactionSynchronization}
 * using {{@link #create(Object)} method.
 *
 * @author Gary Russell
 * @author Oleg Zhurakousky
 * @author Artem Bilan
 *
 * @since 2.2
 */
public class DefaultTransactionSynchronizationFactory implements TransactionSynchronizationFactory {

	private final Log logger = LogFactory.getLog(getClass());

	private final TransactionSynchronizationProcessor processor;

	public DefaultTransactionSynchronizationFactory(TransactionSynchronizationProcessor processor) {
		Assert.notNull(processor, "'processor' must not be null");
		this.processor = processor;
	}

	@Override
	public TransactionSynchronization create(Object key) {
		Assert.notNull(key, "'key' must not be null");
		return new DefaultTransactionalResourceSynchronization(key);
	}

	private final class DefaultTransactionalResourceSynchronization extends IntegrationResourceHolderSynchronization {

		DefaultTransactionalResourceSynchronization(Object resourceKey) {
			super(new IntegrationResourceHolder(), resourceKey);
		}

		@Override
		public void beforeCommit(boolean readOnly) {
			if (DefaultTransactionSynchronizationFactory.this.logger.isTraceEnabled()) {
				DefaultTransactionSynchronizationFactory.this.logger.trace("'pre-Committing' transactional resource");
			}
			DefaultTransactionSynchronizationFactory.this.processor.processBeforeCommit(resourceHolder);
		}

		@Override
		protected boolean shouldReleaseBeforeCompletion() {
			return false;
		}

		@Override
		protected void processResourceAfterCommit(IntegrationResourceHolder resourceHolder) {

			if (DefaultTransactionSynchronizationFactory.this.logger.isTraceEnabled()) {
				DefaultTransactionSynchronizationFactory.this.logger.trace("'Committing' transactional resource");
			}

			DefaultTransactionSynchronizationFactory.this.processor.processAfterCommit(resourceHolder);

		}

		@Override
		public void afterCompletion(int status) {
			if (status != TransactionSynchronization.STATUS_COMMITTED) {
				if (DefaultTransactionSynchronizationFactory.this.logger.isTraceEnabled()) {
					DefaultTransactionSynchronizationFactory.this.logger.trace("'Rolling back' transactional resource");
				}

				DefaultTransactionSynchronizationFactory.this.processor.processAfterRollback(resourceHolder);

			}
			super.afterCompletion(status);
		}

	}

}
