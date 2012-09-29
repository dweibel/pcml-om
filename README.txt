
OVERVIEW:
What is PCML?
The Program Call Markup Language (PCML) is a tag language based on XML to help make Java calls into IBM iSeries programs.  PCML is a full description of the server programs called by Java applications.  Best of all, it can be generated from your COBOL or RPG compiler.

What is the PCML Object Mapper?
The pcml-om library allows the developer to map the parameters of the server programs to objects in Java.  The developer simply provides a mapping file and makes a single call the server program.  No more writing boiler-plate code, long lists of getters/setters, or repetitive data conversions. The library uses reflection to access and populate your Java objects when making server calls.  

Who Can Use the PCML Object Mapper?
The pcml-om library is available to commercial, individual, and academic developers under a liberal open source license. The pcml-om code base is distributed to the public under the Academic Free License 3.0.

Who Can Contribute?
The pcml-om project accepts bug fixes, enhancements, and additions from all developers. Submissions can be sent to the author via email at pcml-om@gmail.com.

LICENSE:
The Academic Free License 3.0
http://opensource.org/licenses/AFL-3.0

INSTALL:
Download pcml-om.jar and include it in your project.  Log4J 1.2 must also be included in your project.

Modifying the PCML Object Mapper:
The following libraries are required to compile this project.
Log4J 1.2
JUnit 4
Mockito 1.8.5

