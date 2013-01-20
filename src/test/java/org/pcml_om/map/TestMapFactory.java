package org.pcml_om.map;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.pcml_om.map.PcmlPojoMap;
import org.pcml_om.map.MapFactory;
import org.pcml_om.map.PcmlElement;
import org.pcml_om.map.PcmlStruct;
import org.pcml_om.map.PojoElement;

public class TestMapFactory {

	PcmlPojoMap pcmlPojoMap = null;
	
	@Before
	public void setUp() throws Exception {
		InputStream pcmlIs = getClass().getResourceAsStream("as400.pcml");
		InputStream pojoIs = getClass().getResourceAsStream("pcmlPojoMap.xml");
		pcmlPojoMap = MapFactory.getPcmlPojoMap(pcmlIs, pojoIs);
	}

	@Test
	public void test() {
		Map<String, PcmlStruct> pcmlStructMap = pcmlPojoMap.getPcmlStructMap("OMTEST100");
		PcmlStruct ps = null;
		PcmlElement pe = null;
		PojoElement pojo = null;
		ps = pcmlStructMap.get("struct_LINK-INTERNET-SESSION");
		assert("OMTEST100.LINK-INTERNET-SESSION.".equals(ps.getPreamble()));
		pe = ps.getElement("NRSESSID");
		assert("test.pcml_om.map.PojoStruct1.sessionId".equals(pe.getPojoElement().getPojoFieldName()));
		ps = pcmlStructMap.get("[OMTEST100]");
		assert("OMTEST100.".equals(ps.getPreamble()));
		pe = ps.getElement("LINK-INSURED-IN");
		pojo = pe.getPojoElement();
		assert("test.pcml_om.map.PojoStruct2.insured".equals(pojo.getPojoFieldName()));
		assert("test.pcml_om.map.PojoStruct2.upperCase".equals(pojo.getConvertToLegacy()));
		assert("test.pcml_om.map.PojoStruct2.mexedCase".equals(pojo.getConvertToPojo()));
		ps = pcmlStructMap.get("struct_LINK-GEO-REC-OUT");
		assert("OMTEST100.LINK-GEO-REC-OUT.".equals(ps.getPreamble()));
		PcmlElement repeatingPe = ps.getElement("LINK-GEO-REC-OUT");
		assert(repeatingPe.getType().equals(PcmlElement.STRUCT));
		assert(repeatingPe.getCount()==10);
		pe = findElement("LINK-EXP-DATE", repeatingPe.getPcmlSubstruct());
//		pe = ps.getElement("LINK-EXP-DATE");
		pojo = pe.getPojoElement();
		assert("test.pcml_om.map.PojoStruct3.expireDate".equals(pojo.getPojoFieldName()));
		assert("MM/dd/yy".equals(pojo.getPojoFormat()));
		assert("yyyyMMdd".equals(pojo.getLegacyFormat()));
		PcmlElement[] peArray = pcmlPojoMap.getPcmlElements("OMTEST100");
		assert(peArray.length == 7);
	}

	private PcmlElement findElement(String peName,
			List<PcmlElement> pcmlSubstruct) {
		for (PcmlElement pe : pcmlSubstruct) {
			if (peName.equals(pe.getName())) {
				return pe;
			}
		}
		return null;
	}
	
	/*
  <struct name="struct_LINK-INTERNET-SESSION">
  		<data 
  			pcmlName="NRSESSID" 
  			pojoFieldName="test.as400_om.map.PojoStruct1.sessionId" />
		...

  <program name="OMTEST100" >  
  		<data 
  			pcmlName="LINK-INSURED-IN" 
  			pojoFieldName="test.as400_om.map.PojoStruct2.insured" 
  			convertToLegacy="test.as400_om.map.PojoStruct2.upperCase" 
  			convertToPojo="test.as400_om.map.PojoStruct2.mixedCase" />
		...

  <struct name="struct_LINK-GEO-REC-OUT">
		...
 		<data 
 			pcmlName="LINK-EXP-DATE" 
 			pojoFieldName="test.as400_om.map.PojoStruct3.expireDate" 
 			pojoFormat="MM/dd/yy" 
 			legacyFormat="yyyyMMdd" />
		...
	 */

}
