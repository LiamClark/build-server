package nl.tudelft.ewi.build;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;

@Slf4j
public class BuildServer {
	
	public static void main(String[] args) throws Exception {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		
		Config config = new Config();
		config.reload();
			
		BuildServer server = new BuildServer(config);
		server.startServer();
	}

	private final Server server;

	public BuildServer(Config config) throws IOException {
		log.info("Starting build-server on port: {}", config.getHttpPort());
		this.server = new Server(config.getHttpPort());
		server.setHandler(new BuildServerHandler(config));
	}
	
	public void startServer() throws Exception {
		server.start();
		server.join();
	}
	
	public void stopServer() throws Exception {
		server.stop();
	}
	
	private static class BuildServerHandler extends ServletContextHandler {
		
		public BuildServerHandler(final Config config) {
			addEventListener(new GuiceResteasyBootstrapServletContextListener() {
				@Override
				protected List<Module> getModules(ServletContext context) {
					return ImmutableList.<Module>of(new BuildServerModule(config));
				}
				
				@Override
				protected void withInjector(Injector injector) { }
			});
			
			addServlet(HttpServletDispatcher.class, "/");
		}
	}
	
}
