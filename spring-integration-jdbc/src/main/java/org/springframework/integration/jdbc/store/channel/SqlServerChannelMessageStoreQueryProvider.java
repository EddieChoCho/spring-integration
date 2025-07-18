/*
 * Copyright 2018-present the original author or authors.
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

package org.springframework.integration.jdbc.store.channel;

/**
 * Channel message store query provider for Microsoft SQL Server / Azure SQL database.
 *
 * @author Sundara Balaji
 * @author Adama Sorho
 * @author Artem Bilan
 *
 * @since 5.1
 */
public class SqlServerChannelMessageStoreQueryProvider implements ChannelMessageStoreQueryProvider {

	@Override
	public String getPollFromGroupExcludeIdsQuery() {
		return SELECT_COMMON +
				"and %PREFIX%CHANNEL_MESSAGE.MESSAGE_ID not in (:message_ids) order by CREATED_DATE, MESSAGE_SEQUENCE";
	}

	@Override
	public String getPollFromGroupQuery() {
		return SELECT_COMMON +
				"order by CREATED_DATE, MESSAGE_SEQUENCE";
	}

	@Override
	public String getPriorityPollFromGroupExcludeIdsQuery() {
		return SELECT_COMMON +
				"and %PREFIX%CHANNEL_MESSAGE.MESSAGE_ID not in (:message_ids) " +
				"order by MESSAGE_PRIORITY DESC, CREATED_DATE, MESSAGE_SEQUENCE";
	}

	@Override
	public String getPriorityPollFromGroupQuery() {
		return SELECT_COMMON +
				"order by MESSAGE_PRIORITY DESC, CREATED_DATE, MESSAGE_SEQUENCE";
	}

	@Override
	public String getCreateMessageQuery() {
		return """
				INSERT into %PREFIX%CHANNEL_MESSAGE(
					MESSAGE_ID,
					GROUP_KEY,
					REGION,
					CREATED_DATE,
					MESSAGE_PRIORITY,
					MESSAGE_SEQUENCE,
					MESSAGE_BYTES)
				values (?, ?, ?, ?, ?,(NEXT VALUE FOR %PREFIX%MESSAGE_SEQ), ?)
				""";
	}

}
