//
//  MicControlViewModel.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import Foundation
import SimplyCoreAudio
import AppKit
import AudioToolbox
import AVFoundation

class MicControlViewModel : ObservableObject {

    private let inputDeviceService : InputDeviceService
    private let outputDeviceService : OutputDeviceService
    
    private let micActivatedSoundPlayer : SoundPlayer
    private let micMutedSoundPlayer : SoundPlayer
    
    private var lastChangeVolumeDirection: ChangeVolumeDirection? = nil
    
    @Published var micState : MicState = .activated

    init(inputDeviceService : InputDeviceService, outputDeviceService : OutputDeviceService) {
        self.inputDeviceService = inputDeviceService
        self.outputDeviceService = outputDeviceService
      
        self.micActivatedSoundPlayer = try! SoundPlayer(filename: "sound_mic_on")
        self.micMutedSoundPlayer = try! SoundPlayer(filename: "sound_mic_off")
        
        self.outputDeviceService.setMasterDeviceVolumeChangedListener { old, new in
            let masterDevice = self.outputDeviceService.getMasterDeviceDetails().device
            return self.onVolumeChanged(outputDevice: masterDevice, old: old, new: new)
        }
        
        self.micState = getMicState()
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
        switch direction {
        case .up:
            self.inputDeviceService.activate()
            self.micActivatedSoundPlayer.play()
            break;
        case .down:
            self.inputDeviceService.mute()
            self.micMutedSoundPlayer.play()
            break;
        }
        
        self.micState = getMicState()
    }

    
    private func getMicState() -> MicState {
        let isMuted = self.inputDeviceService.isMuted()
        return MicState.create(isMuted: isMuted)
    }
    
    func toggleMicState(){
        self.micState = self.micState.toggle()
    
        switch self.micState {
        case .activated:
            self.inputDeviceService.activate()
            self.micActivatedSoundPlayer.play()
            break;

        case .muted:
            self.inputDeviceService.mute()
            self.micMutedSoundPlayer.play()
            break;
        }
    }
}
