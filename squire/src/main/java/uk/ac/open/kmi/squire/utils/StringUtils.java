/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author carloallocca
 *
 *
 */
public class StringUtils {

	/**
	 * 
	 * @param s
	 * @return an <em>immutable</em> list
	 */
	public static Set<String> commaSeparated2List(String s) {
		return commaSeparated2List(s, true);
	}

	/**
	 * 
	 * @param s
	 * @param immutable
	 *            whether the returned list should be unmodifiable
	 * @return
	 */
	public static Set<String> commaSeparated2List(String s, boolean immutable) {
		if (s == null)
			throw new IllegalArgumentException("String cannot be null");
		if (!s.isEmpty()) {
			String[] sSplit = s.substring(1, s.length() - 1).split(",");
			// Shit needs trimming...
			Set<String> cleaned = new HashSet<>();
			for (int i = 0; i < sSplit.length; i++) {
				String sc = sSplit[i].trim();
				if (!sc.isEmpty())
					cleaned.add(sc);
			}
			return cleaned;
			// Would use if I could trust it was clean:
			// return new HashSet<>(Arrays.asList(sSplit));
		}
		return Collections.emptySet();
	}

	public static String getLocalName(String uri) {
		String localName = "";
		if (uri.startsWith("http://") || uri.startsWith("https://")) {
			if (uri.contains("#")) {
				localName = uri.substring(uri.indexOf("#") + 1, uri.length());
				return localName;
			}
			localName = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
			return localName;
		} else
			return uri;
	}

}
