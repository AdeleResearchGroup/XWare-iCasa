package fr.liglab.adele.interop.iop.publisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleContext;

import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;

import org.ow2.chameleon.fuchsia.core.component.AbstractExportManagerComponent;
import org.ow2.chameleon.fuchsia.core.component.ExportManagerIntrospection;
import org.ow2.chameleon.fuchsia.core.component.ExportManagerService;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;

import de.mannheim.wifo2.iop.service.model.IFunctionality;
import de.mannheim.wifo2.iop.service.model.impl.Functionality;

import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.iop.device.api.IOPService;
import fr.liglab.adele.iop.device.exporter.ServiceDeclaration;

@Component
@Provides(specifications = {ExportManagerService.class, ExportManagerIntrospection.class })
public class LocatedObjectPublisher extends AbstractExportManagerComponent {

	@ServiceProperty(name = Factory.INSTANCE_NAME_PROPERTY)
	private String name;

	private final Map<String,ExportDeclaration> published = new ConcurrentHashMap<>();
	
	protected LocatedObjectPublisher(BundleContext bundleContext) {
		super(bundleContext);
	}

	@Override
	public String getName() {
		return name;
	}

	@Validate
	protected void start() {
		super.start();
	}

	@Invalidate
	protected void stop() {
		super.stop();
	}

	
	@Bind(id="device", proxy = false, optional = true, aggregate = true)
	public void serviceBound(LocatedObject service, Map<String,?> properties) {
		
		if (service instanceof IOPService)
			return;

		String component						= (String) properties.get("factory.name");
		String id								= String.valueOf((Long) properties.get(org.osgi.framework.Constants.SERVICE_ID));
		List<IFunctionality> functionalities 	= new ArrayList<>();
		
		for (String provided : (String[])properties.get(org.osgi.framework.Constants.OBJECTCLASS)) {
			functionalities.add(new Functionality(provided));
		}
		
		
		ExportDeclaration declaration = ServiceDeclaration.from(component,service,id,functionalities,exportedProperties(service,properties));
		published.put(id,declaration);
		registerExportDeclaration(declaration);
	}
	
	@Unbind(id="device")
	public void serviceUnbound(LocatedObject service, Map<String,?> properties) {
		String id						= String.valueOf((Long) properties.get(org.osgi.framework.Constants.SERVICE_ID));
		ExportDeclaration declaration 	= published.remove(id);
		if (declaration != null) {
			unregisterExportDeclaration(declaration);
		}
	}

	@Modified(id="device")
	public void serviceModifed(LocatedObject service, Map<String,?> properties) {
		
		String id					= String.valueOf((Long) properties.get(org.osgi.framework.Constants.SERVICE_ID));
		ExportDeclaration export 	= published.get(id);
		
		if (modified(export,service,properties)) {
			serviceUnbound(service,properties);
			serviceBound(service,properties);
		}
	}
	
	
	private static Map<String,?> exportedProperties(LocatedObject service, Map<String,?> properties) {
		
		Map<String,Object> exportedProperties	= new HashMap<>();
		
		if (service.getZone() != null && !service.getZone().equals(LocatedObject.LOCATION_UNKNOWN)) {
			exportedProperties.put("location", service.getZone());
		}
		
		return exportedProperties;
	}

	private static boolean modified(ExportDeclaration export, LocatedObject service, Map<String,?> properties) {
		return export != null && ! new ServiceDeclaration(export).getProperties().equals(exportedProperties(service, properties));
	}


}
