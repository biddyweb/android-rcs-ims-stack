package com.orangelabs.rcs.provisioning;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class Roundtrip {
	private XmlPullParser parser;
	private XmlSerializer serializer;

	public Roundtrip(XmlPullParser parser, XmlSerializer serializer) {
		this.parser = parser;
		this.serializer = serializer;
	}

	public void writeStartTag() throws XmlPullParserException, IOException {
		if (!parser
				.getFeature(XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES)) {
			for (int i = parser.getNamespaceCount(parser.getDepth() - 1); i < parser
					.getNamespaceCount(parser.getDepth()) - 1; i++) {
				serializer.setPrefix(parser.getNamespacePrefix(i), parser
						.getNamespaceUri(i));
			}
		}
		serializer.startTag(parser.getNamespace(), parser.getName());

		for (int i = 0; i < parser.getAttributeCount(); i++) {
			serializer.attribute(parser.getAttributeNamespace(i), parser
					.getAttributeName(i), parser.getAttributeValue(i));
		}
	}

	public void writeToken() throws XmlPullParserException, IOException {
		switch (parser.getEventType()) {
			case XmlPullParser.START_DOCUMENT:
				serializer.startDocument(null, null);
				break;
	
			case XmlPullParser.END_DOCUMENT:
				serializer.endDocument();
				break;
	
			case XmlPullParser.START_TAG:
				writeStartTag();
				break;
	
			case XmlPullParser.END_TAG:
				serializer.endTag(parser.getNamespace(), parser.getName());
				break;
	
			case XmlPullParser.IGNORABLE_WHITESPACE:
				// comment it to remove ignorable whtespaces from XML infoset
				serializer.ignorableWhitespace(parser.getText());
				break;
	
			case XmlPullParser.TEXT:
				if (parser.getText() != null) {
					serializer.text(parser.getText());
				}
				break;
	
			case XmlPullParser.ENTITY_REF:
				if (parser.getText() != null)
					serializer.text(parser.getText());
				else
					serializer.entityRef(parser.getName());
				break;
	
			case XmlPullParser.CDSECT:
				serializer.cdsect(parser.getText());
				break;
	
			case XmlPullParser.PROCESSING_INSTRUCTION:
				serializer.processingInstruction(parser.getText());
				break;
	
			case XmlPullParser.COMMENT:
				serializer.comment(parser.getText());
				break;
	
			case XmlPullParser.DOCDECL:
				serializer.docdecl(parser.getText());
				break;
	
			default:
				throw new RuntimeException("unrecognized event: "
						+ parser.getEventType());
		}
	}

	public void roundTrip() throws XmlPullParserException, IOException {
		while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
			writeToken();
			serializer.flush();
			parser.nextToken();
		}
		writeToken();
	}
}