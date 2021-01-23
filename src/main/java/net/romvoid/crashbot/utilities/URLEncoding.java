package net.romvoid.crashbot.utilities;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URLEncoding {
	private static final Logger log = LoggerFactory.getLogger(URLEncoding.class);
	private static final String UTF8 = StandardCharsets.UTF_8.toString();
	
    public static String decode(String s) {
    	try {
    		return URLDecoder.decode(s, UTF8);
    	} catch (UnsupportedEncodingException e) {
			log.error("Error Decoding URL: {}", s);
		}
        return "";
    }

    public static String encode(String s) {
    	try {
    		return URLEncoder.encode(s, UTF8);
    	} catch (UnsupportedEncodingException e) {
			log.error("Error Encoding URL: {}", s);
		}
        return "";
    }
}
