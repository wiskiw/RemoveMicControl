//
//  MicControlViewModel.swift
//  BluetoothMicControl
//
//  Created by Andrei Yablonski on 30.11.22.
//

import Foundation

class MicControlViewModel : ObservableObject {
    
    private let volumeService = OutputVolumeService()
    private let micService = MicrophoneService()
    
    @Published var isMicsMuted : Bool = false
    @Published var debugMessage : String = "nothing"
    
    init() {
        publishMicMuteState()
        
        volumeService.onVolumeChangedListener = { old, new in
            print("volume: \(old) -> \(new)")

            if (old > new){
                self.micService.muteAll()
            } else if (old < new){
                self.micService.activateAll()
            }
            self.publishMicMuteState()
        }
    }
    
    private func publishMicMuteState() {
        self.isMicsMuted = self.micService.isAllMuted()
    }
    
    func populateUi(){
        self.debugMessage = "Hello there!"
    }
    
}
