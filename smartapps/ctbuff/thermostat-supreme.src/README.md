# Thermostat Supreme Temp Sensor
## SmartThings SmartApp for fully customizable thermostat control
Thermostat supreme can help you to control your SmartThings thermostat using schedules, motion sensors, contact sensors, presence sensors and external temperature sensors. 
## Requirements

 1. This SmartApp
 2. [Thermostat Supreme Temp Sensor](https://github.com/curlytailedbuffalo/myST/tree/master/smartapps/ctbuff/thermostat-supreme-temp-sensor.src) - child SmartApp
 3. [Thermostat Supreme Require Trigger](https://github.com/curlytailedbuffalo/myST/tree/master/smartapps/ctbuff/thermostat-supreme-require-trigger-src) - child SmartApp

## How It Works
Thermostat Supreme works with the use of 3 main components:

 1. **Thermostat**
 2. **Temperature Sensors**
 3. **Triggers**


> **Relation**
>
>     A Thermostat -> *has many* -> Temperature Sensors
>
>     A Temperature Sensor -> *has many* -> Triggers

 
### Thermostat
The thermostat is the main thermostat that you will configure that will have control over your HVAC system. Currently you can only configure a single thermostat to control.

### Temperature Sensors
Temperature sensors will report the temperature back to the SmartThings app and potentially trigger the heating or cooling system based on your configured setpoints. 
It is best to think of the thermostat as a thermostat AND a temperature sensor.


> **Config - Main Sensor** 
>
>     Each temperature sensor (including the main thermostat sensor) must be set to either the main sensor for Heating, Cooling, or None. The main sensor is the one which will take precedence and which temperature setpoints will be used when the thermostat is set to the configured state.

### Triggers
There are many different triggers to be considered and each temperature sensor can be configured with their own triggers.

**The triggers include:**

 1. **Require Triggers** - *motion sensors/ presence sensors*
	 A require trigger is a motion/presence sensor that can be added to a temperature sensor. The temperature sensor can only call for heating/cooling (is active) if this trigger is active.
	 You may add many require triggers, each with it's own delay. This delay  will keep the temperature sensor active for a specified time after the motion/presence sensor configured has gone inactive. 
	 
2. **Negate Triggers** - *contact sensors*
	 A negate trigger works similar to a require trigger. A negate trigger is a contact sensor that can be added to a temperature sensor. The temperature sensor can only call for heating/cooling (is active) if the contact sensor is closed. 
	 You can add many negate triggers, but unlike require triggers the delay is global for that temperature sensor.

## Action Flow
**This system has a action flows:**

 1. Temperature change - on a temperature change the system checks any require and negate triggers for the specified sensor and then continues to send control commands to the thermostat
 2. Contact sensor opens - when a contact sensor that is configured as a negate trigger for a temperature sensor opens it will begin a countdown configured as the delay time. Once the delay is met, it will shutdown the thermostat. This only happens if the temperature sensor related to the contact sensor is the sensor that has activated the thermostat in the first place - otherwise, if a different temperature sensor activated the thermostat, no action will take place
 3. Presence sensor detected - when a presence sensor is detected the system will check all the other require and negate triggers for that temperature sensor - including the temperature. If needed the system then activates the thermostat
