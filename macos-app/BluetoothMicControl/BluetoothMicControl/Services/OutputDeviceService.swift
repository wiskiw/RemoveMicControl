//
//  OutputDeviceService.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import Foundation
import SimplyCoreAudio


class OutputDeviceService {
    
    private var outputDevices: [OutputDevice] = []
    private var defaultOutputDevice: OutputDevice
    
    private var masterDeviceVolumeChangedListener: ((Float32, Float32)->(Bool))?
    private var deviceListChangedListener: (()->())?
    
    private var deviceListChangedObserver : NSObjectProtocol?
    private var defaultOutputDeviceChangedObserver : NSObjectProtocol?
    
    private var masterDevice: OutputDevice
    private var useDefaultAsMaster: Bool
    
    
    private static func getDefaultOutputDevice(simplyCA : SimplyCoreAudio) throws -> OutputDevice{
        guard let defaultAudioDevice = simplyCA.defaultOutputDevice else { throw OptionsError.noDefaultOutputDevice }
        return OutputDevice(audioDevice: defaultAudioDevice)
    }
    
    private static func getOutputDevices(simplyCA : SimplyCoreAudio) -> [OutputDevice]{
        return simplyCA.allOutputDevices
            .compactMap{$0} // filter not nil
            .map{ device in
                NSLog("Found output device '\(device.name)'")
                return OutputDevice(audioDevice: device)
            }
    }
    
    
    init(simplyCA: SimplyCoreAudio) throws {
        self.outputDevices = OutputDeviceService.getOutputDevices(simplyCA: simplyCA)
        
        let defaultOutputDevice = try OutputDeviceService.getDefaultOutputDevice(simplyCA: simplyCA)
        self.defaultOutputDevice = defaultOutputDevice
        self.masterDevice = defaultOutputDevice
        self.useDefaultAsMaster = true
        
        self.deviceListChangedObserver = NotificationCenter.default.addObserver(forName: .deviceListChanged,
                                                                                object: nil,
                                                                                queue: .main) { (notification) in
            NSLog("Rescanning output devices...")
            self.outputDevices.removeAll()
            self.outputDevices = OutputDeviceService.getOutputDevices(simplyCA: simplyCA)

            self.deviceListChangedListener?()
        }
        
        
        self.defaultOutputDeviceChangedObserver = NotificationCenter.default.addObserver(forName: .defaultOutputDeviceChanged,
                                                                                         object: nil,
                                                                                         queue: .main) { (notification) in
            self.defaultOutputDevice.volumeChangedListener = nil
            self.defaultOutputDevice = try! OutputDeviceService.getDefaultOutputDevice(simplyCA: simplyCA)
            NSLog("New default output device detected: \(self.defaultOutputDevice.audioDevice.name) (type: \(self.defaultOutputDevice.audioDevice.transportType ?? TransportType.unknown))")
            
            self.defaultOutputDevice.volumeChangedListener = self.masterDeviceVolumeChangedListener
            self.deviceListChangedListener?()
        }
    }
    
    
    func setMasterDeviceVolumeChangedListener(listener: ((Float32, Float32)->(Bool))?){
        self.masterDeviceVolumeChangedListener = listener
        self.masterDevice.volumeChangedListener = listener
    }
    
    func setDeviceListChangedListener(listener: (()->())?){
        self.deviceListChangedListener = listener
    }
    
    func getMasterDeviceDetails() -> MasterDeviceDetails {
        return MasterDeviceDetails(
            device: self.masterDevice,
            isSystemDefault: useDefaultAsMaster
        )
    }

    func setMasterDevice(device : OutputDevice){
        self.masterDevice.volumeChangedListener = nil
        self.masterDevice = device
        self.useDefaultAsMaster = false
        
        self.masterDevice.volumeChangedListener = self.masterDeviceVolumeChangedListener
    }
    
    func useSystemDefaultAsMasterDevice(){
        self.masterDevice.volumeChangedListener = nil
        self.masterDevice = self.defaultOutputDevice
        self.useDefaultAsMaster = true
        
        self.masterDevice.volumeChangedListener = self.masterDeviceVolumeChangedListener
    }
    
    func getDefaultDevice() -> OutputDevice {
        return self.defaultOutputDevice
    }
    
    func getDevices() -> [OutputDevice] {
        return self.outputDevices
    }
    
    deinit {
        if let observer = self.deviceListChangedObserver {
            NotificationCenter.default.removeObserver(observer)
        }
        
        if let observer = self.defaultOutputDeviceChangedObserver {
            NotificationCenter.default.removeObserver(observer)
        }
    }
}

struct MasterDeviceDetails {
    let device : OutputDevice
    let isSystemDefault : Bool
}

enum OptionsError: Error {
    case noDefaultOutputDevice
}
