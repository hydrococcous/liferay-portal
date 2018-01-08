/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.consumer.talend.avro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.IndexedRecord;

import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.LogicalTypeUtils;
import org.talend.daikon.avro.SchemaConstants;
import org.talend.daikon.avro.converter.AvroConverter;
import org.talend.daikon.avro.converter.string.StringBooleanConverter;
import org.talend.daikon.avro.converter.string.StringIntConverter;
import org.talend.daikon.avro.converter.string.StringLongConverter;
import org.talend.daikon.avro.converter.string.StringStringConverter;
import org.talend.daikon.avro.converter.string.StringTimestampConverter;

/**
 * @author Zoltán Takács
 *
 * Converts data row as List<Object> to {@link IndexedRecord} using schema to
 * guess value type
 */
@SuppressWarnings("rawtypes")
public class ResourceEntityConverter
	extends AbstractAvroConverter<List, IndexedRecord> {

	/**
	 * Constructor sets outgoing record schema and {@link List} class as datum
	 * class
	 *
	 * @param clazz
	 * @param schema
	 */
	public ResourceEntityConverter(Schema schema) {
		super(List.class, schema);
		_initConverters(schema);
	}

	@Override
	@SuppressWarnings("unchecked")
	public IndexedRecord convertToAvro(List row) {
		IndexedRecord record = new GenericData.Record(getSchema());

		for (int i = 0; i < row.size(); i++) {
			Object value = _converters[i].convertToAvro(row.get(i));

			record.put(i, value);
		}

		return record;
	}

	/**
	 * @throws UnsupportedOperationException as this method is not supported yet
	 */
	@Override
	public List<Object> convertToDatum(IndexedRecord value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Initialize converters per each schema field
	 *
	 * @param schema design schema
	 */
	private void _initConverters(Schema schema) {
		_converters = new AvroConverter[schema.getFields().size()];
		List<Field> fields = schema.getFields();

		for (int i = 0; i < schema.getFields().size(); i++) {
			Field field = fields.get(i);

			Schema fieldSchema = field.schema();

			fieldSchema = AvroUtils.unwrapIfNullable(fieldSchema);

			if (LogicalTypeUtils.isLogicalTimestampMillis(fieldSchema)) {
				String datePattern = field.getProp(
					SchemaConstants.TALEND_COLUMN_PATTERN);

				_converters[i] = new StringTimestampConverter(datePattern);
			}
			else {
				Type type = fieldSchema.getType();

				_converters[i] = _converterRegistry.get(type);
			}
		}
	}

	/**
	 * Contains available {@link StringConverter}.
	 */
	private static final Map<Type, AvroConverter> _converterRegistry;

	/**
	 * Fill in converter registry
	 */
	static {
		_converterRegistry = new HashMap<>();

		_converterRegistry.put(Type.BOOLEAN, new StringBooleanConverter());
		_converterRegistry.put(Type.INT, new StringIntConverter());
		_converterRegistry.put(Type.LONG, new StringLongConverter());
		_converterRegistry.put(Type.STRING, new StringStringConverter());
	}

	/**
	 * Stores converters. Array index corresponds to field index
	 */
	private AvroConverter[] _converters;

}