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
    
    private var masterDeviceWrapper: OutputDeviceWrapper = OutputDeviceWrapper.systemDefault
    
    
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
        self.defaultOutputDevice = try OutputDeviceService.getDefaultOutputDevice(simplyCA: simplyCA)
        
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
    
    
    func setActiveDeviceVolumeChangedListener(listener: ((Float32, Float32)->(Bool))?){
        self.masterDeviceVolumeChangedListener = listener
        self.getMasterDevice().volumeChangedListener = listener
    }
    
    func setDeviceListChangedListener(listener: (()->())?){
        self.deviceListChangedListener = listener
    }
    
    func setMasterDeviceWrapper(wrapper : OutputDeviceWrapper){
        getMasterDevice().volumeChangedListener = nil
        self.masterDeviceWrapper = wrapper
        getMasterDevice().volumeChangedListener = self.masterDeviceVolumeChangedListener
    }
    
    func getMasterDeviceWrapper() -> OutputDeviceWrapper {
        return self.masterDeviceWrapper
    }
    
    func getMasterDevice() -> OutputDevice {
        switch self.masterDeviceWrapper {
        case .systemDefault:
            return self.defaultOutputDevice
            
        case .specified (let outputDevice):
            return outputDevice
        }
    }
    
    deinit {
        if let observer = self.deviceListChangedObserver {
            NotificationCenter.default.removeObserver(observer)
        }
        
        if let observer = self.defaultOutputDeviceChangedObserver {
            NotificationCenter.default.removeObserver(observer)
        }
    }
    
    
    enum OutputDeviceWrapper {
        case systemDefault
        case specified(OutputDevice)
    }
    
    enum OptionsError: Error {
        case noDefaultOutputDevice
    }
}
