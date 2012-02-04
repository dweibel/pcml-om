package test.pcml_om;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.pcml_om.PcmlCallException;
import org.pcml_om.PcmlConverter;
import org.pcml_om.map.PcmlElement;
import org.pcml_om.map.PojoElement;

import static org.mockito.Mockito.*;

public class TestPcmlConverter {

	PcmlConverter pcmlConverter;
	PcmlElement pcmlElement;
	PojoElement pojoElement;
	PojoTestClass pojoDao = new PojoTestClass();
	
	@Before
	public void setUp() throws Exception {
		pcmlConverter = new PcmlConverter("yyyyMMdd");
		pcmlElement = mock(PcmlElement.class);
	}

	@Test
	public void testGetString() throws PcmlCallException {
		pojoElement = new PojoElement();
		when(pcmlElement.getPojoElement()).thenReturn(pojoElement);
		String fieldname = pojoDao.getClass().getName() + ".string";
		pojoElement.setPojoFieldName(fieldname);
		pojoElement.setPojoFieldType(String.class);
		BigDecimal bd = new BigDecimal(12345);
		pcmlConverter.get(pcmlElement, bd, pojoDao);
		assert(pojoDao.getString().equals(bd.toString()));
	}
	
	@Test
	public void testGetByte() throws PcmlCallException {
		pojoElement = new PojoElement();
		when(pcmlElement.getPojoElement()).thenReturn(pojoElement);
		String fieldname = pojoDao.getClass().getName() + ".objByte";
		pojoElement.setPojoFieldName(fieldname);
		pojoElement.setPojoFieldType(Byte.class);
		Byte val = new Byte((byte)7);
		pcmlConverter.get(pcmlElement, val, pojoDao);
		assertEquals(pojoDao.getObjByte(), val);
	}
	
	@Test
	public void testGetScalarByte() throws PcmlCallException {
		pojoElement = new PojoElement();
		when(pcmlElement.getPojoElement()).thenReturn(pojoElement);
		String fieldname = pojoDao.getClass().getName() + ".scalarByte";
		pojoElement.setPojoFieldName(fieldname);
		pojoElement.setPojoFieldType(byte.class);
		byte val = 7;
		pcmlConverter.get(pcmlElement, val, pojoDao);
		assertEquals(pojoDao.getScalarByte(), val);
	}
	
}
