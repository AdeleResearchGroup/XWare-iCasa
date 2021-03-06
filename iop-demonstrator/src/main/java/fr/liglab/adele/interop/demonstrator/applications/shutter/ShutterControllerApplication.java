package fr.liglab.adele.interop.demonstrator.applications.shutter;

import tec.units.ri.unit.Units;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.functional.extension.FunctionalExtension;

import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;

import fr.liglab.adele.icasa.layering.services.location.ZoneService;
import fr.liglab.adele.icasa.layering.services.location.ZoneServiceFunctionalExtension;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.BindingPolicy;
import org.apache.felix.ipojo.annotations.Modified;
import org.apache.felix.ipojo.annotations.Requires;
import fr.liglab.adele.cream.facilities.ipojo.annotation.ContextRequirement;

import fr.liglab.adele.interop.services.shutter.ShutterController;

import fr.liglab.adele.icasa.location.LocatedObject;
import fr.liglab.adele.icasa.device.doorWindow.WindowShutter;
import fr.liglab.adele.icasa.device.light.Photometer;

@ContextEntity(coreServices = {ApplicationLayer.class, ShutterController.class})

@FunctionalExtension(id="ZoneService",contextServices = ZoneService.class, implementation = ZoneServiceFunctionalExtension.class)

public class ShutterControllerApplication implements ApplicationLayer, ShutterController {

	
	@Requires(id="photometer", filter=ZoneService.OBJECTS_IN_ZONE, optional=false, proxy=false, policy= BindingPolicy.DYNAMIC_PRIORITY)
    @ContextRequirement(spec = {LocatedObject.class})
	private Photometer photometer;
 
    @Requires(id="shutters", filter=ZoneService.OBJECTS_IN_ZONE, optional=false,  proxy=false)
    @ContextRequirement(spec = {LocatedObject.class})
    private WindowShutter[] shutters;

    private double threshold = 400;
    
    @Override
	public double getThreshold() {
    	return this.threshold;
    }

    @Override
	public void setThreshold(double threshold) {
    	this.threshold = threshold;
    	control();
    }

    @Bind(id="photometer")
    private void bind() {
    	control();
    }

    @Modified(id="photometer")
    private void modified() {
    	control();
    }

    private final static double OPEN 	= 1d;
    private final static double CLOSED 	= 0d;

    private void control() {

    	double currentLuminosity = photometer.getIlluminance().to(Units.LUX).getValue().doubleValue();
		double shutterLevel 	=  currentLuminosity <= threshold ? CLOSED : OPEN;
		
		for (WindowShutter	shutter : shutters) {
			shutter.setShutterLevel(shutterLevel);
		}
    	
    }

}
