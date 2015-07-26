package com.j256.ormlite.field.types;

import com.j256.ormlite.field.DataType;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class UnknownTypeTest extends BaseTypeTest {

	@Test
	public void testUnknownGetResult() {
		DataType dataType = DataType.UNKNOWN;
		assertNull(dataType.getDataPersister());
	}
}
