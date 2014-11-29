package com.netx.eap.R1.core;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.netx.generics.R1.sql.Database;
import com.netx.generics.R1.sql.JdbcDriver;
import com.netx.generics.R1.util.Strings;
import com.netx.generics.R1.util.Tools;
import com.netx.generics.R1.util.ConstructionException;
import com.netx.basic.R1.shared.Globals;
import com.netx.basic.R1.io.FileSystem;
import com.netx.basic.R1.io.AccessDeniedException;
import com.netx.basic.R1.io.FileSystemException;
import com.netx.basic.R1.io.ReadWriteException;
import com.netx.basic.R1.logging.LogFile;
import com.netx.basic.R1.eh.IntegrityException;
import com.netx.bl.R1.core.Repository;
import com.netx.bl.R1.core.RepositoryConfig;
import com.netx.bl.R1.core.Connection;
import com.netx.eap.R1.core.EapRequest.METHOD;
import com.netx.eap.R1.bl.Functions;
import com.netx.eap.R1.bl.FunctionInstance;
import com.netx.rit.R1.bl.RIT;


public class SrvInitializer extends HttpServlet {

	public void init(ServletConfig config) throws ServletException {
		// We do not catch ServletException here; this really should not happen,
		// so if it does we want to treat this as an unrecoverable situation and 
		// prevent the initializer servlet from continue loading.
		super.init(config);
		EapContext ctx = new EapContext();
		getServletContext().setAttribute(Constants.SRVCTX_EAP_CTX, ctx);
		try {
			// Read configuration:
			Properties props = new Properties();
			props.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
			Config.LOG_DIR = props.getProperty("log-dir");
			if(Strings.isEmpty(Config.LOG_DIR)) {
				throw new IntegrityException();
			}
			String dbServer = props.getProperty("db-server");
			String dbName = props.getProperty("db-name");
			String dbPort = props.getProperty("db-port");
			String dbUsername = props.getProperty("db-username");
			String dbPassword = props.getProperty("db-password");
			Config.DB_CONNECTION = new Database(JdbcDriver.MYSQL, dbServer, new Integer(dbPort), dbName, dbUsername, dbPassword);
			if(Config.DB_CONNECTION == null) {
				throw new IntegrityException();
			}
			// Set locale:
			Globals.setSystemLocale(Config.SYSTEM_LOCALE);
			Globals.setApplicationLocale(Config.APP_LOCALE);
			// Initialize logger:
			final String filename = Config.APP_NAME+"_<date>.txt";
			try {
				FileSystem logDir = new FileSystem(Config.LOG_DIR);
				LogFile logFile = new LogFile(logDir, filename, 10);
				Config.LOGGER.setOutput(logFile);
				Config.LOGGER.setLevel(Config.LOG_LEVEL);
			}
			catch(FileSystemException fse) {
				// TODO find a way to put FileSystemException and InputOutputException under the same hierarchy 
				Config.LOGGER.warn("could not create log file '"+filename+"' in directory '"+Config.LOG_DIR+"'", fse);
			}
			catch(ReadWriteException ioe) {
				Config.LOGGER.warn("could not create log file '"+filename+"' in directory '"+Config.LOG_DIR+"'", ioe);
			}
			
			// Initialize Servlet context properties:
			Config.LOGGER.info("initializing application");
			Config.LOGGER.info("system locale: "+Globals.getSystemLocale().toString());
			try {
				ctx.setApplicationRoot(new FileSystem(getServletContext().getRealPath("/")));
			}
			catch(AccessDeniedException ade) {
				ctx.addInitError(this, null, ade);
			}
			
			// Initialize database connectivity:
			// TODO ensure that SQL error handling is handled by BL with rollbacks if necessary
			RepositoryConfig dbConfig = new RepositoryConfig();
			dbConfig.setCacheEnabled(true);
			// TODO remove this with JDK7
			@SuppressWarnings("unchecked")
			final Repository rep = Repository.load(RIT.class);
			rep.connect(Config.DB_CONNECTION, dbConfig);
			Connection c = rep.getConnection();
			ctx.setRepository(rep);
			
			// Get Forms from database:
			List<FunctionInstance> eiList = Functions.getInstance().listAll(c);
			if(eiList.isEmpty()) {
				ctx.addInitError(this, "could not find any AF forms on the database", null);
			}
			
			// Initialize Functions:
			Config.LOGGER.info("loading Functions...");
			Map<String,Function> functions = new HashMap<String,Function>();
			for(FunctionInstance entry : eiList) {
				final String errorMsg = "while initializing Function '"+entry.getAlias()+"': ";
				try {
					// TODO consider change Tools.createObject to accept a String for class name instead of the actual class
					Class<?> aClass = Class.forName(entry.getPackageName()+"."+entry.getClassName());
					Object o = Tools.createObject(aClass);
					if(o instanceof Function) {
						Function f = (Function)o;
						f.startup(entry.getAlias(), entry.getPermissionId());
						f.init(ctx);
						functions.put(f.getAlias(), f);
						Config.LOGGER.info("loaded Function '"+f.getAlias()+"'");
					}
					else {
						ctx.addInitError(this, errorMsg+"class '"+entry.getClassName()+"' is not a subclass of "+Function.class.getName(), null);
					}
				}
				catch(ClassNotFoundException cnfe) {
					ctx.addInitError(this, errorMsg+"class '"+entry.getClassName()+"' was not found in the classpath", null);
				}
				catch(ConstructionException ce) {
					ctx.addInitError(this, errorMsg+ce.getMessage(), ce);
				}
				catch(Exception e) {
					// Exception can be thrown by Function.init
					ctx.addInitError(this, errorMsg+e.getMessage(), e);
				}
			}
			getServletContext().setAttribute(Constants.SRVCTX_EAP_FUNCTIONS, functions);
			// TODO either put this on a finally or do some form of exception handling
			c.close();
			
			// TODO other inits

			Config.LOGGER.info("application initialized");
		}
		catch(Throwable t) {
			ctx.addInitError(this, null, t);
		}
	}

	@SuppressWarnings("unchecked")
	public void destroy() {
		EapContext ctx = (EapContext)getServletContext().getAttribute(Constants.SRVCTX_EAP_CTX);
		// Destroy all Functions:
		Map<String,Function> functions = (Map<String,Function>)getServletContext().getAttribute(Constants.SRVCTX_EAP_FUNCTIONS);
		for(Function f : functions.values()) {
			Config.LOGGER.info("destroying Function "+f.getAlias()+"...");
			try {
				f.destroy(ctx);
			}
			catch(Exception e) {
				Config.LOGGER.error("while destroying Function", e);
			}
		}
		// Release the log file just in case, although this should be done automatically
		Config.LOGGER.setOutput(System.out);
		// Please note that this message will appear on the regular application server logs rather
		// than in the EAP logs, since destroy() is only called once all threads have finalized.
		Config.LOGGER.info("application has shut down");
	}

	protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		throw new MethodNotAllowedException(METHOD.GET);
	}

	protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		throw new MethodNotAllowedException(METHOD.POST);
	}
}
