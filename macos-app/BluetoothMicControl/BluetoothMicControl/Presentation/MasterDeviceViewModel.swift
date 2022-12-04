//
//  MasterDeviceViewModel.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 4.12.22.
//

import Foundation


class MasterDeviceViewModel : ObservableObject {
    
    private let outputDeviceService : OutputDeviceService

    @Published var selectedOption : OutputDeviceOption {
        didSet {
            if (selectedOption.isDefault){
                outputDeviceService.useSystemDefaultAsMasterDevice()
            } else {
                outputDeviceService.setMasterDevice(device: selectedOption.device)
            }
        }
    }

    @Published var availableDeviceOptions : [OutputDeviceOption] = []
    
    init(outputDeviceService: OutputDeviceService) {
        self.outputDeviceService = outputDeviceService
        

        self.selectedOption = outputDeviceService.getMasterDeviceDetails().convertToOption()

        let systemDefaultOption = outputDeviceService.getDefaultDevice().convertToOption(isDefault: true)
        availableDeviceOptions.append(systemDefaultOption)
        
        outputDeviceService.getDevices()
            .map{ $0.convertToOption(isDefault: false) }
            .forEach { availableDeviceOptions.append($0) }
        
        
        outputDeviceService.setDeviceListChangedListener(listener: {
            self.availableDeviceOptions.removeAll()

            self.selectedOption = outputDeviceService.getMasterDeviceDetails().convertToOption()

            let systemDefaultOption = outputDeviceService.getDefaultDevice().convertToOption(isDefault: true)
            self.availableDeviceOptions.append(systemDefaultOption)
            
            outputDeviceService.getDevices()
                .map{ $0.convertToOption(isDefault: false) }
                .forEach { self.availableDeviceOptions.append($0) }
        })
    }
    
    deinit {
        outputDeviceService.setDeviceListChangedListener(listener: nil)
    }
    
}

extension OutputDevice {
    
    func convertToOption(isDefault: Bool) ->OutputDeviceOption {
        return OutputDeviceOption(
            name: self.audioDevice.name,
            isDefault: isDefault,
            device: self
        )
    }
}

extension MasterDeviceDetails {
    
    func convertToOption() ->OutputDeviceOption {
        return OutputDeviceOption(
            name: self.device.audioDevice.name,
            isDefault: self.isSystemDefault,
            device: self.device
        )
    }
}

struct OutputDeviceOption : Hashable {
    let name : String
    let isDefault : Bool
    let device : OutputDevice
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(isDefault)
        hasher.combine(device.audioDevice.id)
    }
}

