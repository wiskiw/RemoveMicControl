//
//  MicControlViewModel.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import Foundation
import SimplyCoreAudio

class MicControlViewModel : ObservableObject {

    private let inputDeviceService : InputDeviceService
    private let outputDeviceService : OutputDeviceService
    
    private var lastChangeVolumeDirection: ChangeVolumeDirection? = nil
    
    @Published var micState : MicState = .activated
    
    init(inputDeviceService : InputDeviceService, outputDeviceService : OutputDeviceService) {
        self.inputDeviceService = inputDeviceService
        self.outputDeviceService = outputDeviceService
        
        
        self.outputDeviceService.setActiveDeviceVolumeChangedListener { old, new in
            let activeDevice = self.outputDeviceService.getMasterDevice()
            return self.onVolumeChanged(outputDevice: activeDevice, old: old, new: new)
        }
        
        self.publishMuteState()
    }
    
    private func onVolumeChanged(outputDevice: OutputDevice, old:Float32, new:Float32) -> Bool{
        NSLog("Master output device volume changed '\(outputDevice.audioDevice.name)': \(old) -> \(new)")
        
        
        if let direction = getChangeVolumeDirection(old: old, new: new) {
            if (self.lastChangeVolumeDirection == nil || self.lastChangeVolumeDirection != direction){
                self.lastChangeVolumeDirection = direction
                self.onNewVolumeDirection(direction: direction)
                return true
            }
            
        }
        
        return false
    }
    
    private func getChangeVolumeDirection(old:Float32, new:Float32) -> ChangeVolumeDirection? {
        if (old > new) {
            return .down
        } else if (old < new) {
            return .up
        } else {
            return nil
        }
    }
    
    private func onNewVolumeDirection(direction : ChangeVolumeDirection){
        self.setMute(mute: direction == .down)
    }
    
    func setMute(mute : Bool){
        if (mute) {
            self.inputDeviceService.mute()
        } else {
            self.inputDeviceService.activate()
        }
        self.publishMuteState()
    }
    
    private func publishMuteState() {
        self.micState = MicState.create(isMuted: self.inputDeviceService.isMuted())
    }    
}
