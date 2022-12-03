//
//  MicControlViewModel.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import Foundation
import SimplyCoreAudio

class MicControlViewModel : ObservableObject {
    
    private let simplyCA = SimplyCoreAudio()
    private lazy var volumeService = OutputVolumeService(simplyCA: simplyCA)
    private lazy var micService = MicrophoneService(simplyCA: simplyCA)
    
    private var lastChangeVolumeDirection: ChangeVolumeDirection? = nil
    
    @Published var isMicsMuted : Bool = false
    @Published var debugMessage : String = "nothing"
    
    init() {
        self.publishMicMuteState()
        
        self.volumeService.onVolumeChangedListener = { old, new in
            NSLog("volume changed: \(old) -> \(new)")
            
            let direction = old > new ? ChangeVolumeDirection.down :  ChangeVolumeDirection.up
            if (self.lastChangeVolumeDirection == nil || direction != self.lastChangeVolumeDirection) {
                self.lastChangeVolumeDirection = direction
                self.handleNewVolumeDirection(direction: direction)
                return true
            }
            return false
        }
    }
    
    private func handleNewVolumeDirection(direction : ChangeVolumeDirection){
        if (direction == .down){
            self.micService.muteAll()
        } else {
            self.micService.activateAll()
        }
        
        self.publishMicMuteState()
    }
    
    private func publishMicMuteState() {
        self.isMicsMuted = self.micService.isAllMuted()
    }
    
    func populateUi(){
        self.debugMessage = "Hello there!"
    }
    
    enum ChangeVolumeDirection {
        case up, down
    }
    
}
