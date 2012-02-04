package test.pcml_om;

import org.apache.log4j.BasicConfigurator;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import test.pcml_om.map.TestMapFactory;
import test.pcml_om.map.TestPojoReflectionHelper;

@RunWith(Suite.class)
@SuiteClasses({ 
	TestPcmlConverter.class, 
	TestPojoConverter.class, 
	TestMapFactory.class, 
	TestPojoReflectionHelper.class 
	})


public class AllTests {
	
    @BeforeClass
    public static void setUp() {
    	BasicConfigurator.configure();
    }


}
