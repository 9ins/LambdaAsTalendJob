package org.chaostocosmos.talend.aws;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**  
 * Logger : 
 * Description : 
 * Logging for debug information
 *  
 * Modification Information  
 *  ---------   ---------   -------------------------------
 *  20161118	9ins		draft
 *  
 * @author 9ins
 * @since 20161118
 * @version 1.0
 */
public class Logger {
	/**
	 * align column size
	 */
	public static final int alignCharNum = 180;	
	/**
	 * info filter
	 */
	public static boolean INFO_FILTER = true;	
	/**
	 * debug filter
	 */
	public static boolean DEBUG_FILTER = true;	
	/**
	 * error filter
	 */
	public static boolean ERROR_FILTER = true;	
	/**
	 * fatal filter
	 */
	public static boolean FATAL_FILTER = true;	
	/**
	 * throwable filter
	 */
	public static boolean THROWABLE_FILTER = true;
	/**
	 * log file size to split(default : 100MB)
	 */
	public static long LOG_SIZE = 1024*1024*100;
	/**
	 * encoding(default : UTF-8)
	 */
	public static String ENCODING = "UTF-16";
	/**
	 * log file duration to split (default : 24시간)
	 */
	public static int LOG_HOUR = 24;
	/**
	 * log file suffix
	 */
    private String logSuffix = ".log";    
	private boolean isAppend;
    private PrintWriter log;    
    private PrintStream ps;    
    private long startTime;    
    private long logInterval;    
    private String fs = System.getProperty("file.separator");    
    private File logFile;    
    private String logName;    
    private static Logger logger;
    
    /**
     * get logger instance
     * @return
     */
    public static Logger getInstance() {
    	if(logger == null) {
    		logger = new Logger(LOG_HOUR, LOG_SIZE, "iq_debug");
    	}
    	return logger;
    }    
    /**
     * get logger instance
     * @param hour
     * @return
     */
    public static Logger getInstance(int hour) {
    	return getInstance(hour, LOG_SIZE, "main");
    }    
    /**
     * get logger instance
     * @param logSize
     * @return
     */
    public static Logger getInstance(long logSize) {
    	return getInstance(LOG_HOUR, logSize, "main");
    }    
    /**
     * get logger instance
     * @param hour
     * @param logSize
     * @return
     */
    public static Logger getInstance(int hour, long logSize) {
    	return getInstance(hour, logSize, "main");
    }
    /**
     * get logger instance
     * @param hour
     * @param logSize
     * @param logPath
     * @return
     */
    public static Logger getInstance(int hour, long logSize, String logPath) {
    	return getInstance(hour, logSize, logPath, ENCODING);
    }    
    /**
     * get logger instance
     * @param hour
     * @param logSize
     * @param logPath
     * @param encoding
     * @return
     */
    public static Logger getInstance(int hour, long logSize, String logPath, String encoding) {
    	if(logger == null)
    		logger = new Logger(hour, logSize, logPath, encoding);
    	return logger;    	
    }    
    /**
     * get logger instance
     * @param hour
     * @param logSize
     * @param logPath
     */
    private Logger(int hour, long logSize, String logPath) {
    	this(hour, logSize, logPath, ENCODING);
    }    
    /**
     * get logger instance
     * @param hour
     * @param logSize
     * @param logPath
     * @param encoding
     */
    private Logger(int hour, long logSize, String logPath, String encoding) {
    	init(hour, logSize, logPath, encoding);
    }    
    /**
     * get logger instance
     * @param hour
     * @param logSize
     * @param logPath
     * @param enc
     */
    private void init(int hour, long logSize, String logPath, String enc) {
    	LOG_SIZE = logSize;
    	ENCODING = enc;    	
    	this.logInterval = (long)(hour*60*60*1000);
    	this.startTime = System.currentTimeMillis();
    	this.logName = logPath.replace("/", fs)+logSuffix;
    	this.logFile = new File(this.logName);
    	this.ps = System.out;
  		createLogFile(startTime);
    }       
    /**
     * create log file
     * @param millis
     */
    private void createLogFile(long millis) {
    	try {  		
    		if(this.logFile.exists()) {
    			if(log != null)
    				log.close();
    			if(this.logFile.length() > LOG_SIZE) {
    				File oldFile = new File(getLogFileName(millis));
    				//System.out.println("file : "+newFile.toString());
    				if(!this.logFile.renameTo(oldFile)) {
    					throw new IOException("Can't change log file name. BEFORE : "+this.logFile.getAbsolutePath());
    				}
    				this.logFile = new File(this.logName);
    			}
    		} else {
    			if(!this.logFile.createNewFile()) {
    				throw new IOException("Can't create new file : "+this.logFile.getAbsolutePath());
    			}
    		}
			this.isAppend = true;
    		log = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.logFile, this.isAppend), ENCODING), true);
    	} catch(IOException e) { 
    		e.printStackTrace();
		}    	
    }
    
    /**
     * get log file name
     * @param millis
     * @return
     * @throws IOException
     */
    public String getLogFileName(long millis) throws IOException {
    	SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");    	
    	String date = df.format(new Date());
    	String logPath = logFile.getAbsolutePath(); 
    	String path = logPath.substring(0, logPath.lastIndexOf(fs)+1);
    	String name = logName.substring(logName.lastIndexOf(fs)+1);
        return path+name+"_"+date+logSuffix;
    }
    
    /**
     * to compare log duration time
     */
    private void compare() {
    	long currentMillis = System.currentTimeMillis();
    	long elapse = currentMillis-this.startTime;
    	if(elapse > this.logInterval || this.logFile.length() > LOG_SIZE) {
    		//System.out.println("elapse time : "+elapse+"   interval : "+this.logInterval+"  LogSize : "+this.logFile.length()+"  LOG_LIMIT : "+LOG_SIZE);
    		this.startTime = currentMillis;
   			createLogFile(this.startTime);
    	} 
    }    
    
    /**
     * log for infomation
     * @param msg
     */
    public void info(String msg) {
    	info(msg, true);
    }
    
    /**
     * log for information
     */
    public void info(String msg, boolean isSysOut) {
    	synchronized(log) {
            if(INFO_FILTER) {
                compare();
                StackTraceElement[] ele = (new Exception()).getStackTrace();
                String className = ele[ele.length-1].getClassName();
                className = className.substring(className.lastIndexOf(".")+1);
                int lineNumber = ele[ele.length-1].getLineNumber();
            	String msgStr = "[INFO]["+className+":"+lineNumber+"]"+alignString(msg);        	
                if(isSysOut) {
                	stdOut(msgStr);
                }
                log.println(msgStr);
            }
    	}
    }
    
    /**
     * log for debug
     * @param msg
     */
    public void debug(String msg) {
    	debug(msg, true);
    }

    /**
     * log for debug
     * @param msg
     * @param isSysOut
     */
    public void debug(String msg, boolean isSysOut) {
        synchronized (log) {
            if(DEBUG_FILTER) {
                compare();
                StackTraceElement[] ele = (new Exception()).getStackTrace();
                String className = ele[ele.length-1].getClassName();
                className = className.substring(className.lastIndexOf(".")+1);
                int lineNumber = ele[ele.length-1].getLineNumber();
            	String msgStr = "[DEBUG]["+className+":"+lineNumber+"]"+alignString(msg);        	
                if(isSysOut) {
                	stdOut(msgStr);
                }
                log.println(msgStr);
            }
    	}
    }    
    
    /**
     * log for error
     * @param msg
     */
    public void error(String msg) {
    	error(msg, true);
    }
    
    /**
     * log for error
     * @param msg
     * @param isSysOut
     */
    public void error(String msg, boolean isSysOut) {
        synchronized (log) {
            if(ERROR_FILTER) {
                compare();
                StackTraceElement[] ele = (new Exception()).getStackTrace(); 
                String className = ele[ele.length-1].getClassName();
                className = className.substring(className.lastIndexOf(".")+1);
                int lineNumber = ele[ele.length-1].getLineNumber();
            	String msgStr = "[ERROR]["+className+":"+lineNumber+"]"+alignString(msg);  
                if(isSysOut) {
                	stdOut(msgStr);
                }
            log.println(msgStr);
            }
    	}
    }    

    /**
     * log for fatal
     * @param msg
     */
    public void fatal(String msg) {
    	fatal(msg, true);
    }
    
    /**
     * log for fatal
     * @param msg
     * @param isSysOut
     */
    public void fatal(String msg, boolean isSysOut) {
        synchronized (log) {
            if(FATAL_FILTER) {
                compare();
                StackTraceElement[] ele = (new Exception()).getStackTrace();
                String className = ele[ele.length-1].getClassName();
                className = className.substring(className.lastIndexOf(".")+1);
                int lineNumber = ele[ele.length-1].getLineNumber();
            	String msgStr = "[FATAL]["+className+":"+lineNumber+"]"+alignString(msg);  
                if(isSysOut) {
                	stdOut(msgStr);
                }
            log.println(msgStr);
            }
    	}
    }    

    /**
     * log for throwable
     * @param e
     * @return
     */
    public String throwable(Throwable e) {
    	return throwable(e, true);
    }
    
    /**
     * log for throwable
     * @param e
     * @param isSysout
     * @return
     */
    public String throwable(Throwable e, boolean isSysout) {
    	String allMsg = "";
        synchronized(log) {
        	if(THROWABLE_FILTER) {
                compare();
                if(e != null) {            	
                	StackTraceElement[] ele = (new Exception()).getStackTrace();
                    String className = ele[ele.length-1].getClassName();
                    className = className.substring(className.lastIndexOf(".")+1);
                    int lineNumber = ele[ele.length-1].getLineNumber();
                	String msgStr = "[THROWABLE]["+className+":"+lineNumber+"]"+alignString(e.toString()+" : "+e.getMessage());  
                  	if(isSysout) {
                  		stdOut(msgStr);
                  	}
               		log.println(msgStr);
               		allMsg += msgStr;
                    StackTraceElement[] elements = e.getStackTrace();
                    for(int i=0; i<elements.length; i++) {
                    	String msg = "\tat "+elements[i].toString();
                       	if(isSysout) {
                       		stdOut(msg);
                       	}
                    	log.println(msg);
                    	allMsg += msg;
                    }
                }
        	}
    	}
        return allMsg;
    }
    
    /**
     * log for HEX string
     * @param bytes
     */
    public void debugHexCode(byte[] bytes) {
    	debugHexCode(bytes, true);
    }
    
	/**
	 * log for HEX string
	 * @param bytes
	 * @param isSysOut
	 */
    public void debugHexCode(byte[] bytes, boolean isSysOut) {
    	String hex = "";
        for(int i=0; i<bytes.length; i++)
        {
            String str = Integer.toHexString((new Byte(bytes[i])).intValue());
            hex += (str.length() > 2)?str.substring(str.length()-2)+" ":(str.length()==1)?"0"+str+" ":str+" ";
        }
        this.debug(hex, true);
    }
    
    /**
     * log for Map
     * @param map
     */
    public void debugMap(Map<?, ?> map) {
    	this.debugMap(map, true);
    }
    
    /**
     * log for Map
     * @param map
     * @param isSysOut
     */
    public void debugMap(Map<?, ?> map, boolean isSysOut) {
    	String str = "";
    	Iterator<?> iter = map.keySet().iterator();
    	while(iter.hasNext()) {
    		Object key = iter.next();
    		str += key + "=" + map.get(key)+System.getProperty("line.separator");
    	}
    	this.debug(str, true);
    }
    
    /**
     * get aligned string
     * @param msg
     * @return
     */
    public String alignString(String msg) {
    	Calendar cal = Calendar.getInstance();
    	cal.setTimeInMillis(System.currentTimeMillis());
        String year = cal.get(Calendar.YEAR)+"";
        int m = cal.get(Calendar.MONTH)+1;
        String month = (m<10)?"0"+m:m+"";
        int d = cal.get(Calendar.DAY_OF_MONTH);
        String day = (d<10)?"0"+d:d+"";
        int h = cal.get(Calendar.HOUR_OF_DAY);
        String hour = (h<10)?"0"+h:h+"";
        int mi = cal.get(Calendar.MINUTE);
        String minute = (mi<10)?"0"+mi:mi+"";
        int s = cal.get(Calendar.SECOND);
        String second = (s<10)?"0"+s:s+"";

    	String msgStr = "["+ year+"-"+month+"-"+day+" "+hour+":"+minute+":"+second + "] " + msg;
    	//int tab = alignCharNum - msgStr.length();
    	//for(int i=0; i<tab; i++)
    	//	msgStr += " ";
    	return msgStr;
    }
    
    /**
     * print to std IO 
     * @param str
     */
	public void stdOut(String str) {
		ps.println(str);
	}
	
	/**
	 * whether print info
	 * @param isInfo
	 */
	public void setInfo(boolean isInfo) {
		INFO_FILTER = isInfo;
	}
	
	/**
	 * whether print debug
	 * @param isDebug
	 */
	public void setDebug(boolean isDebug) {
		DEBUG_FILTER = isDebug;
	}
	
	/**
	 * whether print error
	 * @param isError
	 */
	public void setError(boolean isError) {
		ERROR_FILTER = isError;
	}
	
	/**
	 * whether print fatal
	 * @param isFatal
	 */
	public void setFatal(boolean isFatal) {
		FATAL_FILTER = isFatal;
	}
	
	/**
	 * whether print throwable
	 * @param isThrowable
	 */
	public void setThrowable(boolean isThrowable) {
		THROWABLE_FILTER = isThrowable;
	}
	
	/**
	 * get whether print info
	 * @return
	 */
	public boolean isInfo() {
		return INFO_FILTER;
	}
	
	/**
	 * get whether print debug
	 * @return
	 */
	public boolean isDebug() {
		return DEBUG_FILTER;
	}
	
	/**
	 * get whether print error
	 * @return
	 */
	public boolean isError() {
		return ERROR_FILTER;
	}
	
	/**
	 * get whether print fatal
	 * @return
	 */
	public boolean isFatal() {
		return FATAL_FILTER;
	}
	
	/**
	 * get whether print throwable
	 * @return
	 */
	public boolean isThrowable() {
		return THROWABLE_FILTER;
	}
	
	/**
	 * set enable to log
	 * @param isEnable
	 */
	public void setEnable(boolean isEnable) {
		this.setInfo(isEnable);
		this.setDebug(isEnable);
		this.setError(isEnable);
		this.setFatal(isEnable);
		this.setThrowable(isEnable);
	}
}
