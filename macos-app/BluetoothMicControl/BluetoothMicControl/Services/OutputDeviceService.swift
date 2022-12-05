//
//  OutputDeviceService.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import Foundation
import SimplyCoreAudio


class OutputDeviceService {
    
    private let throttleQueue = OperationQueue()
    
    private var outputDevices: Set<OutputDevice> = []
    
    private var volumeChangedListener: ((OutputDevice, Float32, Float32)->(Bool))?
    private var deviceListChangedListener: (()->())?
    
    private var deviceListChangedObserver : NSObjectProtocol?
    private var volumeChangedObserver : NSObjectProtocol?
    
    private static func getOutputDevices(simplyCA : SimplyCoreAudio) -> Set<OutputDevice>{
        let deviceList = simplyCA.allOutputDevices
            .compactMap{$0} // filter not nil
            .map{ device in
                NSLog("Found output device '\(device.name)'")
                return OutputDevice(audioDevice: device)
            }
        
        return Set(deviceList)
    }
    
    
    init(simplyCA: SimplyCoreAudio) throws {
        self.outputDevices = OutputDeviceService.getOutputDevices(simplyCA: simplyCA)
        
        self.deviceListChangedObserver = NotificationCenter.default.addObserver(forName: .deviceListChanged,
                                                                                object: nil,
                                                                                queue: .main) { [weak self] (notification) in
            // deviceListChanged does not triggered if simpliCA.allDevices is not called inside observer :/
            simplyCA.allDevices
            
            if let addedDevices = notification.userInfo?["addedDevices"] as? [AudioDevice] {
                self?.onDevicesAdded(devices: addedDevices)
            }

            if let removedDevices = notification.userInfo?["removedDevices"] as? [AudioDevice] {
                self?.onDevicesRemoved(devices: removedDevices)
            }
        }
        
        self.volumeChangedObserver = NotificationCenter.default.addObserver(forName: .deviceVolumeDidChange,
                                                                            object: nil,
                                                                            queue: .main) { (notification) in
            self.handleVolumeChangedEvent()
        }
    }
    
    deinit {
        if let observer = self.deviceListChangedObserver {
            NotificationCenter.default.removeObserver(observer)
        }
        if let observer = volumeChangedObserver {
            NotificationCenter.default.removeObserver(observer)
        }
    }
    
    private func onDevicesAdded(devices : [AudioDevice]){
        devices
            .filter { $0.channels(scope: .output) > 0 }
            .map { OutputDevice(audioDevice: $0) }
            .filter { !self.outputDevices.contains($0) }
            .forEach { device in
                device.volumeChangedListener = { [weak self] old, new in
                    self?.volumeChangedListener?(device, old, new) ?? false
                }
                self.outputDevices.insert(device)
                
                NSLog("Output device was attached: \(device.audioDevice.name)")
            }
    }
    
    private func onDevicesRemoved(devices : [AudioDevice]){
        devices
            .map { OutputDevice(audioDevice: $0) }
            .forEach { device in
                if let removedDevice = self.outputDevices.remove(device) {
                    removedDevice.volumeChangedListener = nil
                    NSLog("Output device was detached: \(device.audioDevice.name)")
                }
            }
    }
    
    func setVolumeChangedListener(listener: ((OutputDevice, Float32, Float32)->(Bool))?){
        self.volumeChangedListener = listener
        self.setVolumeChangedListenerToAllDevices()
      
    }
    
    private func handleVolumeChangedEvent() {
        let throttleOperation = VolumeChangedThrottleOperation() {
            self.outputDevices.forEach { device in
                device.syncVolumeWithSystem()
            }
        }
        
        self.throttleQueue.cancelAllOperations()
        self.throttleQueue.addOperation(throttleOperation)
    }
    
    private func setVolumeChangedListenerToAllDevices(){
        self.outputDevices.forEach { device in
            device.volumeChangedListener = { [weak self] old, new in
                self?.volumeChangedListener?(device, old, new) ?? false
            }
        }
    }
    
    func setDeviceListChangedListener(listener: (()->())?){
        self.deviceListChangedListener = listener
    }
}

private class VolumeChangedThrottleOperation: Operation {
    
    private static let throttleDelayMs = 96
    private let onComplete : (() -> Void)!
    
    init(onComplete : @escaping () -> Void) {
        self.onComplete = onComplete
    }
    
    override func main() {
//        NSLog("Trottling...")
        sleepMs(durationMs: VolumeChangedThrottleOperation.throttleDelayMs)
        
        if (isCancelled){
            return
        } else {
            DispatchQueue.main.async(){
//                NSLog("Notifing...")
                self.onComplete()
            }
        }
    }
}
