package com.netx.eap.R1.core;
import java.util.Map;
import java.util.HashMap;
import com.netx.basic.R1.eh.Checker;
import com.netx.basic.R1.io.File;


public class MimeTypes {

	// Text:
	public static final String TEXT_PLAIN = "text/plain";
	public static final String TEXT_HTML = "text/html";
	public static final String TEXT_XML = "text/xml";
	public static final String TEXT_CSS = "text/css";
	public static final String TEXT_JS = "text/javascript";
	public static final String TEXT_CSV = "text/csv";
	// Images:
	public static final String IMAGE_JPG = "image/jpeg";
	public static final String IMAGE_GIF = "image/gif";
	public static final String IMAGE_PNG = "image/png";
	public static final String IMAGE_TIFF = "image/tiff";
	public static final String IMAGE_BMP = "image/bmp";
	public static final String IMAGE_SVG = "image/svg+xml";
	public static final String IMAGE_ICO = "image/vnd.microsoft.icon";
	// Audio:
	public static final String AUDIO_BASIC = "audio/basic";
	public static final String AUDIO_MID = "audio/mid";
	public static final String AUDIO_MPEG = "audio/mpeg"; // Note: audio/mpeg is also the content type for MP3
	public static final String AUDIO_MP4 = "audio/mp4";
	public static final String AUDIO_OGG = "audio/ogg";
	public static final String AUDIO_WMA = "audio/x-ms-wma";
	public static final String AUDIO_WAV = "audio/x-wav";
	// Video:
	public static final String VIDEO_MPEG = "video/mpeg";
	public static final String VIDEO_MP4 = "video/mp4"; // Note: MP4 extension is registered for audio content, not video
	public static final String VIDEO_OGG = "video/ogg"; // Note: OGG extension is registered for audio content, not video
	public static final String VIDEO_AVI = "video/x-msvideo";
	public static final String VIDEO_WMV = "video/x-ms-wmv";
	public static final String VIDEO_QUICKTIME = "video/quicktime";
	// Application:
	public static final String APP_BINARY = "application/octet-stream";
	public static final String APP_PDF = "application/pdf";
	public static final String APP_POSTSCRIPT = "application/postscript";
	public static final String APP_SOAP = "application/soap+xml";
	public static final String APP_XHTML = "application/xhtml+xml";
	public static final String APP_DTD = "application/xml-dtd";
	public static final String APP_ZIP = "application/zip";
	public static final String APP_GZIP = "application/x-gzip";
	public static final String APP_Z = "application/x-compress";
	public static final String APP_TGZ = "application/x-compressed";
	public static final String APP_TAR = "application/x-tar";
	public static final String APP_GTAR = "application/x-gtar";
	public static final String APP_RAR = "application/x-rar-compressed";
	public static final String APP_ARJ = "application/x-arj";
	public static final String APP_JAR = "application/java-archive";
	public static final String APP_TTF = "application/x-font-ttf";
	public static final String APP_SWF = "application/x-shockwave-flash";
	public static final String APP_RTF = "application/rtf";
	public static final String APP_WINHELP = "application/winhlp";
	// Microsoft Office 2003:
	public static final String OFFICE_2003_WORD = "application/vnd.ms-word";
	public static final String OFFICE_2003_EXCEL = "application/vnd.ms-excel";
	public static final String OFFICE_2003_OUTLOOK = "application/vnd.ms-outlook";
	public static final String OFFICE_2003_POWERPOINT = "application/vnd.ms-powerpoint";
	public static final String OFFICE_2003_ACCESS = "application/vnd.ms-access";
	public static final String OFFICE_2003_PROJECT = "application/vnd.ms-project";
	public static final String OFFICE_2003_VISIO = "application/vnd.visio";
	public static final String OFFICE_2003_WORKS = "application/vnd.ms-works";
	public static final String OFFICE_2007_WORD = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	public static final String OFFICE_2007_WORD_ME = "application/vnd.ms-word.document.macroEnabled.12";
	public static final String OFFICE_2007_WORD_TP = "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
	public static final String OFFICE_2007_WORD_TP_ME = "application/vnd.ms-word.template.macroEnabled.12";
	public static final String OFFICE_2007_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public static final String OFFICE_2007_EXCEL_ME = "application/vnd.ms-excel.sheet.macroEnabled.12";
	public static final String OFFICE_2007_EXCEL_TP = "application/vnd.openxmlformats-officedocument.spreadsheetml.template";
	public static final String OFFICE_2007_EXCEL_TP_ME = "application/vnd.ms-excel.template.macroEnabled.12";
	public static final String OFFICE_2007_EXCEL_BIN = "application/vnd.ms-excel.sheet.binary.macroEnabled.12";
	public static final String OFFICE_2007_EXCEL_ADDIN = "application/vnd.ms-excel.addin.macroEnabled.12";
	public static final String OFFICE_2007_POWERPOINT = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
	public static final String OFFICE_2007_POWERPOINT_ME = "application/vnd.ms-powerpoint.presentation.macroEnabled.12";
	public static final String OFFICE_2007_POWERPOINT_SS = "application/vnd.openxmlformats-officedocument.presentationml.slideshow";
	public static final String OFFICE_2007_POWERPOINT_SS_ME = "application/vnd.ms-powerpoint.slideshow.macroEnabled.12";
	public static final String OFFICE_2007_POWERPOINT_TP = "application/vnd.openxmlformats-officedocument.presentationml.template";
	public static final String OFFICE_2007_POWERPOINT_TP_ME = "application/vnd.ms-powerpoint.template.macroEnabled.12";
	public static final String OFFICE_2007_POWERPOINT_ADDIN = "application/vnd.ms-powerpoint.addin.macroEnabled.12";
	public static final String OFFICE_2007_POWERPOINT_SLIDE = "application/vnd.openxmlformats-officedocument.presentationml.slide";
	public static final String OFFICE_2007_POWERPOINT_SLIDE_ME = "application/vnd.ms-powerpoint.slide.macroEnabled.12";
	public static final String OFFICE_2007_ONENOTE = "application/onenote";
	public static final String OFFICE_2007_THEME = "application/vnd.ms-officetheme";

	private final static Map<String,String> _cTypes = new HashMap<String,String>();
	
	static {
		// Text:
		_register(TEXT_PLAIN, "txt", "log", "c", "h", "java");
		_register(TEXT_HTML, "html", "htm");
		_register(TEXT_XML, "xml");
		_register(TEXT_CSS, "css");
		_register(TEXT_JS, "js");
		_register(TEXT_CSV, "csv");
		// Images:
		_register(IMAGE_JPG, "jpg", "jpe", "jpeg");
		_register(IMAGE_GIF, "gif");
		_register(IMAGE_PNG, "png");
		_register(IMAGE_TIFF, "tif", "tiff");
		_register(IMAGE_BMP, "bmp");
		_register(IMAGE_SVG, "svg");
		_register(IMAGE_ICO, "ico");
		// Audio:
		_register(AUDIO_BASIC, "au", "snd");
		_register(AUDIO_MID, "mid", "rmi");
		_register(AUDIO_MPEG, "mp3");
		_register(AUDIO_MP4, "mp4");
		_register(AUDIO_OGG, "ogg");
		_register(AUDIO_WMA, "wma");
		_register(AUDIO_WAV, "wav");
		// Video:
		_register(VIDEO_MPEG, "mpg", "mpe", "mpeg");
		_register(VIDEO_AVI, "avi");
		_register(VIDEO_WMV, "wmv");
		_register(VIDEO_QUICKTIME, "mov", "qt");
		// Applications:
		_register(APP_BINARY, "exe", "class", "bin");
		_register(APP_PDF, "pdf");
		_register(APP_POSTSCRIPT, "ps", "eps", "ai");
		_register(APP_XHTML, "xhtml");
		_register(APP_DTD, "dtd");
		_register(APP_ZIP, "zip");
		_register(APP_GZIP, "gz");
		_register(APP_Z, "z");
		_register(APP_TGZ, "tgz");
		_register(APP_TAR, "tar");
		_register(APP_GTAR, "gtar");
		_register(APP_RAR, "rar");
		_register(APP_ARJ, "arj");
		_register(APP_JAR, "jar");
		_register(APP_TTF, "ttf");
		_register(APP_SWF, "swf");
		_register(APP_RTF, "rtf");
		_register(APP_WINHELP, "hlp");
		// Microsoft Office:
		_register(OFFICE_2003_WORD, "doc", "dot");
		_register(OFFICE_2003_EXCEL, "xls", "xlt", "xla", "xlc", "xlm", "xlw");
		_register(OFFICE_2003_OUTLOOK, "msg");
		_register(OFFICE_2003_POWERPOINT, "ppt", "pps", "pot");
		_register(OFFICE_2003_ACCESS, "mdb");
		_register(OFFICE_2003_PROJECT, "mpp");
		_register(OFFICE_2003_VISIO, "vsd", "vst", "vss", "vsw");
		_register(OFFICE_2003_WORKS, "wcm", "wdb", "wks", "wps");
		_register(OFFICE_2007_WORD, "docx");
		_register(OFFICE_2007_WORD_ME, "docm");
		_register(OFFICE_2007_WORD_TP, "dotx");
		_register(OFFICE_2007_WORD_TP_ME, "dotm");
		_register(OFFICE_2007_EXCEL, "xlsx");
		_register(OFFICE_2007_EXCEL_ME, "xlsm");
		_register(OFFICE_2007_EXCEL_TP, "xltx");
		_register(OFFICE_2007_EXCEL_TP_ME, "xltm");
		_register(OFFICE_2007_EXCEL_BIN, "xlsb");
		_register(OFFICE_2007_EXCEL_ADDIN, "xlam");
		_register(OFFICE_2007_POWERPOINT, "pptx");
		_register(OFFICE_2007_POWERPOINT_ME, "pptm");
		_register(OFFICE_2007_POWERPOINT_SS, "ppsx");
		_register(OFFICE_2007_POWERPOINT_SS_ME, "ppsm");
		_register(OFFICE_2007_POWERPOINT_TP, "potx");
		_register(OFFICE_2007_POWERPOINT_TP_ME, "potm");
		_register(OFFICE_2007_POWERPOINT_ADDIN, "ppam");
		_register(OFFICE_2007_POWERPOINT_SLIDE, "sldx");
		_register(OFFICE_2007_POWERPOINT_SLIDE_ME, "sldm");
		_register(OFFICE_2007_ONENOTE, "one", "onetoc2", "onetmp", "onepkg");
		_register(OFFICE_2007_THEME, "thmx");
	}
	
	public static String getAssociatedContentType(File f) {
		Checker.checkNull(f, "f");
		String ext = f.getExtension();
		if(ext == null) {
			return null;
		}
		return _cTypes.get(ext.toLowerCase());
	}
	
	private static void _register(String contentType, String ... extensions) {
		for(String ext : extensions) {
			ext = ext.toLowerCase();
			if(_cTypes.get(ext) != null) {
				throw new IllegalArgumentException("extension '"+ext+"' already registered");
			}
			_cTypes.put(ext, contentType);
		}
	}
}
