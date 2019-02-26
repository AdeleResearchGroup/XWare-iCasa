package fr.liglab.adele.interop.services.dataManip;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.icasa.device.temperature.Thermometer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.List;

@ContextEntity(coreServices = {pidService.class, ServiceLayer.class})
public class pidServiceImpl implements pidService,ServiceLayer {

    @ContextEntity.State.Field(service = pidService.class, state = pidService.SERVICE_STATUS,value="ready")
    private String status;

    @ContextEntity.State.Field(service = ServiceLayer.class,state = ServiceLayer.NAME)
            public String name;
    @ContextEntity.State.Field(service = ServiceLayer.class, state = ServiceLayer.SERVICE_QOS,value="100",directAccess = true)
            private int SrvQoS;

    MiniPID miniPID;
    @Requires(id="localThermos",specification = Thermometer.class,optional = false)
    List<Thermometer> th;

    @Override
    public int getMinQos() {
        return 100;
    }

    @Override
    public int getServiceQoS() {
        SrvQoS=100;
        return SrvQoS;
    }

    @Override
    public String getServiceName() {
        return name;
    }

    //STATES CHANGE
    @ContextEntity.State.Push(service = pidService.class, state = pidService.SERVICE_STATUS)
    public String pushService(String serviceState) {
        return serviceState;
    }


    double P;
    double I;
    double D;

    @Override
    public boolean setPIDvars(double p, double i, double d) {
        P=p;
        I=i;
        D=d;
        return true;
    }

    @Override
    public boolean startPID(double objective) {
        miniPID = new MiniPID(P,I,D);
        miniPID.setOutputLimits(0,1);
        miniPID.setSetpointRange(4);
        miniPID.setSetpoint(objective);
        pushService("init");
        return true;
    }

    @Override
    public double getControlVariableValue(double objective, double currentValue){
            double controlSignal = miniPID.getOutput(currentValue,objective);

            //System.err.printf("Target\tActual\tOutput\tError\n");
            //System.err.printf("%3.2f\t%3.2f\t%3.2f\t%3.2f\n",objective,currentValue,controlSignal,(objective-currentValue));
            return controlSignal;

    }

    @Override
    public String getServiceStatus() {
        return status;
    }
}