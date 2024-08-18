package org.terraform.watchdog;

import org.jetbrains.annotations.Nullable;
import org.terraform.main.TLogger;
import org.terraform.main.config.TConfig;
import org.terraform.utils.injection.Inject;
import org.terraform.utils.injection.InjectableObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TfgWatchdogSuppressant implements InjectableObject {
	@Nullable
    Field instanceField = null;
	@Nullable
    Field lastTickField = null;
	@Nullable
    Class<?> watchdogThreadClass = null;
	@Nullable
    Method tickMethod = null;
	@Nullable
    Object watchdogThreadInstance = null;
    @Inject
    TConfig config;
    @Inject
    TLogger logger;
	
	public TfgWatchdogSuppressant(){
		if(config.DEVSTUFF_SUPPRESS_WATCHDOG)
			try {
				logger.info("[NOTICE] TerraformGenerator will suppress the server's watchdog "
						+ "while generating chunks to prevent unnecessary stacktrace warnings. Unless you specifically need the"
						+ "watchdog now (to take aikar timings or debug lag), you don't need to take any action.");
				logger.info("It is recommended to pregenerate to reduce lag problems.");
				Class<?> watchdogThreadClass = Class.forName("org.spigotmc.WatchdogThread");
				
		        instanceField = watchdogThreadClass.getDeclaredField("instance");
		        instanceField.setAccessible(true);
		
		        lastTickField = watchdogThreadClass.getDeclaredField("lastTick");
		        lastTickField.setAccessible(true);
		        
		        tickMethod = watchdogThreadClass.getDeclaredMethod("tick");
		        tickMethod.setAccessible(true);
		        
		        watchdogThreadInstance = this.instanceField.get(null);
	        	logger.info("Watchdog Thread hooked.");
			}
	        catch(SecurityException | NoSuchFieldException | ClassNotFoundException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException e) {
	        	logger.info("Watchdog instance could not be found.");
	        	logger.stackTrace(e);
	        	instanceField = null;
	        	lastTickField = null;
	        	watchdogThreadClass = null;
	        	watchdogThreadInstance = null;
	        	tickMethod = null;
	        }
	}
	
	public void tickWatchdog() {
		if(watchdogThreadInstance == null || lastTickField == null || tickMethod == null) return;
        try {
            if ((long) lastTickField.get(watchdogThreadInstance) != 0) {
            	tickMethod.invoke(watchdogThreadInstance);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        	logger.stackTrace(e);
            logger.info("Failed to tick watchdog");
        }
    }
	
}
