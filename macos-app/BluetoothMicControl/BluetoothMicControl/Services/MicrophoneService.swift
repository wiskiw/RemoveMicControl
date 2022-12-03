//
//  VolumeWatcherService.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 01.12.22.
//

import Foundation
import SimplyCoreAudio

class MicrophoneService {
    
    private let simplyCA: SimplyCoreAudio
    private var deviceListChangedObserver : NSObjectProtocol? = nil
    
    private var micConfigDict = [AudioDevice: MicConfig]()
    private var isMuted = false
    
    
    init (simplyCA : SimplyCoreAudio){
        self.simplyCA = simplyCA
        
        deviceListChangedObserver = NotificationCenter.default.addObserver(forName: .deviceListChanged,
                                                                           object: nil,
                                                                           queue: .main) { (notification) in
            NSLog("Device list changed")
            
            // Get added devices.
            let addedDevices = notification.userInfo?["addedDevices"] as? [AudioDevice]
            if (addedDevices != nil){
                addedDevices?.forEach{
                    self.saveInputDeviceConfig(device: $0)
                }
            }
            
            // Get removed devices.
            let removedDevices = notification.userInfo?["removedDevices"] as? [AudioDevice]
            if (removedDevices != nil){
                removedDevices?.forEach{
                    self.deleteInputDeviceConfig(device: $0)
                }
            }
            
            self.scanMics()
        }
        
        scanMics()
    }
    
    private func scanMics() {
        simplyCA.allInputDevices
            .compactMap{$0} // filter not optinal
            .forEach { saveInputDeviceConfig(device: $0)}
    }
    
    private func saveInputDeviceConfig(device : AudioDevice){
        if (self.micConfigDict[device] == nil){
            // no device info in dict
            
            if let volume = device.volume(channel: 0, scope: .input){
                let config = MicConfig(originalVolume: volume)
                self.micConfigDict[device] = config
                
                NSLog("Save config for '\(device.name)' - \(config)")
            }
            
        }
    }
    
    
    private func deleteInputDeviceConfig(device : AudioDevice){
        let removedMicDetails = self.micConfigDict.removeValue(forKey: device)
        
        if (removedMicDetails != nil){
            // no device info in dict
            
            NSLog("Delete config for '\(device.name)'")
        }
    }
    
    func muteAll() {
        for (device, _) in self.micConfigDict {
            let isMuted = device.setMute(true, channel: 0, scope: .input)
            
            // to move volume bar in System Preferences
            let isVolumeSet = device.setVolume(0, channel: 0, scope: .input)
            
            if (isMuted || isVolumeSet){
                NSLog("Mic '\(device)' was muted")
            } else {
                NSLog("Failed to mute '\(device)'")
            }
        }
        
        self.isMuted = true
    }
    
    func activateAll() {
        for (device, config) in self.micConfigDict {
            let isActivated = device.setMute(false, channel: 0, scope: .input)
            
            // to move volume bar in System Preferences
            let isVolumeSet = device.setVolume(config.originalVolume, channel: 0, scope: .input)
            
            if (isActivated || isVolumeSet){
                NSLog("Mic '\(device)' was activated")
            } else {
                NSLog("Failed to activate '\(device)'")
            }
            
            // to move volume bar in System Preferences
            device.setVolume(0, channel: 0, scope: .input)
        }
        
        self.isMuted = false
    }
    
    func isAllMuted() -> Bool{
        return self.isMuted
    }
    
    deinit {
        if let observer = deviceListChangedObserver {
            NotificationCenter.default.removeObserver(observer)
        }
    }
    
    struct MicConfig {
        let originalVolume : Float
    }
}


