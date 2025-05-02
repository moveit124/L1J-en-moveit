/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import l1j.server.server.model.L1Location;
import l1j.server.server.templates.L1ItemSetItem;
import l1j.server.server.utils.IterableElementList;

public class GMCommandsConfig {
	private static Logger _log = LoggerFactory.getLogger(GMCommandsConfig.class
			.getName());

	private interface ConfigLoader {
		public void load(Element element);
	}

	private abstract class ListLoaderAdapter implements ConfigLoader {
		private final String _listName;

		public ListLoaderAdapter(String listName) {
			_listName = listName;
		}

		@Override
		public final void load(Element element) {
			try {
				// Get all child elements that match our list name
				NodeList nodes = element.getElementsByTagName(_listName);
				if (nodes.getLength() == 0) {
					_log.warn("No {} elements found in the document", _listName);
					return;
				}
				
				// We expect only one list element of our type
				Element listElement = (Element) nodes.item(0);
				loadElement(listElement);
				
			} catch (Exception e) {
				_log.error("Error in ListLoaderAdapter for " + _listName + ": " + e.getMessage(), e);
			}
		}

		public abstract void loadElement(Element element);
	}

	private class RoomLoader extends ListLoaderAdapter {
		public RoomLoader() {
			super("RoomList");
		}

		@Override
		public void loadElement(Element element) {
			NodeList nodes = element.getChildNodes();
			for (Element elem : new IterableElementList(nodes)) {
				if (elem.getNodeName().equalsIgnoreCase("Room")) {
					try {
						String name = elem.getAttribute("Name");
						if (name == null || name.trim().isEmpty()) {
							_log.warn("Skipping room with missing Name attribute");
							continue;
						}

						String locXStr = elem.getAttribute("LocX");
						String locYStr = elem.getAttribute("LocY");
						String mapIdStr = elem.getAttribute("MapId");

						// Validate required attributes
						if (locXStr == null || locXStr.trim().isEmpty() ||
							locYStr == null || locYStr.trim().isEmpty() ||
							mapIdStr == null || mapIdStr.trim().isEmpty()) {
							_log.warn("Skipping room '{}' with missing coordinates or map ID", name);
							continue;
						}

						int locX = Integer.valueOf(locXStr.trim());
						int locY = Integer.valueOf(locYStr.trim());
						int mapId = Integer.valueOf(mapIdStr.trim());

						ROOMS.put(name.toLowerCase(), new L1Location(locX, locY, mapId));
					} catch (NumberFormatException e) {
						_log.warn("Error parsing room coordinates: " + e.getMessage());
					}
				}
			}
		}
	}

	private class ItemSetLoader extends ListLoaderAdapter {
		public ItemSetLoader() {
			super("ItemSetList");
		}

		public L1ItemSetItem loadItem(Element element) {
			if (element == null) {
				return null;
			}

			String name = element.getParentNode() != null ? 
				((Element)element.getParentNode()).getAttribute("Name") : "unknown";
			
			try {
				String idStr = element.getAttribute("Id");
				String amountStr = element.getAttribute("Amount");
				String enchantStr = element.getAttribute("Enchant");
				
				// Log the raw values for debugging
				_log.info("Loading item in set '{}' - Id:'{}' Amount:'{}' Enchant:'{}'", 
					name, idStr, amountStr, enchantStr);
				
				// Validate Id (required)
				if (idStr == null || idStr.trim().isEmpty()) {
					_log.warn("Skipping item in set '{}' with missing Id attribute", name);
					return null;
				}
				
				// Parse Id
				int id;
				try {
					id = Integer.valueOf(idStr.trim());
				} catch (NumberFormatException e) {
					_log.warn("Invalid item Id '{}' in set '{}', skipping item", idStr, name);
					return null;
				}
				
				// Parse Amount (optional, defaults to 1)
				int amount = 1;
				if (amountStr != null && !amountStr.trim().isEmpty()) {
					try {
						amount = Integer.valueOf(amountStr.trim());
						if (amount <= 0) {
							_log.warn("Invalid Amount {} for item Id {} in set '{}', using default: 1", 
								amount, id, name);
							amount = 1;
						}
					} catch (NumberFormatException e) {
						_log.warn("Invalid Amount '{}' for item Id {} in set '{}', using default: 1", 
							amountStr, id, name);
					}
				}
				
				// Parse Enchant (optional, defaults to 0)
				int enchant = 0;
				if (enchantStr != null && !enchantStr.trim().isEmpty()) {
					try {
						enchant = Integer.valueOf(enchantStr.trim());
					} catch (NumberFormatException e) {
						_log.warn("Invalid Enchant '{}' for item Id {} in set '{}', using default: 0", 
							enchantStr, id, name);
					}
				}
				
				return new L1ItemSetItem(id, amount, enchant);
			} catch (Exception e) {
				_log.error("Error loading item in set '{}': {}", name, e.getMessage());
				return null;
			}
		}

		@Override
		public void loadElement(Element element) {
			List<L1ItemSetItem> list = new ArrayList<L1ItemSetItem>();
			NodeList nodes = element.getChildNodes();
			for (Element elem : new IterableElementList(nodes)) {
				if (elem.getNodeName().equalsIgnoreCase("Item")) {
					L1ItemSetItem item = loadItem(elem);
					if (item != null) {
						list.add(item);
					}
				}
			}
			String name = element.getAttribute("Name");
			if (!list.isEmpty() && name != null && !name.trim().isEmpty()) {
				ITEM_SETS.put(name.toLowerCase(), list);
			}
		}
	}

	private static HashMap<String, ConfigLoader> _loaders = new HashMap<String, ConfigLoader>();
	static {
		GMCommandsConfig instance = new GMCommandsConfig();
		_loaders.put("roomlist", instance.new RoomLoader());
		_loaders.put("itemsetlist", instance.new ItemSetLoader());
	}

	public static HashMap<String, L1Location> ROOMS = new HashMap<String, L1Location>();
	public static HashMap<String, List<L1ItemSetItem>> ITEM_SETS = new HashMap<String, List<L1ItemSetItem>>();

	private static Document loadXml(String file)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
			@Override
			public void warning(SAXParseException e) throws SAXException {
				_log.warn("Warning while parsing " + file + ": " + e.getMessage());
			}
			
			@Override
			public void error(SAXParseException e) throws SAXException {
				_log.error("Error while parsing " + file + ": " + e.getMessage());
				throw e;
			}
			
			@Override
			public void fatalError(SAXParseException e) throws SAXException {
				_log.error("Fatal error while parsing " + file + ": " + e.getMessage());
				throw e;
			}
		});
		
		return builder.parse(file);
	}

	public static void load() {
		try {
			// Clear both maps before loading
			ROOMS.clear();
			ITEM_SETS.clear();
			
			Document doc = loadXml("./data/xml/GmCommands/GMCommands.xml");
			if (doc == null) {
				_log.error("Failed to load GMCommands.xml - document is null");
				return;
			}
			
			Element root = doc.getDocumentElement();
			if (root == null || !root.getNodeName().equals("GMCommands")) {
				_log.error("Failed to load GMCommands.xml - invalid root element");
				return;
			}
			
			// Process each loader with the root element
			for (Map.Entry<String, ConfigLoader> entry : _loaders.entrySet()) {
				try {
					entry.getValue().load(root);
				} catch (Exception e) {
					_log.error("Error loading section '{}': {}", entry.getKey(), e.getMessage(), e);
				}
			}
			
			_log.info("Loaded {} GM rooms and {} item sets", ROOMS.size(), ITEM_SETS.size());
			
		} catch (Exception e) {
			_log.error("Failed to load GMCommands.xml", e);
			// Clear maps if loading failed
			ROOMS.clear();
			ITEM_SETS.clear();
		}
	}
}