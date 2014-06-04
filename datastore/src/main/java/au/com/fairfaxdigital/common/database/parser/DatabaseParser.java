package au.com.fairfaxdigital.common.database.parser;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import au.com.fairfaxdigital.common.database.DatabaseSchema;
import au.com.fairfaxdigital.common.database.DatabaseUtils;
import au.com.fairfaxdigital.common.database.view.DatabaseColumn;
import au.com.fairfaxdigital.common.database.view.DatabaseTable;
import au.com.fairfaxdigital.common.database.utils.StringUtils;

/**
 * This class parses a Database XML file and returns the contents a
 * DatabaseSchema object. It uses reflection to create the tables and columns
 * specific to the database this schema is to be created on.
 * 
 * @author mdelaney
 * @author Author: michael.delaney
 * @created March 16, 2012
 * @date Date: 16/03/2012 01:50:39
 * @version Revision: 1.0
 */
public class DatabaseParser {
	private static final String SCHEMA = "Schema";
	private static final String NAME = "Name";
	private static final String VERSION = "Version";
	private static final String TABLE = "Table";
	private static final String COLUMN = "Column";
	private static final String TYPE = "Type";
	private static final String PRIMARY = "Primary";
	private static final String AUTOINCREMENT = "AutoIncrement";
	private static final String NULLABLE = "Nullable";
	private static final String LENGTH = "length";

	private DatabaseSchema mySchema = new DatabaseSchema();

	private Class<DatabaseTable> myTableType;
	private Class<DatabaseColumn> myColumnType;

	public DatabaseParser(Class tableType, Class columnType) {
		mySchema = new DatabaseSchema();
		myTableType = tableType;
		myColumnType = columnType;
	}

	public DatabaseSchema parse(String databaseLocation) {
		try {
			InputStream is = new FileInputStream(new File(databaseLocation));
			parse(is);
		} catch (IOException e) {
			// TODO
			// throw cannot complete exception
			System.out.println("IOException " + e);
			e.printStackTrace();
		}
		return mySchema;
	}

	public DatabaseSchema parse(InputStream is) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(is, myHandler);
		} catch (MalformedURLException e) {
			// TODO
			// throw cannot complete exception
			System.out.println("MalformedURLException " + e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO
			// throw cannot complete exception
			System.out.println("IOException " + e);
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO
			// throw cannot complete exception
			System.out.println("SAXException " + e);
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO
			// throw cannot complete exception
			System.out.println("ParserConfigurationException " + e);
			e.printStackTrace();
		}

		return mySchema;
	}

	private DefaultHandler myHandler = new DefaultHandler() {
		private String myCurrentElement;
		private String myCurrentBlock;

		private Map<String, String> myTableValues;
		private Map<String, String> myColumnValues;

		private List<DatabaseColumn> myCols = new ArrayList<DatabaseColumn>();

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			myCurrentElement = qName;
			if (myCurrentElement.equals(SCHEMA)) {
				myCurrentBlock = qName;
			} else if (myCurrentElement.equals(TABLE)) {
				myCurrentBlock = qName;
			} else if (myCurrentElement.equals(COLUMN)) {
				myCurrentBlock = qName;
			}

			if (myCurrentBlock.equals(TABLE)) {
				if (myTableValues == null || myCols == null) {
					myTableValues = new HashMap<String, String>();
					myCols = new ArrayList<DatabaseColumn>();
				}
			} else if (myCurrentBlock.equals(COLUMN)) {
				if (myColumnValues == null) {
					myColumnValues = new HashMap<String, String>();
				}

				if (myCurrentElement.equals(TYPE)) {
					myColumnValues.put(LENGTH, attributes.getValue(LENGTH));
				}
			}
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			myCurrentElement = StringUtils.EmptyString;

			if (qName.equals(TABLE)) {
				mySchema.addTable(buildTable());
				myTableValues = new HashMap<String, String>();
				myCols = new ArrayList<DatabaseColumn>();
			} else if (qName.equals(COLUMN)) {
				myCols.add(buildCol());
				myColumnValues = new HashMap<String, String>();
			}
		}

		public void characters(char ch[], int start, int length)
				throws SAXException {
			String value = new String(ch).substring(start, start + length);

			if (myCurrentBlock.equals(SCHEMA)) {
				if (myCurrentElement.equals(NAME)) {
					mySchema.setName(value);
				} else if (myCurrentElement.equals(VERSION)) {
					mySchema.setVersion(Integer.parseInt(value));
				}

			} else if (myCurrentBlock.equals(TABLE)
					&& !myCurrentBlock.equals(myCurrentElement)) {
				myTableValues.put(myCurrentElement, value);
			} else if (myCurrentBlock.equals(COLUMN)
					&& !myCurrentBlock.equals(myCurrentElement)
					&& !TextUtils.isEmpty(myCurrentElement)) {
				myColumnValues.put(myCurrentElement, value);
			}
		}

		/**
		 * This method builds a Database table using reflection based on the
		 * Database table type that was passed into the constructor when
		 * creating an instance of this class
		 * 
		 * @return
		 */
		private DatabaseTable buildTable() {
			try {
				String name = myTableValues.get(NAME);
				DatabaseTable table = (DatabaseTable) myTableType
						.getConstructor(String.class).newInstance(name);
				for (DatabaseColumn col : myCols) {
					table.addColumn(col);
				}
				return table;
			} catch (Exception e) {
				System.out.println("Failed to create Table " + e);
			}
			return null;
		}

		/**
		 * This method builds a Database column using reflection based on the
		 * Database column type that was passed into the constructor when
		 * creating an instance of this class
		 * 
		 * @return
		 */
		private DatabaseColumn buildCol() {
			DatabaseColumn dbColumn = null;
			try {
				String name = myColumnValues.get(NAME);
				int type = DatabaseUtils.getIntDatatype(StringUtils
						.getStringNotNull(myColumnValues.get(TYPE)));
				int length = Integer.parseInt(StringUtils
						.getStringNotNull(myColumnValues.get(LENGTH)));
				boolean primary = StringUtils.getStringNotNull(
						myColumnValues.get(PRIMARY)).equalsIgnoreCase(
						Boolean.TRUE.toString());
				boolean autoIncrement = StringUtils.getStringNotNull(
						myColumnValues.get(AUTOINCREMENT)).equalsIgnoreCase(
						Boolean.TRUE.toString());
				boolean nullable = StringUtils.getStringNotNull(
						myColumnValues.get(NULLABLE)).equalsIgnoreCase(
						Boolean.TRUE.toString());

				dbColumn = (DatabaseColumn) myColumnType.getConstructor(
						String.class, Integer.TYPE, Integer.TYPE, Boolean.TYPE,
						Boolean.TYPE, Boolean.TYPE).newInstance(name, type,
						length, primary, nullable, autoIncrement);
			} catch (Throwable e) {
				// TODO
				// throw cannot complete exception
				System.out.println("Failed to create Column " + e);
				e.printStackTrace();
			}

			return dbColumn;
		}
	};
}