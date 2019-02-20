package fr.liglab.adele.interop.demonstrator.database;

import fr.liglab.adele.cream.annotations.entity.ContextEntity;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.icasa.clockservice.Clock;
import fr.liglab.adele.icasa.layering.applications.api.ApplicationLayer;
import fr.liglab.adele.icasa.layering.services.api.ServiceLayer;
import fr.liglab.adele.icasa.service.scheduler.PeriodicRunnable;
import fr.liglab.adele.interop.services.database.InfluxService;
import fr.liglab.adele.interop.services.database.InfluxServiceImpl;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ContextEntity(coreServices = {ApplicationLayer.class, ConstantDatabaseWrite.class, PeriodicRunnable.class})
//@Provides(specifications = ConstantDatabaseWrite.class)
public class ConstantDatabaseWriteApp implements ApplicationLayer, ConstantDatabaseWrite, PeriodicRunnable {
    private static final Logger LOG = LoggerFactory.getLogger(ConstantDatabaseWriteApp.class);

    //APPLICATION's STATES
    @ContextEntity.State.Field(service = ConstantDatabaseWrite.class, state = ConstantDatabaseWrite.APPLICATION_STATE)
    private String appState;

    @Requires
    Clock clock;
    @Requires(id = "database", specification = InfluxService.class, optional = true)
    private InfluxService influxDB;

    private @Creator.Field
    Creator.Entity<InfluxServiceImpl> DBserice;

    @Validate
    public void start() {
        Map<String, Object> SrvDBParam = new HashMap<>();
        SrvDBParam.put(ContextEntity.State.id(ServiceLayer.class, ServiceLayer.NAME), "DB");
        DBserice.create("DB", SrvDBParam);
        influxDB.eraseDB();
    }


    @ContextEntity.State.Push(service = ConstantDatabaseWrite.class, state = ConstantDatabaseWrite.APPLICATION_STATE)
    public String pushChange(DateTime currentTime) {
        if(influxDB.isInfluxRunning()){
            influxDB.writeAllSensorsState(currentTime.getMillis());
        }
        return currentTime.toString();
    }

    @Override
    public long getPeriod() {
        return 1;
    }

    @Override
    public TimeUnit getUnit() {
        return TimeUnit.HOURS;
    }

    @Override
    public void run() {
        pushChange(new DateTime(clock.currentTimeMillis()));
    }
}
