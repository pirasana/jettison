/*
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
package org.codehaus.jettison;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public final class XMLStreamReaderTestSupport {
    private XMLStreamReaderTestSupport() {
    }

    public static void assertNext(TestCase test, XMLStreamReader reader, int event, String expected)
            throws XMLStreamException {
        test.assertEquals(event, reader.next());
        if (event == XMLStreamConstants.START_ELEMENT || event == XMLStreamConstants.END_ELEMENT) {
            test.assertEquals(expected, reader.getName().getLocalPart());
        } else if (event == XMLStreamConstants.CHARACTERS) {
            test.assertEquals(expected, reader.getText());
        } else {
            throw new IllegalArgumentException("Unsupported XML stream event: " + event);
        }
    }

    public static void assertNextName(TestCase test, XMLStreamReader reader, int event, String expected)
            throws XMLStreamException {
        test.assertEquals(event, reader.next());
        test.assertEquals(expected, reader.getName().getLocalPart());
    }

    public static String readEvents(XMLStreamReader reader) throws XMLStreamException {
        StringBuilder sb = new StringBuilder();
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                sb.append('<').append(reader.getLocalName());
                for (int i = 0; i < reader.getAttributeCount(); i++) {
                    sb.append(' ').append(reader.getAttributeLocalName(i))
                        .append("=\"").append(reader.getAttributeValue(i)).append('"');
                }
                sb.append('>');
            } else if (event == XMLStreamConstants.CHARACTERS) {
                sb.append(reader.getText());
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                sb.append("</").append(reader.getLocalName()).append('>');
            }
        }
        return sb.toString();
    }
}
