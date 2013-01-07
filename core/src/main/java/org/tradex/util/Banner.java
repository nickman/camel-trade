/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2007, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package org.tradex.util;

import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Title: Banner</p>
 * <p>Description: Utility class for generating and printing console banners.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.util.Banner</code></p>
 */

public class Banner {
	/** A cache of prebuilt banner what-nots */
	private static final Map<String, String[]> bannerLineCache = new ConcurrentHashMap<String, String[]>();
	/** The default symbol */
	public static final String DEFAULT_SYMBOL = "=";
	/** The default indent */
	public static final int DEFAULT_INDENT = 1;
	/** The default length */
	public static final int DEFAULT_LENGTH = 40;
	/** The EOL symbol for this VM */
	public static final String EOL = System.getProperty("line.separator", "\n");
	/** The tab indent index */
	public static final int TAB = 0;
	/** The line index */
	public static final int LINE = 1;

	
	static {
		bannerLine(DEFAULT_SYMBOL, DEFAULT_INDENT, DEFAULT_LENGTH);
	}
	
	
	/**
	 * Creates the banner content. The generated banner will start end end with an EOL 
	 * @param symbol The symbol to build the banner from
	 * @param indent The number of tabs to indent by
	 * @param length The length of the line (after the tabs)
	 * @param content One CharSequence per line, each of which will be prefixed by an EOL and the tab indent.
	 * @return the banner content
	 */
	public static StringBuilder banner(CharSequence symbol, int indent, int length, CharSequence...content) {
		if(symbol==null) symbol = DEFAULT_SYMBOL;
		StringBuilder contentBuff = new StringBuilder();
		String[] whatNot = bannerLine(symbol, indent, length);
		contentBuff.append(EOL).append(whatNot[TAB]).append(whatNot[LINE]);
		if(content!=null) {
			for(CharSequence cs: content) {
				if(cs!=null) {
					contentBuff.append(EOL).append(whatNot[TAB]).append(cs.toString().trim());
				}
			}
		}
		contentBuff.append(EOL).append(whatNot[TAB]).append(whatNot[LINE]);		
		contentBuff.append(EOL);
		return contentBuff;
	}
	
	/**
	 * Creates the banner content using the default formatting options 
	 * @param content One CharSequence per line, each of which will be prefixed by an EOL and the tab indent.
	 * @return the banner content
	 */
	public static StringBuilder banner(CharSequence...content) {
		return banner(DEFAULT_SYMBOL, DEFAULT_INDENT, DEFAULT_LENGTH, content);
	}

	/**
	 * Creates the banner content and outputs it to the passed print stream. The generated banner will start end end with an EOL
	 * @param stream The print stream to send the output to 
	 * @param symbol The symbol to build the banner from
	 * @param indent The number of tabs to indent by
	 * @param length The length of the line (after the tabs)
	 * @param content One CharSequence per line, each of which will be prefixed by an EOL and the tab indent.
	 */
	public static void banner(PrintStream stream, CharSequence symbol, int indent, int length, CharSequence...content) {
		if(stream==null) stream = System.out;
		stream.print(banner(symbol, indent, length, content));
	}
	
	/**
	 * Creates the banner content using the default format and outputs it to the passed print stream. The generated banner will start end end with an EOL
	 * @param stream The print stream to send the output to 
	 * @param content One CharSequence per line, each of which will be prefixed by an EOL and the tab indent.
	 */
	public static void banner(PrintStream stream, CharSequence...content) {
		if(stream==null) stream = System.out;
		stream.print(banner(DEFAULT_SYMBOL, DEFAULT_INDENT, DEFAULT_LENGTH, content));
	}
	
	
	/**
	 * Creates the banner content and outputs it to System.out. The generated banner will start end end with an EOL
	 * @param symbol The symbol to build the banner from
	 * @param indent The number of tabs to indent by
	 * @param length The length of the line (after the tabs)
	 * @param content One CharSequence per line, each of which will be prefixed by an EOL and the tab indent.
	 */
	public static void bannerOut(CharSequence symbol, int indent, int length, CharSequence...content) {
		banner(System.out, symbol, indent, length, content);
	}
	
	/**
	 * Creates the banner content using the default format and outputs it to System.out. The generated banner will start end end with an EOL
	 * @param content One CharSequence per line, each of which will be prefixed by an EOL and the tab indent.
	 */
	public static void bannerOut(CharSequence...content) {
		banner(System.out, content);
	}
	
	/**
	 * Creates the banner content and outputs it to System.err. The generated banner will start end end with an EOL
	 * @param symbol The symbol to build the banner from
	 * @param indent The number of tabs to indent by
	 * @param length The length of the line (after the tabs)
	 * @param content One CharSequence per line, each of which will be prefixed by an EOL and the tab indent.
	 */
	public static void bannerErr(CharSequence symbol, int indent, int length, CharSequence...content) {
		banner(System.err, symbol, indent, length, content);
	}

	/**
	 * Creates the banner content using the default format and outputs it to System.err. The generated banner will start end end with an EOL
	 * @param content One CharSequence per line, each of which will be prefixed by an EOL and the tab indent.
	 */
	public static void bannerErr(CharSequence...content) {
		banner(System.err, content);
	}
	
	
	/**
	 * Creates and caches the tab indent and line strings for a banner
	 * @param symbol The symbol to build the line from
	 * @param indent The number of tabs to indent by
	 * @param length The length of the line (after the tabs)
	 * @return A String array where index 0 is the tab indent and index 1 is the line
	 */
	public static String[] bannerLine(CharSequence symbol, int indent, int length) {
		String key = new StringBuilder(symbol).append(indent).append(length).toString();
		String[] whatNots = bannerLineCache.get(key);
		if(whatNots==null) {
			synchronized(bannerLineCache) {
				whatNots = bannerLineCache.get(key);
				if(whatNots==null) {
					whatNots = new String[2];
					StringBuilder tabs = new StringBuilder(indent);
					for(int i = 0; i < indent; i++) {
						tabs.append("\t");
					}
					StringBuilder line = new StringBuilder(indent * length * symbol.length());
					for(int i = 0; i < length; i++) {
						line.append(symbol);
					}
					whatNots[0] = tabs.toString();
					whatNots[1] = line.toString();
					bannerLineCache.put(key, whatNots);
				}
			}
		}
		return whatNots;
	}
	
	


}
