package org.pcml_om;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.pcml_om.PcmlCallException;
import org.pcml_om.PojoConverter;
import org.pcml_om.map.PcmlElement;
import org.pcml_om.map.PojoElement;

public class TestPojoConverter {

	PojoConverter pojoConverter;
	PcmlElement pcmlElement;
	PojoElement pojoElement;
	PojoTestClass pojoDao = new PojoTestClass();
	
	@Before
	public void setUp() throws Exception {
		pojoConverter = new PojoConverter("yyyyMMdd");
		pcmlElement = mock(PcmlElement.class);
	}

	@Test
	public void testGetString() throws PcmlCallException {
		pojoElement = new PojoElement();
		when(pcmlElement.getPojoElement()).thenReturn(pojoElement);
		when(pcmlElement.getType()).thenReturn(PcmlElement.CHAR);
		String fieldname = pojoDao.getClass().getName() + ".string";
		pojoElement.setPojoFieldName(fieldname);
		pojoElement.setPojoFieldType(String.class);
		pojoDao.setString("Test 123");
		Object val = pojoConverter.get(pcmlElement, pojoDao);
		assertEquals(pojoDao.getString(), val);
	}
	
	@Test
	public void testGetByte() throws PcmlCallException {
		pojoElement = new PojoElement();
		when(pcmlElement.getPojoElement()).thenReturn(pojoElement);
		when(pcmlElement.getType()).thenReturn(PcmlElement.BYTE);
		String fieldname = pojoDao.getClass().getName() + ".objByte";
		pojoElement.setPojoFieldName(fieldname);
		pojoElement.setPojoFieldType(Byte.class);
		pojoDao.setObjByte(new Byte((byte)7));
		Object val = pojoConverter.get(pcmlElement, pojoDao);
		assertEquals(pojoDao.getObjByte(), new Byte(val.toString()));
	}

	@Test
	public void testGetScalarByte() throws PcmlCallException {
		pojoElement = new PojoElement();
		when(pcmlElement.getPojoElement()).thenReturn(pojoElement);
		when(pcmlElement.getType()).thenReturn(PcmlElement.BYTE);
		String fieldname = pojoDao.getClass().getName() + ".scalarByte";
		pojoElement.setPojoFieldName(fieldname);
		pojoElement.setPojoFieldType(byte.class);
		pojoDao.setScalarByte((byte)7);
		Object val = pojoConverter.get(pcmlElement, pojoDao);
		assertEquals(pojoDao.getScalarByte(), Byte.parseByte(val.toString()));
	}

}
