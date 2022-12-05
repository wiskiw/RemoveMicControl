//
//  InputDeviceService.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 01.12.22.
//

import Foundation
import SimplyCoreAudio

class InputDeviceService {

    private var deviceListChangedObserver : NSObjectProtocol? = nil
    
    private var inputDevices : Set<InputDevice> = []
    private var isAllMuted = false

    private static func getInputDevices(simplyCA : SimplyCoreAudio) -> Set<InputDevice>{
        let deviceList = simplyCA.allInputDevices
            .compactMap{$0} // filter not nil
            .map{ device in
                NSLog("Found input device '\(device.name)'")
                return InputDevice(audioDevice: device)
            }
        
        return Set(deviceList)
    }

    
    init (simplyCA : SimplyCoreAudio){
        self.inputDevices = InputDeviceService.getInputDevices(simplyCA: simplyCA)
        
        self.deviceListChangedObserver = NotificationCenter.default.addObserver(forName: .deviceListChanged,
                                                                           object: nil,
                                                                           queue: .main) { (notification) in
            // deviceListChanged does not triggered if simpliCA.allDevices is not called inside observer :/
            simplyCA.allDevices

            if let addedDevices = notification.userInfo?["addedDevices"] as? [AudioDevice] {
                self.onDevicesAdded(devices: addedDevices)
            }

            if let removedDevices = notification.userInfo?["removedDevices"] as? [AudioDevice] {
                self.onDevicesRemoved(devices: removedDevices)
            }
        }
    }
    
    deinit {
        if let observer = deviceListChangedObserver {
            NotificationCenter.default.removeObserver(observer)
        }
    }
    
    private func onDevicesAdded(devices : [AudioDevice]){
        devices
            .filter { $0.channels(scope: .input) > 0 }
            .map { InputDevice(audioDevice: $0) }
            .filter { !self.inputDevices.contains($0) }
            .forEach { device in
                NSLog("Input device was attached: \(device.audioDevice.name)")
                self.inputDevices.insert(device)

                if (self.isAllMuted) {
                    device.mute()
                }
            }
    }
    
    private func onDevicesRemoved(devices : [AudioDevice]){
        devices
            .map { InputDevice(audioDevice: $0) }
            .forEach { device in
                let removedDevice = self.inputDevices.remove(device)
                if (removedDevice != nil) {
                    NSLog("Input device was detached: \(device.audioDevice.name)")
                }
            }
    }
    
    func mute() {
        self.inputDevices.forEach{
            $0.mute()
        }
        self.isAllMuted = true
    }
    
    func activate() {
        self.inputDevices.forEach{
            $0.activate()
        }
        self.isAllMuted = false
    }
    
    func isMuted() -> Bool{
        return self.isAllMuted
    }

}
