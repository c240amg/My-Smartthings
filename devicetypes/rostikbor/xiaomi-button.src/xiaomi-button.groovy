/**
 *  Xiaomi Zigbee Button
 *
 *  Modified by RaveTam from Eric Maycock implementation below. Added to support Holdable Button and Battery status reporting
 *  https://github.com/erocm123/SmartThingsPublic/blob/master/devicetypes/erocm123/xiaomi-smart-button.src/xiaomi-smart-button.groovy
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
 */
metadata {
	definition (name: "Xiaomi Button", namespace: "RostikBor", author: "RostikBor") {
		capability "Battery"
        capability "Button"
		capability "Holdable Button"
        capability "Configuration"
		capability "Sensor"
        capability "Refresh"
        
        attribute "lastPress", "string"
        attribute "batterylevel", "string"
	}
    
    simulator {
   	  status "button 1 pressed": "on/off: 0"
      status "button 1 released": "on/off: 1"
    }
    
    preferences{
    	input ("holdTime", "number", title: "Minimum time in seconds for a press to count as \"held\"",
        		defaultValue: 4, displayDuringSetup: false)
    }

	tiles(scale: 2) {
    	standardTile("button", "device.button", decoration: "flat", width: 2, height: 2) {
        	state "default", icon: "st.unknown.zwave.remote-controller", backgroundColor: "#ffffff"
        }
        valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false, width: 2, height: 2) {
			state "battery", label:'${currentValue}% battery', unit:""
		}
        standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
        valueTile("lastPressed", "device.lastPressed", decoration: "flat", inactiveLabel: false, width: 5, height: 1) {
			state "default", label:'Last Press: ${currentValue}'
		}
        valueTile("lastcheckin", "device.lastCheckin", decoration: "flat", inactiveLabel: false, width: 5, height: 1) {
			state "default", label:'Last Checkin: ${currentValue}'
		}
		main (["button"])
		details(["button", "battery", "refresh","lastPressed","lastcheckin"])
	}
}

def parse(String description) {
  //  send event for heartbeat    
   def now = new Date().format("MMM-d-yyyy h:mm a", location.timeZone)
   sendEvent(name: "lastCheckin", value: now, descriptionText: "Check-in")

  log.debug "Parsing '${description}'"
  def value = zigbee.parse(description)?.text
  log.debug "Parse: $value"
  def descMap = zigbee.parseDescriptionAsMap(description)
  def results = []
  if (description?.startsWith('on/off: '))
		results = parseCustomMessage(description)
  if (description?.startsWith('catchall:')) 
		results = parseCatchAllMessage(description)
        
  return results;
}

def configure(){
    [
    "zdo bind 0x${device.deviceNetworkId} 1 2 0 {${device.zigbeeId}} {}", "delay 5000",
    "zcl global send-me-a-report 2 0 0x10 1 0 {01}", "delay 500",
    "send 0x${device.deviceNetworkId} 1 2"
    ]
}

def refresh(){
	"st rattr 0x${device.deviceNetworkId} 1 2 0"
    "st rattr 0x${device.deviceNetworkId} 1 0 0"
	log.debug "refreshing"
    
    createEvent([name: 'batterylevel', value: '100', data:[buttonNumber: 1], displayed: false])
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)
	log.debug cluster
	if (cluster) {
		switch(cluster.clusterId) {
			case 0x0000:
			resultMap = getBatteryResult(cluster.data.last())
			break

			case 0xFC02:
			log.debug 'ACCELERATION'
			break

			case 0x0402:
			log.debug 'TEMP'
				// temp is last 2 data values. reverse to swap endian
				String temp = cluster.data[-2..-1].reverse().collect { cluster.hex1(it) }.join()
				def value = getTemperature(temp)
				resultMap = getTemperatureResult(value)
				break
		}
	}

	return resultMap
}

private Map getBatteryResult(rawValue) {
	log.debug 'Battery'
	def linkText = getLinkText(device)

	log.debug rawValue
    
    int battValue = rawValue

	def result = [
		name: 'battery',
		value: battValue,
        unit: "%",
        isStateChange:true,
        descriptionText : "${linkText} battery was ${rawValue}%"
	]
    
    log.debug result.descriptionText
    state.lastbatt = new Date().time
    return createEvent(result)
}

private Map parseCustomMessage(String description) {
	if (description?.startsWith('on/off: ')) {
    	if (description == 'on/off: 0') 		//button pressed
    		return createPressEvent(1)
    	else if (description == 'on/off: 1') 	//button released
    		return createButtonEvent(1)
	}
}

//this method determines if a press should count as a push or a hold and returns the relevant event type
private createButtonEvent(button) {
	def currentTime = now()
    def startOfPress = device.latestState('lastPress').date.getTime()
    def timeDif = currentTime - startOfPress
    def holdTimeMillisec = (settings.holdTime?:3).toInteger() * 1000
    def now = new Date().format("MMM-d-yyyy h:mm a", location.timeZone)
    
    if (timeDif < 0) 
    	return []	//likely a message sequence issue. Drop this press and wait for another. Probably won't happen...
    else if (timeDif < holdTimeMillisec)
    {
    	sendEvent(name: "lastPressed", value: now, descriptionText: "")
    	return createEvent(name: "button", value: "pushed", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pushed", isStateChange: true)
    }
    else
    {
    	sendEvent(name: "lastPressed", value: now, descriptionText: "")
    	return createEvent(name: "button", value: "held", data: [buttonNumber: button], descriptionText: "$device.displayName button $button was held", isStateChange: true)
	}
}

private createPressEvent(button) {
	return createEvent([name: 'lastPress', value: now(), data:[buttonNumber: button], displayed: false])
}

//Need to reverse array of size 2
private byte[] reverseArray(byte[] array) {
    byte tmp;
    tmp = array[1];
    array[1] = array[0];
    array[0] = tmp;
    return array
}

private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}