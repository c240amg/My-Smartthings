/**
 *  ST_Anything_Ethernet.device.groovy
 *
 *  Copyright 2017 Dan G Ogorchock 
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2017-02-08  Dan Ogorchock  Original Creation
 *    2017-02-12  Dan Ogorchock  Modified to work with Ethernet based devices instead of ThingShield
 *
 */
 
metadata {
	definition (name: "NodeMCU 1", namespace: "RostikBor", author: "RostikBor") {
		capability "Configuration"
		capability "Illuminance Measurement"
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		//capability "Water Sensor"
		capability "Motion Sensor"
		capability "Switch"
		capability "Sensor"
		capability "Alarm"
		capability "Contact Sensor"
		capability "Polling"
        capability "Button"
        capability "Holdable Button"

		command "test"
		command "alarmoff"
        command "pushRight"
	}

    simulator {
 
    }

    // Preferences
	preferences {
		input "ip", "text", title: "Arduino IP Address", description: "ip", required: true, displayDuringSetup: true
		input "port", "text", title: "Arduino Port", description: "port", required: true, displayDuringSetup: true
		input "mac", "text", title: "Arduino MAC Addr", description: "mac", required: true, displayDuringSetup: true
		input "illuminanceSampleRate", "number", title: "Light Sensor Inputs", description: "Sampling Interval (seconds)", defaultValue: 30, required: true, displayDuringSetup: true
		input "temphumidSampleRate", "number", title: "Temperature/Humidity Sensor Inputs", description: "Sampling Interval (seconds)", defaultValue: 30, required: true, displayDuringSetup: true
		input "waterSampleRate", "number", title: "Water Sensor Inputs", description: "Sampling Interval (seconds)", defaultValue: 30, required: true, displayDuringSetup: true
		input "numButtons", "number", title: "Number of Buttons", description: "Number of Buttons to be implemented", defaultValue: 0, required: true, displayDuringSetup: true
}

	// Tile Definitions
	tiles {
    	multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4){
			tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
				attributeState "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#ffa81e"
				attributeState "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
			}
            	tileAttribute("device.lastMotion", key: "SECONDARY_CONTROL") {
    				attributeState("default", label:'Last Motion: ${currentValue}')
            }
		}
		valueTile("illuminance", "device.illuminance", width: 2, height: 2) {
			state("illuminance", label:'${currentValue}', //unit:"lux",
				backgroundColors:[
					[value: 9, color: "#767676"],
					[value: 315, color: "#ffa81e"],
					[value: 1000, color: "#fbd41b"]
				]
			)
		}        
        valueTile("temperature1", "device.temperature1", width: 1, height: 1) {
			state("temperature1", label:'${currentValue}°')
		}
        valueTile("temperature", "device.temperature", width: 2, height: 2) {
			state("temperature", label:'${currentValue}°', 
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
			)
		}
        valueTile("temperature3", "device.temperature3", width: 1, height: 1) {
			state("temperature3", label:'${currentValue}°')
		}
        valueTile("humidity", "device.humidity", inactiveLabel: false) {
			state "humidity", label:'${currentValue}%', unit:""
		}
		standardTile("water", "device.water", width: 2, height: 2) {
			state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
			state "wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
		} /*
		standardTile("motion", "device.motion", width: 1, height: 1) {
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#ffa81e")
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
		} */
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		}

		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		standardTile("contact", "device.contact", width: 2, height: 2) {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}
		standardTile("alarm", "device.alarm", width: 2, height: 2) {
			state "off", label:'off', action:'alarm.siren', icon:"st.alarm.alarm.alarm", backgroundColor:"#ffffff"
            state "strobe", label:'', action:'alarmoff', icon:"st.secondary.strobe", backgroundColor:"#cccccc"
            state "siren", label:'siren!', action:'alarmoff', icon:"st.alarm.beep.beep", backgroundColor:"#e86d13"
			state "both", label:'alarm!', action:'alarmoff', icon:"st.alarm.alarm.alarm", backgroundColor:"#e86d13"
		}

		standardTile("test", "device.alarm", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"test", icon:"st.secondary.test"
		}
        
		standardTile("off", "device.alarm", width: 2, height: 2) {
			state "default", label:'Alarm', action:"alarmoff", icon:"st.secondary.off"
		}
		//standardTile("off", "device.alarm", inactiveLabel: false, decoration: "flat") {
		//	state "default", label:'', action:"alarmoff", icon:"st.secondary.off"
		//}
		standardTile("rightDoor", "device.rightDoor", width: 2, height: 2, canChangeIcon: true, canChangeBackground: true) {
			state "closed", label: 'Closed', action: "pushRight", icon: "st.doors.garage.garage-closed", backgroundColor: "#79b821", nextState: "closed"
            state "open", label: 'Open', action: "pushRight", icon: "st.doors.garage.garage-open", backgroundColor: "#ffa81e", nextState: "open"
            state "opening", label: 'Opening', action: "pushRight", icon: "st.doors.garage.garage-opening", backgroundColor: "89C2E8", nextState: "opening"
            state "closing", label: 'Closing', action: "pushRight", icon: "st.doors.garage.garage-closing", backgroundColor: "89C2E8", nextState: "closing"
 		}
        valueTile("lastcheckin", "device.lastCheckin", decoration: "flat", inactiveLabel: false, width: 4, height: 1) {
			state "default", label:'Check in: ${currentValue}'
		}
        main(["temperature","motion","humidity","illuminance","switch","contact","alarm","water"])
        details(["motion","temperature","illuminance","contact","switch","lastcheckin"])//,"rightDoor"
	}
}

// parse events into attributes
def parse(String description) {
	//log.debug "Parsing '${description}'"
	def msg = parseLanMessage(description)
	def headerString = msg.header
    
	//send event for heartbeat    
    def now = new Date().format("MMM-d-yyyy h:mm a", location.timeZone)
    sendEvent(name: "lastCheckin", value: now, descriptionText: "Check-in")
    
	if (!headerString) {
		//log.debug "headerstring was null for some reason :("
    }

	def bodyString = msg.body

	if (bodyString) {
        log.debug "BodyString: $bodyString"
    	def parts = bodyString.split(" ")
    	def name  = parts.length>0?parts[0].trim():null
    	def value = parts.length>1?parts[1].trim():null
		def results = []
		if (name.startsWith("button")) {
        	def pieces = name.split(":")
            def btnName = pieces.length>0?pieces[0].trim():null
            def btnNum = pieces.length>1?pieces[1].trim():null
			//log.debug "In parse:  name = ${name}, value = ${value}, btnName = ${btnName}, btnNum = ${btnNum}"
        	results = createEvent([name: btnName, value: value, data: [buttonNumber: btnNum], descriptionText: "${btnName} ${btnNum} was ${value} ", isStateChange: true, displayed: true])
        }
        /*else if (name.startsWith("illuminance")) {
        	name  = parts[0]
            value = ((parts[1] as Double)*3.4)/1024
            results = createEvent(name: name, value: value)
        }*/
        else {
        	if (name.startsWith("motion") && value.startsWith("active")) {
        		now = new Date().format("MMM-d-yyyy h:mm a", location.timeZone)
        		sendEvent(name: "lastMotion", value: now, descriptionText: "")
            }
    		results = createEvent(name: name, value: value)
		}

		log.debug results
        return results

	}
}
def pushRight() {
	log.debug "Executing 'rightDoor on'"
	sendEthernet("rightDoor on")
}

private getHostAddress() {
    def ip = settings.ip
    def port = settings.port

	log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
    return ip + ":" + port
}


def sendEthernet(message) {
	log.debug "Executing 'sendEthernet' ${message}"
	new physicalgraph.device.HubAction(
    	method: "POST",
    	path: "/${message}?",
    	headers: [ HOST: "${getHostAddress()}" ]
	)
}

// handle commands

def on() {
	log.debug "Executing 'switch on'"
	sendEthernet("switch on")
}

def off() {
	log.debug "Executing 'switch off'"
	sendEthernet("switch off")
}

def alarmoff() {
	log.debug "Executing 'alarm off'"
	sendEthernet("alarm off")
}

def strobe() {
	log.debug "Executing 'alarm strobe'"
	sendEthernet("alarm strobe")
}

def siren() {
	log.debug "Executing 'alarm siren'"
	sendEthernet("alarm siren")
}

def both() {
	log.debug "Executing 'alarm both'"
	sendEthernet("alarm both")
}

def test() {
	log.debug "Executing 'alarm test'"
	[
		sendEthernet("alarm both"),
		"delay 3000",
		sendEthernet("alarm off")
	]
}

def poll() {
	//temporarily implement poll() to issue a configure() command to send the polling interval settings to the arduino
	configure()
}


def configure() {
	log.debug "Executing 'configure'"
    updateDeviceNetworkID()
    sendEvent(name: "numberOfButtons", value: numButtons)
    //log.debug "illuminance " + illuminanceSampleRate + "|temphumid " + temphumidSampleRate + "|water " + waterSampleRate
    log.debug "water " + waterSampleRate
    log.debug "illuminance " + illuminanceSampleRate
	log.debug "temphumid " + temphumidSampleRate
	[
		sendEthernet("water " + waterSampleRate),
        "delay 1000",
        sendEthernet("illuminance " + illuminanceSampleRate),
        "delay 1000",
        sendEthernet("temphumid " + temphumidSampleRate)
    ]
    

}

def updateDeviceNetworkID() {
	log.debug "Executing 'updateDeviceNetworkID'"
    if(device.deviceNetworkId!=mac) {
    	log.debug "setting deviceNetworkID = ${mac}"
        device.setDeviceNetworkId("${mac}")
	}
}

def updated() {
	if (!state.updatedLastRanAt || now() >= state.updatedLastRanAt + 5000) {
		state.updatedLastRanAt = now()
		log.debug "Executing 'updated'"
    	runIn(3, updateDeviceNetworkID)
        sendEvent(name: "numberOfButtons", value: numButtons)
	}
	else {
		log.trace "updated(): Ran within last 5 seconds so aborting."
	}
}

def initialize() {
	sendEvent(name: "numberOfButtons", value: numButtons)
}