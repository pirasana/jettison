/**
 * Copyright 2006 Envoi Solutions LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.jettison.mapped;

import static org.codehaus.jettison.XMLStreamReaderTestSupport.assertNext;
import static org.codehaus.jettison.XMLStreamReaderTestSupport.assertNextName;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.codehaus.jettison.XMLStreamReaderTestSupport;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

public class MappedXMLStreamReaderTest extends TestCase {
    public void testStreamReader() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"child1\" : \"child1\"" +
                           "} }");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");
    }
    
    public void testStreamReaderNullTextAsString() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"child1\" : \"null\"" +
                           "} }");
        Configuration c = new Configuration();
        c.setReadNullAsString(true);
        MappedNamespaceConvention con = new MappedNamespaceConvention(c);
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "null");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");
    }
    
    public void testStreamReaderNullTextAsNull() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"child1\" : \"null\"" +
                           "} }");
        Configuration c = new Configuration();
        MappedNamespaceConvention con = new MappedNamespaceConvention(c);
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, null);
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");
    }

    public void testMultipleChildren() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"child1\" : \"child1\"," +
                           "\"child2\" : \"child2\"" +
                           "} }");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child1");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child2");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "child2");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child2");
        
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");
    }
    
    public void testMultipleChildrenWithSameName() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"child1\" : \"child1\"," +
                           "\"child1\" : \"child11\"" +
                           "} }");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child1");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "child11");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child1");
        
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");
    }
    
    public void testNestedArrayOfChildren() throws Exception {
        JSONObject obj = 
            new JSONObject("{" +
            		"\"root\":" +
            				"{\"child1\":" +
            					"[{\"subchild2\":" +
            						"[\"first sub2\",\"second sub2\",\"third sub2\"]}" +
            					",\"sub1\",\"sub2\"]" +
            				",\"child2\":\"child2\"}" +
            		"}");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child1");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "subchild2");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "first sub2");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "subchild2");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "subchild2");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "second sub2");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "subchild2");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "subchild2");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "third sub2");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "subchild2");
              
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child1");
                  
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "sub1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child1");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "sub2");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child1");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child2");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "child2");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child2");
        
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");
    }

    
    public void testNamespaces() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"foo.root\" : { " +
                           "\"foo.child1\" : \"childtext\"," +
                           "} }");
        
        Map xtoj = new HashMap();
        xtoj.put("http://foo/", "foo");
        MappedNamespaceConvention con = new MappedNamespaceConvention(new Configuration(xtoj));
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);

        assertNamespacedReader(reader);
    }
    
    public void testDefaultNamespace() throws Exception {
		JSONObject obj = new JSONObject("{ " + "\"root\" : { "
				+ "\"child1\" : \"childtext\"," + "} }");

		Map xtoj = new HashMap();
		xtoj.put("http://foo/", "");
		MappedNamespaceConvention con = new MappedNamespaceConvention(
				new Configuration(xtoj));
		XMLStreamReader reader = new MappedXMLStreamReader(obj, con);

		assertNamespacedReader(reader);
	}    
    
    public void testArrayWithSameJSONObjects() throws Exception {
    	String str = "{\"theBook\":"
           + "{" 
           + "\"Names\":[{\"Name\":\"1\"}, {\"Name\":\"2\"}]"
           + " }                   "
           + "} ";
    	Configuration config = new Configuration();
    	config.setPrimitiveArrayKeys(Collections.singleton("Names"));
    	MappedXMLInputFactory factory = new MappedXMLInputFactory(config);
    	XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(str));
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "theBook");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "Names");
                
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "Name");
        
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "1");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "Name");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "Name");
        
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "2");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "Name");
        
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "Names");
        
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "theBook");
    	
    }
    
    public void testArrayWithSameJSONObjects2() throws Exception {
    	String str = "{\"theBook\":[{\"Name\":\"1\"}, {\"Name\":\"2\"}]}";
    	Configuration config = new Configuration();
    	config.setPrimitiveArrayKeys(Collections.singleton("theBook"));
    	MappedXMLInputFactory factory = new MappedXMLInputFactory(config);
    	XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(str));
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "theBook");
                
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "Name");
        
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "1");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "Name");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "Name");
        
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "2");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "Name");
        
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "theBook");
    	
    }
    
    public void testArrayWithSameJSONObjects3() throws Exception {
    	String str = "{\"theBook\":"
           + "{" 
           + "\"Names\":["
           + "{\"Name\":[{\"value\":\"11\"}, {\"value\":\"12\"}]},"
           + "{\"Name\":[{\"value\":\"21\"}, {\"value\":\"22\"}]}"
           + "]}"
           + "} ";
    	Configuration config = new Configuration();
    	Set<String> set = new HashSet<String>();
    	set.add("Names");
    	set.add("Name");
    	config.setPrimitiveArrayKeys(set);
    	MappedXMLInputFactory factory = new MappedXMLInputFactory(config);
    	XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(str));
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "theBook");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "Names");
                
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "Name");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "value");
        
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "11");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "value");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "value");
        
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "12");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "value");
        
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "Name");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "Name");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "value");
        
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "21");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "value");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "value");
        
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "22");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "value");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "Name");
        
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "Names");
        
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "theBook");
    	
    }
    
    public void testExceptionLocation() throws Exception {
    	String str = "{\"junk";
    	Configuration config = new Configuration();
    	MappedXMLInputFactory factory = new MappedXMLInputFactory(config);
    	try {
    	    factory.createXMLStreamReader(new StringReader(str));
    	    fail("Exception expected");
    	} catch (XMLStreamException ex) {
    		Location loc = ex.getLocation();
    		assertNotNull(loc);
    		assertEquals(0, loc.getLineNumber());
    		assertEquals(6, loc.getColumnNumber());
    	}
    }
    
    public void testArrayWithNotSameJSONObjects() throws Exception {
    	String str = "{\"theBook\":[{\"Name\":\"1\"}, {\"Bar\":\"2\"}]}";
    	Configuration config = new Configuration();
    	config.setPrimitiveArrayKeys(Collections.singleton("theBook"));
    	MappedXMLInputFactory factory = new MappedXMLInputFactory(config);
    	XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(str));
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "theBook");
                
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "theBook");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "Name");
        
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "1");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "Name");
        
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "theBook");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "theBook");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "Bar");
        
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "2");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "Bar");
        
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "theBook");
    	
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "theBook");
    }
    
    
    public void testMultipleArrays() throws Exception {
		JSONObject obj = new JSONObject("{ \"root\": " 
										+ " [ "
											+ " { "
											+ "     \"relationships\":[\"friend\"] , "
											+ "     \"emails\":[{\"value\":\"f@foo.com\"},{\"value\":\"b@bar.com\"}] "
											+ " } "
										+ " ] "
									+ " } ");

        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
                
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "relationships");

        assertNext(this, reader, XMLStreamReader.CHARACTERS, "friend");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "relationships");

        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "emails");

        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "value");

        assertNext(this, reader, XMLStreamReader.CHARACTERS, "f@foo.com");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "value");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "emails");

        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "emails");

        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "value");

        assertNext(this, reader, XMLStreamReader.CHARACTERS, "b@bar.com");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "value");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "emails");
        
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");
        
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");

    }
    
    public void testSingleArrayWithOneElement() throws Exception {
		JSONObject obj = new JSONObject("{ \"root\": " 
										+ " [ "
											+ " { "
											+ "     \"relationship\":\"friend\" , "
											+ "     \"email\":\"f@foo.com\" "
											+ " } "
											+ " , "
											+ " { "
											+ "     \"relationship\":\"relative\" , "
											+ "     \"email\":\"b@foo.com\" "
											+ " } "
										+ " ] "
									+ " } ");

        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "relationship");

        assertNext(this, reader, XMLStreamReader.CHARACTERS, "friend");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "relationship");

        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "email");

        assertNext(this, reader, XMLStreamReader.CHARACTERS, "f@foo.com");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "email");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "relationship");

        assertNext(this, reader, XMLStreamReader.CHARACTERS, "relative");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "relationship");

        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "email");

        assertNext(this, reader, XMLStreamReader.CHARACTERS, "b@foo.com");

        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "email");
                
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");

    }
    
    public void testStreamReaderWithNullValue() throws Exception {
        JSONObject obj = new JSONObject("{ " +
                               "\"root\" : { " +
                               "\"child1\" : null" +
                               "} }");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
    
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child1");
        assertEquals(null, reader.getText());
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");
    }
    
    public void testGetElementTextNull() throws Exception {
        JSONObject obj = new JSONObject("{ " +
    	                          "\"root\" : null }");
    	Configuration conf = new Configuration();
    	MappedNamespaceConvention con = new MappedNamespaceConvention(conf);
    	XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
    	assertEquals(null, reader.getElementText());
    	assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
    }
        
    public void testAttributes() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"@att\" : \"attvalue\"," +
                           "\"child1\" : \"child1\"" +
                           "} }");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);

        assertRootAttributeAndChild(reader);
    }
    
    public void testAttributesAsElements() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"@att\" : \"attvalue\"," +
                           "\"att2\" : \"attvalue\"," +
                           "\"child1\" : \"child1\"" +
                           "} }");
        List atts = new ArrayList();
        atts.add(new QName("att2"));
        Configuration c = new Configuration();
        c.setAttributesAsElements(atts);
        
        MappedNamespaceConvention con = new MappedNamespaceConvention(c);
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        
        assertEquals(1, reader.getAttributeCount());
        assertEquals("att", reader.getAttributeLocalName(0));
        assertEquals("", reader.getAttributeNamespace(0));
        assertEquals("attvalue", reader.getAttributeValue(0));
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "att2");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "attvalue");
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");
    }
    
    public void testElementNameWithDot() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"org.codehaus.jettison.mapped.root\" : { " +
                           "\"org.codehaus.jettison.mapped.child1\" : \"org.codehaus.jettison.mapped.child1\"" +
                           "} }");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("org.codehaus.jettison.mapped.root", reader.getName().getLocalPart());
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "org.codehaus.jettison.mapped.child1");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "org.codehaus.jettison.mapped.child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "org.codehaus.jettison.mapped.child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "org.codehaus.jettison.mapped.root");
    }
    
    
    public void testNonStringObjects() throws Exception {
    	JSONObject obj = new JSONObject("{\"root\":{\"foo\":true, \"foo2\":3.14, \"foo3\":17}}");
        
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("root", reader.getName().getLocalPart());        
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("foo", reader.getName().getLocalPart());
        
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "true");
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("foo2", reader.getName().getLocalPart());
        
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "3.14");
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());        
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("foo3", reader.getName().getLocalPart());
        
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "17");
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
    }
    
    public void testNonStringAttributes() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"@att\" : 1," +
                           "\"child1\" : \"child1\"" +
                           "} }");
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        
        assertEquals(1, reader.getAttributeCount());
        assertEquals("att", reader.getAttributeLocalName(0));
        assertEquals("", reader.getAttributeNamespace(0));
        assertEquals("1", reader.getAttributeValue(0));
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");
    }
    
    // issue 29
    public void testComplexElements() throws Exception {
    	JSONObject obj = new JSONObject("{\"a\":{\"o\":{\"@class\":\"string\",\"$\":\"1\"}}}");
        
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("a", reader.getName().getLocalPart());        
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());  
        assertEquals("o", reader.getName().getLocalPart());
        
        assertEquals(1, reader.getAttributeCount());
        assertEquals("class", reader.getAttributeLocalName(0));
        assertEquals("", reader.getAttributeNamespace(0));
        assertEquals("string", reader.getAttributeValue(0));
        
        assertNextName(this, reader, XMLStreamReader.CHARACTERS, "$");
        assertEquals("1", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals(XMLStreamReader.END_DOCUMENT, reader.next());
        
    }
    
    // issue 29
    public void testIgnoreComplexElements() throws Exception {
    	JSONObject obj = new JSONObject("{\"a\":{\"o\":{\"@class\":\"string\",\"$\":\"1\"}}}");
        
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        MappedXMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        reader.setValueKey(null);

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("a", reader.getName().getLocalPart());        
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());  
        assertEquals("o", reader.getName().getLocalPart());
        
        assertEquals(1, reader.getAttributeCount());
        assertEquals("class", reader.getAttributeLocalName(0));
        assertEquals("", reader.getAttributeNamespace(0));
        assertEquals("string", reader.getAttributeValue(0));
        
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertNextName(this, reader, XMLStreamReader.CHARACTERS, "$");
        assertEquals("1", reader.getText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals(XMLStreamReader.END_DOCUMENT, reader.next());
        
    }    
    
    // issue 16
    public void testSimple() throws Exception {
    	JSONObject obj = new JSONObject("{\"root\":\"json string\"}");
        
        MappedNamespaceConvention con = new MappedNamespaceConvention();
        MappedXMLStreamReader reader = new MappedXMLStreamReader(obj, con);

        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());       
        assertEquals("root", reader.getName().getLocalPart());     
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "json string");
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        
        
    }   
    
    //issue 52
    public void testAttributeKey() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : { " +
                           "\"!att\" : \"attvalue\"," +
                           "\"child1\" : \"child1\"" +
                           "} }");
        Configuration conf = new Configuration();
        conf.setAttributeKey("!");
        MappedNamespaceConvention con = new MappedNamespaceConvention(conf);
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);

        assertRootAttributeAndChild(reader);
    }    
    
    //issue 50
    public void testGetElementText() throws Exception {
        JSONObject obj = 
            new JSONObject("{ " +
                           "\"root\" : \"test\" }");
        
        Configuration conf = new Configuration();
        MappedNamespaceConvention con = new MappedNamespaceConvention(conf);
        XMLStreamReader reader = new MappedXMLStreamReader(obj, con);
        
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        
        assertEquals("test", reader.getElementText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
    }
    // Issue 59
    public void testListOfObject() throws Exception {
        String json = "{\"folders\":{" +
                        "\"folder\" : [" +
                            "{" +
                                "\"name\":\"folder1\"," +
                                "\"subfolder\":" +
                                "[" +
                                    "{\"name\":\"subfolder1\"}," +
                                    "{\"name\":\"subfolder2\"}" +
                                "]," +
                                "\"file\":" +
                                "[" +
                                    "{\"name\":\"file1\"}," +
                                    "{\"name\":\"file2\"}" +
                                "]" +                               
                            "}," +
                        "]" +
                       "}}";
        // Expected XML
        String expXml = "<folders>" +
                            "<folder>" +
                                "<name>folder1</name>" +
                                "<subfolder>" +
                                    "<name>subfolder1</name>" +
                                "</subfolder>" +
                                "<subfolder>" +
                                    "<name>subfolder2</name>" +
                                "</subfolder>" +
                                "<file>" +
                                    "<name>file1</name>" +
                                "</file>" +
                                "<file>" +
                                    "<name>file2</name>" +
                                "</file>" +
                            "</folder>" +
                        "</folders>";

        doTestEvents(json, expXml);
    }
    
    private void doTestEvents(String json, String expXml) throws JSONException, XMLStreamException,
            FactoryConfigurationError {
        JSONObject obj = new JSONObject(new JSONTokener(json));
        Configuration config = new Configuration();

        
        ArrayList refEvents = new ArrayList();

        // reference reader
        XMLStreamReader refReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(expXml));
        while (refReader.hasNext()) {
            refEvents.add(Integer.valueOf(refReader.next()));            
        }
        refReader.close();

        // tested reader
        MappedXMLStreamReader mpdReader = new MappedXMLStreamReader(obj, new MappedNamespaceConvention(config));

        ArrayList mpdEvents = new ArrayList();        
        StringBuilder sb = new StringBuilder();
        while (mpdReader.hasNext()) {
            int eventType = mpdReader.next();
        
            mpdEvents.add(Integer.valueOf(eventType));
            
            if (eventType == XMLStreamConstants.START_ELEMENT) {
                sb.append("<").append(mpdReader.getName()).append(">");
            } else if (eventType == XMLStreamConstants.CHARACTERS) {
                sb.append(mpdReader.getTextCharacters());
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                sb.append("</").append(mpdReader.getName()).append(">");                
            }
        }
        mpdReader.close();

        String mpdXml = sb.toString();
        
        assertEquals(expXml, mpdXml);
        assertEquals(refEvents, mpdEvents);
    }

    public void testEmptyObjectIsRejected() throws Exception {
        try {
            new MappedXMLStreamReader(new JSONObject("{}"));
            fail("Failure expected on a JSON object with no root element");
        } catch (XMLStreamException ex) {
            assertEquals("The JSON object has no root element", ex.getMessage());
        }
    }

    public void testEmptyObjectIsRejectedByFactory() throws Exception {
        try {
            new MappedXMLInputFactory(new Configuration()).createXMLStreamReader(new StringReader(" { } "));
            fail("Failure expected on a JSON object with no root element");
        } catch (XMLStreamException ex) {
            assertEquals("The JSON object has no root element", ex.getMessage());
        }
    }

    public void testSupressAtAttributesReading() throws Exception {
        Configuration config = new Configuration();
        config.setSupressAtAttributes(true);
        MappedXMLInputFactory factory = new MappedXMLInputFactory(config);

        assertEquals("<root><@id>1</@id><name>x</name><child><@lang>en</@lang>text</child></root>",
            readEvents(factory, "{\"root\":{\"@id\":\"1\",\"name\":\"x\",\"child\":{\"@lang\":\"en\",\"$\":\"text\"}}}"));
        assertEquals("<root><a>1</a><a>2</a><b><@c>3</@c><d>4</d></b></root>",
            readEvents(factory, "{\"root\":{\"a\":[1,2],\"b\":{\"@c\":\"3\",\"d\":\"4\"}}}"));
    }

    public void testSupressAtAttributesDoesNotRetainKeys() throws Exception {
        Configuration config = new Configuration();
        config.setSupressAtAttributes(true);
        MappedXMLInputFactory factory = new MappedXMLInputFactory(config);

        // Keys read from the input used to be added to a list on the shared
        // convention, which grew without bound and made reading quadratic in
        // the number of distinct keys. The list isn't exposed, so check it
        // via reflection.
        MappedNamespaceConvention convention = ((MappedXMLStreamReader) factory.createXMLStreamReader(
            new StringReader("{\"root\":{\"a\":1}}"))).getConvention();
        java.lang.reflect.Field field =
            MappedNamespaceConvention.class.getDeclaredField("jsonAttributesAsElements");
        field.setAccessible(true);
        int sizeBefore = ((List<?>) field.get(convention)).size();

        StringBuilder json = new StringBuilder("{\"root\":{");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append("\"k").append(i).append("\":1");
        }
        json.append("}}");
        readEvents(factory, json.toString());

        assertEquals(sizeBefore, ((List<?>) field.get(convention)).size());
    }

    private void assertNamespacedReader(XMLStreamReader reader) throws Exception {
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        assertEquals("http://foo/", reader.getName().getNamespaceURI());
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child1");
        assertEquals("http://foo/", reader.getName().getNamespaceURI());
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "childtext");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child1");
        assertEquals("http://foo/", reader.getName().getNamespaceURI());
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");
        assertEquals("http://foo/", reader.getName().getNamespaceURI());
    }

    private void assertRootAttributeAndChild(XMLStreamReader reader) throws Exception {
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "root");
        assertEquals(1, reader.getAttributeCount());
        assertEquals("att", reader.getAttributeLocalName(0));
        assertEquals("", reader.getAttributeNamespace(0));
        assertEquals("attvalue", reader.getAttributeValue(0));
        assertNext(this, reader, XMLStreamReader.START_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.CHARACTERS, "child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "child1");
        assertNext(this, reader, XMLStreamReader.END_ELEMENT, "root");
    }

    private static String readEvents(MappedXMLInputFactory factory, String json) throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(json));
        return XMLStreamReaderTestSupport.readEvents(reader);
    }

    // https://github.com/jettison-json/jettison/issues/9
    public void testGetElementTextLeavesReaderOnEndElement() throws Exception {
        JSONObject obj = new JSONObject("{\"root\":{\"a\":\"text\",\"b\":{\"@attr\":\"x\",\"$\":\"more\"},"
            + "\"c\":[\"one\",\"two\"],\"d\":{},\"e\":\"\",\"f\":null}}");
        XMLStreamReader reader = new MappedXMLStreamReader(obj, new MappedNamespaceConvention(standard()));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("root", reader.getLocalName());

        assertElementText(reader, "a", "text");
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("x", reader.getAttributeValue(null, "attr"));
        assertEquals("b", reader.getLocalName());
        assertEquals("more", reader.getElementText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.getEventType());
        assertEquals("b", reader.getLocalName());
        // repeated elements from a JSON array
        assertElementText(reader, "c", "one");
        assertElementText(reader, "c", "two");
        // an empty element and an empty string
        assertElementText(reader, "d", null);
        assertElementText(reader, "e", "");
        // a JSON null
        assertElementText(reader, "f", null);

        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
        assertEquals("root", reader.getLocalName());
        assertEquals(XMLStreamReader.END_DOCUMENT, reader.next());
    }

    private static void assertElementText(XMLStreamReader reader, String name, String text) throws Exception {
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals(name, reader.getLocalName());
        assertEquals(text, reader.getElementText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.getEventType());
        assertEquals(name, reader.getLocalName());
    }

    public void testGetElementTextNullString() throws Exception {
        // the string "null" is read as null, unless readNullAsString is configured
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"root\":\"null\"}"),
            new MappedNamespaceConvention(standard()));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertNull(reader.getElementText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.getEventType());

        Configuration conf = standard();
        conf.setReadNullAsString(true);
        reader = new MappedXMLStreamReader(new JSONObject("{\"root\":\"null\"}"),
            new MappedNamespaceConvention(conf));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals("null", reader.getElementText());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.getEventType());
    }

    public void testGetElementTextOnCharacters() throws Exception {
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"root\":\"test\"}"),
            new MappedNamespaceConvention(standard()));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        assertEquals(XMLStreamReader.CHARACTERS, reader.next());
        assertEquals("test", reader.getElementText());
        assertEquals(XMLStreamReader.CHARACTERS, reader.getEventType());
        assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
    }

    public void testGetElementTextWithChildElement() throws Exception {
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"root\":{\"child\":\"test\"}}"),
            new MappedNamespaceConvention(standard()));
        assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
        try {
            reader.getElementText();
            fail("Failure expected on an element with a child element");
        } catch (XMLStreamException ex) {
            // expected
        }
    }

    public void testDefaultGetElementText() throws Exception {
        // by default the reader is left on CHARACTERS, so next() returns the END_ELEMENT
        XMLStreamReader[] readers = {
            new MappedXMLStreamReader(new JSONObject("{\"root\":\"test\"}"),
                new MappedNamespaceConvention(new Configuration())),
            new MappedXMLStreamReader(new JSONObject("{\"root\":\"test\"}"))
        };
        for (XMLStreamReader reader : readers) {
            assertEquals(XMLStreamReader.START_ELEMENT, reader.next());
            assertEquals("test", reader.getElementText());
            assertEquals(XMLStreamReader.CHARACTERS, reader.getEventType());
            assertEquals(XMLStreamReader.END_ELEMENT, reader.next());
            assertEquals(XMLStreamReader.END_DOCUMENT, reader.next());
        }
    }

    private static Configuration standard() {
        Configuration conf = new Configuration();
        conf.setStandardGetElementText(true);
        return conf;
    }

    public void testHasNameAndRequire() throws Exception {
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("urn:1", "p1");
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"p1.root\":{\"child\":\"text\"}}"),
            new MappedNamespaceConvention(new Configuration(namespaces)));

        assertFalse(reader.hasName());
        reader.require(XMLStreamConstants.START_DOCUMENT, null, null);

        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertTrue(reader.hasName());
        reader.require(XMLStreamConstants.START_ELEMENT, "urn:1", "root");
        reader.require(XMLStreamConstants.START_ELEMENT, null, "root");
        reader.require(XMLStreamConstants.START_ELEMENT, "urn:1", null);
        assertRequireFails(reader, XMLStreamConstants.END_ELEMENT, null, null);
        assertRequireFails(reader, XMLStreamConstants.START_ELEMENT, "urn:2", "root");
        assertRequireFails(reader, XMLStreamConstants.START_ELEMENT, "", "root");
        assertRequireFails(reader, XMLStreamConstants.START_ELEMENT, "urn:1", "other");

        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        reader.require(XMLStreamConstants.START_ELEMENT, "", "child");

        assertEquals(XMLStreamConstants.CHARACTERS, reader.next());
        assertFalse(reader.hasName());
        reader.require(XMLStreamConstants.CHARACTERS, null, null);
        // a name can't be checked on an event that has no name
        assertRequireFails(reader, XMLStreamConstants.CHARACTERS, null, "child");

        assertEquals(XMLStreamConstants.END_ELEMENT, reader.next());
        assertTrue(reader.hasName());
        reader.require(XMLStreamConstants.END_ELEMENT, "", "child");

        assertEquals(XMLStreamConstants.END_ELEMENT, reader.next());
        reader.require(XMLStreamConstants.END_ELEMENT, "urn:1", "root");

        assertEquals(XMLStreamConstants.END_DOCUMENT, reader.next());
        assertFalse(reader.hasName());
        reader.require(XMLStreamConstants.END_DOCUMENT, null, null);
    }

    private static void assertRequireFails(XMLStreamReader reader, int type, String namespaceURI,
                                           String localName) {
        try {
            reader.require(type, namespaceURI, localName);
            fail("Failure expected on require(" + type + ", " + namespaceURI + ", " + localName + ")");
        } catch (XMLStreamException ex) {
            // expected
        }
    }

    public void testGetTextCharacters() throws Exception {
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"root\":\"hello\"}"));
        assertEquals(XMLStreamConstants.START_ELEMENT, reader.next());
        assertEquals(XMLStreamConstants.CHARACTERS, reader.next());

        // a target buffer larger than the text
        char[] target = new char[16];
        assertEquals(5, reader.getTextCharacters(0, target, 0, 16));
        assertEquals("hello", new String(target, 0, 5));

        // copying into the middle of the target, starting part way through the text
        target = new char[] {'x', 'x', 'x', 'x', 'x', 'x'};
        assertEquals(3, reader.getTextCharacters(2, target, 1, 5));
        assertEquals("xllox", new String(target, 0, 5));

        // no more text
        assertEquals(0, reader.getTextCharacters(5, target, 0, 3));
        assertEquals(0, reader.getTextCharacters(10, target, 0, 3));

        // reading the text in fixed size chunks, as in the XMLStreamReader Javadoc
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[2];
        for (int sourceStart = 0;; sourceStart += buffer.length) {
            int copied = reader.getTextCharacters(sourceStart, buffer, 0, buffer.length);
            sb.append(buffer, 0, copied);
            if (copied < buffer.length) {
                break;
            }
        }
        assertEquals("hello", sb.toString());

        // invalid arguments
        assertGetTextCharactersFails(reader, -1, new char[5], 0, 5);
        assertGetTextCharactersFails(reader, 0, new char[5], -1, 5);
        assertGetTextCharactersFails(reader, 0, new char[5], 6, 0);
        assertGetTextCharactersFails(reader, 0, new char[5], 0, -1);
        assertGetTextCharactersFails(reader, 0, new char[5], 2, 4);
    }

    private static void assertGetTextCharactersFails(XMLStreamReader reader, int sourceStart, char[] target,
                                                     int targetStart, int length) throws XMLStreamException {
        try {
            reader.getTextCharacters(sourceStart, target, targetStart, length);
            fail("Failure expected on invalid arguments");
        } catch (IndexOutOfBoundsException ex) {
            // expected
        }
    }

    public void testNamespaceContextGetPrefixes() throws Exception {
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("urn:1", "p1");
        namespaces.put("urn:2", "p2");
        XMLStreamReader reader = new MappedXMLStreamReader(new JSONObject("{\"p1.root\":\"x\"}"),
            new MappedNamespaceConvention(new Configuration(namespaces)));
        NamespaceContext ctx = reader.getNamespaceContext();

        Iterator<?> prefixes = ctx.getPrefixes("urn:1");
        assertEquals("p1", prefixes.next());
        assertFalse(prefixes.hasNext());
        prefixes = ctx.getPrefixes("urn:2");
        assertEquals("p2", prefixes.next());
        assertFalse(prefixes.hasNext());
        assertFalse(ctx.getPrefixes("urn:unknown").hasNext());
        assertEquals("p1", ctx.getPrefix("urn:1"));
    }
}
